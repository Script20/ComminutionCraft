package com.script.comminutioncraft.data;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.block.StoneSet;
import com.script.comminutioncraft.registry.ModBlocks;
import com.script.comminutioncraft.registry.ModItems;
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
