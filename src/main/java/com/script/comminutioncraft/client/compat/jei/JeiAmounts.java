package com.script.comminutioncraft.client.compat.jei;

import mezz.jei.api.gui.placement.HorizontalAlignment;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import net.minecraft.network.chat.Component;

/** Draws a fluid slot's amount over its bottom-right corner, like an item stack's count ("250", "1k"). */
final class JeiAmounts {
    static void label(IRecipeExtrasBuilder builder, int slotX, int slotY, int millibuckets) {
        String text = millibuckets >= 1_000 && millibuckets % 1_000 == 0 ? millibuckets / 1_000 + "k" : Integer.toString(millibuckets);
        builder.addText(Component.literal(text), 18, 9)
                .setPosition(slotX - 1, slotY + 9)
                .setTextAlignment(HorizontalAlignment.RIGHT)
                .setColor(0xFFFFFFFF)
                .setShadow(true);
    }

    private JeiAmounts() {}
}
