package com.script.comminutioncraft.common.registry;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.machine.ElectricGeneratorMenu;
import com.script.comminutioncraft.common.machine.ElectricMachineMenu;
import com.script.comminutioncraft.common.machine.EnergyBatteryMenu;
import com.script.comminutioncraft.common.machine.FuelMachineMenu;
import com.script.comminutioncraft.common.machine.AtmosphericWaterGeneratorMenu;
import com.script.comminutioncraft.common.machine.ElectrolyserBlockEntity;
import com.script.comminutioncraft.common.machine.FluidMachineMenu;
import com.script.comminutioncraft.common.machine.MhdGeneratorBlockEntity;
import com.script.comminutioncraft.common.machine.IsotopeSeparatorBlockEntity;
import com.script.comminutioncraft.common.machine.PelletPressMenu;
import com.script.comminutioncraft.common.machine.TritiumBreederBlockEntity;
import com.script.comminutioncraft.common.machine.FusionReactorMenu;
import com.script.comminutioncraft.common.machine.OrePurifierMenu;
import com.script.comminutioncraft.common.machine.CreativeFluidTankMenu;
import com.script.comminutioncraft.common.machine.PlasmaGeneratorBlockEntity;
import com.script.comminutioncraft.common.machine.PlasmaMachineMenu;
import com.script.comminutioncraft.common.machine.OreWasherMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS = DeferredRegister.create(Registries.MENU, ComminutionCraft.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<FuelMachineMenu>> COAL_GRANULATOR =
            MENUS.register("coal_granulator", () -> new MenuType<>(FuelMachineMenu::clientGranulator, FeatureFlags.VANILLA_SET));

    public static final DeferredHolder<MenuType<?>, MenuType<FuelMachineMenu>> COAL_SMELTER =
            MENUS.register("coal_smelter", () -> new MenuType<>(FuelMachineMenu::clientSmelter, FeatureFlags.VANILLA_SET));

    public static final DeferredHolder<MenuType<?>, MenuType<ElectricMachineMenu>> ELECTRIC_GRANULATOR =
            MENUS.register("electric_granulator", () -> new MenuType<>(ElectricMachineMenu::clientGranulator, FeatureFlags.VANILLA_SET));

    public static final DeferredHolder<MenuType<?>, MenuType<ElectricMachineMenu>> ELECTRIC_SMELTER =
            MENUS.register("electric_smelter", () -> new MenuType<>(ElectricMachineMenu::clientSmelter, FeatureFlags.VANILLA_SET));

    public static final DeferredHolder<MenuType<?>, MenuType<ElectricGeneratorMenu>> ELECTRIC_GENERATOR =
            MENUS.register("electric_generator", () -> new MenuType<>(ElectricGeneratorMenu::client, FeatureFlags.VANILLA_SET));

    public static final DeferredHolder<MenuType<?>, MenuType<EnergyBatteryMenu>> ENERGY_BATTERY =
            MENUS.register("energy_battery", () -> new MenuType<>(EnergyBatteryMenu::client, FeatureFlags.VANILLA_SET));

    public static final DeferredHolder<MenuType<?>, MenuType<FluidMachineMenu>> PLASMA_GENERATOR = MENUS.register("plasma_generator",
            () -> new MenuType<>((id, inventory) -> FluidMachineMenu.client(ModMenus.PLASMA_GENERATOR.get(), id, inventory,
                    PlasmaGeneratorBlockEntity.TANK_COUNT), FeatureFlags.VANILLA_SET));

    public static final DeferredHolder<MenuType<?>, MenuType<AtmosphericWaterGeneratorMenu>> ATMOSPHERIC_WATER_GENERATOR =
            MENUS.register("atmospheric_water_generator", () -> new MenuType<>(AtmosphericWaterGeneratorMenu::client, FeatureFlags.VANILLA_SET));

    public static final DeferredHolder<MenuType<?>, MenuType<FluidMachineMenu>> ELECTROLYSER = MENUS.register("electrolyser",
            () -> new MenuType<>((id, inventory) -> FluidMachineMenu.client(ModMenus.ELECTROLYSER.get(), id, inventory,
                    ElectrolyserBlockEntity.TANK_COUNT), FeatureFlags.VANILLA_SET));

    public static final DeferredHolder<MenuType<?>, MenuType<FluidMachineMenu>> MHD_GENERATOR = MENUS.register("mhd_generator",
            () -> new MenuType<>((id, inventory) -> FluidMachineMenu.client(ModMenus.MHD_GENERATOR.get(), id, inventory,
                    MhdGeneratorBlockEntity.TANK_COUNT), FeatureFlags.VANILLA_SET));

    public static final DeferredHolder<MenuType<?>, MenuType<ElectricMachineMenu>> PLASMA_GRANULATOR =
            MENUS.register("plasma_granulator", () -> new MenuType<>(ElectricMachineMenu::clientPlasmaGranulator, FeatureFlags.VANILLA_SET));

    public static final DeferredHolder<MenuType<?>, MenuType<ElectricMachineMenu>> PLASMA_SMELTER =
            MENUS.register("plasma_smelter", () -> new MenuType<>(ElectricMachineMenu::clientPlasmaSmelter, FeatureFlags.VANILLA_SET));

    public static final DeferredHolder<MenuType<?>, MenuType<EnergyBatteryMenu>> PLASMA_ENERGY_BATTERY =
            MENUS.register("plasma_energy_battery", () -> new MenuType<>(EnergyBatteryMenu::clientPlasma, FeatureFlags.VANILLA_SET));

    public static final DeferredHolder<MenuType<?>, MenuType<PlasmaMachineMenu>> CIRCUIT_ASSEMBLER =
            MENUS.register("circuit_assembler", () -> new MenuType<>(PlasmaMachineMenu::clientAssembler, FeatureFlags.VANILLA_SET));

    public static final DeferredHolder<MenuType<?>, MenuType<OreWasherMenu>> ORE_WASHER =
            MENUS.register("ore_washer", () -> new MenuType<>(OreWasherMenu::client, FeatureFlags.VANILLA_SET));

    public static final DeferredHolder<MenuType<?>, MenuType<FluidMachineMenu>> ISOTOPE_SEPARATOR = MENUS.register("isotope_separator",
            () -> new MenuType<>((id, inventory) -> FluidMachineMenu.client(ModMenus.ISOTOPE_SEPARATOR.get(), id, inventory,
                    IsotopeSeparatorBlockEntity.TANK_COUNT), FeatureFlags.VANILLA_SET));

    public static final DeferredHolder<MenuType<?>, MenuType<FluidMachineMenu>> TRITIUM_BREEDER = MENUS.register("tritium_breeder",
            () -> new MenuType<>((id, inventory) -> FluidMachineMenu.client(ModMenus.TRITIUM_BREEDER.get(), id, inventory,
                    TritiumBreederBlockEntity.TANK_COUNT), FeatureFlags.VANILLA_SET));

    public static final DeferredHolder<MenuType<?>, MenuType<PelletPressMenu>> PELLET_PRESS =
            MENUS.register("pellet_press", () -> new MenuType<>(PelletPressMenu::client, FeatureFlags.VANILLA_SET));

    public static final DeferredHolder<MenuType<?>, MenuType<FusionReactorMenu>> FUSION_REACTOR =
            MENUS.register("reactor_controller", () -> new MenuType<>(FusionReactorMenu::client, FeatureFlags.VANILLA_SET));

    public static final DeferredHolder<MenuType<?>, MenuType<EnergyBatteryMenu>> FUSION_ENERGY_BATTERY =
            MENUS.register("fusion_energy_battery", () -> new MenuType<>(EnergyBatteryMenu::clientFusion, FeatureFlags.VANILLA_SET));

    public static final DeferredHolder<MenuType<?>, MenuType<OrePurifierMenu>> ORE_PURIFIER =
            MENUS.register("ore_purifier", () -> new MenuType<>(OrePurifierMenu::client, FeatureFlags.VANILLA_SET));

    public static final DeferredHolder<MenuType<?>, MenuType<CreativeFluidTankMenu>> CREATIVE_FLUID_TANK =
            MENUS.register("creative_fluid_tank", () -> new MenuType<>(CreativeFluidTankMenu::client, FeatureFlags.VANILLA_SET));

    private ModMenus() {}
}
