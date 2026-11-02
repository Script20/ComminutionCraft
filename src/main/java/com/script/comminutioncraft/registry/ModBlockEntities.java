package com.script.comminutioncraft.registry;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.machine.CoalGranulatorBlockEntity;
import com.script.comminutioncraft.machine.CoalSmelterBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, ComminutionCraft.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CoalGranulatorBlockEntity>> COAL_GRANULATOR =
            BLOCK_ENTITY_TYPES.register("coal_granulator",
                    () -> new BlockEntityType<>(CoalGranulatorBlockEntity::new, ModBlocks.COAL_GRANULATOR.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<CoalSmelterBlockEntity>> COAL_SMELTER =
            BLOCK_ENTITY_TYPES.register("coal_smelter",
                    () -> new BlockEntityType<>(CoalSmelterBlockEntity::new, ModBlocks.COAL_SMELTER.get()));

    private ModBlockEntities() {}
}
