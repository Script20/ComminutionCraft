package com.script.comminutioncraft.registry;

import com.script.comminutioncraft.ComminutionCraft;
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

    // Machine block items
    public static final DeferredItem<BlockItem> COAL_GRANULATOR = ITEMS.registerSimpleBlockItem("coal_granulator", ModBlocks.COAL_GRANULATOR);
    public static final DeferredItem<BlockItem> COAL_SMELTER = ITEMS.registerSimpleBlockItem("coal_smelter", ModBlocks.COAL_SMELTER);

    private ModItems() {}
}
