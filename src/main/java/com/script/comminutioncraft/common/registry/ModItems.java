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

    // Crafting components
    public static final DeferredItem<Item> ELECTRONIC_CIRCUIT = ITEMS.registerSimpleItem("electronic_circuit");
    /** Tier-3 recipes use it; the Circuit Assembler makes it from an Electronic Circuit and plasma. */
    public static final DeferredItem<Item> PLASMA_CIRCUIT = ITEMS.registerSimpleItem("plasma_circuit");
    // Tier 4: the "plasma-forged parts" (Circuit Assembler) and the reactor's fuel (Pellet Press)
    public static final DeferredItem<Item> FUSION_CIRCUIT = ITEMS.registerSimpleItem("fusion_circuit");
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
