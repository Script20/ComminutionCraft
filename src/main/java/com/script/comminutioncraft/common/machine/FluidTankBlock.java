package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;

/**
 * Fluid Tank block: right-click with a bucket (or any fluid container) to fill or empty it. It has no GUI; its
 * faces are set with the wrench. Its window is see-through (the model renders cutout), the contents are drawn by
 * FluidTankRenderer. Comparators read how full it is.
 */
public class FluidTankBlock extends MachineBlock {
    public FluidTankBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected BlockEntityType<? extends FluidTankBlockEntity> getBlockEntityType() {
        return ModBlockEntities.FLUID_TANK.get();
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                          InteractionHand hand, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof FluidTankBlockEntity tank
                && FluidUtil.interactWithFluidHandler(player, hand, pos, tank.getTank(), null)) {
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }

    @Override
    protected boolean hasAnalogOutputSignal(BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(BlockState state, Level level, BlockPos pos, Direction direction) {
        return level.getBlockEntity(pos) instanceof FluidTankBlockEntity tank ? tank.getComparatorSignal() : 0;
    }
}
