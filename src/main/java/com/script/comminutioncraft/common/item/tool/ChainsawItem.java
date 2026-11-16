package com.script.comminutioncraft.common.item.tool;

import com.script.comminutioncraft.common.config.MachineConfig;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * The chainsaw: an axe that also cuts leaves. Fusion and up fell every log of a tree in one swing; the Singularity
 * Chainsaw, right-clicked on a tree, clears the lot (logs and leaves), replants a sapling and gives sticks for the
 * leaves. A "tree" is logs with natural leaves on them, so log buildings are safe.
 */
public class ChainsawItem extends PoweredTool {
    /** Sticks per this many leaves cleared. */
    private static final int LEAVES_PER_STICK = 6;

    public ChainsawItem(ToolTier tier, Properties properties) {
        super(tier, ModToolMaterials.MINEABLE_CHAINSAW, 4.0F + tier.level, -3.0F, properties);
    }

    @Override
    protected void afterMining(ItemStack stack, ServerLevel level, BlockState state, BlockPos pos, ServerPlayer player) {
        if (tier.level >= 4 && state.is(BlockTags.LOGS)) {
            fell(stack, level, pos, state.getBlock(), player, false);
        }
    }

    /** Singularity Chainsaw: right-click a tree to clear it and replant. */
    @Override
    public InteractionResult useOn(UseOnContext context) {
        BlockState state = context.getLevel().getBlockState(context.getClickedPos());
        if (tier.level < 6 || !state.is(BlockTags.LOGS) || !hasPower(context.getItemInHand())) {
            return super.useOn(context);
        }
        if (context.getLevel() instanceof ServerLevel level && context.getPlayer() instanceof ServerPlayer player) {
            fell(context.getItemInHand(), level, context.getClickedPos(), state.getBlock(), player, true);
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * Fells the tree around {@code start}: logs of the same kind (a swing takes those at and above the cut, like
     * felling a real tree). When {@code clear}: the whole trunk goes, {@code start} included, the tree's leaves are
     * removed (no drops: sticks instead) and a sapling is planted where the trunk stood.
     */
    public void fell(ItemStack stack, ServerLevel level, BlockPos start, Block log, ServerPlayer player, boolean clear) {
        List<BlockPos> logs = AreaBreaker.connected(start, p -> (clear || p.getY() >= start.getY()) && level.getBlockState(p).is(log),
                MachineConfig.TOOL_TREE_MAX.get());
        List<BlockPos> trunk = new ArrayList<>(logs);
        if (clear) {
            trunk.add(start);
        }
        if (trunk.stream().noneMatch(p -> touchesNaturalLeaves(level, p))) {
            return; // not a tree (a log wall, a cabin...): just the one block
        }
        List<BlockPos> leaves = clear ? treeLeaves(level, trunk) : List.of();
        BlockPos base = trunk.stream().min((a, b) -> Integer.compare(a.getY(), b.getY())).orElse(start);
        if (clear && !breakExtra(stack, player, start)) {
            return;
        }
        for (BlockPos p : logs) {
            if (!breakExtra(stack, player, p)) {
                break;
            }
        }
        if (!clear) {
            return;
        }
        for (BlockPos p : leaves) {
            level.removeBlock(p, false);
        }
        int sticks = leaves.isEmpty() ? 0 : Math.max(1, leaves.size() / LEAVES_PER_STICK);
        while (sticks > 0) {
            int n = Math.min(64, sticks);
            Block.popResource(level, base, new ItemStack(Items.STICK, n));
            sticks -= n;
        }
        Block sapling = saplingFor(log);
        if (sapling != null && level.getBlockState(base).isAir() && sapling.defaultBlockState().canSurvive(level, base)) {
            level.setBlockAndUpdate(base, sapling.defaultBlockState());
        }
    }

    private static boolean isNaturalLeaves(BlockState state) {
        return state.getBlock() instanceof LeavesBlock && !state.getValue(LeavesBlock.PERSISTENT);
    }

    private static boolean touchesNaturalLeaves(ServerLevel level, BlockPos pos) {
        for (BlockPos p : BlockPos.betweenClosed(pos.offset(-1, -1, -1), pos.offset(1, 1, 1))) {
            if (isNaturalLeaves(level.getBlockState(p))) {
                return true;
            }
        }
        return false;
    }

    /** The natural leaves connected to the trunk (through other leaves), up to the configured limit. */
    private static List<BlockPos> treeLeaves(ServerLevel level, List<BlockPos> trunk) {
        int max = MachineConfig.TOOL_LEAVES_MAX.get();
        Set<BlockPos> found = new LinkedHashSet<>();
        for (BlockPos log : trunk) {
            if (found.size() >= max) {
                break;
            }
            for (BlockPos p : AreaBreaker.connected(log, q -> isNaturalLeaves(level.getBlockState(q)) && q.distManhattan(log) <= 8,
                    max - found.size())) {
                found.add(p);
            }
        }
        return new ArrayList<>(found);
    }

    /** The sapling that grows this log: oak_log -> oak_sapling, mangrove_log -> mangrove_propagule, crimson_stem -> crimson_fungus. */
    static @Nullable Block saplingFor(Block log) {
        Identifier id = BuiltInRegistries.BLOCK.getKey(log);
        String path = id.getPath();
        String sapling;
        if (path.equals("mangrove_log")) {
            sapling = "mangrove_propagule";
        } else if (path.endsWith("_log")) {
            sapling = path.substring(0, path.length() - 4) + "_sapling";
        } else if (path.endsWith("_stem")) {
            sapling = path.substring(0, path.length() - 5) + "_fungus";
        } else {
            return null;
        }
        Identifier saplingId = id.withPath(sapling);
        return BuiltInRegistries.BLOCK.containsKey(saplingId) ? BuiltInRegistries.BLOCK.getValue(saplingId) : null;
    }
}
