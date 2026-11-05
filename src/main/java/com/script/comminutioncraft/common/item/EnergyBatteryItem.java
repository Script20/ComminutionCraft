package com.script.comminutioncraft.common.item;

import com.script.comminutioncraft.common.registry.ModDataComponents;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

/** Battery block item: a charge bar for the FE kept from a wrench pickup (the FE line is in {@code ModTooltips}). */
public class EnergyBatteryItem extends BlockItem {
    private final int capacity;
    private final int barColor;

    /** {@code barColor} is the tier colour (RGB). */
    public EnergyBatteryItem(Block block, Properties properties, int capacity, int barColor) {
        super(block, properties);
        this.capacity = capacity;
        this.barColor = barColor;
    }

    public int capacity() {
        return capacity;
    }

    public static int stored(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.STORED_ENERGY.get(), 0);
    }

    @Override
    public boolean isBarVisible(ItemStack stack) {
        return stored(stack) > 0;
    }

    @Override
    public int getBarWidth(ItemStack stack) {
        return Mth.clamp(Math.round(13.0F * stored(stack) / capacity), 0, 13);
    }

    @Override
    public int getBarColor(ItemStack stack) {
        return barColor;
    }
}
