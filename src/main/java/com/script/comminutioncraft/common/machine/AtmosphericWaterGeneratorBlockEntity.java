package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.fluid.MachineFluidTank;
import com.script.comminutioncraft.common.machine.control.ControllableMachine;
import com.script.comminutioncraft.common.machine.control.MachineControls;
import com.script.comminutioncraft.common.machine.control.SideMode;
import com.script.comminutioncraft.common.registry.ModBlockEntities;
import com.script.comminutioncraft.common.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.attribute.EnvironmentAttributes;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jspecify.annotations.Nullable;

/**
 * Tier 3 Atmospheric Water Generator: condenses water out of the air, no FE needed. The start of the power chain
 * (water -> Electrolyser -> hydrogen -> Plasma Generator -> plasma -> MHD Generator -> FE), and a water supply for the
 * Ore Washer.
 * <p>
 * It breathes: every face touching an air block is an intake making {@link #WATER_PER_INTAKE} mB/t at 100% humidity
 * (fully walled in = nothing). Humidity comes from the biome (see {@link #humidity}): almost nothing in a desert,
 * more by a river, none where water evaporates (the Nether). An Air Filter doubles the output and wears 1 point
 * per {@link #MB_PER_FILTER_WEAR} mB. Water goes out of the faces set to Output.
 */
