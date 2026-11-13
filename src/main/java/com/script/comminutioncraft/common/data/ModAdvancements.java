package com.script.comminutioncraft.common.data;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.advancement.MachineEventTrigger;
import com.script.comminutioncraft.common.registry.ModItems;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementType;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.InventoryChangeTrigger;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.advancements.AdvancementSubProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;

/**
 * The mod's advancements (owner-approved mock-up 2026-11-13: design/previews/advancements.png). Root = craft a Wrench;
 * one tree per tier for the ore processing progression; a Special Parts tree (circuits, Reactor Plating, Fusion Fuel
 * Pellet, Singularity Shard). Most are "have the item"; the machine moments use {@link MachineEventTrigger}. Titles
 * and descriptions are lang keys ({@link #LANG}, written by ModLanguageProvider).
 */
public class ModAdvancements implements AdvancementSubProvider {
    /** Lang key -> English, filled while generating (and by {@link #lang()} for the language provider). */
    public static final Map<String, String> LANG = new LinkedHashMap<>();
    public static final Identifier BACKGROUND = ComminutionCraft.id("gui/advancements/backgrounds/chassis");

    private HolderGetter<Item> items;
    private Consumer<AdvancementHolder> out;

    /** Every advancement's title and description, for the language provider (runs the tree without saving). */
    public static Map<String, String> lang() {
        if (LANG.isEmpty()) {
            new ModAdvancements().generate(null, holder -> {});
        }
        return LANG;
    }

