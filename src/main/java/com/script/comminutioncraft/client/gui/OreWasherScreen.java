package com.script.comminutioncraft.client.gui;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.machine.OreWasherMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Ore Washer GUI: the FE machine screen plus a water gauge on the right (fill sprite at (196, 31)). */
public class OreWasherScreen extends ElectricMachineScreen {
    private final OreWasherMenu washer;

    public OreWasherScreen(OreWasherMenu menu, Inventory inventory, Component title) {
        this(menu, inventory, title, "ore_washer", Tier.PLASMA);
    }

    private OreWasherScreen(OreWasherMenu menu, Inventory inventory, Component title, String sheet, Tier tier) {
        super(menu, inventory, title, ComminutionCraft.id("textures/gui/container/" + sheet + ".png"), tier);
        this.washer = menu;
    }

    public static OreWasherScreen antimatter(OreWasherMenu menu, Inventory inventory, Component title) {
        return new OreWasherScreen(menu, inventory, title, "antimatter_washer", Tier.ANTIMATTER);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        drawGauge(graphics, PLASMA_X, WATER_U, washer.getWater(), washer.getWaterCapacity());
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);
        gaugeTooltip(graphics, mouseX, mouseY, PLASMA_X, "gui.comminutioncraft.water", washer.getWater(), washer.getWaterCapacity());
    }
}
