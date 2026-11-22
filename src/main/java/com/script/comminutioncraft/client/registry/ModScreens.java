package com.script.comminutioncraft.client.registry;

import com.script.comminutioncraft.client.gui.AtmosphericWaterGeneratorScreen;
import com.script.comminutioncraft.client.gui.CoalGranulatorScreen;
import com.script.comminutioncraft.client.gui.CoalSmelterScreen;
import com.script.comminutioncraft.client.gui.CreativeChestScreen;
import com.script.comminutioncraft.client.gui.CreativeFluidTankScreen;
import com.script.comminutioncraft.client.gui.ElectricGeneratorScreen;
import com.script.comminutioncraft.client.gui.ElectricMachineScreen;
import com.script.comminutioncraft.client.gui.EnergyBatteryScreen;
import com.script.comminutioncraft.client.gui.EventHorizonScreen;
import com.script.comminutioncraft.client.gui.FactoryScreen;
import com.script.comminutioncraft.client.gui.FluidMachineScreen;
import com.script.comminutioncraft.client.gui.FrothFlotationCellScreen;
import com.script.comminutioncraft.client.gui.FusionReactorScreen;
import com.script.comminutioncraft.client.gui.ItemPipeScreen;
import com.script.comminutioncraft.client.gui.OreMinerScreen;
import com.script.comminutioncraft.client.gui.OrePurifierScreen;
import com.script.comminutioncraft.client.gui.OreWasherScreen;
import com.script.comminutioncraft.client.gui.ParticleAcceleratorScreen;
import com.script.comminutioncraft.client.gui.PelletPressScreen;
import com.script.comminutioncraft.client.gui.PlasmaMachineScreen;
import com.script.comminutioncraft.client.gui.SingularityProcessorScreen;
import com.script.comminutioncraft.common.machine.ElectricMachineMenu;
import com.script.comminutioncraft.common.machine.FrothFlotationCellMenu;
import com.script.comminutioncraft.common.machine.OrePurifierMenu;
import com.script.comminutioncraft.common.machine.OreWasherMenu;
import com.script.comminutioncraft.common.machine.PelletPressMenu;
import com.script.comminutioncraft.common.machine.SingularityProcessorMenu;
import com.script.comminutioncraft.common.registry.ModMenus;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/**
 * Which screen opens for each menu, by tier. Machines whose menu type is declared as {@code ElectricMachineMenu} but
 * holds a subclass (washer, purifier...) cast it for their own screen.
 */
public final class ModScreens {
    private ModScreens() {
    }

