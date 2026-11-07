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
                    .icon(() -> new ItemStack(ModItems.WRENCH.get()))
                    .displayItems((parameters, output) -> {
                        // Tools
                        output.accept(ModItems.WRENCH.get());
                        // Processing, by tier
                        output.accept(ModItems.COAL_GRANULATOR.get());
                        output.accept(ModItems.COAL_SMELTER.get());
                        output.accept(ModItems.ELECTRIC_GRANULATOR.get());
                        output.accept(ModItems.ELECTRIC_SMELTER.get());
                        output.accept(ModItems.PLASMA_GRANULATOR.get());
                        output.accept(ModItems.PLASMA_SMELTER.get());
                        output.accept(ModItems.ORE_WASHER.get());
                        output.accept(ModItems.ORE_PURIFIER.get());
                        output.accept(ModItems.ANTIMATTER_GRANULATOR.get());
                        output.accept(ModItems.ANTIMATTER_SMELTER.get());
                        output.accept(ModItems.FROTH_FLOTATION_CELL.get());
                        output.accept(ModItems.ANTIMATTER_WASHER.get());
                        output.accept(ModItems.ANTIMATTER_PURIFIER.get());
                        output.accept(ModItems.SINGULARITY_PROCESSOR.get());
                        // Machines
                        output.accept(ModItems.ATMOSPHERIC_WATER_GENERATOR.get());
                        output.accept(ModItems.ELECTROLYSER.get());
                        // Power
                        output.accept(ModItems.ELECTRIC_GENERATOR.get());
                        output.accept(ModItems.PLASMA_GENERATOR.get());
                        output.accept(ModItems.MHD_GENERATOR.get());
                        output.accept(ModItems.ANNIHILATION_GENERATOR.get());
                        // Fusion reactor
                        output.accept(ModItems.REACTOR_CONTROLLER.get());
                        output.accept(ModItems.REACTOR_CASING.get());
                        output.accept(ModItems.REACTOR_GLASS.get());
                        output.accept(ModItems.REACTOR_PORT.get());
                        // Particle accelerator
                        output.accept(ModItems.ACCELERATOR_CONTROLLER.get());
                        output.accept(ModItems.ACCELERATOR_SEGMENT.get());
                        output.accept(ModItems.ACCELERATOR_PORT.get());
                        // Event horizon reactor
                        output.accept(ModItems.HORIZON_CONTROLLER.get());
                        output.accept(ModItems.CONTAINMENT_FRAME.get());
                        output.accept(ModItems.HORIZON_PORT.get());
                        // Fuel and parts
                        output.accept(ModItems.CIRCUIT_ASSEMBLER.get());
                        output.accept(ModItems.ISOTOPE_SEPARATOR.get());
                        output.accept(ModItems.PELLET_PRESS.get());
                        output.accept(ModItems.TRITIUM_BREEDER.get());
                        // Storage
                        output.accept(ModItems.ENERGY_BATTERY.get());
                        output.accept(ModItems.PLASMA_ENERGY_BATTERY.get());
                        output.accept(ModItems.FUSION_ENERGY_BATTERY.get());
                        output.accept(ModItems.ANTIMATTER_ENERGY_BATTERY.get());
                        output.accept(ModItems.SINGULARITY_ENERGY_BATTERY.get());
                        output.accept(ModItems.FLUID_TANK.get());
                        output.accept(ModItems.CONTAINMENT_CELL.get());
                        output.accept(ModItems.SOARYN_CHEST.get());
                        // Cables and pipes
                        output.accept(ModItems.ENERGY_CABLE.get());
                        output.accept(ModItems.FUSION_CABLE.get());
                        output.accept(ModItems.ANTIMATTER_CABLE.get());
                        output.accept(ModItems.SINGULARITY_CABLE.get());
                        output.accept(ModItems.FLUID_PIPE.get());
                        output.accept(ModItems.PLASMA_CABLE.get());
                        output.accept(ModItems.MAGNETIC_CONDUIT.get());
                        // Items
                        output.accept(ModItems.ELECTRONIC_CIRCUIT.get());
                        output.accept(ModItems.PLASMA_CIRCUIT.get());
                        output.accept(ModItems.FUSION_CIRCUIT.get());
                        output.accept(ModItems.ANTIMATTER_CIRCUIT.get());
                        output.accept(ModItems.SINGULARITY_CIRCUIT.get());
                        output.accept(ModItems.SINGULARITY_SHARD.get());
                        output.accept(ModItems.AIR_FILTER.get());
                        output.accept(ModItems.REACTOR_PLATING.get());
                        output.accept(ModItems.FUSION_FUEL_PELLET.get());
                        // Ores: washed, purified, granulated per metal. Other mods' metals only appear when some installed mod adds them.
                        ProcessedMetal.ALL.stream()
                                .filter(ProcessedMetal::isAvailable)
                                .forEach(metal -> {
                                    output.accept(metal.concentrate().get());
                                    output.accept(metal.washed().get());
                                    output.accept(metal.purified().get());
                                    output.accept(metal.granulated().get());
                                });
                        // Creative only
                        output.accept(ModItems.CREATIVE_ENERGY_BATTERY.get());
                        output.accept(ModItems.CREATIVE_FLUID_TANK.get());
                        // Decorative stone
                        ModBlocks.STONE_SETS.forEach(set -> set.all().forEach(block -> output.accept(block.get())));
                    })
                    .build());

    private ModCreativeTabs() {}
}
