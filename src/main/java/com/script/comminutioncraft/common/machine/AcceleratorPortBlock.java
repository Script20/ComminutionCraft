package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * Particle Accelerator port: a machine block (LIT while the ring runs) without a GUI. The whole block is either an
 * Input port (every face takes FE and hydrogen) or an Output port ({@link #OUTPUT}: every face gives antimatter); the
 * wrench's Configure mode switches it. New ports start as Input.
 */
public class AcceleratorPortBlock extends MachineBlock {
    public static final BooleanProperty OUTPUT = BooleanProperty.create("output");

    public AcceleratorPortBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(OUTPUT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(OUTPUT);
    }

    @Override
    protected BlockEntityType<? extends net.minecraft.world.level.block.entity.BlockEntity> getBlockEntityType() {
        return ModBlockEntities.ACCELERATOR_PORT.get();
    }

    public static boolean isOutput(BlockState state) {
        return state.getValue(OUTPUT);
    }

    /** Switches the port at pos between Input and Output; returns the new mode (true = Output). */
    public static boolean toggle(Level level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        boolean output = !state.getValue(OUTPUT);
        level.setBlock(pos, state.setValue(OUTPUT, output), Block.UPDATE_ALL);
        if (level.getBlockEntity(pos) instanceof com.script.comminutioncraft.common.machine.control.ControllableMachine port) {
            port.refreshFaces(); // pipes and cables next to it re-check what it now offers
        }
        return output;
    }
}
