package com.script.comminutioncraft.common.pipe;

import com.script.comminutioncraft.common.cable.CableConnection;
import com.script.comminutioncraft.common.cable.ConduitBlock;
import com.script.comminutioncraft.common.registry.ModBlockEntities;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;
import org.jspecify.annotations.Nullable;

/**
 * Item Pipe (tier 2): a thicker conduit you can see into (core 8x8x8, arms 6x6, a 10x10 plate on blocks). It joins
 * other Item Pipes and any block with an item handler. Each side touching a block is Insert (the default), Extract
 * or None; None is the conduit's cut side, so the wrench's Cables/Pipes mode cycles Insert -> Extract -> None.
 * Right-click opens the GUI on the clicked side.
 */
public class ItemPipeBlock extends ConduitBlock implements EntityBlock {
    private static final VoxelShape CORE = box(4, 4, 4, 12, 12, 12);
    private static final Map<Direction, VoxelShape> ARMS = new EnumMap<>(Map.of(
            Direction.NORTH, box(5, 5, 0, 11, 11, 4),
            Direction.SOUTH, box(5, 5, 12, 11, 11, 16),
            Direction.WEST, box(0, 5, 5, 4, 11, 11),
            Direction.EAST, box(12, 5, 5, 16, 11, 11),
            Direction.DOWN, box(5, 0, 5, 11, 4, 11),
            Direction.UP, box(5, 12, 5, 11, 16, 11)));
    private static final Map<Direction, VoxelShape> PLATES = new EnumMap<>(Map.of(
            Direction.NORTH, box(3, 3, 0, 13, 13, 1),
            Direction.SOUTH, box(3, 3, 15, 13, 13, 16),
            Direction.WEST, box(0, 3, 3, 1, 13, 13),
            Direction.EAST, box(15, 3, 3, 16, 13, 13),
            Direction.DOWN, box(3, 0, 3, 13, 1, 13),
            Direction.UP, box(3, 15, 3, 13, 16, 13)));

    private final Function<BlockState, VoxelShape> pipeShapes;

    public ItemPipeBlock(Properties properties) {
        super(properties);
        pipeShapes = getShapeForEachState(ItemPipeBlock::shapeOf);
    }

    private static VoxelShape shapeOf(BlockState state) {
        VoxelShape shape = CORE;
        for (Direction direction : Direction.values()) {
            CableConnection connection = state.getValue(CONNECTIONS.get(direction));
            if (connection.isConnected()) {
                shape = Shapes.or(shape, ARMS.get(direction));
            }
            if (connection == CableConnection.MACHINE) {
                shape = Shapes.or(shape, PLATES.get(direction));
            }
        }
        return shape;
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return pipeShapes.apply(state);
    }

    @Override
    protected boolean connectsTo(Level level, BlockPos neighbourPos, Direction side) {
        return hasInventory(level, neighbourPos, side);
    }

    /** Whether the block at {@code neighbourPos} has an item handler on {@code side} (and isn't an Item Pipe). */
    static boolean hasInventory(Level level, BlockPos neighbourPos, Direction side) {
        return !(level.getBlockState(neighbourPos).getBlock() instanceof ItemPipeBlock)
                && level.getCapability(Capabilities.Item.BLOCK, neighbourPos, side) != null;
    }

    /** The sides of the pipe at {@code pos} that touch a block with an inventory, as a bit mask (bit = ordinal). */
    public static int inventorySides(Level level, BlockPos pos) {
        int mask = 0;
        for (Direction direction : Direction.values()) {
            if (hasInventory(level, pos.relative(direction), direction.getOpposite())) {
                mask |= 1 << direction.ordinal();
            }
        }
        return mask;
    }

    /** Insert, Extract or None for a side touching a block. */
    public enum Mode {
        INSERT, EXTRACT, NONE;

        public Component displayName() {
            return Component.translatable("gui.comminutioncraft.item_pipe.mode." + name().toLowerCase(Locale.ROOT));
        }
    }

