package com.script.comminutioncraft.common.machine.miner;

import com.mojang.serialization.Codec;
import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.config.MachineConfig;
import com.script.comminutioncraft.common.energy.EnergySync;
import com.script.comminutioncraft.common.energy.MachineEnergyStorage;
import com.script.comminutioncraft.common.machine.MachineBlock;
import com.script.comminutioncraft.common.machine.TickingMachine;
import com.script.comminutioncraft.common.machine.control.ControllableMachine;
import com.script.comminutioncraft.common.machine.control.MachineControls;
import com.script.comminutioncraft.common.machine.control.SideMode;
import com.script.comminutioncraft.common.machine.upgrade.MachineUpgrades;
import com.script.comminutioncraft.common.machine.upgrade.UpgradeHolder;
import com.script.comminutioncraft.common.registry.ModBlockEntities;
import it.unimi.dsi.fastutil.longs.LongArrayList;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.world.chunk.RegisterTicketControllersEvent;
import net.neoforged.neoforge.common.world.chunk.TicketController;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import org.jspecify.annotations.Nullable;

/**
 * Ore Miner (owner's design, 2026-11-18/22): no digging. It works through a square of chunks, the width of its tier,
 * with its own chunk in the middle of the back row and the rest stretching the way its front faces, in a snake (back
 * row left to right, next row right to left, ...). Each chunk is first scanned bottom up to the surface (free, a set
 * number of blocks a tick), then its ores ({@code c:ores}) are pulled out one at a time for FE, their drops going into
 * the 9 output slots, and the ore's spot filled with matching stone or air. Raw drops (tier 2) or the ore block (tier 3+,
 * a switch); tier 3+ also takes its tier's circuit for a whitelist / blacklist filter. It keeps its own chunk and the
 * one it's working on loaded. Turning it starts it over (the area moves with its front).
 */
