package com.script.comminutioncraft.common.cable;

import com.script.comminutioncraft.common.config.ConfigInt;
import com.script.comminutioncraft.common.config.MachineConfig;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Function;
import java.util.function.IntSupplier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.capabilities.Capabilities;

/**
 * Energy cable: connects to anything with FE on that side, except other kinds of conduit. Power moves through
 * {@link CableEnergyHandler}, at most {@link #getTransferRate()} FE per tick per network.
 */
public class EnergyCableBlock extends ConduitBlock {
    /** Tier 2 Energy Cable. */
    public static final ConfigInt ENERGY_CABLE_RATE = MachineConfig.CABLES_ENERGY_CABLE;
    /** Tier 4 Fusion Cable. */
    public static final ConfigInt FUSION_CABLE_RATE = MachineConfig.CABLES_FUSION_CABLE;
    public static final ConfigInt ANTIMATTER_CABLE_RATE = MachineConfig.CABLES_ANTIMATTER_CABLE;
    public static final ConfigInt SINGULARITY_CABLE_RATE = MachineConfig.CABLES_SINGULARITY_CABLE;

    /** True when it runs straight through (two opposite connections only): no node, just the cable. */
    public static final BooleanProperty STRAIGHT = BooleanProperty.create("straight");

    private static final VoxelShape NODE = box(4, 4, 4, 12, 12, 12);
    private static final Map<Direction, VoxelShape> ARMS = new EnumMap<>(Map.of(
            Direction.NORTH, box(5, 5, 0, 11, 11, 8),
            Direction.SOUTH, box(5, 5, 8, 11, 11, 16),
            Direction.WEST, box(0, 5, 5, 8, 11, 11),
            Direction.EAST, box(8, 5, 5, 16, 11, 11),
            Direction.DOWN, box(5, 0, 5, 11, 8, 11),
            Direction.UP, box(5, 8, 5, 11, 16, 11)));
    private static final Map<Direction, VoxelShape> PLATES = new EnumMap<>(Map.of(
            Direction.NORTH, box(4, 4, 0, 12, 12, 1),
            Direction.SOUTH, box(4, 4, 15, 12, 12, 16),
            Direction.WEST, box(0, 4, 4, 1, 12, 12),
            Direction.EAST, box(15, 4, 4, 16, 12, 12),
            Direction.DOWN, box(4, 0, 4, 12, 1, 12),
            Direction.UP, box(4, 15, 4, 12, 16, 12)));

    private final IntSupplier transferRate;
    private final Function<BlockState, VoxelShape> cableShapes;

    public EnergyCableBlock(Properties properties, IntSupplier transferRate) {
        super(properties);
        this.transferRate = transferRate;
        registerDefaultState(defaultBlockState().setValue(STRAIGHT, false));
        cableShapes = getShapeForEachState(EnergyCableBlock::shapeOf);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(STRAIGHT);
    }

    /** Straight = connected on exactly two opposite sides (to cables or machines) and nowhere else. */
    @Override
    protected BlockState withShape(BlockState state) {
        Direction first = null;
        int count = 0;
        for (Direction direction : Direction.values()) {
            if (state.getValue(CONNECTIONS.get(direction)).isConnected()) {
                count++;
                if (first == null) {
                    first = direction;
                }
            }
        }
        boolean straight = count == 2 && state.getValue(CONNECTIONS.get(first.getOpposite())).isConnected();
        return state.setValue(STRAIGHT, straight);
    }

    private static VoxelShape shapeOf(BlockState state) {
        VoxelShape shape = state.getValue(STRAIGHT) ? Shapes.empty() : NODE;
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
        return cableShapes.apply(state);
    }

    public int getTransferRate() {
        return transferRate.getAsInt();
    }

    @Override
    protected boolean connectsTo(Level level, BlockPos neighbourPos, Direction side) {
        return !(level.getBlockState(neighbourPos).getBlock() instanceof ConduitBlock)
                && level.getCapability(Capabilities.Energy.BLOCK, neighbourPos, side) != null;
    }
}
