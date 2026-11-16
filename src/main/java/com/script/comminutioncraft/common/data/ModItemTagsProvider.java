package com.script.comminutioncraft.common.data;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.block.StoneSet;
import com.script.comminutioncraft.common.item.tool.ModToolMaterials;
import com.script.comminutioncraft.common.registry.ModBlocks;
import com.script.comminutioncraft.common.registry.ModItems;
import java.util.List;
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
        tag(ProcessedMetal.commonItemTag("circuits/ultimate")).add(ModItems.ANTIMATTER_CIRCUIT.get());
        tag(ProcessedMetal.commonItemTag("circuits/singularity")).add(ModItems.SINGULARITY_CIRCUIT.get());

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

        // Purpleheart (as the block tags; furnace fuel comes from these vanilla tags too)
        var wood = ModBlocks.PURPLEHEART;
        var logs = WoodFamily.logsItemTag(wood);
        wood.logs().forEach(block -> tag(logs).add(block.get().asItem()));
        tag(ItemTags.LOGS_THAT_BURN).addTag(logs);
        tag(Tags.Items.STRIPPED_LOGS).add(wood.strippedLog.get().asItem());
        tag(Tags.Items.STRIPPED_WOODS).add(wood.strippedWood.get().asItem());
        tag(ItemTags.PLANKS).add(wood.planks.get().asItem());
        tag(ItemTags.WOODEN_STAIRS).add(wood.stairs.get().asItem());
        tag(ItemTags.WOODEN_SLABS).add(wood.slab.get().asItem());
        tag(ItemTags.WOODEN_FENCES).add(wood.fence.get().asItem());
        tag(Tags.Items.FENCES_WOODEN).add(wood.fence.get().asItem());
        tag(ItemTags.FENCE_GATES).add(wood.fenceGate.get().asItem());
        tag(Tags.Items.FENCE_GATES_WOODEN).add(wood.fenceGate.get().asItem());
        tag(ItemTags.WOODEN_DOORS).add(ModItems.PURPLEHEART_DOOR.get());
        tag(ItemTags.WOODEN_TRAPDOORS).add(wood.trapdoor.get().asItem());
        tag(ItemTags.WOODEN_PRESSURE_PLATES).add(wood.pressurePlate.get().asItem());
        tag(ItemTags.WOODEN_BUTTONS).add(wood.button.get().asItem());
        tag(ItemTags.SIGNS).add(ModItems.PURPLEHEART_SIGN.get());
        tag(ItemTags.HANGING_SIGNS).add(ModItems.PURPLEHEART_HANGING_SIGN.get());
        tag(ItemTags.LEAVES).add(wood.leaves.get().asItem());
        tag(ItemTags.SAPLINGS).add(wood.sapling.get().asItem());
        tag(ItemTags.BOATS).add(ModItems.PURPLEHEART_BOAT.get());
        tag(ItemTags.CHEST_BOATS).add(ModItems.PURPLEHEART_CHEST_BOAT.get());

        // Tools
        tag(ModToolMaterials.PLATING_REPAIR).add(ModItems.REACTOR_PLATING.get());
        tag(ModToolMaterials.SINGULARITY_REPAIR).add(ModItems.SINGULARITY_SHARD.get());
        tag(ItemTags.PICKAXES).add(ModItems.ORE_HAMMER.get());
        for (ModItems.ToolSet set : List.of(ModItems.REACTOR_PLATING_TOOLS, ModItems.SINGULARITY_TOOLS)) {
            tag(ItemTags.PICKAXES).add(set.pickaxe().get(), set.paxel().get());
            tag(ItemTags.AXES).add(set.axe().get(), set.paxel().get());
            tag(ItemTags.SHOVELS).add(set.shovel().get(), set.paxel().get());
            tag(ItemTags.HOES).add(set.hoe().get(), set.paxel().get());
            tag(ItemTags.SWORDS).add(set.sword().get());
        }
        ModItems.DRILLS.forEach(drill -> {
            tag(ItemTags.PICKAXES).add(drill.get());
            tag(ItemTags.SHOVELS).add(drill.get());
        });
        ModItems.CHAINSAWS.forEach(chainsaw -> tag(ItemTags.AXES).add(chainsaw.get()));
    }
}
