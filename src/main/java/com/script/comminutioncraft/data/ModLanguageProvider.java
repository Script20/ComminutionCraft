package com.script.comminutioncraft.data;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.registry.ModBlocks;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ModLanguageProvider extends LanguageProvider {
    public ModLanguageProvider(PackOutput output) {
        super(output, ComminutionCraft.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add("itemGroup.comminutioncraft", "ComminutionCraft");
        add(ModBlocks.COAL_GRANULATOR.get(), "Coal Granulator");
        add(ModBlocks.COAL_SMELTER.get(), "Coal Smelter");
        for (ProcessedMetal metal : ProcessedMetal.ALL) {
            add(metal.granulated().get(), metal.displayName());
        }
    }
}
