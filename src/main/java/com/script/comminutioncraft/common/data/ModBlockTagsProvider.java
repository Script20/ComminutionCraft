package com.script.comminutioncraft.common.data;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.block.StoneSet;
import com.script.comminutioncraft.common.item.tool.ModToolMaterials;
import com.script.comminutioncraft.common.registry.ModBlocks;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;

public class ModBlockTagsProvider extends BlockTagsProvider {
    public ModBlockTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, ComminutionCraft.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(BlockTags.MINEABLE_WITH_AXE).add(ModBlocks.SOARYN_CHEST.get());
        tag(BlockTags.MINEABLE_WITH_PICKAXE)
                .add(ModBlocks.COAL_GRANULATOR.get())
                .add(ModBlocks.COAL_SMELTER.get())
                .add(ModBlocks.ELECTRIC_GRANULATOR.get())
                .add(ModBlocks.ELECTRIC_SMELTER.get())
                .add(ModBlocks.ELECTRIC_GENERATOR.get())
                .add(ModBlocks.ENERGY_BATTERY.get())
                .add(ModBlocks.PLASMA_GENERATOR.get())
                .add(ModBlocks.ATMOSPHERIC_WATER_GENERATOR.get())
                .add(ModBlocks.ELECTROLYSER.get())
                .add(ModBlocks.MHD_GENERATOR.get())
                .add(ModBlocks.ISOTOPE_SEPARATOR.get())
                .add(ModBlocks.TRITIUM_BREEDER.get())
                .add(ModBlocks.PELLET_PRESS.get())
                .add(ModBlocks.ORE_PURIFIER.get())
                .add(ModBlocks.CREATIVE_ENERGY_BATTERY.get())
                .add(ModBlocks.CREATIVE_FLUID_TANK.get())
                .add(ModBlocks.CREATIVE_CHEST.get())
                .add(ModBlocks.REACTOR_CASING.get())
                .add(ModBlocks.REACTOR_GLASS.get())
                .add(ModBlocks.REACTOR_PORT.get())
                .add(ModBlocks.REACTOR_CONTROLLER.get())
                .add(ModBlocks.FUSION_CABLE.get())
                .add(ModBlocks.FUSION_ENERGY_BATTERY.get())
                .add(ModBlocks.ANTIMATTER_GRANULATOR.get())
                .add(ModBlocks.ANTIMATTER_SMELTER.get())
                .add(ModBlocks.FROTH_FLOTATION_CELL.get())
                .add(ModBlocks.ANTIMATTER_WASHER.get())
                .add(ModBlocks.ANTIMATTER_PURIFIER.get())
                .add(ModBlocks.ELECTRIC_SMELTING_FACTORY.get())
                .add(ModBlocks.PLASMA_GRANULATING_FACTORY.get())
                .add(ModBlocks.PLASMA_SMELTING_FACTORY.get())
                .add(ModBlocks.PURIFYING_FACTORY.get())
                .add(ModBlocks.ANTIMATTER_WASHING_FACTORY.get())
                .add(ModBlocks.ANTIMATTER_PURIFYING_FACTORY.get())
                .add(ModBlocks.ANTIMATTER_GRANULATING_FACTORY.get())
                .add(ModBlocks.ANTIMATTER_SMELTING_FACTORY.get())
                .add(ModBlocks.ANNIHILATION_GENERATOR.get())
                .add(ModBlocks.CONTAINMENT_FRAME.get())
                .add(ModBlocks.SINGULARITY_PROCESSOR.get())
                .add(ModBlocks.SINGULARITY_CABLE.get())
                .add(ModBlocks.SINGULARITY_ENERGY_BATTERY.get())
                .add(ModBlocks.HORIZON_CONTROLLER.get())
                .add(ModBlocks.HORIZON_PORT.get())
                .add(ModBlocks.ANTIMATTER_CABLE.get())
                .add(ModBlocks.ANTIMATTER_ENERGY_BATTERY.get())
                .add(ModBlocks.FLUID_PIPE.get())
                .add(ModBlocks.ITEM_PIPE.get())
                .add(ModBlocks.FUSION_ITEM_PIPE.get())
                .add(ModBlocks.FLUID_TANK.get())
                .add(ModBlocks.CONTAINMENT_CELL.get())
                .add(ModBlocks.MAGNETIC_CONDUIT.get())
                .add(ModBlocks.ACCELERATOR_SEGMENT.get())
                .add(ModBlocks.ACCELERATOR_PORT.get())
                .add(ModBlocks.ACCELERATOR_CONTROLLER.get())
                .add(ModBlocks.ENERGY_CABLE.get())
                .add(ModBlocks.PLASMA_GRANULATOR.get())
                .add(ModBlocks.PLASMA_SMELTER.get())
                .add(ModBlocks.CIRCUIT_ASSEMBLER.get())
                .add(ModBlocks.ORE_WASHER.get())
                .add(ModBlocks.PLASMA_ENERGY_BATTERY.get())
                .add(ModBlocks.PLASMA_CABLE.get());

