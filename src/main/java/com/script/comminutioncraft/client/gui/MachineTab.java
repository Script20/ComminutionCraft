package com.script.comminutioncraft.client.gui;

import java.util.function.Supplier;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * A Dark Chassis tab on the right edge of a machine GUI, showing an item icon. The icon and tooltip are suppliers,
 * so they follow the machine's current settings (e.g. the redstone mode).
 */
public class MachineTab extends AbstractButton {
    public static final int SIZE = 22;

    private static final int OUTLINE = 0xFF101114;
    private static final int FILL_TOP = 0xFF3C4049;
    private static final int FILL_BOTTOM = 0xFF2A2D34;
    private static final int HIGHLIGHT = 0xFF545964;
    private static final int SHADOW = 0xFF1C1E23;

    private final Supplier<ItemStack> icon;
    private final Supplier<Component> tooltip;
    private final int seamColor;
    private final Runnable onPress;
    private Component shownTooltip;

    public MachineTab(int x, int y, Supplier<ItemStack> icon, Supplier<Component> tooltip, int seamColor, Runnable onPress) {
        super(x, y, SIZE, SIZE, tooltip.get());
        this.icon = icon;
        this.tooltip = tooltip;
        this.seamColor = seamColor;
        this.onPress = onPress;
    }

    @Override
    public void onPress(InputWithModifiers input) {
        onPress.run();
    }

    @Override
    protected void extractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        Component current = tooltip.get();
        if (!current.equals(shownTooltip)) {
            shownTooltip = current;
            setMessage(current);
            setTooltip(Tooltip.create(current));
        }

        int x = getX();
        int y = getY();
        int x1 = x + width;
        int y1 = y + height;
        // Tab body: rounded outline, gradient fill, bevel; the left edge tucks under the GUI's right edge.
        graphics.fill(x, y + 1, x1 - 1, y1 - 1, OUTLINE);
        graphics.fill(x, y, x1 - 2, y + 1, OUTLINE);
        graphics.fill(x, y1 - 1, x1 - 2, y1, OUTLINE);
        graphics.fillGradient(x, y + 1, x1 - 2, y1 - 1, FILL_TOP, FILL_BOTTOM);
        graphics.fill(x, y + 1, x1 - 2, y + 2, HIGHLIGHT);
        graphics.fill(x1 - 3, y + 2, x1 - 2, y1 - 1, SHADOW);
        // Tier-coloured seam on the outer edge, brighter when hovered
        graphics.fill(x1 - 2, y + 3, x1 - 1, y1 - 3, isHoveredOrFocused() ? seamColor : (seamColor & 0x00FFFFFF) | 0x80000000);

        graphics.item(icon.get(), x + 2, y + 3);
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
