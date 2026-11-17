package com.script.comminutioncraft.client.gui;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.machine.EnergyBatteryMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** GUI for both Energy Batteries: energy bar, stored FE and what flowed in and out last tick (seams in the tier colour). */
public class EnergyBatteryScreen extends MachineScreen<EnergyBatteryMenu> {
    private static final Identifier TEXTURE = ComminutionCraft.id("textures/gui/container/energy_battery.png");
    private static final int VALUE_COLOR = 0xFFE4E6EC;
    private static final int LABEL_COLOR = 0xFF9EA4B0;
    private static final int TEXT_X = 32;

    public EnergyBatteryScreen(EnergyBatteryMenu menu, Inventory inventory, Component title, Tier tier) {
        super(menu, inventory, title, TEXTURE, tier);
    }

    public static EnergyBatteryScreen electric(EnergyBatteryMenu menu, Inventory inventory, Component title) {
        return new EnergyBatteryScreen(menu, inventory, title, Tier.ELECTRIC);
    }

    public static EnergyBatteryScreen plasma(EnergyBatteryMenu menu, Inventory inventory, Component title) {
        return new EnergyBatteryScreen(menu, inventory, title, Tier.PLASMA);
    }

    public static EnergyBatteryScreen singularity(EnergyBatteryMenu menu, Inventory inventory, Component title) {
        return new EnergyBatteryScreen(menu, inventory, title, Tier.SINGULARITY);
    }

    public static EnergyBatteryScreen antimatter(EnergyBatteryMenu menu, Inventory inventory, Component title) {
        return new EnergyBatteryScreen(menu, inventory, title, Tier.ANTIMATTER);
    }

    public static EnergyBatteryScreen fusion(EnergyBatteryMenu menu, Inventory inventory, Component title) {
        return new EnergyBatteryScreen(menu, inventory, title, Tier.FUSION);
    }

    @Override
    protected boolean isActive() {
        return menu.getReceived() > 0 || menu.getSent() > 0;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        drawEnergyBar(graphics, menu.getEnergy(), menu.getEnergyCapacity());
        // the charge slot: a rim in the tier colour, and a faint bolt while it's empty
        int x = leftPos + EnergyBatteryMenu.CHARGE_X;
        int y = topPos + EnergyBatteryMenu.CHARGE_Y;
        int rim = tier.seamOn;
        graphics.fill(x - 2, y - 2, x + 18, y - 1, rim);
        graphics.fill(x - 2, y + 17, x + 18, y + 18, rim);
        graphics.fill(x - 2, y - 1, x - 1, y + 17, rim);
        graphics.fill(x + 17, y - 1, x + 18, y + 17, rim);
        if (!menu.getSlot(EnergyBatteryMenu.CHARGE_SLOT).hasItem()) {
            graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, x, y, 192, 0, 16, 16, 256, 256);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);
        graphics.text(font, Component.translatable("gui.comminutioncraft.battery.stored", formatFe(menu.getEnergy())),
                TEXT_X, 22, VALUE_COLOR, false);
        graphics.text(font, Component.translatable("gui.comminutioncraft.battery.capacity", formatFe(menu.getEnergyCapacity())),
                TEXT_X, 32, LABEL_COLOR, false);
        graphics.text(font, Component.translatable("gui.comminutioncraft.battery.input", formatFe(menu.getReceived())),
                TEXT_X, 48, LABEL_COLOR, false);
        graphics.text(font, Component.translatable("gui.comminutioncraft.battery.output", formatFe(menu.getSent())),
                TEXT_X, 58, LABEL_COLOR, false);
        Component charge = Component.translatable("gui.comminutioncraft.battery.charge");
        graphics.text(font, charge, EnergyBatteryMenu.CHARGE_X + 6 - font.width(charge) / 2, EnergyBatteryMenu.CHARGE_Y + 21, LABEL_COLOR, false);
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);
        energyTooltip(graphics, mouseX, mouseY, menu.getEnergy(), menu.getEnergyCapacity());
    }
}
