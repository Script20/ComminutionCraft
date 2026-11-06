package com.script.comminutioncraft.common.registry;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.item.EnergyBatteryItem;
import com.script.comminutioncraft.common.item.WrenchItem;
import com.script.comminutioncraft.common.machine.EnergyBatteryBlockEntity;
import com.script.comminutioncraft.common.machine.AtmosphericWaterGeneratorBlockEntity;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ComminutionCraft.MODID);

    // Granulated ores (2 per raw ore from the granulator, smelt into 1 ingot each)
    public static final DeferredItem<Item> GRANULATED_IRON = ITEMS.registerSimpleItem("granulated_iron");
    public static final DeferredItem<Item> GRANULATED_COPPER = ITEMS.registerSimpleItem("granulated_copper");
    public static final DeferredItem<Item> GRANULATED_GOLD = ITEMS.registerSimpleItem("granulated_gold");
    // Other mods' metals: always registered, only shown/used when some mod adds that metal (see ProcessedMetal)
    public static final DeferredItem<Item> GRANULATED_TIN = ITEMS.registerSimpleItem("granulated_tin");
    public static final DeferredItem<Item> GRANULATED_LEAD = ITEMS.registerSimpleItem("granulated_lead");
    public static final DeferredItem<Item> GRANULATED_SILVER = ITEMS.registerSimpleItem("granulated_silver");
    public static final DeferredItem<Item> GRANULATED_NICKEL = ITEMS.registerSimpleItem("granulated_nickel");
    public static final DeferredItem<Item> GRANULATED_ZINC = ITEMS.registerSimpleItem("granulated_zinc");
    public static final DeferredItem<Item> GRANULATED_ALUMINUM = ITEMS.registerSimpleItem("granulated_aluminum");
    public static final DeferredItem<Item> GRANULATED_OSMIUM = ITEMS.registerSimpleItem("granulated_osmium");
    public static final DeferredItem<Item> GRANULATED_URANIUM = ITEMS.registerSimpleItem("granulated_uranium");

    // Washed ores (3 per raw ore or ore block from the Ore Washer; an Electric or Plasma Granulator makes each one granulated)
    public static final DeferredItem<Item> WASHED_IRON = ITEMS.registerSimpleItem("washed_iron");
    public static final DeferredItem<Item> WASHED_COPPER = ITEMS.registerSimpleItem("washed_copper");
    public static final DeferredItem<Item> WASHED_GOLD = ITEMS.registerSimpleItem("washed_gold");
    public static final DeferredItem<Item> WASHED_TIN = ITEMS.registerSimpleItem("washed_tin");
    public static final DeferredItem<Item> WASHED_LEAD = ITEMS.registerSimpleItem("washed_lead");
    public static final DeferredItem<Item> WASHED_SILVER = ITEMS.registerSimpleItem("washed_silver");
    public static final DeferredItem<Item> WASHED_NICKEL = ITEMS.registerSimpleItem("washed_nickel");
    public static final DeferredItem<Item> WASHED_ZINC = ITEMS.registerSimpleItem("washed_zinc");
    public static final DeferredItem<Item> WASHED_ALUMINUM = ITEMS.registerSimpleItem("washed_aluminum");
    public static final DeferredItem<Item> WASHED_OSMIUM = ITEMS.registerSimpleItem("washed_osmium");
    public static final DeferredItem<Item> WASHED_URANIUM = ITEMS.registerSimpleItem("washed_uranium");

    // Purified ores (Ore Purifier: 1 washed -> 1 purified, sometimes a by-product; granulate like washed ore)
    public static final DeferredItem<Item> PURIFIED_IRON = ITEMS.registerSimpleItem("purified_iron");
    public static final DeferredItem<Item> PURIFIED_COPPER = ITEMS.registerSimpleItem("purified_copper");
    public static final DeferredItem<Item> PURIFIED_GOLD = ITEMS.registerSimpleItem("purified_gold");
    public static final DeferredItem<Item> PURIFIED_TIN = ITEMS.registerSimpleItem("purified_tin");
    public static final DeferredItem<Item> PURIFIED_LEAD = ITEMS.registerSimpleItem("purified_lead");
    public static final DeferredItem<Item> PURIFIED_SILVER = ITEMS.registerSimpleItem("purified_silver");
    public static final DeferredItem<Item> PURIFIED_NICKEL = ITEMS.registerSimpleItem("purified_nickel");
    public static final DeferredItem<Item> PURIFIED_ZINC = ITEMS.registerSimpleItem("purified_zinc");
    public static final DeferredItem<Item> PURIFIED_ALUMINUM = ITEMS.registerSimpleItem("purified_aluminum");
    public static final DeferredItem<Item> PURIFIED_OSMIUM = ITEMS.registerSimpleItem("purified_osmium");
    public static final DeferredItem<Item> PURIFIED_URANIUM = ITEMS.registerSimpleItem("purified_uranium");
    // Tier 5: Ore Concentrate from the Froth Flotation Cell (4 per raw ore)
    public static final DeferredItem<Item> CONCENTRATE_IRON = ITEMS.registerSimpleItem("concentrate_iron");
    public static final DeferredItem<Item> CONCENTRATE_COPPER = ITEMS.registerSimpleItem("concentrate_copper");
    public static final DeferredItem<Item> CONCENTRATE_GOLD = ITEMS.registerSimpleItem("concentrate_gold");
    public static final DeferredItem<Item> CONCENTRATE_TIN = ITEMS.registerSimpleItem("concentrate_tin");
    public static final DeferredItem<Item> CONCENTRATE_LEAD = ITEMS.registerSimpleItem("concentrate_lead");
    public static final DeferredItem<Item> CONCENTRATE_SILVER = ITEMS.registerSimpleItem("concentrate_silver");
    public static final DeferredItem<Item> CONCENTRATE_NICKEL = ITEMS.registerSimpleItem("concentrate_nickel");
    public static final DeferredItem<Item> CONCENTRATE_ZINC = ITEMS.registerSimpleItem("concentrate_zinc");
    public static final DeferredItem<Item> CONCENTRATE_ALUMINUM = ITEMS.registerSimpleItem("concentrate_aluminum");
    public static final DeferredItem<Item> CONCENTRATE_OSMIUM = ITEMS.registerSimpleItem("concentrate_osmium");
    public static final DeferredItem<Item> CONCENTRATE_URANIUM = ITEMS.registerSimpleItem("concentrate_uranium");

    // Crafting components
    public static final DeferredItem<Item> ELECTRONIC_CIRCUIT = ITEMS.registerSimpleItem("electronic_circuit");
    /** Tier-3 recipes use it; the Circuit Assembler makes it from an Electronic Circuit and plasma. */
    public static final DeferredItem<Item> PLASMA_CIRCUIT = ITEMS.registerSimpleItem("plasma_circuit");
    // Tier 4: the "plasma-forged parts" (Circuit Assembler) and the reactor's fuel (Pellet Press)
    public static final DeferredItem<Item> FUSION_CIRCUIT = ITEMS.registerSimpleItem("fusion_circuit");
    public static final DeferredItem<Item> ANTIMATTER_CIRCUIT = ITEMS.registerSimpleItem("antimatter_circuit");
    public static final DeferredItem<Item> REACTOR_PLATING = ITEMS.registerSimpleItem("reactor_plating");
    public static final DeferredItem<Item> FUSION_FUEL_PELLET = ITEMS.registerSimpleItem("fusion_fuel_pellet");
    /** Doubles an Atmospheric Water Generator's output; wears down by 1 per 100 mB of water it makes. */
    public static final DeferredItem<Item> AIR_FILTER = ITEMS.registerSimpleItem("air_filter",
            properties -> properties.durability(AtmosphericWaterGeneratorBlockEntity.FILTER_DURABILITY));

    // Tools
    public static final DeferredItem<WrenchItem> WRENCH = ITEMS.registerItem("wrench", WrenchItem::new, properties -> properties.stacksTo(1));

    // Machine block items
    public static final DeferredItem<BlockItem> COAL_GRANULATOR = ITEMS.registerSimpleBlockItem("coal_granulator", ModBlocks.COAL_GRANULATOR);
    public static final DeferredItem<BlockItem> COAL_SMELTER = ITEMS.registerSimpleBlockItem("coal_smelter", ModBlocks.COAL_SMELTER);
    public static final DeferredItem<BlockItem> ELECTRIC_GRANULATOR = ITEMS.registerSimpleBlockItem("electric_granulator", ModBlocks.ELECTRIC_GRANULATOR);
    public static final DeferredItem<BlockItem> ELECTRIC_SMELTER = ITEMS.registerSimpleBlockItem("electric_smelter", ModBlocks.ELECTRIC_SMELTER);
    public static final DeferredItem<BlockItem> ELECTRIC_GENERATOR = ITEMS.registerSimpleBlockItem("electric_generator", ModBlocks.ELECTRIC_GENERATOR);
    public static final DeferredItem<EnergyBatteryItem> ENERGY_BATTERY = ITEMS.registerItem("energy_battery",
            properties -> new EnergyBatteryItem(ModBlocks.ENERGY_BATTERY.get(), properties, EnergyBatteryBlockEntity.CAPACITY, 0x40C4FF),
            properties -> properties.useBlockDescriptionPrefix());
    public static final DeferredItem<BlockItem> ATMOSPHERIC_WATER_GENERATOR =
            ITEMS.registerSimpleBlockItem("atmospheric_water_generator", ModBlocks.ATMOSPHERIC_WATER_GENERATOR);
    public static final DeferredItem<BlockItem> ELECTROLYSER = ITEMS.registerSimpleBlockItem("electrolyser", ModBlocks.ELECTROLYSER);
    public static final DeferredItem<BlockItem> PLASMA_GENERATOR = ITEMS.registerSimpleBlockItem("plasma_generator", ModBlocks.PLASMA_GENERATOR);
    public static final DeferredItem<BlockItem> MHD_GENERATOR = ITEMS.registerSimpleBlockItem("mhd_generator", ModBlocks.MHD_GENERATOR);
    public static final DeferredItem<BlockItem> PLASMA_GRANULATOR = ITEMS.registerSimpleBlockItem("plasma_granulator", ModBlocks.PLASMA_GRANULATOR);
    public static final DeferredItem<BlockItem> PLASMA_SMELTER = ITEMS.registerSimpleBlockItem("plasma_smelter", ModBlocks.PLASMA_SMELTER);
    public static final DeferredItem<EnergyBatteryItem> PLASMA_ENERGY_BATTERY = ITEMS.registerItem("plasma_energy_battery",
            properties -> new EnergyBatteryItem(ModBlocks.PLASMA_ENERGY_BATTERY.get(), properties, EnergyBatteryBlockEntity.PLASMA_CAPACITY, 0xFF50D2),
            properties -> properties.useBlockDescriptionPrefix());
    public static final DeferredItem<BlockItem> CIRCUIT_ASSEMBLER = ITEMS.registerSimpleBlockItem("circuit_assembler", ModBlocks.CIRCUIT_ASSEMBLER);
    public static final DeferredItem<BlockItem> ORE_WASHER = ITEMS.registerSimpleBlockItem("ore_washer", ModBlocks.ORE_WASHER);
    public static final DeferredItem<BlockItem> ISOTOPE_SEPARATOR = ITEMS.registerSimpleBlockItem("isotope_separator", ModBlocks.ISOTOPE_SEPARATOR);
    public static final DeferredItem<BlockItem> TRITIUM_BREEDER = ITEMS.registerSimpleBlockItem("tritium_breeder", ModBlocks.TRITIUM_BREEDER);
    public static final DeferredItem<BlockItem> PELLET_PRESS = ITEMS.registerSimpleBlockItem("pellet_press", ModBlocks.PELLET_PRESS);
    public static final DeferredItem<BlockItem> REACTOR_CASING = ITEMS.registerSimpleBlockItem("reactor_casing", ModBlocks.REACTOR_CASING);
    public static final DeferredItem<BlockItem> REACTOR_GLASS = ITEMS.registerSimpleBlockItem("reactor_glass", ModBlocks.REACTOR_GLASS);
    public static final DeferredItem<BlockItem> REACTOR_PORT = ITEMS.registerSimpleBlockItem("reactor_port", ModBlocks.REACTOR_PORT);
    public static final DeferredItem<BlockItem> REACTOR_CONTROLLER = ITEMS.registerSimpleBlockItem("reactor_controller", ModBlocks.REACTOR_CONTROLLER);
    public static final DeferredItem<BlockItem> FUSION_CABLE = ITEMS.registerSimpleBlockItem("fusion_cable", ModBlocks.FUSION_CABLE);
    public static final DeferredItem<EnergyBatteryItem> FUSION_ENERGY_BATTERY = ITEMS.registerItem("fusion_energy_battery",
            properties -> new EnergyBatteryItem(ModBlocks.FUSION_ENERGY_BATTERY.get(), properties, EnergyBatteryBlockEntity.FUSION_CAPACITY, 0x60FF80),
            properties -> properties.useBlockDescriptionPrefix());
    public static final DeferredItem<BlockItem> ORE_PURIFIER = ITEMS.registerSimpleBlockItem("ore_purifier", ModBlocks.ORE_PURIFIER);
    public static final DeferredItem<BlockItem> CREATIVE_ENERGY_BATTERY =
            ITEMS.registerSimpleBlockItem("creative_energy_battery", ModBlocks.CREATIVE_ENERGY_BATTERY);
    public static final DeferredItem<BlockItem> CREATIVE_FLUID_TANK = ITEMS.registerSimpleBlockItem("creative_fluid_tank", ModBlocks.CREATIVE_FLUID_TANK);
    public static final DeferredItem<BlockItem> SOARYN_CHEST = ITEMS.registerSimpleBlockItem("soaryn_chest", ModBlocks.SOARYN_CHEST);
    public static final DeferredItem<BlockItem> CONTAINMENT_CELL = ITEMS.registerSimpleBlockItem("containment_cell", ModBlocks.CONTAINMENT_CELL);
    public static final DeferredItem<BlockItem> MAGNETIC_CONDUIT = ITEMS.registerSimpleBlockItem("magnetic_conduit", ModBlocks.MAGNETIC_CONDUIT);
    public static final DeferredItem<BlockItem> ANTIMATTER_GRANULATOR = ITEMS.registerSimpleBlockItem("antimatter_granulator", ModBlocks.ANTIMATTER_GRANULATOR);
    public static final DeferredItem<BlockItem> ANTIMATTER_SMELTER = ITEMS.registerSimpleBlockItem("antimatter_smelter", ModBlocks.ANTIMATTER_SMELTER);
    public static final DeferredItem<BlockItem> FROTH_FLOTATION_CELL = ITEMS.registerSimpleBlockItem("froth_flotation_cell", ModBlocks.FROTH_FLOTATION_CELL);
    public static final DeferredItem<BlockItem> ANTIMATTER_WASHER = ITEMS.registerSimpleBlockItem("antimatter_washer", ModBlocks.ANTIMATTER_WASHER);
    public static final DeferredItem<BlockItem> ANTIMATTER_PURIFIER = ITEMS.registerSimpleBlockItem("antimatter_purifier", ModBlocks.ANTIMATTER_PURIFIER);
    public static final DeferredItem<BlockItem> ANNIHILATION_GENERATOR = ITEMS.registerSimpleBlockItem("annihilation_generator", ModBlocks.ANNIHILATION_GENERATOR);
    public static final DeferredItem<BlockItem> ANTIMATTER_CABLE = ITEMS.registerSimpleBlockItem("antimatter_cable", ModBlocks.ANTIMATTER_CABLE);
    public static final DeferredItem<EnergyBatteryItem> ANTIMATTER_ENERGY_BATTERY = ITEMS.registerItem("antimatter_energy_battery",
            properties -> new EnergyBatteryItem(ModBlocks.ANTIMATTER_ENERGY_BATTERY.get(), properties, EnergyBatteryBlockEntity.ANTIMATTER_CAPACITY, 0xD6E4FF),
            properties -> properties.useBlockDescriptionPrefix());
    public static final DeferredItem<BlockItem> ACCELERATOR_SEGMENT = ITEMS.registerSimpleBlockItem("accelerator_segment", ModBlocks.ACCELERATOR_SEGMENT);
    public static final DeferredItem<BlockItem> ACCELERATOR_PORT = ITEMS.registerSimpleBlockItem("accelerator_port", ModBlocks.ACCELERATOR_PORT);
    public static final DeferredItem<BlockItem> ACCELERATOR_CONTROLLER = ITEMS.registerSimpleBlockItem("accelerator_controller", ModBlocks.ACCELERATOR_CONTROLLER);
    public static final DeferredItem<BlockItem> CONTAINMENT_FRAME = ITEMS.registerSimpleBlockItem("containment_frame", ModBlocks.CONTAINMENT_FRAME);
    public static final DeferredItem<BlockItem> HORIZON_CONTROLLER = ITEMS.registerSimpleBlockItem("horizon_controller", ModBlocks.HORIZON_CONTROLLER);
    public static final DeferredItem<BlockItem> HORIZON_PORT = ITEMS.registerSimpleBlockItem("horizon_port", ModBlocks.HORIZON_PORT);
    public static final DeferredItem<BlockItem> PLASMA_CABLE = ITEMS.registerSimpleBlockItem("plasma_cable", ModBlocks.PLASMA_CABLE);
    public static final DeferredItem<BlockItem> FLUID_PIPE = ITEMS.registerSimpleBlockItem("fluid_pipe", ModBlocks.FLUID_PIPE);
    public static final DeferredItem<BlockItem> FLUID_TANK = ITEMS.registerSimpleBlockItem("fluid_tank", ModBlocks.FLUID_TANK);
    public static final DeferredItem<BlockItem> ENERGY_CABLE = ITEMS.registerSimpleBlockItem("energy_cable", ModBlocks.ENERGY_CABLE);

    // Block items for every decorative stone block
    static {
        ModBlocks.STONE_SETS.forEach(set -> set.all().forEach(ITEMS::registerSimpleBlockItem));
    }

    private ModItems() {}
}
