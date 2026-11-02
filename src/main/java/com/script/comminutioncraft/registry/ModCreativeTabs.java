package com.script.comminutioncraft.registry;

import com.script.comminutioncraft.ComminutionCraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ComminutionCraft.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MAIN = CREATIVE_MODE_TABS.register("main",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.comminutioncraft"))
                    .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
                    .icon(() -> new ItemStack(ModItems.COAL_GRANULATOR.get()))
                    .displayItems((parameters, output) -> {
                        output.accept(ModItems.COAL_GRANULATOR.get());
                        output.accept(ModItems.COAL_SMELTER.get());
                        output.accept(ModItems.ELECTRIC_GENERATOR.get());
                        output.accept(ModItems.ELECTRIC_GRANULATOR.get());
                        output.accept(ModItems.ELECTRIC_SMELTER.get());
                        output.accept(ModItems.ENERGY_CABLE.get());
                        output.accept(ModItems.WRENCH.get());
                        output.accept(ModItems.GRANULATED_IRON.get());
                        output.accept(ModItems.GRANULATED_COPPER.get());
                        output.accept(ModItems.GRANULATED_GOLD.get());
                    })
                    .build());

    private ModCreativeTabs() {}
}