    @Override
    public void generate(HolderLookup.Provider registries, Consumer<AdvancementHolder> output) {
        items = BuiltInRegistries.ITEM; // (also works without a lookup, for lang())
        out = output;

        AdvancementHolder root = Advancement.Builder.advancement()
                .display(ModItems.WRENCH.get(), title("root", "Comminution Craft"), description("root", "Craft a Wrench"),
                        BACKGROUND, AdvancementType.TASK, false, false, false)
                .addCriterion("wrench", has(ModItems.WRENCH.get()))
                .save(out, id("root"));

        // Tier 1 - Coal
        AdvancementHolder coalGranulator = item(root, "coal_granulator", ModItems.COAL_GRANULATOR.get(), "Grind It Down", "Build a Coal Granulator");
        AdvancementHolder granulated = any(coalGranulator, "twice_the_ore", ModItems.GRANULATED_IRON.get(), AdvancementType.TASK,
                "Twice the Ore", "Granulate raw ore into two", ProcessedMetal::granulated);
        item(granulated, "coal_smelter", ModItems.COAL_SMELTER.get(), "Hot Off the Line", "Build a Coal Smelter");

        // Tier 2 - Electric
        AdvancementHolder generator = item(root, "electric_generator", ModItems.ELECTRIC_GENERATOR.get(), "It's Electric", "Build an Electric Generator");
        AdvancementHolder electricGranulator = item(generator, "electric_granulator", ModItems.ELECTRIC_GRANULATOR.get(), "Plugged In", "Build an Electric Granulator");
        item(electricGranulator, "electric_smelter", ModItems.ELECTRIC_SMELTER.get(), "Current Events", "Build an Electric Smelter");
        item(generator, "energy_battery", ModItems.ENERGY_BATTERY.get(), "Charged Up", "Build an Energy Battery");

        // Tier 3 - Plasma
        AdvancementHolder water = item(root, "atmospheric_water_generator", ModItems.ATMOSPHERIC_WATER_GENERATOR.get(), "Out of Thin Air", "Build an Atmospheric Water Generator");
        AdvancementHolder electrolyser = item(water, "electrolyser", ModItems.ELECTROLYSER.get(), "Split Decision", "Build an Electrolyser");
        AdvancementHolder plasmaGenerator = item(electrolyser, "plasma_generator", ModItems.PLASMA_GENERATOR.get(), "The Fourth State", "Build a Plasma Generator");
        item(plasmaGenerator, "mhd_generator", ModItems.MHD_GENERATOR.get(), "Plasma Power", "Build an MHD Generator");
        AdvancementHolder washer = item(plasmaGenerator, "ore_washer", ModItems.ORE_WASHER.get(), "Squeaky Clean", "Build an Ore Washer");
        AdvancementHolder washed = any(washer, "triple_threat", ModItems.WASHED_IRON.get(), AdvancementType.GOAL,
                "Triple Threat", "Wash raw ore into three", ProcessedMetal::washed);
        AdvancementHolder plasmaGranulator = item(washed, "plasma_granulator", ModItems.PLASMA_GRANULATOR.get(), "Finer Grit", "Build a Plasma Granulator");
        item(plasmaGranulator, "plasma_smelter", ModItems.PLASMA_SMELTER.get(), "White Hot", "Build a Plasma Smelter");

        // Tier 4 - Fusion
        AdvancementHolder separator = item(root, "isotope_separator", ModItems.ISOTOPE_SEPARATOR.get(), "Heavy Water", "Build an Isotope Separator");
        AdvancementHolder breeder = item(separator, "tritium_breeder", ModItems.TRITIUM_BREEDER.get(), "Breeding Program", "Build a Tritium Breeder");
        AdvancementHolder press = item(breeder, "pellet_press", ModItems.PELLET_PRESS.get(), "Under Pressure", "Build a Pellet Press");
        event(press, "star_in_a_box", ModItems.REACTOR_CONTROLLER.get(), AdvancementType.GOAL, false,
                "Star in a Box", "Light a Fusion Reactor", MachineEventTrigger.FUSION_REACTOR_LIT);
        AdvancementHolder purifier = item(separator, "ore_purifier", ModItems.ORE_PURIFIER.get(), "Purity Test", "Build an Ore Purifier");
        event(purifier, "bonus_round", ModItems.PURIFIED_IRON.get(), AdvancementType.TASK, false,
                "Bonus Round", "Get a by-product from an Ore Purifier", MachineEventTrigger.PURIFIER_BYPRODUCT);

        // Tier 5 - Antimatter
        AdvancementHolder accelerator = event(root, "ring_of_power", ModItems.ACCELERATOR_CONTROLLER.get(), AdvancementType.GOAL, false,
                "Ring of Power", "Make antimatter in a Particle Accelerator", MachineEventTrigger.ANTIMATTER_MADE);
        AdvancementHolder cell = item(accelerator, "containment_cell", ModItems.CONTAINMENT_CELL.get(), "Handle With Care", "Build a Containment Cell");
        item(cell, "annihilation_generator", ModItems.ANNIHILATION_GENERATOR.get(), "E = mc²", "Build an Annihilation Generator");
        AdvancementHolder flotation = any(accelerator, "fourfold", ModItems.CONCENTRATE_IRON.get(), AdvancementType.GOAL,
                "Fourfold", "Float raw ore into four concentrate", ProcessedMetal::concentrate);
        Advancement.Builder.advancement().parent(flotation)
                .display(ModItems.ANTIMATTER_GRANULATOR.get(), title("full_suite", "Full Suite"),
                        description("full_suite", "Build all four antimatter machines"), null, AdvancementType.TASK, true, true, false)
                .addCriterion("granulator", has(ModItems.ANTIMATTER_GRANULATOR.get()))
                .addCriterion("smelter", has(ModItems.ANTIMATTER_SMELTER.get()))
                .addCriterion("washer", has(ModItems.ANTIMATTER_WASHER.get()))
                .addCriterion("purifier", has(ModItems.ANTIMATTER_PURIFIER.get()))
                .save(out, id("full_suite"));

        // Tier 6 - Singularity
        AdvancementHolder horizon = event(root, "event_horizon", ModItems.HORIZON_CONTROLLER.get(), AdvancementType.CHALLENGE, false,
                "Event Horizon", "Ignite a black hole", MachineEventTrigger.BLACK_HOLE_IGNITED);
        event(horizon, "five_alive", ModItems.SINGULARITY_PROCESSOR.get(), AdvancementType.GOAL, false,
                "Five Alive", "Turn one raw ore into five ingots", MachineEventTrigger.SINGULARITY_PROCESSED);
        item(horizon, "singularity_energy_battery", ModItems.SINGULARITY_ENERGY_BATTERY.get(), "Two Billion", "Build a Singularity Energy Battery");
        event(horizon, "spaghettified", ModItems.CONTAINMENT_FRAME.get(), AdvancementType.CHALLENGE, true,
                "Spaghettified", "Walk into the event horizon", MachineEventTrigger.SPAGHETTIFIED);

        // Special parts
        AdvancementHolder electronic = item(root, "electronic_circuit", ModItems.ELECTRONIC_CIRCUIT.get(), "Circuitry", "Craft an Electronic Circuit");
        AdvancementHolder plasma = item(electronic, "plasma_circuit", ModItems.PLASMA_CIRCUIT.get(), "Plasma-Forged", "Assemble a Plasma Circuit");
        AdvancementHolder fusion = item(plasma, "fusion_circuit", ModItems.FUSION_CIRCUIT.get(), "Fused", "Assemble a Fusion Circuit");
        AdvancementHolder antimatter = item(fusion, "antimatter_circuit", ModItems.ANTIMATTER_CIRCUIT.get(), "Anti-Matters", "Assemble an Antimatter Circuit");
        AdvancementHolder ultimate = item(antimatter, "singularity_circuit", ModItems.SINGULARITY_CIRCUIT.get(), AdvancementType.GOAL,
                "Ultimate Circuitry", "Assemble a Singularity Circuit");
        item(ultimate, "singularity_shard", ModItems.SINGULARITY_SHARD.get(), AdvancementType.CHALLENGE, "Shard of Infinity", "Find a Singularity Shard");
        AdvancementHolder plating = item(plasma, "reactor_plating", ModItems.REACTOR_PLATING.get(), "Plated", "Forge Reactor Plating");
        item(plating, "fusion_fuel_pellet", ModItems.FUSION_FUEL_PELLET.get(), "Fuel Up", "Press a Fusion Fuel Pellet");
    }

