package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.energy.EnergySync;
import com.script.comminutioncraft.common.energy.MachineEnergyStorage;
import com.script.comminutioncraft.common.machine.control.ControllableMachine;
import com.script.comminutioncraft.common.machine.control.MachineControls;
import com.script.comminutioncraft.common.registry.ModBlockEntities;
import com.script.comminutioncraft.common.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandlerUtil;
import net.neoforged.neoforge.transfer.energy.LimitingEnergyHandler;
import org.jspecify.annotations.Nullable;

/**
 * Energy Battery. Tier 2 stores {@link #CAPACITY} FE and moves {@link #TRANSFER_RATE} FE/t; the tier-3 Plasma Energy
 * Battery stores {@link #PLASMA_CAPACITY} and moves {@link #PLASMA_TRANSFER_RATE}. Input faces accept up to the rate
 * per insert; every tick it pushes up to the rate in total out of its output faces (into cables or straight into
 * machines). None faces don't connect at all. Redstone-off stops the output but it keeps charging.
 * <p>
 * Mined with a pickaxe it forgets everything; picked up with the wrench it keeps its charge and settings.
 */
public class EnergyBatteryBlockEntity extends BlockEntity implements TickingMachine, ControllableMachine, MenuProvider {
    public static final com.script.comminutioncraft.common.config.ConfigInt CAPACITY = com.script.comminutioncraft.common.config.MachineConfig.ENERGY_BATTERY_CAPACITY;
    public static final com.script.comminutioncraft.common.config.ConfigInt TRANSFER_RATE = com.script.comminutioncraft.common.config.MachineConfig.ENERGY_BATTERY_TRANSFER_RATE;
    public static final com.script.comminutioncraft.common.config.ConfigInt PLASMA_CAPACITY = com.script.comminutioncraft.common.config.MachineConfig.PLASMA_ENERGY_BATTERY_CAPACITY;
    public static final com.script.comminutioncraft.common.config.ConfigInt PLASMA_TRANSFER_RATE = com.script.comminutioncraft.common.config.MachineConfig.PLASMA_ENERGY_BATTERY_TRANSFER_RATE;
    public static final com.script.comminutioncraft.common.config.ConfigInt FUSION_CAPACITY = com.script.comminutioncraft.common.config.MachineConfig.FUSION_ENERGY_BATTERY_CAPACITY;
    public static final com.script.comminutioncraft.common.config.ConfigInt FUSION_TRANSFER_RATE = com.script.comminutioncraft.common.config.MachineConfig.FUSION_ENERGY_BATTERY_TRANSFER_RATE;
    public static final com.script.comminutioncraft.common.config.ConfigInt ANTIMATTER_CAPACITY = com.script.comminutioncraft.common.config.MachineConfig.ANTIMATTER_ENERGY_BATTERY_CAPACITY;
    public static final com.script.comminutioncraft.common.config.ConfigInt ANTIMATTER_TRANSFER_RATE = com.script.comminutioncraft.common.config.MachineConfig.ANTIMATTER_ENERGY_BATTERY_TRANSFER_RATE;
    /** Charge cells on the front texture (blockstate {@code charge} 0..CELLS). */
    public static final int CELLS = 6;

    // ContainerData indices, synced to the open GUI (energy split in two, see EnergySync)
    public static final int DATA_ENERGY_LOW = 0;
    public static final int DATA_ENERGY_HIGH = 1;
    public static final int DATA_CAPACITY_LOW = 2;
    public static final int DATA_CAPACITY_HIGH = 3;
    public static final int DATA_REDSTONE = 4;
    public static final int DATA_SIDES = 5;
    public static final int DATA_RECEIVED = 6;
    public static final int DATA_SENT = 7;
    public static final int DATA_RECEIVED_HIGH = 8;
    public static final int DATA_SENT_HIGH = 9;
    public static final int DATA_COUNT = 10;

    private final int capacity;
    private final int transferRate;
    private final MenuType<EnergyBatteryMenu> menuType;
    private final MachineEnergyStorage energy;
    private final EnergyHandler inputFace;
    private final EnergyHandler outputFace;
    private final MachineControls controls = new MachineControls(MachineControls.outputBelow(), this::onControlsChanged);
    /** FE that came in / went out during the last tick (for the GUI and Jade). */
    private int received;
    private int sent;
    private int energyAfterLastTick;
    private int lastComparatorSignal;

    protected final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case DATA_ENERGY_LOW -> EnergySync.low(energy.getEnergy());
                case DATA_ENERGY_HIGH -> EnergySync.high(energy.getEnergy());
                case DATA_CAPACITY_LOW -> EnergySync.low(energy.getCapacity());
                case DATA_CAPACITY_HIGH -> EnergySync.high(energy.getCapacity());
                case DATA_REDSTONE -> controls.redstone().ordinal();
                case DATA_SIDES -> controls.packSides();
                case DATA_RECEIVED -> EnergySync.low(received);
                case DATA_SENT -> EnergySync.low(sent);
                case DATA_RECEIVED_HIGH -> EnergySync.high(received);
                case DATA_SENT_HIGH -> EnergySync.high(sent);
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

