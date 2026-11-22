package com.script.comminutioncraft.common.machine.miner;

import com.script.comminutioncraft.common.machine.MachineBlock;
import com.script.comminutioncraft.common.registry.ModBlockEntities;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;

/** An Ore Miner: its front shows what it's doing ({@link #SCREEN}): idle, scanning, mining or finished. */
public class OreMinerBlock extends MachineBlock {
    public enum Screen implements StringRepresentable {
        IDLE, SCANNING, MINING, FINISHED;

        @Override
        public String getSerializedName() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    public static final EnumProperty<Screen> SCREEN = EnumProperty.create("screen", Screen.class);

    private final OreMinerTier tier;

    public OreMinerBlock(Properties properties, OreMinerTier tier) {
        super(properties);
        this.tier = tier;
        registerDefaultState(defaultBlockState().setValue(SCREEN, Screen.IDLE));
    }

    public OreMinerTier tier() {
        return tier;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(SCREEN);
    }

    /** The GUI needs the tier on the client, so it comes with the open packet. */
    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hit) {
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof OreMinerBlockEntity miner) {
            player.openMenu(miner, buffer -> buffer.writeVarInt(tier.ordinal()));
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected BlockEntityType<OreMinerBlockEntity> getBlockEntityType() {
        return ModBlockEntities.ORE_MINER.get();
    }
}
