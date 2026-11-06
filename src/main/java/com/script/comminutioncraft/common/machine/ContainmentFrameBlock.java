package com.script.comminutioncraft.common.machine;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/** Event Horizon Reactor frame block (the cage's edges). Its rails glow ({@link #LIT}) while the black hole is alive. */
public class ContainmentFrameBlock extends Block {
    public static final BooleanProperty LIT = MachineBlock.LIT;

    public ContainmentFrameBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }
}
