package com.script.comminutioncraft.common.registry;

import com.script.comminutioncraft.common.block.ModWoods;
import com.script.comminutioncraft.common.compat.patchouli.GuideBook;
import com.script.comminutioncraft.common.machine.miner.OreMinerBlockEntity;
import com.script.comminutioncraft.common.network.SetGhostItemPayload;
import com.script.comminutioncraft.common.network.SwitchDrillModePayload;
import com.script.comminutioncraft.common.network.SwitchWrenchModePayload;
import com.script.comminutioncraft.gametest.ModGameTests;
import net.neoforged.bus.api.IEventBus;

/** Everything the mod registers on the mod event bus: the deferred registers and the registration events. */
public final class ModRegistries {
    private ModRegistries() {
    }

    public static void register(IEventBus modEventBus) {
        ModFluids.FLUID_TYPES.register(modEventBus);
        ModFluids.FLUIDS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModDataComponents.DATA_COMPONENTS.register(modEventBus);
        ModFeatures.FEATURES.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITY_TYPES.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModSounds.SOUND_EVENTS.register(modEventBus);
        ModTriggers.TRIGGERS.register(modEventBus);
        GuideBook.ATTACHMENTS.register(modEventBus); // who has been given the guide book (Patchouli, optional)
        ModMenus.MENUS.register(modEventBus);
        ModRecipes.RECIPE_TYPES.register(modEventBus);
        ModRecipes.RECIPE_SERIALIZERS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        ModGameTests.TEST_FUNCTIONS.register(modEventBus);

        modEventBus.addListener(ModCapabilities::register);
        modEventBus.addListener(ModDataMaps::register);
        modEventBus.addListener(OreMinerBlockEntity::registerTickets);
        modEventBus.addListener(ModGameTests::registerTests);
        modEventBus.addListener(SwitchWrenchModePayload::register);
        modEventBus.addListener(SwitchDrillModePayload::register);
        modEventBus.addListener(SetGhostItemPayload::register);
        ModWoods.register(modEventBus);
    }
}
