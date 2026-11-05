package com.script.comminutioncraft.common.storage;

import com.script.comminutioncraft.common.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** A vanilla chest block entity with its own type and name. */
public class SoarynChestBlockEntity extends ChestBlockEntity {
    public SoarynChestBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.SOARYN_CHEST.get(), pos, state);
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("container.comminutioncraft.soaryn_chest");
    }
}
