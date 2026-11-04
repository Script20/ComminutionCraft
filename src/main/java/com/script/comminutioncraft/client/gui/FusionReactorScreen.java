package com.script.comminutioncraft.client.gui;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.machine.FusionReactorBlockEntity;
import com.script.comminutioncraft.common.machine.FusionReactorMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * GUI for the Fusion Reactor controller: energy (left), plasma for ignition (x 50), tritium (right), the pellet slot,
 * a core-heat bar and the status. Sheet layout: see tools/gui_dark_chassis.py ({@code fusion_reactor_sheet}).
 */
public class FusionReactorScreen extends MachineScreen<FusionReactorMenu> {
    private static final Identifier TEXTURE = ComminutionCraft.id("textures/gui/container/reactor_controller.png");
    private static final int LABEL_COLOR = 0xFF9EA4B0;
    private static final int GOOD_COLOR = 0xFF60FF80;
    private static final int WARNING_COLOR = 0xFFFFB04A;
    private static final int HEAT_X = 65;
    private static final int HEAT_Y = 20;
    private static final int HEAT_W = 47;
    private static final int HEAT_H = 5;

    public FusionReactorScreen(FusionReactorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, TEXTURE, Tier.FUSION);
    }

    @Override
    protected boolean isActive() {
        return menu.getRate() > 0;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        drawEnergyBar(graphics, menu.getEnergy(), menu.getEnergyCapacity());
        drawGauge(graphics, 50, PLASMA_U, menu.getPlasma(), menu.getPlasmaCapacity());
        drawGauge(graphics, PLASMA_X, TRITIUM_U, menu.getTritium(), menu.getTritiumCapacity());
        int width = Math.round((float) HEAT_W * menu.getHeat() / FusionReactorBlockEntity.MAX_HEAT);
        if (width > 0) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, texture, leftPos + HEAT_X, topPos + HEAT_Y, 176, 90, width, HEAT_H, TEX_SIZE, TEX_SIZE);
        }
    }

    private Component status() {
        if (!menu.isFormed()) {
            return Component.translatable("gui.comminutioncraft.reactor.not_formed");
        }
        if (menu.getRate() > 0) {
            return Component.translatable("gui.comminutioncraft.reactor.running");
        }
        if (menu.getHeat() > 0 && menu.getHeat() < FusionReactorBlockEntity.MAX_HEAT) {
            return Component.translatable("gui.comminutioncraft.reactor.heating", menu.getHeat());
        }
        return Component.translatable("gui.comminutioncraft.reactor.idle");
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);
        int color = !menu.isFormed() ? WARNING_COLOR : menu.getRate() > 0 ? GOOD_COLOR : LABEL_COLOR;
        graphics.text(font, status(), 64, 30, color, false);
        graphics.text(font, Component.translatable("gui.comminutioncraft.generating", formatFe(menu.getRate())), 64, 40, LABEL_COLOR, false);
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);
        energyTooltip(graphics, mouseX, mouseY, menu.getEnergy(), menu.getEnergyCapacity());
        gaugeTooltip(graphics, mouseX, mouseY, 50, "gui.comminutioncraft.plasma", menu.getPlasma(), menu.getPlasmaCapacity());
        gaugeTooltip(graphics, mouseX, mouseY, PLASMA_X, "gui.comminutioncraft.tritium", menu.getTritium(), menu.getTritiumCapacity());
        if (isHovering(HEAT_X, HEAT_Y, HEAT_W, HEAT_H, mouseX, mouseY)) {
            graphics.setTooltipForNextFrame(Component.translatable("gui.comminutioncraft.reactor.heat", menu.getHeat()), mouseX, mouseY);
        }
    }
}
