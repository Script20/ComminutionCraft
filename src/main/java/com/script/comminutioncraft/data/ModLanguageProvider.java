package com.script.comminutioncraft.data;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.registry.ModBlocks;
import com.script.comminutioncraft.registry.ModItems;
import java.util.Arrays;
import java.util.stream.Collectors;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class ModLanguageProvider extends LanguageProvider {
    public ModLanguageProvider(PackOutput output) {
        super(output, ComminutionCraft.MODID, "en_us");
    }

    @Override
    protected void addTranslations() {
        add("itemGroup.comminutioncraft", "Comminution Craft");
        add(ModBlocks.COAL_GRANULATOR.get(), "Coal Granulator");
        add(ModBlocks.COAL_SMELTER.get(), "Coal Smelter");
        add(ModBlocks.ELECTRIC_GRANULATOR.get(), "Electric Granulator");
        add(ModBlocks.ELECTRIC_SMELTER.get(), "Electric Smelter");
        add(ModBlocks.ELECTRIC_GENERATOR.get(), "Electric Generator");
        add(ModBlocks.ENERGY_CABLE.get(), "Energy Cable");
        ModBlocks.STONE_SETS.forEach(set -> set.all().forEach(block -> add(block.get(), titleCase(block.getId().getPath()))));

        add(ModItems.WRENCH.get(), "Wrench");
        add("item.comminutioncraft.wrench.mode.rotate", "Mode: Rotate");
        add("item.comminutioncraft.wrench.mode.cable", "Mode: Cable");
        add("item.comminutioncraft.wrench.help.rotate", "Right-click a block to rotate it");
        add("item.comminutioncraft.wrench.help.cable", "Right-click a cable's arm to cut or restore that connection");
        add("item.comminutioncraft.wrench.help.switch", "Sneak + right-click to switch mode");
        add("message.comminutioncraft.wrench.cable_cut", "Cable connection cut");
        add("message.comminutioncraft.wrench.cable_restored", "Cable connection restored");

        add("gui.comminutioncraft.energy", "%s / %s FE");
        add("gui.comminutioncraft.generating", "%s FE/t");
        for (ProcessedMetal metal : ProcessedMetal.ALL) {
            add(metal.granulated().get(), metal.displayName());
        }
    }

    /** "mossy_cobbled_volcanic_basalt" -> "Mossy Cobbled Volcanic Basalt" */
    private static String titleCase(String id) {
        return Arrays.stream(id.split("_"))
                .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
                .collect(Collectors.joining(" "));
    }
}
