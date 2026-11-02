package com.script.comminutioncraft.common.machine;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jspecify.annotations.Nullable;

/**
 * Furnace-style machine block shared by every tier: faces the player when placed, LIT while working,
 * opens its block entity's GUI on right-click, and ticks its {@link TickingMachine} block entity.
 */
public abstract class MachineBlock extends Block implements EntityBlock {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;

    protected MachineBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, false));
    }

    protected abstract BlockEntityType<? extends BlockEntity> getBlockEntityType();

    /** Fuel-burning machines crackle and smoke at the front while lit. */
    protected boolean burnsFuel() {
        return false;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, LIT);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof MenuProvider machine) {
            player.openMenu(machine);
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return getBlockEntityType().create(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != getBlockEntityType()) {
            return null;
        }
        return (tickLevel, pos, tickState, blockEntity) -> {
            if (blockEntity instanceof TickingMachine machine) {
                machine.serverTick((ServerLevel) tickLevel, pos, tickState);
            }
        };
    }

    /** Smoke and flame at the front while burning, plus the furnace crackle. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (!burnsFuel() || !state.getValue(LIT)) {
            return;
        }
        double x = pos.getX() + 0.5;
        double y = pos.getY();
        double z = pos.getZ() + 0.5;
        if (random.nextDouble() < 0.1) {
            level.playLocalSound(x, y, z, SoundEvents.FURNACE_FIRE_CRACKLE, SoundSource.BLOCKS, 1.0F, 1.0F, false);
        }

        Direction facing = state.getValue(FACING);
        Direction.Axis axis = facing.getAxis();
        double along = random.nextDouble() * 0.6 - 0.3;
        double dx = axis == Direction.Axis.X ? facing.getStepX() * 0.52 : along;
        double dz = axis == Direction.Axis.Z ? facing.getStepZ() * 0.52 : along;
        double py = y + getFireboxBottom() + random.nextDouble() * 4.0 / 16.0;
        level.addParticle(ParticleTypes.SMOKE, x + dx, py, z + dz, 0.0, 0.0, 0.0);
        level.addParticle(ParticleTypes.FLAME, x + dx, py, z + dz, 0.0, 0.0, 0.0);
    }

    /** Height (in blocks) of the bottom of the firebox on the front texture. Tier 1: the lower third. */
    protected double getFireboxBottom() {
        return 1.0 / 16.0;
    }
}
