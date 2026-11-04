package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.registry.ModMenus;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;

/** Ore Washer menu: the electric machine layout plus a water gauge. */
public class OreWasherMenu extends ElectricMachineMenu {
    private final ContainerData data;

    // Client-side constructor (called when the server tells the client to open the GUI)
    public static OreWasherMenu client(int containerId, Inventory inventory) {
        return new OreWasherMenu(containerId, inventory,
                new SimpleContainer(ElectricMachineBlockEntity.SLOT_COUNT), new SimpleContainerData(OreWasherBlockEntity.DATA_COUNT));
    }

    public OreWasherMenu(int containerId, Inventory inventory, Container container, ContainerData data) {
        super(ModMenus.ORE_WASHER.get(), containerId, inventory, container, data);
        checkContainerDataCount(data, OreWasherBlockEntity.DATA_COUNT);
        this.data = data;
    }

    public int getWater() {
        return data.get(OreWasherBlockEntity.DATA_WATER);
    }

    public int getWaterCapacity() {
        return data.get(OreWasherBlockEntity.DATA_WATER_CAPACITY);
    }
}
