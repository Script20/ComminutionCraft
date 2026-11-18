package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.registry.ModBlocks;
import com.script.comminutioncraft.common.registry.ModItems;
import java.util.List;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import org.jspecify.annotations.Nullable;

/**
 * Every factory (owner, 2026-11-18): each machine after a tier's multiplying step gets one with as many lanes as that
 * step multiplies (tier 2: 2, tier 3 after the Ore Washer: 3, tier 5 after the Froth Flotation Cell: 4). A Factory
 * Upgrade Kit of the tier turns the placed machine into its factory; the kit + machine also craft it.
 */
public final class Factories {
    /** {@code kit} turns a placed {@code machine} into {@code factory}. */
    public record Factory(DeferredBlock<?> machine, DeferredBlock<?> factory, DeferredItem<?> kit, int lanes) {
    }

    public static final List<Factory> ALL = List.of(
            new Factory(ModBlocks.ELECTRIC_SMELTER, ModBlocks.ELECTRIC_SMELTING_FACTORY, ModItems.ELECTRIC_FACTORY_KIT, 2),
            new Factory(ModBlocks.PLASMA_GRANULATOR, ModBlocks.PLASMA_GRANULATING_FACTORY, ModItems.PLASMA_FACTORY_KIT, 3),
            new Factory(ModBlocks.PLASMA_SMELTER, ModBlocks.PLASMA_SMELTING_FACTORY, ModItems.PLASMA_FACTORY_KIT, 3),
            new Factory(ModBlocks.ORE_PURIFIER, ModBlocks.PURIFYING_FACTORY, ModItems.FUSION_FACTORY_KIT, 3),
            new Factory(ModBlocks.ANTIMATTER_WASHER, ModBlocks.ANTIMATTER_WASHING_FACTORY, ModItems.ANTIMATTER_FACTORY_KIT, 4),
            new Factory(ModBlocks.ANTIMATTER_PURIFIER, ModBlocks.ANTIMATTER_PURIFYING_FACTORY, ModItems.ANTIMATTER_FACTORY_KIT, 4),
            new Factory(ModBlocks.ANTIMATTER_GRANULATOR, ModBlocks.ANTIMATTER_GRANULATING_FACTORY, ModItems.ANTIMATTER_FACTORY_KIT, 4),
            new Factory(ModBlocks.ANTIMATTER_SMELTER, ModBlocks.ANTIMATTER_SMELTING_FACTORY, ModItems.ANTIMATTER_FACTORY_KIT, 4));

    private Factories() {
    }

    /** The factory this kit makes out of this machine, or null. */
    public static @Nullable Factory upgrade(Block machine, Item kit) {
        for (Factory factory : ALL) {
            if (factory.machine().get() == machine && factory.kit().get() == kit) {
                return factory;
            }
        }
        return null;
    }

    /** The factory entry of a factory block, or null. */
    public static @Nullable Factory forFactory(Block block) {
        for (Factory factory : ALL) {
            if (factory.factory().get() == block) {
                return factory;
            }
        }
        return null;
    }

    /** The machines a kit upgrades (for its tooltip). */
    public static List<Factory> forKit(Item kit) {
        return ALL.stream().filter(factory -> factory.kit().get() == kit).toList();
    }
}
