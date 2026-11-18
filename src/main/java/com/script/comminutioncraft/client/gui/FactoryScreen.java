package com.script.comminutioncraft.client.gui;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.machine.FactoryMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;

/**
 * Every factory's GUI: the energy tube, one column per lane (input, a progress bar filling downwards in the tier
 * colour, output), the purifiers' by-product slot and the water / oxygen gauge. The tier follows the lane count
 * (2 Electric, 3 Plasma or Fusion for the purifier, 4 Antimatter).
 */
public class FactoryScreen extends MachineScreen<FactoryMenu> {
    private static final int SLOT = 0xFF16181C;
    private static final int SLOT_DARK = 0xFF101114;
    private static final int SLOT_LIGHT = 0xFF545964;
    private static final int TRACK = 0xFF1E2025;
    private static final int BAR_TOP = FactoryMenu.INPUT_Y + 19;
    private static final int BAR_HEIGHT = FactoryMenu.OUTPUT_Y - 3 - BAR_TOP;

    public FactoryScreen(FactoryMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, ComminutionCraft.id("textures/gui/container/" + (menu.layout().fluid() != 0 ? "factory_fluid" : "factory") + ".png"),
                tierOf(menu.layout()));
    }

    private static Tier tierOf(FactoryMenu.Layout layout) {
        return switch (layout.lanes()) {
            case 2 -> Tier.ELECTRIC;
            case 4 -> Tier.ANTIMATTER;
            default -> layout.byproduct() ? Tier.FUSION : Tier.PLASMA;
        };
    }

    @Override
    protected boolean isActive() {
        return menu.isWorking();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        drawEnergyBar(graphics, menu.getEnergy(), menu.getEnergyCapacity());
        FactoryMenu.Layout layout = menu.layout();
        for (int lane = 0; lane < layout.lanes(); lane++) {
            int x = leftPos + layout.columnX(lane);
            slotFrame(graphics, x, topPos + FactoryMenu.INPUT_Y);
            slotFrame(graphics, x, topPos + FactoryMenu.OUTPUT_Y);
            int barX = x + 6;
            int barY = topPos + BAR_TOP;
            graphics.fill(barX, barY, barX + 4, barY + BAR_HEIGHT, TRACK);
            int filled = Mth.ceil(menu.getProgress(lane) * BAR_HEIGHT);
            if (filled > 0) {
                graphics.fill(barX, barY, barX + 4, barY + filled, tier.seamOn);
            }
        }
        if (layout.byproduct()) {
            slotFrame(graphics, leftPos + layout.columnX(layout.lanes()), topPos + FactoryMenu.OUTPUT_Y);
        }
        if (layout.fluid() != 0) {
            drawGauge(graphics, PLASMA_X, layout.fluid() == 1 ? WATER_FLUID : OXYGEN_FLUID, menu.getFluid(), menu.getFluidCapacity());
        }
    }

    /** A recessed 18x18 slot round an item at (x, y). */
    private static void slotFrame(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.fill(x - 1, y - 1, x + 17, y + 17, SLOT);
        graphics.fill(x - 1, y - 1, x + 16, y, SLOT_DARK);
        graphics.fill(x - 1, y - 1, x, y + 16, SLOT_DARK);
        graphics.fill(x, y + 16, x + 17, y + 17, SLOT_LIGHT);
        graphics.fill(x + 16, y, x + 17, y + 17, SLOT_LIGHT);
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);
        energyTooltip(graphics, mouseX, mouseY, menu.getEnergy(), menu.getEnergyCapacity());
        FactoryMenu.Layout layout = menu.layout();
        if (layout.fluid() != 0) {
            gaugeTooltip(graphics, mouseX, mouseY, PLASMA_X, layout.fluid() == 1 ? "gui.comminutioncraft.water" : "gui.comminutioncraft.oxygen",
                    menu.getFluid(), menu.getFluidCapacity());
        }
    }
}
