package com.script.comminutioncraft.common.registry;

import com.mojang.serialization.Codec;
import com.script.comminutioncraft.ComminutionCraft;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

/**
 * {@code comminutioncraft:matter_mass} - how much mass one of an item gives the Event Horizon Reactor's black hole
 * ({@code data/<ns>/data_maps/item/matter_mass.json}, datapack-editable). Items without an entry weigh 1;
 * 0 = swallowed for nothing.
 */
public final class ModDataMaps {
    public static final DataMapType<Item, Integer> MATTER_MASS = DataMapType.builder(
                    ComminutionCraft.id("matter_mass"), Registries.ITEM, Codec.intRange(0, Integer.MAX_VALUE))
            .synced(Codec.intRange(0, Integer.MAX_VALUE), false)
            .build();

    public static void register(RegisterDataMapTypesEvent event) {
        event.register(MATTER_MASS);
    }

    /** Mass of one of this stack's item (1 if the data map doesn't say). */
    public static int massOf(ItemStack stack) {
        Integer mass = BuiltInRegistries.ITEM.wrapAsHolder(stack.getItem()).getData(MATTER_MASS);
        return mass == null ? 1 : mass;
    }

    private ModDataMaps() {}
}
