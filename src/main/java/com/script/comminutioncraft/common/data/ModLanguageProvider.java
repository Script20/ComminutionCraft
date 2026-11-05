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
        add(ModBlocks.ENERGY_BATTERY.get(), "Energy Battery");
        add(ModItems.ELECTRONIC_CIRCUIT.get(), "Electronic Circuit");
        add(ModBlocks.PLASMA_GENERATOR.get(), "Plasma Generator");
        add(ModBlocks.ATMOSPHERIC_WATER_GENERATOR.get(), "Atmospheric Water Generator");
        add(ModBlocks.ELECTROLYSER.get(), "Electrolyser");
        add(ModBlocks.MHD_GENERATOR.get(), "MHD Generator");
        add(ModBlocks.ISOTOPE_SEPARATOR.get(), "Isotope Separator");
        add(ModBlocks.TRITIUM_BREEDER.get(), "Tritium Breeder");
        add(ModBlocks.PELLET_PRESS.get(), "Pellet Press");
        add(ModBlocks.ORE_PURIFIER.get(), "Ore Purifier");
        add(ModBlocks.CREATIVE_ENERGY_BATTERY.get(), "Creative Energy Battery");
        add(ModBlocks.CREATIVE_FLUID_TANK.get(), "Creative Fluid Tank");
        add("gui.comminutioncraft.creative_tank.none", "No fluid");
        add("jei.comminutioncraft.purifying", "Ore Purifying");
        add("jei.comminutioncraft.time.ore_purifier", "Ore Purifier: %s");
        add("jei.comminutioncraft.uses_oxygen", "Uses %s mB oxygen");
        add("jei.comminutioncraft.chance", "%s%% chance");
        add(ModBlocks.REACTOR_CASING.get(), "Reactor Casing");
        add(ModBlocks.REACTOR_GLASS.get(), "Reactor Glass");
        add(ModBlocks.REACTOR_PORT.get(), "Reactor Port");
        add(ModBlocks.REACTOR_CONTROLLER.get(), "Fusion Reactor Controller");
        add(ModBlocks.FUSION_CABLE.get(), "Fusion Cable");
        add(ModBlocks.FUSION_ENERGY_BATTERY.get(), "Fusion Energy Battery");
        add("gui.comminutioncraft.reactor.not_formed", "Not formed");
        add("gui.comminutioncraft.reactor.running", "Running");
        add("gui.comminutioncraft.reactor.heating", "Heating: %s%%");
        add("gui.comminutioncraft.reactor.idle", "Idle");
        add("gui.comminutioncraft.reactor.heat", "Core heat: %s%%");
        add(ModItems.FUSION_CIRCUIT.get(), "Fusion Circuit");
        add(ModItems.REACTOR_PLATING.get(), "Reactor Plating");
        add(ModItems.FUSION_FUEL_PELLET.get(), "Fusion Fuel Pellet");
        add("fluid_type.comminutioncraft.deuterium", "Deuterium");
        add("fluid_type.comminutioncraft.tritium", "Tritium");
        add("gui.comminutioncraft.deuterium", "Deuterium: %s / %s mB");
        add("gui.comminutioncraft.tritium", "Tritium: %s / %s mB");
        add("jei.comminutioncraft.pressing", "Pellet Pressing");
        add("jei.comminutioncraft.time.pellet_press", "Pellet Press: %s");
        add("jei.comminutioncraft.uses_fusion_fuel", "Uses %s mB deuterium and %s mB tritium");
        add("fluid_type.comminutioncraft.hydrogen", "Hydrogen");
        add("fluid_type.comminutioncraft.oxygen", "Oxygen");
        add("gui.comminutioncraft.hydrogen", "Hydrogen: %s / %s mB");
        add("gui.comminutioncraft.oxygen", "Oxygen: %s / %s mB");
        add("gui.comminutioncraft.humidity", "Humidity: %s%%");
        add("gui.comminutioncraft.fluid_rate", "%s mB/t");
        add("config.jade.plugin_comminutioncraft.water_generator", "Comminution Craft water generator");
        add("jade.comminutioncraft.water_generator", "Intake %s/6 · Humidity %s%% · %s mB/t");
        add(ModBlocks.FLUID_PIPE.get(), "Fluid Pipe");
        add(ModBlocks.FLUID_TANK.get(), "Fluid Tank");
        add(ModItems.AIR_FILTER.get(), "Air Filter");
        add(ModBlocks.PLASMA_GRANULATOR.get(), "Plasma Granulator");
        add(ModBlocks.PLASMA_SMELTER.get(), "Plasma Smelter");
        add(ModBlocks.PLASMA_ENERGY_BATTERY.get(), "Plasma Energy Battery");
        add(ModBlocks.PLASMA_CABLE.get(), "Plasma Cable");
        add(ModItems.PLASMA_CIRCUIT.get(), "Plasma Circuit");
        add(ModBlocks.CIRCUIT_ASSEMBLER.get(), "Circuit Assembler");
        add(ModBlocks.ORE_WASHER.get(), "Ore Washer");
        add("gui.comminutioncraft.water", "Water: %s / %s mB");
        add("fluid_type.comminutioncraft.plasma", "Plasma");
        ModBlocks.STONE_SETS.forEach(set -> set.all().forEach(block -> add(block.get(), titleCase(block.getId().getPath()))));

        add(ModItems.WRENCH.get(), "Wrench");
        add("item.comminutioncraft.wrench.mode.rotate", "Mode: Rotate");
        add("item.comminutioncraft.wrench.mode.cable", "Mode: Cables/Pipes");
        add("item.comminutioncraft.wrench.help.rotate", "Right-click a block to rotate it");
        add("item.comminutioncraft.wrench.help.cable", "Right-click a cable's or pipe's arm to cut or restore that connection");
        add("item.comminutioncraft.wrench.mode.configure", "Mode: Configure");
        add("item.comminutioncraft.wrench.help.configure", "Right-click a machine face to cycle Input / Output / None; sneak + right-click to pick the machine up");
        add("item.comminutioncraft.wrench.help.switch", "Press %s to switch mode");
        add("key.category.comminutioncraft.main", "Comminution Craft");
        add("key.comminutioncraft.wrench_mode", "Switch Wrench Mode");
        add("tooltip.comminutioncraft.machine_settings", "Keeps its side and redstone settings");
        add("message.comminutioncraft.wrench.cable_cut", "Cable connection cut");
        add("message.comminutioncraft.wrench.cable_restored", "Cable connection restored");

        add("gui.comminutioncraft.energy", "%s / %s FE");
        add("gui.comminutioncraft.generating", "%s FE/t");
        add("gui.comminutioncraft.battery.stored", "%s FE");
        add("gui.comminutioncraft.intake", "Intake: %s/6");
        add("gui.comminutioncraft.plasma_rate", "%s mB/t");
        add("gui.comminutioncraft.plasma", "Plasma: %s / %s mB");
        add("gui.comminutioncraft.no_filter", "No filter");
        add("gui.comminutioncraft.no_filter.desc", "An Air Filter doubles how much water it makes");
        add("tooltip.comminutioncraft.stored_fluid", "%s: %s mB");
        add("gui.comminutioncraft.battery.capacity", "of %s FE");
        add("gui.comminutioncraft.battery.input", "In: %s FE/t");
        add("gui.comminutioncraft.battery.output", "Out: %s FE/t");

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
        add("gui.comminutioncraft.side_mode.byproduct", "By-product");

        add("jei.comminutioncraft.granulating", "Granulating");
        add("jei.comminutioncraft.time.coal_granulator", "Coal Granulator: %s");
        add("jei.comminutioncraft.time.electric_granulator", "Electric Granulator: %s");
        add("jei.comminutioncraft.time.plasma_granulator", "Plasma Granulator: %s");
        add("jei.comminutioncraft.electric_granulators_only", "Electric or Plasma Granulator only");
        add("jei.comminutioncraft.assembling", "Circuit Assembling");
        add("jei.comminutioncraft.time.circuit_assembler", "Circuit Assembler: %s");
        add("jei.comminutioncraft.uses_plasma", "Uses %s mB plasma");
        add("jei.comminutioncraft.washing", "Ore Washing");
        add("jei.comminutioncraft.time.ore_washer", "Ore Washer: %s");
        add("jei.comminutioncraft.uses_water", "Uses %s mB water");

        add("config.jade.plugin_comminutioncraft.machine", "Comminution Craft machines");
        add("config.jade.plugin_comminutioncraft.generator", "Comminution Craft generator");
        add("jade.comminutioncraft.generating", "Generating %s FE/t (%ss left)");
        add("jade.comminutioncraft.idle", "Idle");
        add("config.jade.plugin_comminutioncraft.battery", "Comminution Craft energy battery");
        add("jade.comminutioncraft.battery_flow", "In %s FE/t · Out %s FE/t");
        add("jade.comminutioncraft.no_filter", "No Air Filter (half as much water)");
        for (ProcessedMetal metal : ProcessedMetal.ALL) {
            add(metal.granulated().get(), metal.displayName());
            add(metal.washed().get(), metal.washedDisplayName());
            add(metal.purified().get(), metal.purifiedDisplayName());
        }
    }

    /** "mossy_cobbled_volcanic_basalt" -> "Mossy Cobbled Volcanic Basalt" */
    private static String titleCase(String id) {
        return Arrays.stream(id.split("_"))
                .map(word -> Character.toUpperCase(word.charAt(0)) + word.substring(1))
                .collect(Collectors.joining(" "));
    }
}
