package com.script.comminutioncraft.common.item.tool;

import com.script.comminutioncraft.common.config.MachineConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.Tags;
import org.jspecify.annotations.Nullable;

/**
 * The drill: a pickaxe and a shovel in one. Fusion and up mine 3x3 (the plane facing the player), Antimatter and up
 * carry Fortune III, and the Singularity Drill vein-mines an ore when you right-click it.
 */
public class DrillItem extends PoweredTool {
    public DrillItem(ToolTier tier, Properties properties) {
        super(tier, ModToolMaterials.MINEABLE_DRILL, 2.0F + tier.level * 0.5F, -2.8F, properties);
    }

    @Override
    protected void afterMining(ItemStack stack, ServerLevel level, BlockState state, BlockPos pos, ServerPlayer player) {
        if (tier.level < 4) {
            return;
        }
        Direction.Axis axis = Math.abs(player.getXRot()) > 45 ? Direction.Axis.Y : player.getDirection().getAxis();
        for (int a = -1; a <= 1; a++) {
            for (int b = -1; b <= 1; b++) {
                if (a == 0 && b == 0) {
                    continue;
                }
                BlockPos p = switch (axis) {
                    case X -> pos.offset(0, a, b);
                    case Y -> pos.offset(a, 0, b);
                    case Z -> pos.offset(a, b, 0);
                };
                BlockState other = level.getBlockState(p);
                if (!other.isAir() && other.getDestroySpeed(level, p) >= 0 && isCorrectToolForDrops(stack, other)
                        && !breakExtra(stack, player, p)) {
                    return;
                }
            }
        }
    }

    /** Singularity Drill: right-click an ore to take the whole vein (the same block, touching, diagonals too). */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        if (tier.level < 6 || !state.is(Tags.Blocks.ORES) || !hasPower(context.getItemInHand())) {
            return super.useOn(context);
        }
        if (context.getLevel() instanceof ServerLevel level && context.getPlayer() instanceof ServerPlayer player) {
            veinMine(context.getItemInHand(), level, context.getClickedPos(), state.getBlock(), player);
        }
        return InteractionResult.SUCCESS;
    }

    /** Breaks the ore and every touching block of the same ore, up to the configured limit, while there's FE. */
    public void veinMine(ItemStack stack, ServerLevel level, BlockPos start, Block ore, ServerPlayer player) {
        var vein = AreaBreaker.connected(start, p -> level.getBlockState(p).is(ore), MachineConfig.TOOL_VEIN_MAX.get() - 1);
        if (!breakExtra(stack, player, start)) {
            return;
        }
        for (BlockPos p : vein) {
            if (!breakExtra(stack, player, p)) {
                return;
            }
        }
    }

    /** Antimatter and Singularity Drills carry Fortune III (put back if it's ever taken off). */
    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
        super.inventoryTick(stack, level, owner, slot);
        if (tier.level >= 5) {
            Holder<Enchantment> fortune = level.registryAccess().lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.FORTUNE);
            if (EnchantmentHelper.getItemEnchantmentLevel(fortune, stack) < 3) {
                EnchantmentHelper.updateEnchantments(stack, enchantments -> enchantments.set(fortune, 3));
            }
        }
    }
}
