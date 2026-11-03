package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/**
 * Energy Battery: a machine block (facing, GUI, ticking) whose front shows its charge in {@link #CHARGE} cells.
 * It never sets LIT. Comparators read the charge (0 when empty, 15 when full).
 */
public class EnergyBatteryBlock extends MachineBlock {
    public static final IntegerProperty CHARGE = IntegerProperty.create("charge", 0, EnergyBatteryBlockEntity.CELLS);

    public EnergyBatteryBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(CHARGE, 0));
    }

    @Override
    protected BlockEntityType<EnergyBatteryBlockEntity> getBlockEntityType() {
        return ModBlockEntities.ENERGY_BATTERY.get();
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(CHARGE);
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        return level.getBlockEntity(pos) instanceof EnergyBatteryBlockEntity battery ? battery.getComparatorSignal() : 0;
    }
}
