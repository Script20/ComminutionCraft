package com.script.comminutioncraft.common.registry;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.entity.BlackHoleEntity;
import com.script.comminutioncraft.common.entity.FennecFox;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.vehicle.boat.Boat;
import net.minecraft.world.entity.vehicle.boat.ChestBoat;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(Registries.ENTITY_TYPE, ComminutionCraft.MODID);

    /** A harmless black hole to look at: /summon comminutioncraft:black_hole. */
    public static final DeferredHolder<EntityType<?>, EntityType<BlackHoleEntity>> BLACK_HOLE = ENTITY_TYPES.register("black_hole",
            id -> EntityType.Builder.<BlackHoleEntity>of(BlackHoleEntity::new, MobCategory.MISC)
                    .sized(0.9F, 0.9F)
                    .clientTrackingRange(10)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));

    /** Purpleheart boats: vanilla boats with their own item and texture (same size and tracking as vanilla's). */
    public static final DeferredHolder<EntityType<?>, EntityType<Boat>> PURPLEHEART_BOAT = ENTITY_TYPES.register("purpleheart_boat",
            id -> EntityType.Builder.<Boat>of((type, level) -> new Boat(type, level, () -> ModItems.PURPLEHEART_BOAT.get()), MobCategory.MISC)
                    .noLootTable()
                    .sized(1.375F, 0.5625F)
                    .eyeHeight(0.5625F)
                    .clientTrackingRange(10)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));

    public static final DeferredHolder<EntityType<?>, EntityType<ChestBoat>> PURPLEHEART_CHEST_BOAT = ENTITY_TYPES.register("purpleheart_chest_boat",
            id -> EntityType.Builder.<ChestBoat>of((type, level) -> new ChestBoat(type, level, () -> ModItems.PURPLEHEART_CHEST_BOAT.get()), MobCategory.MISC)
                    .noLootTable()
                    .sized(1.375F, 0.5625F)
                    .eyeHeight(0.5625F)
                    .clientTrackingRange(10)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));

    /** Fennec Fox: a small tamable desert fox (0.5 x 0.6 blocks; a vanilla fox is 0.6 x 0.7). */
    public static final DeferredHolder<EntityType<?>, EntityType<FennecFox>> FENNEC_FOX = ENTITY_TYPES.register("fennec_fox",
            id -> EntityType.Builder.<FennecFox>of(FennecFox::new, MobCategory.CREATURE)
                    .noLootTable()
                    .sized(0.5F, 0.6F)
                    .eyeHeight(0.5F)
                    .clientTrackingRange(8)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, id)));

    private ModEntities() {}
}
