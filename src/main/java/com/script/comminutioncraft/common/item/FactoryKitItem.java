package com.script.comminutioncraft.common.item;

import com.script.comminutioncraft.common.machine.ElectricMachineBlockEntity;
import com.script.comminutioncraft.common.machine.Factories;
import com.script.comminutioncraft.common.machine.MachineBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Factory Upgrade Kit: right-click a placed machine of the kit's tier and it becomes its factory, keeping its FE,
 * settings, upgrades, tanks and items. Acts before the machine's GUI opens ({@code onItemUseFirst}).
 */
public class FactoryKitItem extends Item {
    public FactoryKitItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        Factories.Factory factory = Factories.upgrade(state.getBlock(), this);
        if (factory == null || !(level.getBlockEntity(pos) instanceof ElectricMachineBlockEntity machine)) {
            return InteractionResult.PASS;
        }
        if (level instanceof ServerLevel server) {
            Player player = context.getPlayer();
            if (player instanceof ServerPlayer serverPlayer) {
                machine.awardUsedRecipesAndPopExperience(serverPlayer);
            }
            upgrade(server, pos, state, machine, factory);
            if (player == null || !player.hasInfiniteMaterials()) {
                stack.shrink(1);
            }
            level.playSound(null, pos, SoundEvents.SMITHING_TABLE_USE, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
        return InteractionResult.SUCCESS;
    }

    /** Swaps the machine for its factory (no drops: everything moves across). */
    public static void upgrade(ServerLevel level, BlockPos pos, BlockState state, ElectricMachineBlockEntity machine, Factories.Factory factory) {
        BlockState factoryState = factory.factory().get().defaultBlockState().setValue(MachineBlock.FACING, state.getValue(MachineBlock.FACING));
        level.setBlock(pos, factoryState, Block.UPDATE_ALL | Block.UPDATE_SKIP_BLOCK_ENTITY_SIDEEFFECTS);
        if (level.getBlockEntity(pos) instanceof ElectricMachineBlockEntity upgraded) {
            machine.moveInto(upgraded);
        }
    }
}
