package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.energy.EnergyHandlerUtil;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

/**
 * Creative Energy Battery (creative only, no recipe): endless FE. Every tick it offers up to {@link #PUSH_PER_FACE} FE
 * to the block on each face, and anything can pull as much as it likes from it. It takes nothing in.
 */
public class CreativeEnergyBatteryBlockEntity extends BlockEntity implements TickingMachine {
    public static final com.script.comminutioncraft.common.config.ConfigInt PUSH_PER_FACE = com.script.comminutioncraft.common.config.MachineConfig.CREATIVE_ENERGY_BATTERY_PUSH_PER_FACE;

    /** Always full, never runs down, accepts nothing. */
    public static final EnergyHandler ENDLESS = new EnergyHandler() {
        @Override
        public long getAmountAsLong() {
            return Integer.MAX_VALUE;
        }

        @Override
        public long getCapacityAsLong() {
            return Integer.MAX_VALUE;
        }

        @Override
        public int insert(int amount, TransactionContext transaction) {
            TransferPreconditions.checkNonNegative(amount);
            return 0;
        }

        @Override
        public int extract(int amount, TransactionContext transaction) {
            TransferPreconditions.checkNonNegative(amount);
            return amount;
        }
    };

    public CreativeEnergyBatteryBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CREATIVE_ENERGY_BATTERY.get(), pos, state);
    }

    @Override
    public void serverTick(ServerLevel level, BlockPos pos, BlockState state) {
        for (Direction direction : Direction.values()) {
            EnergyHandler target = level.getCapability(Capabilities.Energy.BLOCK, pos.relative(direction), direction.getOpposite());
            if (target != null && target != ENDLESS) {
                EnergyHandlerUtil.move(ENDLESS, target, PUSH_PER_FACE.get(), null);
            }
        }
    }
}