    // ---- helpers ----

    private static String id(String name) {
        return ComminutionCraft.id("main/" + name).toString();
    }

    private static Component title(String name, String english) {
        String key = "advancements." + ComminutionCraft.MODID + "." + name + ".title";
        LANG.put(key, english);
        return Component.translatable(key);
    }

    private static Component description(String name, String english) {
        String key = "advancements." + ComminutionCraft.MODID + "." + name + ".description";
        LANG.put(key, english);
        return Component.translatable(key);
    }

    private Criterion<InventoryChangeTrigger.TriggerInstance> has(ItemLike item) {
        return InventoryChangeTrigger.TriggerInstance.hasItems(item);
    }

    private AdvancementHolder item(AdvancementHolder parent, String name, ItemLike icon, String title, String how) {
        return item(parent, name, icon, AdvancementType.TASK, title, how);
    }

    private AdvancementHolder item(AdvancementHolder parent, String name, ItemLike icon, AdvancementType type, String title, String how) {
        return Advancement.Builder.advancement().parent(parent)
                .display(icon, title(name, title), description(name, how), null, type, true, true, false)
                .addCriterion("has", has(icon))
                .save(out, id(name));
    }

    /** Any one of a processed-ore form (any metal's granulated / washed / concentrate ore). */
    private AdvancementHolder any(AdvancementHolder parent, String name, ItemLike icon, AdvancementType type, String title,
                                  String how, Function<ProcessedMetal, ? extends ItemLike> form) {
        List<ItemLike> forms = new ArrayList<>();
        for (ProcessedMetal metal : ProcessedMetal.ALL) {
            forms.add(form.apply(metal));
        }
        return Advancement.Builder.advancement().parent(parent)
                .display(icon, title(name, title), description(name, how), null, type, true, true, false)
                .addCriterion("has", InventoryChangeTrigger.TriggerInstance.hasItems(
                        ItemPredicate.Builder.item().of(items, forms.toArray(ItemLike[]::new))))
                .save(out, id(name));
    }

    private AdvancementHolder event(AdvancementHolder parent, String name, ItemLike icon, AdvancementType type, boolean hidden,
                                    String title, String how, String event) {
        return Advancement.Builder.advancement().parent(parent)
                .display(icon, title(name, title), description(name, how), null, type, true, true, hidden)
                .addCriterion(event, MachineEventTrigger.TriggerInstance.of(event))
                .save(out, id(name));
    }
}
