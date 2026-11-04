package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.registry.ModMenus;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;

/** Pellet Press menu: the electric machine layout plus deuterium and tritium gauges. */
public class PelletPressMenu extends ElectricMachineMenu {
    private final ContainerData data;

    // Client-side constructor (called when the server tells the client to open the GUI)
    public static PelletPressMenu client(int containerId, Inventory inventory) {
        return new PelletPressMenu(containerId, inventory,
                new SimpleContainer(ElectricMachineBlockEntity.SLOT_COUNT), new SimpleContainerData(PelletPressBlockEntity.DATA_COUNT));
    }

    public PelletPressMenu(int containerId, Inventory inventory, Container container, ContainerData data) {
        super(ModMenus.PELLET_PRESS.get(), containerId, inventory, container, data);
        checkContainerDataCount(data, PelletPressBlockEntity.DATA_COUNT);
        this.data = data;
    }

    public int getDeuterium() {
        return data.get(PelletPressBlockEntity.DATA_DEUTERIUM);
    }

    public int getDeuteriumCapacity() {
        return data.get(PelletPressBlockEntity.DATA_DEUTERIUM_CAPACITY);
    }

    public int getTritium() {
        return data.get(PelletPressBlockEntity.DATA_TRITIUM);
    }

    public int getTritiumCapacity() {
        return data.get(PelletPressBlockEntity.DATA_TRITIUM_CAPACITY);
    }
}
