package com.script.comminutioncraft.common.data;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.registry.ModBlocks;
import com.script.comminutioncraft.common.registry.ModItems;
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
        add("item.comminutioncraft.wrench.mode.configure", "Mode: Configure");
        add("item.comminutioncraft.wrench.help.configure", "Right-click a machine face to cycle Input / Output / None");
        add("item.comminutioncraft.wrench.help.switch", "Sneak + right-click to switch mode");
        add("message.comminutioncraft.wrench.cable_cut", "Cable connection cut");
        add("message.comminutioncraft.wrench.cable_restored", "Cable connection restored");

        add("gui.comminutioncraft.energy", "%s / %s FE");
        add("gui.comminutioncraft.generating", "%s FE/t");

        add("gui.comminutioncraft.redstone", "Redstone: %s");
        add("gui.comminutioncraft.redstone.ignore", "Ignore");
        add("gui.comminutioncraft.redstone.ignore.desc", "Always runs");
        add("gui.comminutioncraft.redstone.high", "High");
        add("gui.comminutioncraft.redstone.high.desc", "Runs only with a redstone signal");
        add("gui.comminutioncraft.redstone.low", "Low");
        add("gui.comminutioncraft.redstone.low.desc", "Runs only without a redstone signal");
        add("gui.comminutioncraft.side_config", "Side Configuration");
        add("gui.comminutioncraft.side_config.help", "Left-click: change · Right-drag: turn");
        add("gui.comminutioncraft.side_config.tooltip", "%s: %s");
        add("gui.comminutioncraft.side.front", "Front");
        add("gui.comminutioncraft.side.back", "Back");
        add("gui.comminutioncraft.side.left", "Left");
        add("gui.comminutioncraft.side.right", "Right");
        add("gui.comminutioncraft.side.top", "Top");
        add("gui.comminutioncraft.side.bottom", "Bottom");
        add("gui.comminutioncraft.side_mode.input", "Input");
        add("gui.comminutioncraft.side_mode.output", "Output");
        add("gui.comminutioncraft.side_mode.none", "None");

        add("jei.comminutioncraft.granulating", "Granulating");
        add("jei.comminutioncraft.time.coal_granulator", "Coal Granulator: %s");
        add("jei.comminutioncraft.time.electric_granulator", "Electric Granulator: %s");

        add("config.jade.plugin_comminutioncraft.machine", "ComminutionCraft machines");
        add("config.jade.plugin_comminutioncraft.generator", "ComminutionCraft generator");
        add("jade.comminutioncraft.generating", "Generating %s FE/t (%ss left)");
        add("jade.comminutioncraft.idle", "Idle");
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
