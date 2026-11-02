package com.script.comminutioncraft.common.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;

/**
 * A recipe result given as an item tag (e.g. {@code c:ingots/tin}) instead of a fixed item, for metals that come
 * from whichever other mod is installed. Resolves to the tag's first item each time, so it follows datapack /
 * unification changes. Recipes using it are only loaded when the tag has items (see ModRecipeProvider).
 */
public record TagResult(TagKey<Item> tag, int count) {
    public static final Codec<TagResult> CODEC = RecordCodecBuilder.create(i -> i.group(
            TagKey.codec(Registries.ITEM).fieldOf("tag").forGetter(TagResult::tag),
            Codec.intRange(1, 99).optionalFieldOf("count", 1).forGetter(TagResult::count)
    ).apply(i, TagResult::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, TagResult> STREAM_CODEC = StreamCodec.composite(
            TagKey.streamCodec(Registries.ITEM), TagResult::tag,
            ByteBufCodecs.VAR_INT, TagResult::count,
            TagResult::new);

    /** What vanilla's constructors get; never shown, because the recipes override result(). */
    static final ItemStackTemplate PLACEHOLDER = new ItemStackTemplate(Items.BARRIER.builtInRegistryHolder(), 1);

    public ItemStackTemplate resolve() {
        for (Holder<Item> item : BuiltInRegistries.ITEM.getTagOrEmpty(tag)) {
            return new ItemStackTemplate(item, count);
        }
        return PLACEHOLDER;
    }
}
