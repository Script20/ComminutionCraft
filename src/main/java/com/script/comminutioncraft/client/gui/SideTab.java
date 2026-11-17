package com.script.comminutioncraft.client.gui;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.InputWithModifiers;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

/**
 * A tab on a GUI's left edge (the mirror of {@link MachineTab}): an item icon, a seam on the outer edge that lights
 * when it's the selected tab; greyed out while it can't be picked.
 */
public class SideTab extends AbstractButton {
    public static final int SIZE = 22;

    private static final int OUTLINE = 0xFF101114;
    private static final int FILL_TOP = 0xFF3C4049;
    private static final int FILL_BOTTOM = 0xFF2A2D34;
    private static final int OFF_TOP = 0xFF26282E;
    private static final int OFF_BOTTOM = 0xFF1E2025;
    private static final int HIGHLIGHT = 0xFF545964;

    private final Supplier<ItemStack> icon;
    private final Supplier<Component> tooltip;
    private final BooleanSupplier enabled;
    private final BooleanSupplier selected;
    private final int seamColor;
    private final Runnable onPress;
    /** Drawn instead of the icon when there's none (e.g. "N"). */
    private final Component letter;
    private Component shownTooltip;

    public SideTab(int x, int y, Supplier<ItemStack> icon, Component letter, Supplier<Component> tooltip, BooleanSupplier enabled,
                   BooleanSupplier selected, int seamColor, Runnable onPress) {
        super(x, y, SIZE, SIZE, tooltip.get());
        this.letter = letter;
        this.icon = icon;
        this.tooltip = tooltip;
        this.enabled = enabled;
        this.selected = selected;
        this.seamColor = seamColor;
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
        int x = getX();
        int y = getY();
        int x1 = x + width;
        int y1 = y + height;
        // Tab body: the right edge tucks under the GUI's left edge.
        graphics.fill(x + 1, y + 1, x1, y1 - 1, OUTLINE);
        graphics.fill(x + 2, y, x1, y + 1, OUTLINE);
        graphics.fill(x + 2, y1 - 1, x1, y1, OUTLINE);
        graphics.fillGradient(x + 2, y + 1, x1, y1 - 1, active ? FILL_TOP : OFF_TOP, active ? FILL_BOTTOM : OFF_BOTTOM);
        graphics.fill(x + 2, y + 1, x1, y + 2, HIGHLIGHT);
        if (selected.getAsBoolean()) {
            graphics.fill(x + 1, y + 3, x + 2, y1 - 3, seamColor);
        } else if (active && isHoveredOrFocused()) {
            graphics.fill(x + 1, y + 3, x + 2, y1 - 3, (seamColor & 0x00FFFFFF) | 0x80000000);
        }
        ItemStack stack = icon.get();
        if (!stack.isEmpty()) {
            graphics.item(stack, x + 4, y + 3);
        } else {
            Font font = Minecraft.getInstance().font;
            graphics.text(font, letter, x + 12 - font.width(letter) / 2, y + 7, 0xFF50545E, false);
        }
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        defaultButtonNarrationText(output);
    }
}
