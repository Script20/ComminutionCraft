package com.script.comminutioncraft.common.registry;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.item.EnergyBatteryItem;
import com.script.comminutioncraft.common.item.WrenchItem;
import com.script.comminutioncraft.common.machine.EnergyBatteryBlockEntity;
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

    // Crafting components
    public static final DeferredItem<Item> ELECTRONIC_CIRCUIT = ITEMS.registerSimpleItem("electronic_circuit");

    // Tools
    public static final DeferredItem<WrenchItem> WRENCH = ITEMS.registerItem("wrench", WrenchItem::new, properties -> properties.stacksTo(1));

    // Machine block items
    public static final DeferredItem<BlockItem> COAL_GRANULATOR = ITEMS.registerSimpleBlockItem("coal_granulator", ModBlocks.COAL_GRANULATOR);
    public static final DeferredItem<BlockItem> COAL_SMELTER = ITEMS.registerSimpleBlockItem("coal_smelter", ModBlocks.COAL_SMELTER);
    public static final DeferredItem<BlockItem> ELECTRIC_GRANULATOR = ITEMS.registerSimpleBlockItem("electric_granulator", ModBlocks.ELECTRIC_GRANULATOR);
    public static final DeferredItem<BlockItem> ELECTRIC_SMELTER = ITEMS.registerSimpleBlockItem("electric_smelter", ModBlocks.ELECTRIC_SMELTER);
    public static final DeferredItem<BlockItem> ELECTRIC_GENERATOR = ITEMS.registerSimpleBlockItem("electric_generator", ModBlocks.ELECTRIC_GENERATOR);
    public static final DeferredItem<EnergyBatteryItem> ENERGY_BATTERY = ITEMS.registerItem("energy_battery",
            properties -> new EnergyBatteryItem(ModBlocks.ENERGY_BATTERY.get(), properties, EnergyBatteryBlockEntity.CAPACITY),
            properties -> properties.useBlockDescriptionPrefix());
    public static final DeferredItem<BlockItem> ENERGY_CABLE = ITEMS.registerSimpleBlockItem("energy_cable", ModBlocks.ENERGY_CABLE);

    // Block items for every decorative stone block
    static {
        ModBlocks.STONE_SETS.forEach(set -> set.all().forEach(ITEMS::registerSimpleBlockItem));
    }

    private ModItems() {}
}
