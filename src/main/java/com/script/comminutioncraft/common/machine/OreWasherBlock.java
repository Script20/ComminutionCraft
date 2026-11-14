package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;

public class OreWasherBlock extends MachineBlock {
    public OreWasherBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected BlockEntityType<? extends OreWasherBlockEntity> getBlockEntityType() {
        return ModBlockEntities.ORE_WASHER.get();
    }

    /**
     * Right-click with a water bucket (or any water container) pours it into the washer's tank, from any face (owner,
     * 2026-11-14). Water only goes in: an empty bucket, anything else, or a full tank opens the GUI as usual.
     */
    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player,
                                          InteractionHand hand, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof OreWasherBlockEntity washer
                && FluidUtil.interactWithFluidHandler(player, hand, pos, washer.getWater().insertOnly(), null)) {
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.TRY_WITH_EMPTY_HAND;
    }
}
