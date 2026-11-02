package com.script.comminutioncraft.data;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.registry.ModBlocks;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TexturedModel;
import net.minecraft.data.PackOutput;

/**
 * Blockstates, block models and item models. Machines use vanilla's furnace layout, which expects
 * textures named <block>_top, _side, _front and _front_on (the lit, animated front).
 */
public class ModModelProvider extends ModelProvider {
    public ModModelProvider(PackOutput output) {
        super(output, ComminutionCraft.MODID);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        blockModels.createFurnace(ModBlocks.COAL_GRANULATOR.get(), TexturedModel.ORIENTABLE_ONLY_TOP);
        blockModels.createFurnace(ModBlocks.COAL_SMELTER.get(), TexturedModel.ORIENTABLE_ONLY_TOP);

        for (ProcessedMetal metal : ProcessedMetal.ALL) {
            itemModels.generateFlatItem(metal.granulated().get(), ModelTemplates.FLAT_ITEM);
        }
    }
}
