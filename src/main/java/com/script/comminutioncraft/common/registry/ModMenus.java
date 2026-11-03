package com.script.comminutioncraft.common.registry;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.machine.ElectricGeneratorMenu;
import com.script.comminutioncraft.common.machine.ElectricMachineMenu;
import com.script.comminutioncraft.common.machine.EnergyBatteryMenu;
import com.script.comminutioncraft.common.machine.FuelMachineMenu;
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

    private ModMenus() {}
}