public class OreMinerBlockEntity extends BaseContainerBlockEntity
        implements WorldlyContainer, TickingMachine, ControllableMachine, UpgradeHolder {
    public static final int OUTPUT_SLOTS = 9;
    public static final int FILTER_SLOTS = 9;

    /** Keeps the miner's chunk and the chunk it works on loaded; tickets of miners that are gone are dropped on load. */
    public static final TicketController TICKETS = new TicketController(ComminutionCraft.id("ore_miner"), (level, helper) ->
            helper.getBlockTickets().keySet().forEach(owner -> {
                if (!(level.getBlockState(owner).getBlock() instanceof OreMinerBlock)) {
                    helper.removeAllTickets(owner);
                }
            }));

    // ContainerData indices, synced to the open GUI
    public static final int DATA_ENERGY_LOW = 0;
    public static final int DATA_ENERGY_HIGH = 1;
    public static final int DATA_CAPACITY_LOW = 2;
    public static final int DATA_CAPACITY_HIGH = 3;
    public static final int DATA_REDSTONE = 4;
    public static final int DATA_SIDES = 5;
    public static final int DATA_UPGRADES = 6;
    public static final int DATA_PHASE = 7;
    public static final int DATA_CHUNK = 8;
    public static final int DATA_Y = 9;
    public static final int DATA_MINED_LOW = 10;
    public static final int DATA_MINED_HIGH = 11;
    public static final int DATA_FLAGS = 12;
    public static final int DATA_WAITING = 13;
    public static final int DATA_COUNT = 14;
    public static final int FLAG_FILL_STONE = 1;
    public static final int FLAG_ORE_BLOCKS = 2;
    public static final int FLAG_BLACKLIST = 4;

    /** Menu buttons after the machine controls' own (redstone, sides, clear sides). */
    public static final int BUTTON_FILL = 20;
    public static final int BUTTON_DROPS = 21;
    public static final int BUTTON_LIST = 22;

    /** What the miner is doing: scanning a chunk, mining its ores, or done with the whole area. */
    public enum Phase { SCANNING, MINING, FINISHED }

    /** Why it isn't working right now (shown in the GUI). */
    public enum Waiting { NONE, NO_POWER, OUTPUT_FULL, REDSTONE, LOADING }

    /** How long the front keeps showing work after it stops, so a thin power supply doesn't flicker it. */
    private static final int SCREEN_COOLDOWN = 10;

    private NonNullList<ItemStack> items = NonNullList.withSize(OUTPUT_SLOTS, ItemStack.EMPTY);
    private ItemStack circuit = ItemStack.EMPTY;
    private final NonNullList<ItemStack> filter = NonNullList.withSize(FILTER_SLOTS, ItemStack.EMPTY);
    private boolean blacklist;
    private boolean fillStone = true;
    private boolean oreBlocks;

    private final MachineEnergyStorage energy;
    private final MachineControls controls = new MachineControls(MachineControls.outputBelow(), this::onControlsChanged);
    private final MachineUpgrades upgrades = new MachineUpgrades(this::onUpgradesChanged, List::of);

    // Progress through the area
    private @Nullable Direction startFacing;
    private Phase phase = Phase.SCANNING;
    private int chunkIndex;
    /** Scanning: the next block, counted bottom up layer by layer (layer x 256 + x x 16 + z). */
    private int scanCursor;
    /** Scanning: the highest y worth looking at in this chunk (its surface), or Integer.MIN_VALUE before it's known. */
    private int scanTop = Integer.MIN_VALUE;
    /** The current chunk's ores, bottom up, and the next one to mine. */
    private final LongArrayList ores = new LongArrayList();
    private int oreIndex;
    private int oreProgress;
    private long mined;
    private int shownY;
    private Waiting waiting = Waiting.NONE;
    private int screenCooldown;
    /** The chunk ticketed for work (Long.MAX_VALUE = none); whether the miner's own chunk is ticketed. */
    private long workTicket = Long.MAX_VALUE;
    private boolean ownTicket;

    private final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_ENERGY_LOW -> EnergySync.low(energy.getEnergy());
                case DATA_ENERGY_HIGH -> EnergySync.high(energy.getEnergy());
                case DATA_CAPACITY_LOW -> EnergySync.low(energy.getCapacity());
                case DATA_CAPACITY_HIGH -> EnergySync.high(energy.getCapacity());
                case DATA_REDSTONE -> controls.redstone().ordinal();
                case DATA_SIDES -> controls.packSides();
                case DATA_UPGRADES -> upgrades.allowedMask();
                case DATA_PHASE -> phase.ordinal();
                case DATA_CHUNK -> Math.min(chunkIndex + 1, tier().chunks());
                case DATA_Y -> shownY;
                case DATA_MINED_LOW -> EnergySync.low((int) Math.min(mined, Integer.MAX_VALUE));
                case DATA_MINED_HIGH -> EnergySync.high((int) Math.min(mined, Integer.MAX_VALUE));
                case DATA_FLAGS -> (fillStone ? FLAG_FILL_STONE : 0) | (oreBlocks ? FLAG_ORE_BLOCKS : 0) | (blacklist ? FLAG_BLACKLIST : 0);
                case DATA_WAITING -> waiting.ordinal();
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {}

        @Override
        public int getCount() {
            return DATA_COUNT;
        }
    };

    public OreMinerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ORE_MINER.get(), pos, state);
        OreMinerTier tier = tierOf(state);
        energy = new MachineEnergyStorage(tier.energyCapacity(), tier.maxEnergyInput(), 0, this::setChanged);
    }

    public static void registerTickets(RegisterTicketControllersEvent event) {
        event.register(TICKETS);
    }

    private static OreMinerTier tierOf(BlockState state) {
        return state.getBlock() instanceof OreMinerBlock miner ? miner.tier() : OreMinerTier.ELECTRIC;
    }

    public OreMinerTier tier() {
        return tierOf(getBlockState());
    }

    public EnergyHandler getEnergyHandler() {
        return energy;
    }

    @Override
    public MachineControls getControls() {
        return controls;
    }

    @Override
    public MachineUpgrades getUpgrades() {
        return upgrades;
    }

    private void onControlsChanged() {
        setChanged();
        refreshFaces();
    }

    private void onUpgradesChanged() {
        if (energy != null) {
            energy.scale(upgrades.bufferSize(), upgrades.energyPerTick());
        }
        setChanged();
    }

    // ---- The area ----

    /**
     * The chunk worked on at {@code index}: rows go out the way the front faces, starting with the miner's own row;
     * even rows run left to right, odd rows back (left = to the left of the front's direction, seen from above).
     */
    public static ChunkPos chunkAt(ChunkPos home, Direction facing, int width, int index) {
        int row = index / width;
        int inRow = index % width;
        int column = row % 2 == 0 ? inRow : width - 1 - inRow;
        int left = width / 2 - column;
        Direction leftward = facing.getCounterClockWise();
        return new ChunkPos(home.x() + facing.getStepX() * row + leftward.getStepX() * left,
                home.z() + facing.getStepZ() * row + leftward.getStepZ() * left);
    }

    private ChunkPos currentChunk() {
        return chunkAt(ChunkPos.containing(worldPosition), getFacing(), tier().width(), chunkIndex);
    }

    /** Back to the first chunk (placed, or turned: the area turns with it). */
    private void restart(Direction facing) {
        startFacing = facing;
        phase = Phase.SCANNING;
        chunkIndex = 0;
        startChunk();
        mined = 0;
        setChanged();
    }

    /** GameTests: start (from now) at chunk {@code index} of the area. */
    public void jumpTo(int index) {
        startFacing = getFacing();
        phase = Phase.SCANNING;
        chunkIndex = index;
        startChunk();
    }

    private void startChunk() {
        scanCursor = 0;
        scanTop = Integer.MIN_VALUE;
        ores.clear();
        oreIndex = 0;
        oreProgress = 0;
    }

    private void nextChunk(ServerLevel level) {
        chunkIndex++;
        startChunk();
        if (chunkIndex >= tier().chunks()) {
            phase = Phase.FINISHED;
            releaseTickets(level);
        } else {
            phase = Phase.SCANNING;
        }
        setChanged();
    }

    // ---- Ticking ----

    @Override
    public void serverTick(ServerLevel level, BlockPos pos, BlockState state) {
        Direction facing = state.getValue(MachineBlock.FACING);
        if (facing != startFacing) {
            restart(facing);
        }
        waiting = Waiting.NONE;
        boolean worked = false;
        if (phase != Phase.FINISHED) {
            keepLoaded(level);
            if (!controls.canRun(level, pos)) {
                waiting = Waiting.REDSTONE;
            } else {
                worked = phase == Phase.SCANNING ? scan(level) : mine(level);
            }
        }
        if (hasOutput()) {
            pushItemsOut(level, pos);
        }
        updateScreen(level, pos, state, worked);
    }

    /** The miner's own chunk (so it keeps running with nobody near) and the one it works on. */
    private void keepLoaded(ServerLevel level) {
        if (!ownTicket) {
            ChunkPos own = ChunkPos.containing(worldPosition);
            TICKETS.forceChunk(level, worldPosition, own.x(), own.z(), true, false);
            ownTicket = true;
        }
        ChunkPos work = currentChunk();
        long key = work.pack();
        if (workTicket != key) {
            releaseWorkTicket(level);
            TICKETS.forceChunk(level, worldPosition, work.x(), work.z(), true, false);
            workTicket = key;
        }
    }

    private void releaseWorkTicket(ServerLevel level) {
        if (workTicket != Long.MAX_VALUE) {
            ChunkPos old = ChunkPos.unpack(workTicket);
            if (!old.equals(ChunkPos.containing(worldPosition))) { // never drop the miner's own chunk with it
                TICKETS.forceChunk(level, worldPosition, old.x(), old.z(), false, false);
            }
            workTicket = Long.MAX_VALUE;
        }
    }

    private void releaseTickets(ServerLevel level) {
        releaseWorkTicket(level);
        if (ownTicket) {
            ChunkPos own = ChunkPos.containing(worldPosition);
            TICKETS.forceChunk(level, worldPosition, own.x(), own.z(), false, false);
            ownTicket = false;
        }
    }

    /** Looks at up to {@code scanPerTick} blocks of the current chunk, bottom up, noting the ores. Costs no FE. */
    private boolean scan(ServerLevel level) {
        ChunkPos target = currentChunk();
        if (!level.hasChunk(target.x(), target.z())) {
            waiting = Waiting.LOADING;
            return false;
        }
        LevelChunk chunk = level.getChunk(target.x(), target.z());
        int minY = level.getMinY();
        if (scanTop == Integer.MIN_VALUE) {
            int top = minY;
            for (int x = 0; x < 16; x++) {
                for (int z = 0; z < 16; z++) {
                    top = Math.max(top, chunk.getHeight(Heightmap.Types.WORLD_SURFACE, x, z));
                }
            }
            scanTop = top;
        }
        BlockPos.MutableBlockPos cursor = new BlockPos.MutableBlockPos();
        for (int budget = MachineConfig.ORE_MINER_SCAN_PER_TICK.get(); budget > 0; budget--) {
            int y = minY + scanCursor / 256;
            if (y > scanTop) {
                phase = Phase.MINING;
                setChanged();
                return true;
            }
            cursor.set(target.getBlockX((scanCursor / 16) % 16), y, target.getBlockZ(scanCursor % 16));
            BlockState state = chunk.getBlockState(cursor);
            if (isOre(state) && passesFilter(level, cursor, state)) {
                ores.add(cursor.asLong());
            }
            scanCursor++;
            shownY = y;
        }
        return true;
    }

    /** Pulls the next ore out: FE every tick for the ore's time, then its drops go into the outputs and its spot is filled. */
    private boolean mine(ServerLevel level) {
        if (oreIndex >= ores.size()) {
            nextChunk(level);
            return true;
        }
        BlockPos pos = BlockPos.of(ores.getLong(oreIndex));
        BlockState state = level.getBlockState(pos);
        if (!isOre(state)) { // gone since the scan (mined by a player, an explosion...)
            oreIndex++;
            return true;
        }
        shownY = pos.getY();
        int time = upgrades.processTime(tier().ticksPerOre());
        if (oreProgress < time) {
            int perTick = upgrades.energyPerTick(Math.max(1, Math.ceilDiv(tier().energyPerOre(), tier().ticksPerOre())));
            if (!energy.consume(perTick)) {
                waiting = Waiting.NO_POWER;
                return false;
            }
            oreProgress++;
            if (oreProgress < time) {
                return true;
            }
        }
        List<ItemStack> drops = Block.getDrops(state, level, pos, null, null, tool(level));
        if (!fits(drops)) {
            waiting = Waiting.OUTPUT_FULL; // keeps its progress and tries again
            return false;
        }
        drops.forEach(this::insert);
        level.setBlock(pos, fillStone ? stoneFor(level, state) : Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        mined++;
        oreIndex++;
        oreProgress = 0;
        setChanged();
        return true;
    }

    /** The front: what it's doing while it works (kept a few ticks), idle while it waits, finished at the end. */
    private void updateScreen(ServerLevel level, BlockPos pos, BlockState state, boolean worked) {
        if (worked) {
            screenCooldown = SCREEN_COOLDOWN;
        } else if (screenCooldown > 0) {
            screenCooldown--;
        }
        OreMinerBlock.Screen screen;
        if (phase == Phase.FINISHED) {
            screen = OreMinerBlock.Screen.FINISHED;
        } else if (screenCooldown > 0) {
            screen = phase == Phase.SCANNING ? OreMinerBlock.Screen.SCANNING : OreMinerBlock.Screen.MINING;
        } else {
            screen = OreMinerBlock.Screen.IDLE;
        }
        boolean lit = screenCooldown > 0 && phase != Phase.FINISHED;
        if (state.getValue(OreMinerBlock.SCREEN) != screen || state.getValue(MachineBlock.LIT) != lit) {
            level.setBlock(pos, state.setValue(OreMinerBlock.SCREEN, screen).setValue(MachineBlock.LIT, lit), Block.UPDATE_ALL);
        }
    }

    // ---- Ores ----

    public static boolean isOre(BlockState state) {
        return state.is(Tags.Blocks.ORES);
    }

    /** What fills a mined ore's spot: deepslate for deepslate ores, the dimension's stone otherwise. */
    public static BlockState stoneFor(Level level, BlockState ore) {
        if (BuiltInRegistries.BLOCK.getKey(ore.getBlock()).getPath().contains("deepslate")) {
            return Blocks.DEEPSLATE.defaultBlockState();
        }
        if (level.dimension() == Level.NETHER) {
            return Blocks.NETHERRACK.defaultBlockState();
        }
        if (level.dimension() == Level.END) {
            return Blocks.END_STONE.defaultBlockState();
        }
        return Blocks.STONE.defaultBlockState();
    }

    /** A netherite pickaxe, with Silk Touch when the miner gives ore blocks. */
    private ItemStack tool(ServerLevel level) {
        ItemStack pickaxe = new ItemStack(Items.NETHERITE_PICKAXE);
        if (oreBlocks && tier().hasOptions()) {
            pickaxe.enchant(level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH), 1);
        }
        return pickaxe;
    }

    /** With a circuit in (tier 3+): an ore passes if its block or what it drops is listed (whitelist), or isn't (blacklist). */
    private boolean passesFilter(ServerLevel level, BlockPos pos, BlockState state) {
        if (!hasCircuit()) {
            return true;
        }
        boolean listed = lists(new ItemStack(state.getBlock()));
        if (!listed) {
            for (ItemStack drop : Block.getDrops(state, level, pos, null, null, new ItemStack(Items.NETHERITE_PICKAXE))) {
                listed |= lists(drop);
            }
        }
        return blacklist != listed;
    }

    public boolean hasCircuit() {
        Item item = tier().circuit();
        return item != null && circuit.is(item);
    }

    private boolean lists(ItemStack stack) {
        for (ItemStack entry : filter) {
            if (!entry.isEmpty() && ItemStack.isSameItem(entry, stack)) {
                return true;
            }
        }
        return false;
    }

    // ---- Output slots ----

    private boolean hasOutput() {
        for (ItemStack stack : items) {
            if (!stack.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    /** Whether all these drops fit into the outputs together. */
    private boolean fits(List<ItemStack> drops) {
        List<ItemStack> copy = new ArrayList<>();
        items.forEach(stack -> copy.add(stack.copy()));
        for (ItemStack drop : drops) {
            ItemStack rest = drop.copy();
            for (int slot = 0; slot < copy.size() && !rest.isEmpty(); slot++) {
                rest = merge(copy, slot, rest);
            }
            if (!rest.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    private void insert(ItemStack drop) {
        ItemStack rest = drop.copy();
        for (int slot = 0; slot < items.size() && !rest.isEmpty(); slot++) {
            rest = merge(items, slot, rest);
        }
    }

    /** Puts what fits of {@code stack} into the slot; returns the rest. */
    private static ItemStack merge(List<ItemStack> slots, int slot, ItemStack stack) {
        ItemStack in = slots.get(slot);
        if (in.isEmpty()) {
            int moved = Math.min(stack.getMaxStackSize(), stack.getCount());
            slots.set(slot, stack.copyWithCount(moved));
            return stack.copyWithCount(stack.getCount() - moved);
        }
        if (ItemStack.isSameItemSameComponents(in, stack)) {
            int moved = Math.min(in.getMaxStackSize() - in.getCount(), stack.getCount());
            in.grow(moved);
            return stack.copyWithCount(stack.getCount() - moved);
        }
        return stack;
    }

    // ---- GUI settings (menu buttons) ----

    public boolean handleButton(int id) {
        switch (id) {
            case BUTTON_FILL -> fillStone = !fillStone;
            case BUTTON_DROPS -> {
                if (!tier().hasOptions()) {
                    return false;
                }
                oreBlocks = !oreBlocks;
            }
            case BUTTON_LIST -> {
                if (!tier().hasOptions()) {
                    return false;
                }
                blacklist = !blacklist;
            }
            default -> {
                return controls.handleButton(id);
            }
        }
        setChanged();
        return true;
    }

    public ContainerData dataAccess() {
        return dataAccess;
    }

    /** The circuit (slot 0) and the 9 ghost filter slots (1-9), for the menu. */
    public final Container filterView = new Container() {
        @Override
        public int getContainerSize() {
            return 1 + FILTER_SLOTS;
        }

        @Override
        public boolean isEmpty() {
            return circuit.isEmpty();
        }

        @Override
        public ItemStack getItem(int slot) {
            return slot == 0 ? circuit : filter.get(slot - 1);
        }

        @Override
        public ItemStack removeItem(int slot, int count) {
            if (slot != 0 || circuit.isEmpty()) {
                return ItemStack.EMPTY;
            }
            ItemStack taken = circuit.split(count);
            setChanged();
            return taken;
        }

        @Override
        public ItemStack removeItemNoUpdate(int slot) {
            return removeItem(slot, 64);
        }

        @Override
        public void setItem(int slot, ItemStack stack) {
            if (slot == 0) {
                circuit = stack;
            } else {
                filter.set(slot - 1, stack.isEmpty() ? ItemStack.EMPTY : stack.copyWithCount(1));
            }
            setChanged();
        }

        @Override
        public void setChanged() {
            OreMinerBlockEntity.this.setChanged();
        }

        @Override
        public boolean stillValid(Player player) {
            return Container.stillValidBlockEntity(OreMinerBlockEntity.this, player);
        }

        @Override
        public void clearContent() {}
    };

    @Override
    protected Component getDefaultName() {
        return getBlockState().getBlock().getName();
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new OreMinerMenu(containerId, inventory, this);
    }

    // ---- Removal: drops and tickets ----

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        super.preRemoveSideEffects(pos, state);
        if (level instanceof ServerLevel serverLevel) {
            releaseTickets(serverLevel);
            Containers.dropItemStack(serverLevel, pos.getX(), pos.getY(), pos.getZ(), circuit);
            circuit = ItemStack.EMPTY;
            Containers.dropContents(serverLevel, pos, upgrades); // (the wrench empties them first)
        }
    }

    // ---- Wrench pickup keeps FE, settings and upgrades ----

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        energy.saveTo(components);
        controls.saveTo(components);
        upgrades.saveTo(components);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        upgrades.loadFrom(components);
        energy.loadFrom(components);
        controls.loadFrom(components);
    }

    // ---- Saving ----

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        items = NonNullList.withSize(OUTPUT_SLOTS, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, items);
        upgrades.load(input); // first: it sets the buffer size the FE is loaded into
        energy.deserialize(input);
        controls.load(input);
        circuit = input.read("circuit", ItemStack.CODEC).orElse(ItemStack.EMPTY);
        List<ItemStack> list = input.read("filter", ItemStack.OPTIONAL_CODEC.listOf()).orElse(List.of());
        for (int i = 0; i < FILTER_SLOTS; i++) {
            filter.set(i, i < list.size() ? list.get(i) : ItemStack.EMPTY);
        }
        blacklist = input.getBooleanOr("blacklist", false);
        fillStone = input.getBooleanOr("fill_stone", true);
        oreBlocks = input.getBooleanOr("ore_blocks", false);
        startFacing = input.read("start_facing", Direction.CODEC).orElse(null);
        phase = Phase.values()[Math.floorMod(input.getIntOr("phase", 0), Phase.values().length)];
        chunkIndex = input.getIntOr("chunk", 0);
        scanCursor = input.getIntOr("scan_cursor", 0);
        scanTop = input.getIntOr("scan_top", Integer.MIN_VALUE);
        ores.clear();
        ores.addAll(input.read("ores", Codec.LONG.listOf()).orElse(List.of()));
        oreIndex = input.getIntOr("ore_index", 0);
        oreProgress = input.getIntOr("ore_progress", 0);
        mined = input.getLongOr("mined", 0);
        workTicket = input.getLongOr("work_ticket", Long.MAX_VALUE);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, items);
        energy.serialize(output);
        upgrades.save(output);
        controls.save(output);
        if (!circuit.isEmpty()) {
            output.store("circuit", ItemStack.CODEC, circuit);
        }
        output.store("filter", ItemStack.OPTIONAL_CODEC.listOf(), filter);
        output.putBoolean("blacklist", blacklist);
        output.putBoolean("fill_stone", fillStone);
        output.putBoolean("ore_blocks", oreBlocks);
        if (startFacing != null) {
            output.store("start_facing", Direction.CODEC, startFacing);
        }
        output.putInt("phase", phase.ordinal());
        output.putInt("chunk", chunkIndex);
        output.putInt("scan_cursor", scanCursor);
        output.putInt("scan_top", scanTop);
        output.store("ores", Codec.LONG.listOf(), ores);
        output.putInt("ore_index", oreIndex);
        output.putInt("ore_progress", oreProgress);
        output.putLong("mined", mined);
        output.putLong("work_ticket", workTicket);
    }

    // ---- Container: the 9 output slots; Output faces give them, nothing goes in ----

    @Override
    public int getContainerSize() {
        return OUTPUT_SLOTS;
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return items;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return false;
    }

    private static final int[] ALL_SLOTS = {0, 1, 2, 3, 4, 5, 6, 7, 8};
    private static final int[] NO_SLOTS = {};

    @Override
    public int[] getSlotsForFace(Direction side) {
        return sideMode(side) == SideMode.OUTPUT ? ALL_SLOTS : NO_SLOTS;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return sideMode(side) == SideMode.OUTPUT;
    }
}
