package com.script.comminutioncraft;

import com.script.comminutioncraft.client.gui.CoalGranulatorScreen;
import com.script.comminutioncraft.client.ClientRecipes;
import com.script.comminutioncraft.client.ModKeyMappings;
import com.script.comminutioncraft.client.ModTooltips;
import net.neoforged.neoforge.common.NeoForge;
import com.script.comminutioncraft.client.gui.CoalSmelterScreen;
import com.script.comminutioncraft.client.gui.ElectricGeneratorScreen;
import com.script.comminutioncraft.client.gui.ElectricMachineScreen;
import com.script.comminutioncraft.client.gui.EnergyBatteryScreen;
import com.script.comminutioncraft.client.gui.AtmosphericWaterGeneratorScreen;
import com.script.comminutioncraft.client.gui.FluidMachineScreen;
import com.script.comminutioncraft.client.gui.PlasmaMachineScreen;
import com.script.comminutioncraft.client.gui.OreWasherScreen;
import com.script.comminutioncraft.client.gui.PelletPressScreen;
import com.script.comminutioncraft.client.gui.FusionReactorScreen;
import com.script.comminutioncraft.client.gui.CreativeFluidTankScreen;
import com.script.comminutioncraft.client.gui.OrePurifierScreen;
import com.script.comminutioncraft.common.machine.OrePurifierMenu;
import com.script.comminutioncraft.client.render.FusionReactorRenderer;
import com.script.comminutioncraft.common.machine.PelletPressMenu;
import com.script.comminutioncraft.common.machine.OreWasherMenu;
import com.script.comminutioncraft.common.machine.ElectricMachineMenu;
import com.script.comminutioncraft.client.render.FluidTankRenderer;
import com.script.comminutioncraft.common.registry.ModBlockEntities;
import com.script.comminutioncraft.common.registry.ModFluids;
import java.util.function.Supplier;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.client.resources.model.sprite.Material;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;
import net.neoforged.neoforge.client.fluid.FluidTintSource;
import com.script.comminutioncraft.common.data.ModDataGenerators;
import com.script.comminutioncraft.common.registry.ModMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/** Client-only entrypoint. Only loaded on the physical client (and for the data run). */
@Mod(value = ComminutionCraft.MODID, dist = Dist.CLIENT)
public class ComminutionCraftClient {
    public ComminutionCraftClient(IEventBus modEventBus, net.neoforged.fml.ModContainer modContainer) {
        // Mods menu > Comminution Craft > Config: NeoForge's generated screen for our config spec
        modContainer.registerExtensionPoint(net.neoforged.neoforge.client.gui.IConfigScreenFactory.class,
                net.neoforged.neoforge.client.gui.ConfigurationScreen::new);
        modEventBus.addListener(ComminutionCraftClient::registerScreens);
        modEventBus.addListener(ModDataGenerators::gatherData);
        NeoForge.EVENT_BUS.addListener(ClientRecipes::onRecipesReceived);
        modEventBus.addListener(ModKeyMappings::register);
        modEventBus.addListener(ComminutionCraftClient::registerRenderers);
        modEventBus.addListener(ComminutionCraftClient::registerFluidModels);
        NeoForge.EVENT_BUS.addListener(ModKeyMappings::onClientTick);
        NeoForge.EVENT_BUS.addListener(ModTooltips::onTooltip);
    }

