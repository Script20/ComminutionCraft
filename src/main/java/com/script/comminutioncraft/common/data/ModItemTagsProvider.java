package com.script.comminutioncraft.common.data;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.block.StoneSet;
import com.script.comminutioncraft.common.registry.ModBlocks;
import com.script.comminutioncraft.common.registry.ModItems;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.ItemTags;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ItemTagsProvider;

public class ModItemTagsProvider extends ItemTagsProvider {
    public ModItemTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, ComminutionCraft.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        for (ProcessedMetal metal : ProcessedMetal.ALL) {
            tag(metal.dustTag()).add(metal.granulated().get());
            tag(ProcessedMetal.commonItemTag("dusts")).addTag(metal.dustTag());
        }
        // Lets other mods' wrench-aware blocks recognise ours.
        tag(Tags.Items.TOOLS_WRENCH).add(ModItems.WRENCH.get());
        // Other mods' recipes that take "a basic circuit" accept ours.
        tag(ProcessedMetal.commonItemTag("circuits/basic")).add(ModItems.ELECTRONIC_CIRCUIT.get());
        tag(ProcessedMetal.commonItemTag("circuits/advanced")).add(ModItems.PLASMA_CIRCUIT.get());
        tag(ProcessedMetal.commonItemTag("circuits/elite")).add(ModItems.FUSION_CIRCUIT.get());

        for (StoneSet set : ModBlocks.STONE_SETS) {
            set.allStairs().forEach(block -> tag(ItemTags.STAIRS).add(block.get().asItem()));
            set.allSlabs().forEach(block -> tag(ItemTags.SLABS).add(block.get().asItem()));
            set.allWalls().forEach(block -> tag(ItemTags.WALLS).add(block.get().asItem()));
            tag(ItemTags.STONE_BUTTONS).add(set.button.get().asItem());
            tag(Tags.Items.STONES).add(set.stone.get().asItem());
            tag(Tags.Items.COBBLESTONES).add(set.cobbled.get().asItem(), set.mossyCobbled.get().asItem());
            // Cobbled forms work like cobblestone / cobbled deepslate for furnaces and stone tools.
            tag(ItemTags.STONE_CRAFTING_MATERIALS).add(set.cobbled.get().asItem());
            tag(ItemTags.STONE_TOOL_MATERIALS).add(set.cobbled.get().asItem());
        }
    }
}
