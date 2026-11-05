package com.script.comminutioncraft.common.machine;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/** Particle Accelerator ring block. Its beam glows ({@link #LIT}) while the accelerator it's part of runs. */
public class AcceleratorSegmentBlock extends Block {
    public static final BooleanProperty LIT = MachineBlock.LIT;

    public AcceleratorSegmentBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }
}
