package com.script.comminutioncraft.common.registry;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.data.ProcessedMetal;
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
                        ModBlocks.STONE_SETS.forEach(set -> set.all().forEach(block -> output.accept(block.get())));
                        // Other mods' metals only appear when some installed mod adds that metal.
                        ProcessedMetal.ALL.stream()
                                .filter(ProcessedMetal::isAvailable)
                                .forEach(metal -> output.accept(metal.granulated().get()));
                    })
                    .build());

    private ModCreativeTabs() {}
}
