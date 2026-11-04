package com.script.comminutioncraft.common.machine;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/** Fusion Reactor frame block. Its seams glow ({@link #LIT}) while the reactor it's part of is burning. */
public class ReactorCasingBlock extends Block {
    public static final BooleanProperty LIT = MachineBlock.LIT;

    public ReactorCasingBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(LIT, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(LIT);
    }
}