    public static Mode mode(Level level, BlockPos pos, Direction side) {
        BlockState state = level.getBlockState(pos);
        if (!(state.getBlock() instanceof ItemPipeBlock) || !(level.getBlockEntity(pos) instanceof ItemPipeBlockEntity pipe)) {
            return Mode.NONE;
        }
        CableConnection connection = state.getValue(CONNECTIONS.get(side));
        if (connection != CableConnection.MACHINE) {
            return Mode.NONE;
        }
        return pipe.side(side).isExtract() ? Mode.EXTRACT : Mode.INSERT;
    }

    /**
     * Insert -> Extract -> None -> Insert on a side touching a block. Returns the new mode, or null if that side
     * doesn't touch a block (then it's a pipe-to-pipe arm, which the wrench cuts / restores as usual).
     */
    public static @Nullable Mode cycleMode(Level level, BlockPos pos, Direction side) {
        if (!(level.getBlockEntity(pos) instanceof ItemPipeBlockEntity pipe)
                || !hasInventory(level, pos.relative(side), side.getOpposite())) {
            return null;
        }
        Mode next = switch (mode(level, pos, side)) {
            case INSERT -> Mode.EXTRACT;
            case EXTRACT -> Mode.NONE;
            case NONE -> Mode.INSERT;
        };
        setMode(level, pos, side, next);
        return next;
    }

    /** Sets a side touching a block to Insert, Extract or None (None cuts the side). */
    public static void setMode(Level level, BlockPos pos, Direction side, Mode mode) {
        if (!(level.getBlockEntity(pos) instanceof ItemPipeBlockEntity pipe)) {
            return;
        }
        BlockState state = level.getBlockState(pos);
        CableConnection connection = state.getValue(CONNECTIONS.get(side));
        if ((mode == Mode.NONE) != (connection == CableConnection.DISABLED)) {
            toggleSide(level, pos, side);
        } else if (mode != Mode.NONE && connection != CableConnection.MACHINE) {
            // not joined yet (e.g. placed without its neighbours updating it): join it now
            level.setBlock(pos, state.setValue(CONNECTIONS.get(side),
                    ((ConduitBlock) state.getBlock()).connectionTo(level, pos, side, connection)), UPDATE_ALL);
        }
        PipeSide settings = pipe.side(side);
        settings.extract = mode == Mode.EXTRACT;
        settings.cooldown = 0;
        pipe.settingsChanged();
    }

    // ---- GUI ----

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof ItemPipeBlockEntity pipe) {
            int sides = inventorySides(level, pos);
            if (sides == 0) {
                return InteractionResult.PASS;
            }
            Vec3 offset = hit.getLocation().subtract(Vec3.atCenterOf(pos));
            Direction clicked = Direction.getApproximateNearest(offset.x, offset.y, offset.z);
            Direction first = (sides & (1 << clicked.ordinal())) != 0 ? clicked : firstSide(sides);
            serverPlayer.openMenu(new SimpleMenuProvider((id, inventory, p) -> new ItemPipeMenu(id, inventory, pipe, first),
                    Component.translatable("block.comminutioncraft.item_pipe")), buffer -> {
                buffer.writeBlockPos(pos);
                buffer.writeEnum(first);
            });
        }
        return level.isClientSide() || inventorySides(level, pos) != 0 ? InteractionResult.SUCCESS : InteractionResult.PASS;
    }

    static Direction firstSide(int sides) {
        for (Direction direction : Direction.values()) {
            if ((sides & (1 << direction.ordinal())) != 0) {
                return direction;
            }
        }
        return Direction.NORTH;
    }

    // ---- Block entity ----

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ItemPipeBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (type != ModBlockEntities.ITEM_PIPE.get()) {
            return null;
        }
        return level.isClientSide()
                ? (tickLevel, pos, tickState, entity) -> ((ItemPipeBlockEntity) entity).clientTick()
                : (tickLevel, pos, tickState, entity) -> ((ItemPipeBlockEntity) entity).serverTick((ServerLevel) tickLevel, pos, tickState);
    }
}
