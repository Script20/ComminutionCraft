package com.script.comminutioncraft.client.gui;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.machine.ParticleAcceleratorBlockEntity;
import com.script.comminutioncraft.common.machine.ParticleAcceleratorBlockEntity.Status;
import com.script.comminutioncraft.common.machine.ParticleAcceleratorMenu;
import java.util.Locale;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * GUI for the Particle Accelerator controller: energy (left), hydrogen (x 141) and antimatter (x 155) tubes, the status,
 * ring size and FE use, and a bar filling towards the next mB of antimatter. Sheet layout: see tools/gui_dark_chassis.py
 * ({@code accelerator_sheet}).
 */
public class ParticleAcceleratorScreen extends MachineScreen<ParticleAcceleratorMenu> {
    private static final Identifier TEXTURE = ComminutionCraft.id("textures/gui/container/accelerator_controller.png");
    private static final int LABEL_COLOR = 0xFF9EA4B0;
    private static final int GOOD_COLOR = 0xFFD6E4FF;
    private static final int WARNING_COLOR = 0xFFFFB04A;
    private static final int HYDROGEN_X = 141;
    private static final int TEXT_X = 30;
    private static final int BAR_X = 30;
    private static final int BAR_Y = 56;
    private static final int BAR_W = 78;
    private static final int BAR_H = 5;

    public ParticleAcceleratorScreen(ParticleAcceleratorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, TEXTURE, Tier.ANTIMATTER);
    }

    @Override
    protected boolean isActive() {
        return menu.getStatus() == Status.RUNNING;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        drawEnergyBar(graphics, menu.getEnergy(), menu.getEnergyCapacity());
        drawGauge(graphics, HYDROGEN_X, HYDROGEN_U, menu.getHydrogen(), menu.getHydrogenCapacity());
        drawGauge(graphics, PLASMA_X, ANTIMATTER_U, menu.getAntimatter(), menu.getAntimatterCapacity());
        int width = Math.min(BAR_W, Math.round((float) BAR_W * menu.getProgress() / menu.getProgressMax()));
        if (width > 0) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, texture, leftPos + BAR_X, topPos + BAR_Y, 176, 90, width, BAR_H, TEX_SIZE, TEX_SIZE);
        }
    }

    private Component status() {
        return Component.translatable("gui.comminutioncraft.accelerator." + menu.getStatus().name().toLowerCase(Locale.ROOT));
    }

    /** mB of antimatter per tick for a ring of this side. */
    private static String rate(int side) {
        float perTick = (float) ParticleAcceleratorBlockEntity.ringBlocks(side) / Math.max(1, ParticleAcceleratorBlockEntity.RING_TICKS_PER_MB.get());
        return String.format(Locale.ROOT, "%.3f", perTick).replaceAll("0+$", "").replaceAll("\\.$", "");
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);
        Status status = menu.getStatus();
        int color = status == Status.RUNNING ? GOOD_COLOR : status == Status.OFF ? LABEL_COLOR : WARNING_COLOR;
        graphics.text(font, status(), TEXT_X, 20, color, false);
        int side = menu.getRingSide();
        Component ring = side > 0
                ? Component.translatable("gui.comminutioncraft.accelerator.ring", side, side)
                : Component.translatable("gui.comminutioncraft.accelerator.ring_hint",
                        ParticleAcceleratorBlockEntity.MIN_RING_SIDE.get(), ParticleAcceleratorBlockEntity.MAX_RING_SIDE.get());
        graphics.text(font, ring, TEXT_X, 31, LABEL_COLOR, false);
        if (side > 0) {
            graphics.text(font, Component.translatable("gui.comminutioncraft.accelerator.rate", rate(side)), TEXT_X, 42, LABEL_COLOR, false);
        }
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);
        energyTooltip(graphics, mouseX, mouseY, menu.getEnergy(), menu.getEnergyCapacity());
        gaugeTooltip(graphics, mouseX, mouseY, HYDROGEN_X, "gui.comminutioncraft.hydrogen", menu.getHydrogen(), menu.getHydrogenCapacity());
        gaugeTooltip(graphics, mouseX, mouseY, PLASMA_X, "gui.comminutioncraft.antimatter", menu.getAntimatter(), menu.getAntimatterCapacity());
        if (isHovering(BAR_X, BAR_Y, BAR_W, BAR_H, mouseX, mouseY)) {
            int percent = Math.min(100, 100 * menu.getProgress() / menu.getProgressMax());
            graphics.setTooltipForNextFrame(java.util.List.of(
                    Component.translatable("gui.comminutioncraft.accelerator.progress", percent).getVisualOrderText(),
                    Component.translatable("gui.comminutioncraft.accelerator.use", formatFe(menu.getEnergyUse())).getVisualOrderText()),
                    mouseX, mouseY);
        }
    }
}
