package com.script.comminutioncraft.data;

import com.script.comminutioncraft.registry.ModItems;
import java.util.List;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * Every metal the mod processes. Adding a metal here generates its granulating recipe,
 * smelting + blasting recipes, c:dusts tag, item model and translation.
 * (The granulated item itself still needs registering in {@link ModItems} and a texture.)
 */
public record ProcessedMetal(String name, DeferredItem<Item> granulated, Item ingot, float experience) {
    public static final List<ProcessedMetal> ALL = List.of(
            new ProcessedMetal("iron", ModItems.GRANULATED_IRON, Items.IRON_INGOT, 0.7F),
            new ProcessedMetal("copper", ModItems.GRANULATED_COPPER, Items.COPPER_INGOT, 0.7F),
            new ProcessedMetal("gold", ModItems.GRANULATED_GOLD, Items.GOLD_INGOT, 1.0F)
    );

    /** c:raw_materials/<metal> — what the granulator accepts. */
    public TagKey<Item> rawTag() {
        return commonItemTag("raw_materials/" + name);
    }

    /** c:dusts/<metal> — how other mods see our granulated ore. */
    public TagKey<Item> dustTag() {
        return commonItemTag("dusts/" + name);
    }

    public String displayName() {
        return "Granulated " + Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    public static TagKey<Item> commonItemTag(String path) {
        return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", path));
    }
}