    private static void registerScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.COAL_GRANULATOR.get(), CoalGranulatorScreen::new);
        event.register(ModMenus.COAL_SMELTER.get(), CoalSmelterScreen::new);
        event.register(ModMenus.ELECTRIC_GRANULATOR.get(), ElectricMachineScreen::granulator);
        event.register(ModMenus.ELECTRIC_SMELTER.get(), ElectricMachineScreen::smelter);
        event.register(ModMenus.ELECTRIC_GENERATOR.get(), ElectricGeneratorScreen::new);
        event.register(ModMenus.ENERGY_BATTERY.get(), EnergyBatteryScreen::electric);
        event.register(ModMenus.PLASMA_GENERATOR.get(), FluidMachineScreen::plasmaGenerator);
        event.register(ModMenus.ATMOSPHERIC_WATER_GENERATOR.get(), AtmosphericWaterGeneratorScreen::new);
        event.register(ModMenus.ELECTROLYSER.get(), FluidMachineScreen::electrolyser);
        event.register(ModMenus.MHD_GENERATOR.get(), FluidMachineScreen::mhdGenerator);
        event.register(ModMenus.ISOTOPE_SEPARATOR.get(), FluidMachineScreen::isotopeSeparator);
        event.register(ModMenus.TRITIUM_BREEDER.get(), FluidMachineScreen::tritiumBreeder);
        event.register(ModMenus.FUSION_REACTOR.get(), FusionReactorScreen::new);
        event.register(ModMenus.EVENT_HORIZON.get(), com.script.comminutioncraft.client.gui.EventHorizonScreen::new);
        event.register(ModMenus.PARTICLE_ACCELERATOR.get(), com.script.comminutioncraft.client.gui.ParticleAcceleratorScreen::new);
        event.register(ModMenus.CREATIVE_FLUID_TANK.get(), CreativeFluidTankScreen::new);
        event.<ElectricMachineMenu, OrePurifierScreen>register(ModMenus.ORE_PURIFIER.get(),
                (menu, inventory, title) -> new OrePurifierScreen((OrePurifierMenu) menu, inventory, title));
        event.register(ModMenus.FUSION_ENERGY_BATTERY.get(), EnergyBatteryScreen::fusion);
        event.register(ModMenus.ANTIMATTER_ENERGY_BATTERY.get(), EnergyBatteryScreen::antimatter);
        event.register(ModMenus.ANNIHILATION_GENERATOR.get(), FluidMachineScreen::annihilationGenerator);
        event.<ElectricMachineMenu, PelletPressScreen>register(ModMenus.PELLET_PRESS.get(),
                (menu, inventory, title) -> new PelletPressScreen((PelletPressMenu) menu, inventory, title));
        event.register(ModMenus.PLASMA_GRANULATOR.get(), ElectricMachineScreen::plasmaGranulator);
        event.register(ModMenus.ANTIMATTER_GRANULATOR.get(), ElectricMachineScreen::antimatterGranulator);
        event.register(ModMenus.ANTIMATTER_SMELTER.get(), ElectricMachineScreen::antimatterSmelter);
        event.<ElectricMachineMenu, OreWasherScreen>register(ModMenus.ANTIMATTER_WASHER.get(),
                (menu, inventory, title) -> OreWasherScreen.antimatter((OreWasherMenu) menu, inventory, title));
        event.<ElectricMachineMenu, OrePurifierScreen>register(ModMenus.ANTIMATTER_PURIFIER.get(),
                (menu, inventory, title) -> OrePurifierScreen.antimatter((OrePurifierMenu) menu, inventory, title));
        event.<ElectricMachineMenu, com.script.comminutioncraft.client.gui.FrothFlotationCellScreen>register(ModMenus.FROTH_FLOTATION_CELL.get(),
                (menu, inventory, title) -> new com.script.comminutioncraft.client.gui.FrothFlotationCellScreen(
                        (com.script.comminutioncraft.common.machine.FrothFlotationCellMenu) menu, inventory, title));
        event.register(ModMenus.PLASMA_SMELTER.get(), ElectricMachineScreen::plasmaSmelter);
        event.register(ModMenus.PLASMA_ENERGY_BATTERY.get(), EnergyBatteryScreen::plasma);
        event.register(ModMenus.CIRCUIT_ASSEMBLER.get(), PlasmaMachineScreen::assembler);
        event.<ElectricMachineMenu, OreWasherScreen>register(ModMenus.ORE_WASHER.get(),
                (menu, inventory, title) -> new OreWasherScreen((OreWasherMenu) menu, inventory, title));
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.FLUID_TANK.get(), context -> new FluidTankRenderer(context.sprites()));
        event.registerBlockEntityRenderer(ModBlockEntities.CREATIVE_FLUID_TANK.get(), context -> new FluidTankRenderer(context.sprites()));
        event.registerBlockEntityRenderer(ModBlockEntities.CONTAINMENT_CELL.get(), context -> new FluidTankRenderer(context.sprites()));
        event.registerBlockEntityRenderer(ModBlockEntities.FUSION_REACTOR.get(), context -> new FusionReactorRenderer(context.sprites()));
        event.registerEntityRenderer(com.script.comminutioncraft.common.registry.ModEntities.BLACK_HOLE.get(),
                com.script.comminutioncraft.client.render.BlackHoleEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.EVENT_HORIZON_REACTOR.get(),
                context -> new com.script.comminutioncraft.client.render.BlackHoleRenderer());
        event.registerBlockEntityRenderer(ModBlockEntities.SOARYN_CHEST.get(), net.minecraft.client.renderer.blockentity.ChestRenderer::new);
    }

    /** Our fluids' textures (they have no world blocks, but tanks, GUIs and JEI draw them from these models). */
    private static void registerFluidModels(RegisterFluidModelsEvent event) {
        fluidModel(event, "plasma", ModFluids.PLASMA, ModFluids.FLOWING_PLASMA);
        fluidModel(event, "hydrogen", ModFluids.HYDROGEN, ModFluids.FLOWING_HYDROGEN);
        fluidModel(event, "oxygen", ModFluids.OXYGEN, ModFluids.FLOWING_OXYGEN);
        fluidModel(event, "deuterium", ModFluids.DEUTERIUM, ModFluids.FLOWING_DEUTERIUM);
        fluidModel(event, "tritium", ModFluids.TRITIUM, ModFluids.FLOWING_TRITIUM);
        fluidModel(event, "antimatter", ModFluids.ANTIMATTER, ModFluids.FLOWING_ANTIMATTER);
    }

    private static void fluidModel(RegisterFluidModelsEvent event, String name,
                                   Supplier<? extends Fluid> still, Supplier<? extends Fluid> flowing) {
        event.register(new FluidModel.Unbaked(new Material(ComminutionCraft.id("block/" + name + "_still")),
                        new Material(ComminutionCraft.id("block/" + name + "_flow")), null, (FluidTintSource) null),
                still, flowing);
    }
}
