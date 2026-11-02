package com.script.comminutioncraft.common.cable;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;

/**
 * Energy cable. Each side is {@link CableConnection#NONE}, joined to another cable, joined to an FE block,
 * or cut with the wrench. Connections update automatically as neighbours change; cut sides stay cut.
 * Power moves through {@link CableEnergyHandler} and {@link CableNetworks}.
 */
public class EnergyCableBlock extends Block {
    public static final Map<Direction, EnumProperty<CableConnection>> CONNECTIONS = new EnumMap<>(Direction.class);

    static {
        for (Direction direction : Direction.values()) {
            CONNECTIONS.put(direction, EnumProperty.create(direction.getSerializedName(), CableConnection.class));
        }
    }

    private static final VoxelShape CORE = Block.box(5, 5, 5, 11, 11, 11);
    private static final Map<Direction, VoxelShape> ARMS = new EnumMap<>(Map.of(
            Direction.NORTH, Block.box(6, 6, 0, 10, 10, 5),
            Direction.SOUTH, Block.box(6, 6, 11, 10, 10, 16),
            Direction.WEST, Block.box(0, 6, 6, 5, 10, 10),
            Direction.EAST, Block.box(11, 6, 6, 16, 10, 10),
            Direction.DOWN, Block.box(6, 0, 6, 10, 5, 10),
            Direction.UP, Block.box(6, 11, 6, 10, 16, 10)));
    /** Connector plate where an arm meets a machine. */
    private static final Map<Direction, VoxelShape> PLATES = new EnumMap<>(Map.of(
            Direction.NORTH, Block.box(4, 4, 0, 12, 12, 1),
            Direction.SOUTH, Block.box(4, 4, 15, 12, 12, 16),
            Direction.WEST, Block.box(0, 4, 4, 1, 12, 12),
            Direction.EAST, Block.box(15, 4, 4, 16, 12, 12),
            Direction.DOWN, Block.box(4, 0, 4, 12, 1, 12),
            Direction.UP, Block.box(4, 15, 4, 12, 16, 12)));

    private final Function<BlockState, VoxelShape> shapes;

    public EnergyCableBlock(Properties properties) {
        super(properties);
        BlockState state = stateDefinition.any();
        for (EnumProperty<CableConnection> property : CONNECTIONS.values()) {
            state = state.setValue(property, CableConnection.NONE);
        }
        registerDefaultState(state);
        shapes = getShapeForEachState(EnergyCableBlock::calculateShape);
    }

    private static VoxelShape calculateShape(BlockState state) {
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
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        CONNECTIONS.values().forEach(builder::add);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapes.apply(state);
    }

    // ---- Connections ----

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockState state = defaultBlockState();
        for (Direction direction : Direction.values()) {
            state = state.setValue(CONNECTIONS.get(direction),
                    connectionTo(context.getLevel(), context.getClickedPos(), direction, CableConnection.NONE));
        }
        return state;
    }

    @Override
    protected BlockState updateShape(BlockState state, LevelReader level, ScheduledTickAccess ticks, BlockPos pos,
                                     Direction directionToNeighbour, BlockPos neighbourPos, BlockState neighbourState,
                                     RandomSource random) {
        EnumProperty<CableConnection> property = CONNECTIONS.get(directionToNeighbour);
        CableConnection updated = connectionTo(level, pos, directionToNeighbour, state.getValue(property));
        if (updated != state.getValue(property)) {
            CableNetworks.invalidate(level);
        }
        return state.setValue(property, updated);
    }

    /**
     * What this cable's arm towards {@code direction} should be. Cut sides stay cut; a neighbouring cable
     * connects unless its side facing us is cut; any other block connects if it exposes FE on that side.
     */
    public static CableConnection connectionTo(LevelReader level, BlockPos pos, Direction direction, CableConnection current) {
        if (current == CableConnection.DISABLED) {
            return CableConnection.DISABLED;
        }
        BlockPos neighbourPos = pos.relative(direction);
        BlockState neighbour = level.getBlockState(neighbourPos);
        if (neighbour.getBlock() instanceof EnergyCableBlock) {
            return neighbour.getValue(CONNECTIONS.get(direction.getOpposite())) == CableConnection.DISABLED
                    ? CableConnection.NONE : CableConnection.CABLE;
        }
        if (!(level instanceof Level realLevel)) {
            // World generation etc.: block entities aren't queryable, so keep what we had.
            return current == CableConnection.CABLE ? CableConnection.NONE : current;
        }
        return realLevel.getCapability(Capabilities.Energy.BLOCK, neighbourPos, direction.getOpposite()) != null
                ? CableConnection.MACHINE : CableConnection.NONE;
    }

    /**
     * Wrench action on one side: cuts a live (or empty) side, or restores a cut one. Clicking the far end of a
     * cut between two cables restores it too. Returns true if the side is connected/connectable again.
     */
    public static boolean toggleSide(Level level, BlockPos pos, Direction direction) {
        BlockState state = level.getBlockState(pos);
        EnumProperty<CableConnection> property = CONNECTIONS.get(direction);
        BlockPos neighbourPos = pos.relative(direction);
        BlockState neighbour = level.getBlockState(neighbourPos);
        EnumProperty<CableConnection> neighbourProperty = CONNECTIONS.get(direction.getOpposite());
        boolean neighbourCut = neighbour.getBlock() instanceof EnergyCableBlock
                && neighbour.getValue(neighbourProperty) == CableConnection.DISABLED;

        boolean restored;
        if (state.getValue(property) == CableConnection.DISABLED || neighbourCut) {
            if (neighbourCut) {
                level.setBlock(neighbourPos, neighbour.setValue(neighbourProperty, CableConnection.NONE), Block.UPDATE_ALL);
                level.invalidateCapabilities(neighbourPos);
            }
            // Re-read: the neighbour update above may have touched our state.
            state = level.getBlockState(pos);
            state = state.setValue(property, connectionTo(level, pos, direction, CableConnection.NONE));
            restored = true;
        } else {
            state = state.setValue(property, CableConnection.DISABLED);
            restored = false;
        }
        level.setBlock(pos, state, Block.UPDATE_ALL);
        level.invalidateCapabilities(pos);
        CableNetworks.invalidate(level);
        return restored;
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        super.onPlace(state, level, pos, oldState, movedByPiston);
        CableNetworks.invalidate(level);
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        super.affectNeighborsAfterRemoval(state, level, pos, movedByPiston);
        CableNetworks.invalidate(level);
    }
}
