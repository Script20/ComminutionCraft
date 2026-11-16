package com.script.comminutioncraft.common.item.tool;

import com.script.comminutioncraft.common.data.ProcessedMetal;
import com.script.comminutioncraft.common.registry.ModItems;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.level.BlockDropsEvent;
import org.jspecify.annotations.Nullable;

/**
 * The Ore Hammer (an iron-level pickaxe): an ore broken with it drops granulated ore instead of raw ore, two for each
 * raw ore it would have dropped - ore doubling by hand, before the Coal Granulator.
 */
public final class OreHammer {
    public static void onBlockDrops(BlockDropsEvent event) {
        if (!event.getTool().is(ModItems.ORE_HAMMER.get()) || !event.getState().is(Tags.Blocks.ORES)) {
            return;
        }
        for (ItemEntity drop : event.getDrops()) {
            ItemStack stack = drop.getItem();
            ProcessedMetal metal = rawMetal(stack);
            if (metal != null) {
                drop.setItem(new ItemStack(metal.granulated().get(), stack.getCount() * 2));
            }
        }
    }

    private static @Nullable ProcessedMetal rawMetal(ItemStack stack) {
        for (ProcessedMetal metal : ProcessedMetal.ALL) {
            if (stack.is(ProcessedMetal.commonItemTag("raw_materials/" + metal.name()))) {
                return metal;
            }
        }
        return null;
    }

    private OreHammer() {}
}
