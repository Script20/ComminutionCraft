package com.script.comminutioncraft.common.compat.jade;

import com.script.comminutioncraft.common.cable.EnergyCableBlock;
import com.script.comminutioncraft.common.cable.FluidPipeBlock;
import com.script.comminutioncraft.common.machine.AtmosphericWaterGeneratorBlock;
import com.script.comminutioncraft.common.machine.AtmosphericWaterGeneratorBlockEntity;
import com.script.comminutioncraft.common.machine.ElectricGeneratorBlock;
import com.script.comminutioncraft.common.machine.ElectricGeneratorBlockEntity;
import com.script.comminutioncraft.common.machine.ElectricMachineBlockEntity;
import com.script.comminutioncraft.common.machine.EnergyBatteryBlock;
import com.script.comminutioncraft.common.machine.EnergyBatteryBlockEntity;
import com.script.comminutioncraft.common.machine.FuelMachineBlockEntity;
import com.script.comminutioncraft.common.machine.MachineBlock;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;
import snownee.jade.api.view.HideThingsExtensionProvider;

/**
 * Jade: furnace-style views for our machines, a status line for the generator, in/out rates for the battery. Stored FE comes from Jade's own
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
        registration.registerBlockDataProvider(BatteryProvider.INSTANCE, EnergyBatteryBlockEntity.class);
        registration.registerBlockDataProvider(WaterGeneratorProvider.INSTANCE, AtmosphericWaterGeneratorBlockEntity.class);

        // Our views already show the slots, so skip Jade's generic item list for them.
        registration.registerItemStorage(HideThingsExtensionProvider.instance(), FuelMachineBlockEntity.class);
        registration.registerItemStorage(HideThingsExtensionProvider.instance(), ElectricMachineBlockEntity.class);
        registration.registerItemStorage(HideThingsExtensionProvider.instance(), ElectricGeneratorBlockEntity.class);
        // Cables and pipes pass things on and store none; a "0 / 1,000" bar would only confuse.
        registration.registerEnergyStorage(HideThingsExtensionProvider.instance(), EnergyCableBlock.class);
        registration.registerFluidStorage(HideThingsExtensionProvider.instance(), FluidPipeBlock.class); // Fluid Pipe and Plasma Cable
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
        registration.registerBlockComponent(MachineProvider.Client.INSTANCE, MachineBlock.class);
        registration.registerBlockComponent(GeneratorProvider.Client.INSTANCE, ElectricGeneratorBlock.class);
        registration.registerBlockComponent(BatteryProvider.Client.INSTANCE, EnergyBatteryBlock.class);
        registration.registerBlockComponent(WaterGeneratorProvider.Client.INSTANCE, AtmosphericWaterGeneratorBlock.class);
    }
}
