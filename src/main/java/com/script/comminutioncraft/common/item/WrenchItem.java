package com.script.comminutioncraft.common.item;

import com.script.comminutioncraft.common.cable.ConduitBlock;
import com.script.comminutioncraft.common.machine.control.ControllableMachine;
import com.script.comminutioncraft.common.machine.control.RelativeSide;
import com.script.comminutioncraft.common.machine.control.SideMode;
import com.script.comminutioncraft.common.network.SwitchWrenchModePayload;
import com.script.comminutioncraft.common.registry.ModDataComponents;
import com.script.comminutioncraft.common.registry.ModItems;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Three modes, cycled with the "Switch Wrench Mode" key (V by default, see {@link SwitchWrenchModePayload}):
 * <ul>
 *   <li><b>Rotate</b> - right-click a block to turn it (machines and anything else with a facing/axis).</li>
 *   <li><b>Cable</b> - right-click a cable's or pipe's arm to cut that connection; click again to restore it.</li>
 *   <li><b>Configure</b> - right-click a machine face to cycle it between Input, Output and None;
 *       sneak + right-click picks the machine up, keeping its settings and stored FE.</li>
 * </ul>
 * The wrench acts in {@link #onItemUseFirst}, before the block's own right-click, so machines don't open their GUI.
 */
public class WrenchItem extends Item {
    /** Key mapping name (registered on the client), also used for the "press V" tooltip. */
    public static final String MODE_KEY = "key.comminutioncraft.wrench_mode";

    public WrenchItem(Properties properties) {
        super(properties);
    }

    public static WrenchMode getMode(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.WRENCH_MODE.get(), WrenchMode.ROTATE);
    }

    /** A wrench stack in the given mode (e.g. for GUI icons). */
    public static ItemStack withMode(WrenchMode mode) {
        ItemStack stack = new ItemStack(ModItems.WRENCH.get());
        stack.set(ModDataComponents.WRENCH_MODE.get(), mode);
        return stack;
    }

    /** Runs before the clicked block's own right-click; PASS (nothing to do here) lets the block handle it as usual. */
    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Player player = context.getPlayer();
        if (player != null && !player.mayUseItemAt(context.getClickedPos(), context.getClickedFace(), stack)) {
            return InteractionResult.FAIL;
        }
        return switch (getMode(stack)) {
            case ROTATE -> rotate(context);
            case CABLE -> useOnCable(context);
            case CONFIGURE -> player != null && player.isSecondaryUseActive() ? pickUp(context, player) : configure(context);
        };
    }

    /** Server side: the next mode (sent by the client when the mode key is pressed). */
    public static void switchMode(Level level, Player player, ItemStack stack) {
        stack.set(ModDataComponents.WRENCH_MODE.get(), getMode(stack).next());
        player.sendOverlayMessage(getMode(stack).displayName());
        level.playSound(null, player.blockPosition(), SoundEvents.ITEM_FRAME_ROTATE_ITEM, SoundSource.PLAYERS, 0.6F, 1.4F);
    }

    // ---- Cable mode ----

    private static InteractionResult useOnCable(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (!(level.getBlockState(pos).getBlock() instanceof ConduitBlock)) {
            return InteractionResult.PASS;
        }
        // The arm (or core face) that was clicked: whichever way the hit point sticks out from the centre most.
        Vec3 offset = context.getClickLocation().subtract(Vec3.atCenterOf(pos));
        Direction side = Direction.getApproximateNearest(offset.x, offset.y, offset.z);
        if (!level.isClientSide()) {
            boolean restored = ConduitBlock.toggleSide(level, pos, side);
            if (context.getPlayer() != null) {
                context.getPlayer().sendOverlayMessage(Component.translatable(restored
                        ? "message.comminutioncraft.wrench.cable_restored" : "message.comminutioncraft.wrench.cable_cut"));
            }
            level.playSound(null, pos, restored ? SoundEvents.COPPER_PLACE : SoundEvents.COPPER_BREAK, SoundSource.BLOCKS, 0.8F, 1.2F);
        }
        return InteractionResult.SUCCESS;
    }

    // ---- Configure mode ----

    /** Cycles the clicked face of a machine between Input, Output and None (same as the GUI's side config). */
    private static InteractionResult configure(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (!(level.getBlockEntity(pos) instanceof ControllableMachine machine)) {
            return InteractionResult.PASS;
        }
        if (level.getBlockState(pos).getBlock() instanceof com.script.comminutioncraft.common.machine.AcceleratorPortBlock) {
            // An Accelerator Port is Input or Output as a whole: the wrench switches it.
            if (!level.isClientSide()) {
                boolean output = com.script.comminutioncraft.common.machine.AcceleratorPortBlock.toggle(level, pos);
                if (context.getPlayer() != null) {
                    String port = level.getBlockState(pos).getBlock() instanceof com.script.comminutioncraft.common.machine.HorizonPortBlock
                            ? "horizon_port" : "port";
                    context.getPlayer().sendOverlayMessage(Component.translatable("message.comminutioncraft.wrench." + port + (output ? "_output" : "_input")));
                }
                level.playSound(null, pos, SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.BLOCKS, 0.4F, 1.2F);
            }
            return InteractionResult.SUCCESS;
        }
        if (!level.isClientSide()) {
            RelativeSide side = RelativeSide.of(machine.getFacing(), context.getClickedFace());
            SideMode mode = machine.getControls().cycleSide(side);
            if (context.getPlayer() != null) {
                context.getPlayer().sendOverlayMessage(Component.translatable("gui.comminutioncraft.side_config.tooltip",
                        side.displayName(), mode.displayName()));
            }
            level.playSound(null, pos, SoundEvents.UI_BUTTON_CLICK.value(), SoundSource.BLOCKS, 0.4F, 1.2F);
        }
        return InteractionResult.SUCCESS;
    }

    /**
     * Sneak + right-click in Configure mode: the machine goes straight into the player's inventory with its settings
     * and stored FE on the item. Its slots and stored XP drop as if it were mined (only a pickaxe drop forgets the rest).
     */
    private static InteractionResult pickUp(UseOnContext context, Player player) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (!(level.getBlockEntity(pos) instanceof BlockEntity machine) || !(machine instanceof ControllableMachine)) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            BlockState state = level.getBlockState(pos);
            ItemStack item = new ItemStack(state.getBlock());
            // The contents drop on removal below, so they mustn't be copied onto the item too.
            item.applyComponents(machine.collectComponents().filter(type -> type != DataComponents.CONTAINER));
            if (machine instanceof com.script.comminutioncraft.common.machine.upgrade.UpgradeHolder holder) {
                holder.getUpgrades().clearContent(); // they're on the item now; don't drop them too
            }
            level.removeBlock(pos, false);
            if (!player.getInventory().add(item)) {
                player.drop(item, false);
            }
            level.playSound(null, pos, state.getSoundType(level, pos, player).getBreakSound(), SoundSource.BLOCKS, 0.8F, 1.0F);
        }
        return InteractionResult.SUCCESS;
    }

    // ---- Rotate mode ----

    private static InteractionResult rotate(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);
        BlockState rotated = rotatedState(level, pos, state);
        if (rotated == null) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide()) {
            level.setBlock(pos, Block.updateFromNeighbourShapes(rotated, level, pos), Block.UPDATE_ALL);
            level.playSound(null, pos, SoundEvents.ITEM_FRAME_ROTATE_ITEM, SoundSource.BLOCKS, 0.8F, 1.0F);
        }
        return InteractionResult.SUCCESS;
    }

    /** The next orientation of this block, or null if it can't (or shouldn't) be turned. */
    private static @Nullable BlockState rotatedState(Level level, BlockPos pos, BlockState state) {
        // Multi-block things would tear in half.
        if (state.hasProperty(BlockStateProperties.BED_PART)
                || state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF)
                || state.getValueOrElse(BlockStateProperties.CHEST_TYPE, ChestType.SINGLE) != ChestType.SINGLE
                || state.getValueOrElse(BlockStateProperties.EXTENDED, false)) {
            return null;
        }

        if (state.hasProperty(BlockStateProperties.FACING)) {
            // Six-way blocks (observers, dispensers...): step through every direction they can survive in.
            Direction start = state.getValue(BlockStateProperties.FACING);
            Direction next = start;
            for (int i = 0; i < Direction.values().length - 1; i++) {
                next = Direction.from3DDataValue((next.get3DDataValue() + 1) % Direction.values().length);
                BlockState candidate = state.setValue(BlockStateProperties.FACING, next);
                if (candidate.canSurvive(level, pos)) {
                    return candidate;
                }
            }
            return null;
        }

        BlockState rotated;
        if (state.hasProperty(BlockStateProperties.AXIS)) {
            Direction.Axis axis = state.getValue(BlockStateProperties.AXIS);
            rotated = state.setValue(BlockStateProperties.AXIS, Direction.Axis.values()[(axis.ordinal() + 1) % 3]);
        } else {
            rotated = state.rotate(level, pos, Rotation.CLOCKWISE_90);
        }
        return rotated.equals(state) || !rotated.canSurvive(level, pos) ? null : rotated;
    }
}
