package com.script.comminutioncraft.common.item;

import com.script.comminutioncraft.common.cable.EnergyCableBlock;
import com.script.comminutioncraft.common.machine.control.ControllableMachine;
import com.script.comminutioncraft.common.machine.control.RelativeSide;
import com.script.comminutioncraft.common.machine.control.SideMode;
import com.script.comminutioncraft.common.registry.ModDataComponents;
import com.script.comminutioncraft.common.registry.ModItems;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Three modes, cycled with sneak + right-click:
 * <ul>
 *   <li><b>Rotate</b> - right-click a block to turn it (machines and anything else with a facing/axis).</li>
 *   <li><b>Cable</b> - right-click an energy cable's arm to cut that connection; click again to restore it.</li>
 *   <li><b>Configure</b> - right-click a machine face to cycle it between Input, Output and None.</li>
 * </ul>
 */
public class WrenchItem extends Item {
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

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!player.isSecondaryUseActive()) {
            return InteractionResult.PASS;
        }
        switchMode(level, player, player.getItemInHand(hand));
        return InteractionResult.SUCCESS;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        if (player != null && player.isSecondaryUseActive()) {
            switchMode(context.getLevel(), player, stack);
            return InteractionResult.SUCCESS;
        }
        if (player != null && !player.mayUseItemAt(context.getClickedPos(), context.getClickedFace(), stack)) {
            return InteractionResult.FAIL;
        }
        return switch (getMode(stack)) {
            case ROTATE -> rotate(context);
            case CABLE -> useOnCable(context);
            case CONFIGURE -> configure(context);
        };
    }

    private static void switchMode(Level level, Player player, ItemStack stack) {
        if (level.isClientSide()) {
            return;
        }
        stack.set(ModDataComponents.WRENCH_MODE.get(), getMode(stack).next());
        player.sendOverlayMessage(getMode(stack).displayName());
        level.playSound(null, player.blockPosition(), SoundEvents.ITEM_FRAME_ROTATE_ITEM, SoundSource.PLAYERS, 0.6F, 1.4F);
    }

    // ---- Cable mode ----

    private static InteractionResult useOnCable(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (!(level.getBlockState(pos).getBlock() instanceof EnergyCableBlock)) {
            return InteractionResult.PASS;
        }
        // The arm (or core face) that was clicked: whichever way the hit point sticks out from the centre most.
        Vec3 offset = context.getClickLocation().subtract(Vec3.atCenterOf(pos));
        Direction side = Direction.getApproximateNearest(offset.x, offset.y, offset.z);
        if (!level.isClientSide()) {
            boolean restored = EnergyCableBlock.toggleSide(level, pos, side);
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

    // ---- Tooltip ----

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> builder, TooltipFlag flag) {
        WrenchMode mode = getMode(stack);
        builder.accept(mode.displayName().copy().withStyle(ChatFormatting.AQUA));
        builder.accept(mode.help().copy().withStyle(ChatFormatting.GRAY));
        builder.accept(Component.translatable("item.comminutioncraft.wrench.help.switch").withStyle(ChatFormatting.DARK_GRAY));
    }
}
