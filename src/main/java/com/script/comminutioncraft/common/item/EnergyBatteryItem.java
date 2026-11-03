package com.script.comminutioncraft.common.item;

import com.script.comminutioncraft.common.registry.ModDataComponents;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.block.Block;

/** Battery block item: a charge bar and "Stored: x / y FE" for the FE kept from a broken battery. */
public class EnergyBatteryItem extends BlockItem {
    private static final int BAR_COLOR = 0x40C4FF;

    private final int capacity;

    public EnergyBatteryItem(Block block, Properties properties, int capacity) {
        super(block, properties);
        this.capacity = capacity;
    }

    private static int stored(ItemStack stack) {
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
        return BAR_COLOR;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display,
                                Consumer<Component> builder, TooltipFlag flag) {
        builder.accept(Component.translatable("gui.comminutioncraft.energy",
                String.format(Locale.ROOT, "%,d", stored(stack)), String.format(Locale.ROOT, "%,d", capacity))
                .withStyle(ChatFormatting.GRAY));
    }
}
