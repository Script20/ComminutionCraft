package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.registry.ModBlockEntities;
import net.minecraft.world.level.block.entity.BlockEntityType;

/** Creative Chest: a machine block (it faces the player, for its latch) whose right-click opens the one-slot GUI. */
public class CreativeChestBlock extends MachineBlock {
    public CreativeChestBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected BlockEntityType<CreativeChestBlockEntity> getBlockEntityType() {
        return ModBlockEntities.CREATIVE_CHEST.get();
    }
}