public class AtmosphericWaterGeneratorBlockEntity extends BaseContainerBlockEntity
        implements WorldlyContainer, TickingMachine, ControllableMachine {
    public static final int SLOT_FILTER = 0;
    public static final int SLOT_COUNT = 1;
    private static final int[] SLOTS_FILTER = {SLOT_FILTER};
    private static final int[] SLOTS_NONE = {};

    public static final float WATER_PER_INTAKE = 1.0F;
    public static final int FILTER_DURABILITY = 10_000;
    public static final int MB_PER_FILTER_WEAR = 100;
    public static final int WATER_CAPACITY = 8_000;
    /** Max mB pushed out per tick, across all output faces. */
    public static final int MAX_WATER_OUTPUT = 500;
    private static final int LIT_COOLDOWN = 10;

    // ContainerData indices, synced to the open GUI
    public static final int DATA_WATER = 0;
    public static final int DATA_WATER_CAPACITY = 1;
    public static final int DATA_INTAKE = 2;
    /** mB per tick x 10 (it makes fractions of a mB per tick in dry biomes). */
    public static final int DATA_RATE_TENTHS = 3;
    public static final int DATA_HUMIDITY = 4;
    public static final int DATA_REDSTONE = 5;
    public static final int DATA_SIDES = 6;
    public static final int DATA_COUNT = 7;

    private NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private final MachineFluidTank tank = new MachineFluidTank(WATER_CAPACITY,
            resource -> resource.is(NeoForgeMod.WATER_TYPE.value()), this::setChanged);
    private final MachineControls controls = new MachineControls(MachineControls.outputBelow(), this::onControlsChanged);
    private int intake;
    private float humidity;
    /** mB made per tick right now (for the GUI and Jade). */
    private float rate;
    /** Fractions of a mB made so far, and mB made since the filter last wore. */
    private float partial;
    private int sinceWear;
    private int litCooldown;

    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_WATER -> tank.getAmount();
                case DATA_WATER_CAPACITY -> tank.getCapacity();
                case DATA_INTAKE -> intake;
                case DATA_RATE_TENTHS -> Math.round(rate * 10);
                case DATA_HUMIDITY -> Math.round(humidity * 100);
                case DATA_REDSTONE -> controls.redstone().ordinal();
                case DATA_SIDES -> controls.packSides();
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

    public AtmosphericWaterGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.ATMOSPHERIC_WATER_GENERATOR.get(), pos, state);
    }

    /** Water can only be taken out, and only through output faces (and internal "no side" access). */
    public @Nullable ResourceHandler<FluidResource> getFluidHandler(@Nullable Direction side) {
        if (side == null) {
            return tank;
        }
        return sideMode(side) == SideMode.OUTPUT ? tank.extractOnly() : null;
    }

    public MachineFluidTank getTank() {
        return tank;
    }

    public int getIntake() {
        return intake;
    }

    public float getHumidity() {
        return humidity;
    }

    /** mB made per tick right now. */
    public float getRate() {
        return rate;
    }

    public boolean hasFilter() {
        return items.get(SLOT_FILTER).is(ModItems.AIR_FILTER.get());
    }

    /**
     * How much water the air holds here, as a multiplier on the output: the biome's rainfall ("downfall") from 5%
     * (desert, badlands, savanna) up to 140% (jungle, swamp), half as much again by rivers, oceans and beaches,
     * and nothing where water evaporates (the Nether).
     */
    public static float humidity(Level level, BlockPos pos) {
        if (level.environmentAttributes().getValue(EnvironmentAttributes.WATER_EVAPORATES, pos)) {
            return 0;
        }
        return humidity(level.getBiome(pos));
    }

    /** The biome part of {@link #humidity(Level, BlockPos)}. */
    public static float humidity(Holder<Biome> biome) {
        float humidity = 0.05F + 1.5F * biome.value().getModifiedClimateSettings().downfall();
        if (biome.is(BiomeTags.IS_RIVER) || biome.is(BiomeTags.IS_OCEAN) || biome.is(BiomeTags.IS_BEACH)) {
            humidity *= 1.5F;
        }
        return Math.min(humidity, 2.0F);
    }

    /** Faces touching an air block. */
    static int countIntakes(ServerLevel level, BlockPos pos) {
        int open = 0;
        for (Direction direction : Direction.values()) {
            if (level.getBlockState(pos.relative(direction)).isAir()) {
                open++;
            }
        }
        return open;
    }

    // ---- Ticking ----

    @Override
    public MachineControls getControls() {
        return controls;
    }

    private void onControlsChanged() {
        setChanged();
        refreshFaces();
    }

    @Override
    public void serverTick(ServerLevel level, BlockPos pos, BlockState state) {
        intake = countIntakes(level, pos);
        humidity = humidity(level, pos);
        rate = 0;
        if (!controls.canRun(level, pos)) {
            litCooldown = 0;
            if (state.getValue(MachineBlock.LIT)) {
                level.setBlock(pos, state.setValue(MachineBlock.LIT, false), Block.UPDATE_ALL);
            }
            return;
        }

        boolean filtered = hasFilter();
        float perTick = intake * WATER_PER_INTAKE * humidity * (filtered ? 2 : 1);
        if (perTick > 0 && tank.getSpace() > 0) {
            rate = perTick;
            partial += perTick;
            int whole = (int) partial;
            partial -= whole;
            int made = whole > 0 ? tank.fill(FluidResource.of(Fluids.WATER), whole) : 0;
            litCooldown = LIT_COOLDOWN;
            if (filtered) {
                sinceWear += made;
                if (sinceWear >= MB_PER_FILTER_WEAR) {
                    items.get(SLOT_FILTER).hurtAndBreak(sinceWear / MB_PER_FILTER_WEAR, level, null, item -> {});
                    sinceWear %= MB_PER_FILTER_WEAR;
                }
            }
        }

        pushWater(level, pos);

        if (litCooldown > 0) {
            litCooldown--;
        }
        boolean lit = litCooldown > 0;
        if (state.getValue(MachineBlock.LIT) != lit) {
            level.setBlock(pos, state.setValue(MachineBlock.LIT, lit), Block.UPDATE_ALL);
        }
    }

    /** Offers water to every neighbour on an output face, up to {@link #MAX_WATER_OUTPUT} mB per tick in total. */
    private void pushWater(ServerLevel level, BlockPos pos) {
        int budget = Math.min(MAX_WATER_OUTPUT, tank.getAmount());
        for (Direction direction : Direction.values()) {
            if (budget <= 0) {
                return;
            }
            if (sideMode(direction) != SideMode.OUTPUT) {
                continue;
            }
            ResourceHandler<FluidResource> target = level.getCapability(Capabilities.Fluid.BLOCK, pos.relative(direction), direction.getOpposite());
            if (target != null) {
                budget -= ResourceHandlerUtil.move(tank, target, resource -> true, budget, null);
            }
        }
    }

    // ---- Wrench pickup: the item keeps the settings and the water (see WrenchItem) ----

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        tank.saveTo(components);
        controls.saveTo(components);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        tank.loadFrom(components);
        controls.loadFrom(components);
    }

    // ---- Saving ----

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        items = NonNullList.withSize(getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(input, items);
        tank.deserialize(input.childOrEmpty("water"));
        sinceWear = input.getIntOr("since_wear", 0);
        controls.load(input);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, items);
        tank.serialize(output.child("water"));
        output.putInt("since_wear", sinceWear);
        controls.save(output);
    }

    // ---- Container ----

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.comminutioncraft.atmospheric_water_generator");
    }

    @Override
    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return new AtmosphericWaterGeneratorMenu(containerId, inventory, this, dataAccess);
    }

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
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
        return stack.is(ModItems.AIR_FILTER.get());
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    // ---- Sided automation: input faces take filters ----

    @Override
    public int[] getSlotsForFace(Direction side) {
        return sideMode(side) == SideMode.INPUT ? SLOTS_FILTER : SLOTS_NONE;
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return sideMode(side) == SideMode.INPUT && canPlaceItem(slot, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return false;
    }
}
