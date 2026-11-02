package com.script.comminutioncraft.common.registry;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.machine.CoalGranulatorBlockEntity;
import com.script.comminutioncraft.common.machine.CoalSmelterBlockEntity;
import com.script.comminutioncraft.common.machine.ElectricGeneratorBlockEntity;
import com.script.comminutioncraft.common.machine.ElectricGranulatorBlockEntity;
import com.script.comminutioncraft.common.machine.ElectricSmelterBlockEntity;
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

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ElectricGranulatorBlockEntity>> ELECTRIC_GRANULATOR =
            BLOCK_ENTITY_TYPES.register("electric_granulator",
                    () -> new BlockEntityType<>(ElectricGranulatorBlockEntity::new, ModBlocks.ELECTRIC_GRANULATOR.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ElectricSmelterBlockEntity>> ELECTRIC_SMELTER =
            BLOCK_ENTITY_TYPES.register("electric_smelter",
                    () -> new BlockEntityType<>(ElectricSmelterBlockEntity::new, ModBlocks.ELECTRIC_SMELTER.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ElectricGeneratorBlockEntity>> ELECTRIC_GENERATOR =
            BLOCK_ENTITY_TYPES.register("electric_generator",
                    () -> new BlockEntityType<>(ElectricGeneratorBlockEntity::new, ModBlocks.ELECTRIC_GENERATOR.get()));

    private ModBlockEntities() {}
}
