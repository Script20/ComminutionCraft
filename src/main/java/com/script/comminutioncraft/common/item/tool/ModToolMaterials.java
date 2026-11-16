package com.script.comminutioncraft.common.item.tool;

import com.script.comminutioncraft.ComminutionCraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.block.Block;

/** The two tool sets (Reactor Plating, Singularity) and the tags the tools mine with. */
public final class ModToolMaterials {
    /** Repairs Reactor Plating tools (an anvil with Reactor Plating). */
    public static final TagKey<Item> PLATING_REPAIR = TagKey.create(Registries.ITEM, ComminutionCraft.id("plating_tool_materials"));
    /** Repairs Singularity tools (an anvil with a Singularity Shard). */
    public static final TagKey<Item> SINGULARITY_REPAIR = TagKey.create(Registries.ITEM, ComminutionCraft.id("singularity_tool_materials"));

    /** Drills: whatever a pickaxe or a shovel mines. */
    public static final TagKey<Block> MINEABLE_DRILL = TagKey.create(Registries.BLOCK, ComminutionCraft.id("mineable/drill"));
    /** Chainsaws: whatever an axe mines, and leaves. */
    public static final TagKey<Block> MINEABLE_CHAINSAW = TagKey.create(Registries.BLOCK, ComminutionCraft.id("mineable/chainsaw"));
    /** Paxels: pickaxe, axe, shovel and hoe in one. */
    public static final TagKey<Block> MINEABLE_PAXEL = TagKey.create(Registries.BLOCK, ComminutionCraft.id("mineable/paxel"));

    /** Between diamond (1,561 uses, speed 8) and netherite (2,031, speed 9). */
    public static final ToolMaterial PLATING = new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 1_800, 8.5F, 3.5F, 12, PLATING_REPAIR);
    /** Beyond netherite. */
    public static final ToolMaterial SINGULARITY = new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 4_000, 12.0F, 5.0F, 20, SINGULARITY_REPAIR);

    private ModToolMaterials() {}
}
