package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.registry.ModMenus;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;

/** Froth Flotation Cell menu: the electric machine layout plus water and oxygen gauges. */
public class FrothFlotationCellMenu extends ElectricMachineMenu {
    private final ContainerData data;

    // Client-side constructor (called when the server tells the client to open the GUI)
    public static FrothFlotationCellMenu client(int containerId, Inventory inventory) {
        return new FrothFlotationCellMenu(containerId, inventory,
                new SimpleContainer(ElectricMachineBlockEntity.SLOT_COUNT), new SimpleContainerData(FrothFlotationCellBlockEntity.DATA_COUNT));
    }

    public FrothFlotationCellMenu(int containerId, Inventory inventory, Container container, ContainerData data) {
        super(ModMenus.FROTH_FLOTATION_CELL.get(), containerId, inventory, container, data);
        checkContainerDataCount(data, FrothFlotationCellBlockEntity.DATA_COUNT);
        this.data = data;
    }

    public int getWater() {
        return data.get(FrothFlotationCellBlockEntity.DATA_WATER);
    }

    public int getWaterCapacity() {
        return data.get(FrothFlotationCellBlockEntity.DATA_WATER_CAPACITY);
    }

    public int getOxygen() {
        return data.get(FrothFlotationCellBlockEntity.DATA_OXYGEN);
    }

    public int getOxygenCapacity() {
        return data.get(FrothFlotationCellBlockEntity.DATA_OXYGEN_CAPACITY);
    }
}
