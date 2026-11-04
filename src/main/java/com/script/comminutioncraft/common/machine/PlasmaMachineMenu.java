package com.script.comminutioncraft.common.machine;

import com.script.comminutioncraft.common.registry.ModMenus;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.SimpleContainerData;

/** Menu for tier-3 machines that use plasma (the Circuit Assembler): the electric machine's layout plus a plasma gauge. */
public class PlasmaMachineMenu extends ElectricMachineMenu {
    private final ContainerData data;

    // Client-side constructor (called when the server tells the client to open the GUI)
    public static PlasmaMachineMenu clientAssembler(int containerId, Inventory inventory) {
        return new PlasmaMachineMenu(ModMenus.CIRCUIT_ASSEMBLER.get(), containerId, inventory,
                new SimpleContainer(ElectricMachineBlockEntity.SLOT_COUNT), new SimpleContainerData(PlasmaMachineBlockEntity.DATA_COUNT));
    }

    public PlasmaMachineMenu(MenuType<?> type, int containerId, Inventory inventory, Container container, ContainerData data) {
        super(type, containerId, inventory, container, data);
        checkContainerDataCount(data, PlasmaMachineBlockEntity.DATA_COUNT);
        this.data = data;
    }

    public int getPlasma() {
        return data.get(PlasmaMachineBlockEntity.DATA_PLASMA);
    }

    public int getPlasmaCapacity() {
        return data.get(PlasmaMachineBlockEntity.DATA_PLASMA_CAPACITY);
    }
}
