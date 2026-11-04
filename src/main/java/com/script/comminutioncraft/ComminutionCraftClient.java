package com.script.comminutioncraft;

import com.script.comminutioncraft.client.gui.CoalGranulatorScreen;
import com.script.comminutioncraft.client.ClientRecipes;
import com.script.comminutioncraft.client.ModKeyMappings;
import com.script.comminutioncraft.client.SavedMachineTooltip;
import net.neoforged.neoforge.common.NeoForge;
import com.script.comminutioncraft.client.gui.CoalSmelterScreen;
import com.script.comminutioncraft.client.gui.ElectricGeneratorScreen;
import com.script.comminutioncraft.client.gui.ElectricMachineScreen;
import com.script.comminutioncraft.client.gui.EnergyBatteryScreen;
import com.script.comminutioncraft.client.gui.AtmosphericWaterGeneratorScreen;
import com.script.comminutioncraft.client.gui.FluidMachineScreen;
import com.script.comminutioncraft.client.gui.PlasmaMachineScreen;
import com.script.comminutioncraft.client.gui.OreWasherScreen;
import com.script.comminutioncraft.common.machine.OreWasherMenu;
import com.script.comminutioncraft.common.machine.PlasmaMachineMenu;
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
    public ComminutionCraftClient(IEventBus modEventBus) {
        modEventBus.addListener(ComminutionCraftClient::registerScreens);
        modEventBus.addListener(ModDataGenerators::gatherData);
        NeoForge.EVENT_BUS.addListener(ClientRecipes::onRecipesReceived);
        modEventBus.addListener(ModKeyMappings::register);
        modEventBus.addListener(ComminutionCraftClient::registerRenderers);
        modEventBus.addListener(ComminutionCraftClient::registerFluidModels);
        NeoForge.EVENT_BUS.addListener(ModKeyMappings::onClientTick);
        NeoForge.EVENT_BUS.addListener(SavedMachineTooltip::onTooltip);
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
        event.register(ModMenus.PLASMA_GRANULATOR.get(), PlasmaMachineScreen::granulator);
        event.register(ModMenus.PLASMA_SMELTER.get(), PlasmaMachineScreen::smelter);
        event.register(ModMenus.PLASMA_ENERGY_BATTERY.get(), EnergyBatteryScreen::plasma);
        event.register(ModMenus.CIRCUIT_ASSEMBLER.get(), PlasmaMachineScreen::assembler);
        event.<PlasmaMachineMenu, OreWasherScreen>register(ModMenus.ORE_WASHER.get(),
                (menu, inventory, title) -> new OreWasherScreen((OreWasherMenu) menu, inventory, title));
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.FLUID_TANK.get(), context -> new FluidTankRenderer(context.sprites()));
    }

    /** Our fluids' textures (they have no world blocks, but tanks, GUIs and JEI draw them from these models). */
    private static void registerFluidModels(RegisterFluidModelsEvent event) {
        fluidModel(event, "plasma", ModFluids.PLASMA, ModFluids.FLOWING_PLASMA);
        fluidModel(event, "hydrogen", ModFluids.HYDROGEN, ModFluids.FLOWING_HYDROGEN);
        fluidModel(event, "oxygen", ModFluids.OXYGEN, ModFluids.FLOWING_OXYGEN);
    }

    private static void fluidModel(RegisterFluidModelsEvent event, String name,
                                   Supplier<? extends Fluid> still, Supplier<? extends Fluid> flowing) {
        event.register(new FluidModel.Unbaked(new Material(ComminutionCraft.id("block/" + name + "_still")),
                        new Material(ComminutionCraft.id("block/" + name + "_flow")), null, (FluidTintSource) null),
                still, flowing);
    }
}
