package com.script.comminutioncraft.common.compat.jade;

import com.script.comminutioncraft.common.cable.EnergyCableBlock;
import com.script.comminutioncraft.common.machine.ElectricGeneratorBlock;
import com.script.comminutioncraft.common.machine.ElectricGeneratorBlockEntity;
import com.script.comminutioncraft.common.machine.ElectricMachineBlockEntity;
import com.script.comminutioncraft.common.machine.FuelMachineBlockEntity;
import com.script.comminutioncraft.common.machine.MachineBlock;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.view.HideThingsExtensionProvider;

/**
 * Jade: furnace-style views for our machines, a status line for the generator. Stored FE comes from Jade's own
 * energy support (our FE capability). Found by Jade through {@link WailaPlugin}; Jade is compileOnly, so none of
 * this loads without it.
 */
@WailaPlugin
public class ComminutionJadePlugin implements IWailaPlugin {
    @Override
    public void register(IWailaCommonRegistration registration) {
        registration.registerBlockDataProvider(MachineProvider.INSTANCE, FuelMachineBlockEntity.class);
        registration.registerBlockDataProvider(MachineProvider.INSTANCE, ElectricMachineBlockEntity.class);
        registration.registerBlockDataProvider(GeneratorProvider.INSTANCE, ElectricGeneratorBlockEntity.class);

        // Our views already show the slots, so skip Jade's generic item list for them.
        registration.registerItemStorage(HideThingsExtensionProvider.instance(), FuelMachineBlockEntity.class);
        registration.registerItemStorage(HideThingsExtensionProvider.instance(), ElectricMachineBlockEntity.class);
        registration.registerItemStorage(HideThingsExtensionProvider.instance(), ElectricGeneratorBlockEntity.class);
        // Cables pass power on and store none; a "0 / 1,000 FE" bar would only confuse.
        registration.registerEnergyStorage(HideThingsExtensionProvider.instance(), EnergyCableBlock.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(MachineProvider.Client.INSTANCE, MachineBlock.class);
        registration.registerBlockComponent(GeneratorProvider.Client.INSTANCE, ElectricGeneratorBlock.class);
    }
}
