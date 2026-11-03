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
import com.script.comminutioncraft.client.gui.PlasmaGeneratorScreen;
import com.script.comminutioncraft.client.render.FluidTankRenderer;
import com.script.comminutioncraft.common.registry.ModBlockEntities;
import com.script.comminutioncraft.common.registry.ModFluids;
import net.minecraft.client.renderer.block.FluidModel;
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
        event.register(ModMenus.ENERGY_BATTERY.get(), EnergyBatteryScreen::new);
        event.register(ModMenus.PLASMA_GENERATOR.get(), PlasmaGeneratorScreen::new);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModBlockEntities.FLUID_TANK.get(), context -> new FluidTankRenderer());
    }

    /** Plasma's textures (it has no world block, but tanks, GUIs and JEI draw it from this model). */
    private static void registerFluidModels(RegisterFluidModelsEvent event) {
        event.register(new FluidModel.Unbaked(new Material(ComminutionCraft.id("block/plasma_still")),
                        new Material(ComminutionCraft.id("block/plasma_flow")), null, (FluidTintSource) null),
                ModFluids.PLASMA, ModFluids.FLOWING_PLASMA);
    }
}
