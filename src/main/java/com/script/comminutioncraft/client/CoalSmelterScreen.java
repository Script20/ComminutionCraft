package com.script.comminutioncraft.client;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.machine.FuelMachineMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

public class CoalSmelterScreen extends FuelMachineScreen {
    private static final Identifier TEXTURE = ComminutionCraft.id("textures/gui/container/coal_smelter.png");

    public CoalSmelterScreen(FuelMachineMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, TEXTURE);
    }
}