    /** Tier 2 Energy Battery. */
    public EnergyBatteryBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntities.ENERGY_BATTERY.get(), ModMenus.ENERGY_BATTERY.get(), pos, state, CAPACITY.get(), TRANSFER_RATE.get());
    }

    /** Tier 3 Plasma Energy Battery. */
    public static EnergyBatteryBlockEntity plasma(BlockPos pos, BlockState state) {
        return new EnergyBatteryBlockEntity(ModBlockEntities.PLASMA_ENERGY_BATTERY.get(), ModMenus.PLASMA_ENERGY_BATTERY.get(),
                pos, state, PLASMA_CAPACITY.get(), PLASMA_TRANSFER_RATE.get());
    }

    /** Tier 4 Fusion Energy Battery. */
    public static EnergyBatteryBlockEntity fusion(BlockPos pos, BlockState state) {
        return new EnergyBatteryBlockEntity(ModBlockEntities.FUSION_ENERGY_BATTERY.get(), ModMenus.FUSION_ENERGY_BATTERY.get(),
                pos, state, FUSION_CAPACITY.get(), FUSION_TRANSFER_RATE.get());
    }

    /** Tier 5 Antimatter Energy Battery. */
    public static EnergyBatteryBlockEntity antimatter(BlockPos pos, BlockState state) {
        return new EnergyBatteryBlockEntity(ModBlockEntities.ANTIMATTER_ENERGY_BATTERY.get(), ModMenus.ANTIMATTER_ENERGY_BATTERY.get(),
                pos, state, ANTIMATTER_CAPACITY.get(), ANTIMATTER_TRANSFER_RATE.get());
    }

    private EnergyBatteryBlockEntity(BlockEntityType<?> type, MenuType<EnergyBatteryMenu> menuType, BlockPos pos,
                                     BlockState state, int capacity, int transferRate) {
        super(type, pos, state);
        this.capacity = capacity;
        this.transferRate = transferRate;
        this.menuType = menuType;
        energy = new MachineEnergyStorage(capacity, transferRate, transferRate, this::setChanged);
        inputFace = new LimitingEnergyHandler(energy, transferRate, 0);
        outputFace = new LimitingEnergyHandler(energy, 0, transferRate);
    }

    public int getCapacity() {
        return capacity;
    }

    /** The FE capability on a face: insert-only on input faces, extract-only on output faces, none on None faces. */
    public @Nullable EnergyHandler getEnergyHandler(@Nullable Direction side) {
        return switch (sideMode(side)) {
            case INPUT -> side == null ? energy : inputFace;
            case OUTPUT -> outputFace;
            case NONE, BYPRODUCT -> null;
        };
    }

    public int getEnergy() {
        return energy.getEnergy();
    }

    /** Sets the stored FE directly (creative/testing). */
    public void setEnergy(int amount) {
        energy.set(Math.clamp(amount, 0, capacity));
    }

    public int getReceived() {
        return received;
    }

    public int getSent() {
        return sent;
    }

    public int getComparatorSignal() {
        int stored = energy.getEnergy();
        return stored <= 0 ? 0 : 1 + (int) ((long) (stored - 1) * 14 / capacity);
    }

    /** How many front cells are lit: 0 only when empty, all of them only above 5/6 full. */
    int chargeLevel(int stored) {
        return stored <= 0 ? 0 : Math.min(CELLS, 1 + (int) ((long) (stored - 1) * CELLS / capacity));
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
        // Inserts happen during other blocks' ticks, so whatever arrived since our last tick counts as received.
        received = Math.max(0, energy.getEnergy() - energyAfterLastTick);
        sent = controls.canRun(level, pos) ? pushEnergy(level, pos) : 0;
        energyAfterLastTick = energy.getEnergy();

        int charge = chargeLevel(energy.getEnergy());
        if (state.getValue(EnergyBatteryBlock.CHARGE) != charge) {
            level.setBlock(pos, state.setValue(EnergyBatteryBlock.CHARGE, charge), Block.UPDATE_CLIENTS);
        }
        int signal = getComparatorSignal();
        if (signal != lastComparatorSignal) {
            lastComparatorSignal = signal;
            level.updateNeighbourForOutputSignal(pos, state.getBlock());
        }
    }

    /** Offers energy to every neighbour on an output face, up to the transfer rate per tick in total. */
    private int pushEnergy(ServerLevel level, BlockPos pos) {
        int budget = Math.min(transferRate, energy.getEnergy());
        int moved = 0;
        for (Direction direction : Direction.values()) {
            if (moved >= budget) {
                break;
            }
            if (getEnergyHandler(direction) != outputFace) {
                continue;
            }
            EnergyHandler target = level.getCapability(Capabilities.Energy.BLOCK, pos.relative(direction), direction.getOpposite());
            if (target != null) {
                moved += EnergyHandlerUtil.move(energy, target, budget - moved, null);
            }
        }
        return moved;
    }

    // ---- Wrench pickup: the item keeps the settings and stored FE (see WrenchItem) ----

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components) {
        super.collectImplicitComponents(components);
        energy.saveTo(components);
        controls.saveTo(components);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter components) {
        super.applyImplicitComponents(components);
        energy.loadFrom(components);
        controls.loadFrom(components);
        energyAfterLastTick = energy.getEnergy();
    }

    // ---- Saving ----

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        energy.deserialize(input);
        energyAfterLastTick = energy.getEnergy();
        controls.load(input);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        energy.serialize(output);
        controls.save(output);
    }

    // ---- GUI ----

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Override
    public AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        return new EnergyBatteryMenu(menuType, containerId, inventory, this, dataAccess);
    }
}
