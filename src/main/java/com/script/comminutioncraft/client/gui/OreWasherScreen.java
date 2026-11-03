package com.script.comminutioncraft.client.gui;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.machine.OreWasherMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Ore Washer GUI: the tier-3 machine screen plus a water gauge left of the plasma one (fill sprite at (196, 31)). */
public class OreWasherScreen extends PlasmaMachineScreen {
    private static final int WATER_X = 141;

    private final OreWasherMenu washer;

    public OreWasherScreen(OreWasherMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, ComminutionCraft.id("textures/gui/container/ore_washer.png"));
        this.washer = menu;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        int capacity = washer.getWaterCapacity();
        int amount = washer.getWater();
        if (capacity > 0 && amount > 0) {
            int height = Math.max(1, Math.round((float) ENERGY_H * Math.min(amount, capacity) / capacity));
            graphics.blit(RenderPipelines.GUI_TEXTURED, texture,
                    leftPos + WATER_X, topPos + ENERGY_Y + ENERGY_H - height,
                    196, 31 + ENERGY_H - height,
                    ENERGY_W, height, TEX_SIZE, TEX_SIZE);
        }
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);
        if (isHovering(WATER_X, ENERGY_Y, ENERGY_W, ENERGY_H, mouseX, mouseY)) {
            graphics.setTooltipForNextFrame(Component.translatable("gui.comminutioncraft.water",
                    formatFe(washer.getWater()), formatFe(washer.getWaterCapacity())), mouseX, mouseY);
        }
    }
}