    public static void register(RegisterMenuScreensEvent event) {
        // Tier 1 - Coal
        event.register(ModMenus.COAL_GRANULATOR.get(), CoalGranulatorScreen::new);
        event.register(ModMenus.COAL_SMELTER.get(), CoalSmelterScreen::new);

        // Tier 2 - Electric
        event.register(ModMenus.ELECTRIC_GRANULATOR.get(), ElectricMachineScreen::granulator);
        event.register(ModMenus.ELECTRIC_SMELTER.get(), ElectricMachineScreen::smelter);
        event.register(ModMenus.ELECTRIC_GENERATOR.get(), ElectricGeneratorScreen::new);
        event.register(ModMenus.ENERGY_BATTERY.get(), EnergyBatteryScreen::electric);

        // Tier 3 - Plasma (machines and the power chain)
        event.register(ModMenus.PLASMA_GRANULATOR.get(), ElectricMachineScreen::plasmaGranulator);
        event.register(ModMenus.PLASMA_SMELTER.get(), ElectricMachineScreen::plasmaSmelter);
        event.<ElectricMachineMenu, OreWasherScreen>register(ModMenus.ORE_WASHER.get(),
                (menu, inventory, title) -> new OreWasherScreen((OreWasherMenu) menu, inventory, title));
        event.register(ModMenus.CIRCUIT_ASSEMBLER.get(), PlasmaMachineScreen::assembler);
        event.register(ModMenus.ATMOSPHERIC_WATER_GENERATOR.get(), AtmosphericWaterGeneratorScreen::new);
        event.register(ModMenus.ELECTROLYSER.get(), FluidMachineScreen::electrolyser);
        event.register(ModMenus.PLASMA_GENERATOR.get(), FluidMachineScreen::plasmaGenerator);
        event.register(ModMenus.MHD_GENERATOR.get(), FluidMachineScreen::mhdGenerator);
        event.register(ModMenus.PLASMA_ENERGY_BATTERY.get(), EnergyBatteryScreen::plasma);

        // Tier 4 - Fusion
        event.register(ModMenus.ISOTOPE_SEPARATOR.get(), FluidMachineScreen::isotopeSeparator);
        event.register(ModMenus.TRITIUM_BREEDER.get(), FluidMachineScreen::tritiumBreeder);
        event.<ElectricMachineMenu, PelletPressScreen>register(ModMenus.PELLET_PRESS.get(),
                (menu, inventory, title) -> new PelletPressScreen((PelletPressMenu) menu, inventory, title));
        event.register(ModMenus.FUSION_REACTOR.get(), FusionReactorScreen::new);
        event.<ElectricMachineMenu, OrePurifierScreen>register(ModMenus.ORE_PURIFIER.get(),
                (menu, inventory, title) -> new OrePurifierScreen((OrePurifierMenu) menu, inventory, title));
        event.register(ModMenus.FUSION_ENERGY_BATTERY.get(), EnergyBatteryScreen::fusion);

        // Tier 5 - Antimatter
        event.register(ModMenus.PARTICLE_ACCELERATOR.get(), ParticleAcceleratorScreen::new);
        event.register(ModMenus.ANNIHILATION_GENERATOR.get(), FluidMachineScreen::annihilationGenerator);
        event.register(ModMenus.ANTIMATTER_GRANULATOR.get(), ElectricMachineScreen::antimatterGranulator);
        event.register(ModMenus.ANTIMATTER_SMELTER.get(), ElectricMachineScreen::antimatterSmelter);
        event.<ElectricMachineMenu, FrothFlotationCellScreen>register(ModMenus.FROTH_FLOTATION_CELL.get(),
                (menu, inventory, title) -> new FrothFlotationCellScreen((FrothFlotationCellMenu) menu, inventory, title));
        event.<ElectricMachineMenu, OreWasherScreen>register(ModMenus.ANTIMATTER_WASHER.get(),
                (menu, inventory, title) -> OreWasherScreen.antimatter((OreWasherMenu) menu, inventory, title));
        event.<ElectricMachineMenu, OrePurifierScreen>register(ModMenus.ANTIMATTER_PURIFIER.get(),
                (menu, inventory, title) -> OrePurifierScreen.antimatter((OrePurifierMenu) menu, inventory, title));
        event.register(ModMenus.ANTIMATTER_ENERGY_BATTERY.get(), EnergyBatteryScreen::antimatter);

        // Tier 6 - Singularity
        event.register(ModMenus.EVENT_HORIZON.get(), EventHorizonScreen::new);
        event.<ElectricMachineMenu, SingularityProcessorScreen>register(ModMenus.SINGULARITY_PROCESSOR.get(),
                (menu, inventory, title) -> new SingularityProcessorScreen((SingularityProcessorMenu) menu, inventory, title));
        event.register(ModMenus.SINGULARITY_ENERGY_BATTERY.get(), EnergyBatteryScreen::singularity);

        // Every tier: ore miners, factories, item pipes, creative blocks
        event.register(ModMenus.ORE_MINER.get(), OreMinerScreen::new);
        event.register(ModMenus.FACTORY.get(), FactoryScreen::new);
        event.register(ModMenus.ITEM_PIPE.get(), ItemPipeScreen::new);
        event.register(ModMenus.CREATIVE_FLUID_TANK.get(), CreativeFluidTankScreen::new);
        event.register(ModMenus.CREATIVE_CHEST.get(), CreativeChestScreen::new);
    }
}
