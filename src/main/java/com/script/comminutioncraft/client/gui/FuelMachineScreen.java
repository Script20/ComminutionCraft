package com.script.comminutioncraft.client.gui;

import com.script.comminutioncraft.common.machine.FuelMachineMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/** GUI for tier-1 machines: input, fuel + flame, progress arrow, output (vanilla furnace layout). */
public class FuelMachineScreen extends MachineScreen<FuelMachineMenu> {
    public FuelMachineScreen(FuelMachineMenu menu, Inventory inventory, Component title, Identifier texture) {
        super(menu, inventory, title, texture, Tier.COAL);
    }

    @Override
    protected boolean isActive() {
        return menu.isLit();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        if (menu.isLit()) {
            drawFlame(graphics, 56, 36, menu.getLitProgress());
        }
        drawArrow(graphics, 79, 34, menu.getBurnProgress());
    }
}