        for (StoneSet set : ModBlocks.STONE_SETS) {
            set.all().forEach(block -> tag(BlockTags.MINEABLE_WITH_PICKAXE).add(block.get()));
            set.allStairs().forEach(block -> tag(BlockTags.STAIRS).add(block.get()));
            set.allSlabs().forEach(block -> tag(BlockTags.SLABS).add(block.get()));
            set.allWalls().forEach(block -> tag(BlockTags.WALLS).add(block.get()));
            tag(BlockTags.STONE_BUTTONS).add(set.button.get());
            // Natural stone: caves carve it and ores/dirt/gravel blobs can replace it, like granite.
            tag(BlockTags.BASE_STONE_OVERWORLD).add(set.stone.get());
            tag(Tags.Blocks.STONES).add(set.stone.get());
            tag(Tags.Blocks.COBBLESTONES).add(set.cobbled.get(), set.mossyCobbled.get());
        }

        // Purpleheart: vanilla's wood tags give axe mining, burning, leaf decay, fences joining, sign behaviour
        var wood = ModBlocks.PURPLEHEART;
        var logs = WoodFamily.logsBlockTag(wood);
        wood.logs().forEach(block -> tag(logs).add(block.get()));
        tag(BlockTags.LOGS_THAT_BURN).addTag(logs);
        tag(BlockTags.OVERWORLD_NATURAL_LOGS).add(wood.log.get());
        tag(Tags.Blocks.STRIPPED_LOGS).add(wood.strippedLog.get());
        tag(Tags.Blocks.STRIPPED_WOODS).add(wood.strippedWood.get());
        tag(BlockTags.PLANKS).add(wood.planks.get());
        tag(BlockTags.WOODEN_STAIRS).add(wood.stairs.get());
        tag(BlockTags.WOODEN_SLABS).add(wood.slab.get());
        tag(BlockTags.WOODEN_FENCES).add(wood.fence.get());
        tag(Tags.Blocks.FENCES_WOODEN).add(wood.fence.get());
        tag(BlockTags.FENCE_GATES).add(wood.fenceGate.get());
        tag(Tags.Blocks.FENCE_GATES_WOODEN).add(wood.fenceGate.get());
        tag(BlockTags.WOODEN_DOORS).add(wood.door.get());
        tag(BlockTags.WOODEN_TRAPDOORS).add(wood.trapdoor.get());
        tag(BlockTags.WOODEN_PRESSURE_PLATES).add(wood.pressurePlate.get());
        tag(BlockTags.WOODEN_BUTTONS).add(wood.button.get());
        tag(BlockTags.STANDING_SIGNS).add(wood.sign.get());
        tag(BlockTags.WALL_SIGNS).add(wood.wallSign.get());
        tag(BlockTags.CEILING_HANGING_SIGNS).add(wood.hangingSign.get());
        tag(BlockTags.WALL_HANGING_SIGNS).add(wood.wallHangingSign.get());
        tag(BlockTags.LEAVES).add(wood.leaves.get());
        tag(BlockTags.SAPLINGS).add(wood.sapling.get());
        tag(BlockTags.FLOWER_POTS).add(wood.pottedSapling.get());

        // Tools: a drill mines like a pickaxe and a shovel, a chainsaw like an axe (and leaves), a paxel like all four
        tag(ModToolMaterials.MINEABLE_DRILL).addTag(BlockTags.MINEABLE_WITH_PICKAXE).addTag(BlockTags.MINEABLE_WITH_SHOVEL);
        tag(ModToolMaterials.MINEABLE_CHAINSAW).addTag(BlockTags.MINEABLE_WITH_AXE).addTag(BlockTags.LEAVES);
        tag(ModToolMaterials.MINEABLE_PAXEL).addTag(BlockTags.MINEABLE_WITH_PICKAXE).addTag(BlockTags.MINEABLE_WITH_AXE)
                .addTag(BlockTags.MINEABLE_WITH_SHOVEL).addTag(BlockTags.MINEABLE_WITH_HOE);
    }
}
