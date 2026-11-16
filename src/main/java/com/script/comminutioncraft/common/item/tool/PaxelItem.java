package com.script.comminutioncraft.common.item.tool;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.neoforged.neoforge.common.ItemAbilities;
import net.neoforged.neoforge.common.ItemAbility;

/**
 * Pickaxe, axe, shovel and hoe in one. Right-click strips / scrapes / unwaxes like an axe; on dirt or grass it makes a
 * path like a shovel, or tills like a hoe while sneaking.
 */
public class PaxelItem extends Item {
    private static final List<ItemAbility> AXE = List.of(ItemAbilities.AXE_STRIP, ItemAbilities.AXE_SCRAPE, ItemAbilities.AXE_WAX_OFF);

    public PaxelItem(ToolMaterial material, float attackDamage, float attackSpeed, Properties properties) {
        super(properties.tool(material, ModToolMaterials.MINEABLE_PAXEL, attackDamage, attackSpeed, 5.0F));
    }

    @Override
    public boolean canPerformAction(ItemInstance stack, ItemAbility ability) {
        // (what it mines comes from its tool component: the mineable/paxel tag)
        return ItemAbilities.DEFAULT_AXE_ACTIONS.contains(ability) || ItemAbilities.DEFAULT_SHOVEL_ACTIONS.contains(ability)
                || ItemAbilities.DEFAULT_HOE_ACTIONS.contains(ability);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        Player player = context.getPlayer();
        boolean sneaking = player != null && player.isSecondaryUseActive();
        boolean openAbove = context.getClickedFace() != Direction.DOWN && level.getBlockState(pos.above()).isAir();
        List<ItemAbility> order = new ArrayList<>(AXE);
        if (openAbove) {
            order.addAll(sneaking ? List.of(ItemAbilities.HOE_TILL, ItemAbilities.SHOVEL_FLATTEN)
                    : List.of(ItemAbilities.SHOVEL_FLATTEN, ItemAbilities.HOE_TILL));
        }
        BlockState state = level.getBlockState(pos);
        for (ItemAbility ability : order) {
            BlockState changed = state.getToolModifiedState(context, ability, false);
            if (changed == null) {
                continue;
            }
            level.playSound(player, pos, sound(ability), SoundSource.BLOCKS, 1.0F, 1.0F);
            if (!level.isClientSide()) {
                level.setBlock(pos, changed, Block.UPDATE_ALL_IMMEDIATE);
                level.gameEvent(GameEvent.BLOCK_CHANGE, pos, GameEvent.Context.of(player, changed));
                if (player != null) {
                    context.getItemInHand().hurtAndBreak(1, player, context.getHand().asEquipmentSlot());
                }
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    private static SoundEvent sound(ItemAbility ability) {
        if (ability == ItemAbilities.AXE_STRIP) {
            return SoundEvents.AXE_STRIP;
        } else if (ability == ItemAbilities.AXE_SCRAPE) {
            return SoundEvents.AXE_SCRAPE;
        } else if (ability == ItemAbilities.AXE_WAX_OFF) {
            return SoundEvents.AXE_WAX_OFF;
        } else if (ability == ItemAbilities.SHOVEL_FLATTEN) {
            return SoundEvents.SHOVEL_FLATTEN;
        }
        return SoundEvents.HOE_TILL;
    }
}
