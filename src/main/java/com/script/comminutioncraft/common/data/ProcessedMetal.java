package com.script.comminutioncraft.common.data;

import com.script.comminutioncraft.common.registry.ModItems;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.registries.DeferredItem;
import org.jspecify.annotations.Nullable;

/**
 * Every metal the mod processes. Adding a metal here generates its granulating, washing (raw ore / ore block -> 3
 * washed), washed-ore granulating, smelting + blasting recipes, c:dusts tag, item models and translations.
 * (The granulated and washed items themselves still need registering in {@link ModItems} and textures.)
 * <p>
 * Vanilla metals smelt into a fixed ingot. Metals from other mods ({@code ingot == null}) smelt into whatever is in
 * {@code c:ingots/<metal>}, and their recipes and granulated item only show up when another installed mod provides
 * both {@code c:raw_materials/<metal>} and {@code c:ingots/<metal>} (see {@link #isAvailable()}).
 */
public record ProcessedMetal(String name, DeferredItem<Item> granulated, DeferredItem<Item> washed, @Nullable Item ingot,
                             float experience) {
    public static final List<ProcessedMetal> ALL = List.of(
            new ProcessedMetal("iron", ModItems.GRANULATED_IRON, ModItems.WASHED_IRON, Items.IRON_INGOT, 0.7F),
            new ProcessedMetal("copper", ModItems.GRANULATED_COPPER, ModItems.WASHED_COPPER, Items.COPPER_INGOT, 0.7F),
            new ProcessedMetal("gold", ModItems.GRANULATED_GOLD, ModItems.WASHED_GOLD, Items.GOLD_INGOT, 1.0F),
            // From other mods (Mekanism, Thermal, Immersive Engineering, Create, ...)
            new ProcessedMetal("tin", ModItems.GRANULATED_TIN, ModItems.WASHED_TIN, null, 0.7F),
            new ProcessedMetal("lead", ModItems.GRANULATED_LEAD, ModItems.WASHED_LEAD, null, 0.7F),
            new ProcessedMetal("silver", ModItems.GRANULATED_SILVER, ModItems.WASHED_SILVER, null, 1.0F),
            new ProcessedMetal("nickel", ModItems.GRANULATED_NICKEL, ModItems.WASHED_NICKEL, null, 0.7F),
            new ProcessedMetal("zinc", ModItems.GRANULATED_ZINC, ModItems.WASHED_ZINC, null, 0.7F),
            new ProcessedMetal("aluminum", ModItems.GRANULATED_ALUMINUM, ModItems.WASHED_ALUMINUM, null, 0.7F),
            new ProcessedMetal("osmium", ModItems.GRANULATED_OSMIUM, ModItems.WASHED_OSMIUM, null, 0.7F),
            new ProcessedMetal("uranium", ModItems.GRANULATED_URANIUM, ModItems.WASHED_URANIUM, null, 1.0F)
    );

    /** True for metals that only exist when another mod adds them. */
    public boolean fromOtherMods() {
        return ingot == null;
    }

    /**
     * Whether this metal exists in the current game: always for vanilla metals; for other mods' metals, once some mod
     * fills both the raw material and ingot tags. Needs tags to be loaded (true in-game on both sides).
     */
    public boolean isAvailable() {
        return !fromOtherMods() || (hasItems(rawTag()) && hasItems(ingotTag()));
    }

    private static boolean hasItems(TagKey<Item> tag) {
        return BuiltInRegistries.ITEM.getTagOrEmpty(tag).iterator().hasNext();
    }

    /** c:raw_materials/<metal> — what the granulator accepts. */
    public TagKey<Item> rawTag() {
        return commonItemTag("raw_materials/" + name);
    }

    /** c:ores/<metal> — ore blocks, which the Ore Washer also takes. */
    public TagKey<Item> oreTag() {
        return commonItemTag("ores/" + name);
    }

    /** c:ingots/<metal> — what other mods' metals smelt into. */
    public TagKey<Item> ingotTag() {
        return commonItemTag("ingots/" + name);
    }

    /** c:dusts/<metal> — how other mods see our granulated ore. */
    public TagKey<Item> dustTag() {
        return commonItemTag("dusts/" + name);
    }

    public String displayName() {
        return "Granulated " + titleName();
    }

    public String washedDisplayName() {
        return "Washed " + titleName();
    }

    private String titleName() {
        return Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    public static TagKey<Item> commonItemTag(String path) {
        return TagKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath("c", path));
    }
}
