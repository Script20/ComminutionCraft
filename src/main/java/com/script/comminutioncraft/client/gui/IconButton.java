package com.script.comminutioncraft.client.gui;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * A 20x20 vanilla button with a 16x16 icon sprite; the icon and the hover text follow the setting it shows (the
 * click cycles it). Greyed out (and unclickable) while {@code active} says no.
 */
public class IconButton extends AbstractButton {
    public static final int SIZE = 20;

    private final Supplier<Identifier> icon;
    private final Supplier<Component> tooltip;
    private final BooleanSupplier enabled;
    private final Runnable onPress;
    private Component shownTooltip;

    public IconButton(int x, int y, Supplier<Identifier> icon, Supplier<Component> tooltip, BooleanSupplier enabled, Runnable onPress) {
        super(x, y, SIZE, SIZE, tooltip.get());
        this.icon = icon;
        this.tooltip = tooltip;
        this.enabled = enabled;
        this.onPress = onPress;
    }

    @Override
    public void onPress(InputWithModifiers input) {
        onPress.run();
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        active = enabled.getAsBoolean();
        Component current = tooltip.get();
        if (!current.equals(shownTooltip)) {
            shownTooltip = current;
            setMessage(current);
            setTooltip(Tooltip.create(current));
        }
        extractDefaultSprite(graphics);
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, icon.get(), getX() + 2, getY() + 2, 16, 16, active ? 1.0F : 0.4F);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
