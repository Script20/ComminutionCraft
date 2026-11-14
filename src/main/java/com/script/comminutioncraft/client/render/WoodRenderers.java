package com.script.comminutioncraft.client.render;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.registry.ModBlocks;
import com.script.comminutioncraft.common.registry.ModEntities;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.object.boat.BoatModel;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.entity.BoatRenderer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/**
 * Client side of the Purpleheart wood: the sign sprites ({@code textures/entity/signs/purpleheart.png} and
 * {@code .../hanging/purpleheart.png}; vanilla's sign renderers and model layers already cover every registered wood
 * type) and the boats, which use vanilla's boat models with our textures ({@code textures/entity/boat/purpleheart.png},
 * {@code .../chest_boat/purpleheart.png}, named after the model layer).
 */
public final class WoodRenderers {
    public static final ModelLayerLocation PURPLEHEART_BOAT = new ModelLayerLocation(ComminutionCraft.id("boat/purpleheart"), "main");
    public static final ModelLayerLocation PURPLEHEART_CHEST_BOAT = new ModelLayerLocation(ComminutionCraft.id("chest_boat/purpleheart"), "main");

    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(WoodRenderers::clientSetup);
        modEventBus.addListener(WoodRenderers::layers);
        modEventBus.addListener(WoodRenderers::renderers);
    }

    private static void clientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(() -> Sheets.addWoodType(ModBlocks.PURPLEHEART.woodType));
    }

    private static void layers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(PURPLEHEART_BOAT, BoatModel::createBoatModel);
        event.registerLayerDefinition(PURPLEHEART_CHEST_BOAT, BoatModel::createChestBoatModel);
    }

    private static void renderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.PURPLEHEART_BOAT.get(), context -> new BoatRenderer(context, PURPLEHEART_BOAT));
        event.registerEntityRenderer(ModEntities.PURPLEHEART_CHEST_BOAT.get(), context -> new BoatRenderer(context, PURPLEHEART_CHEST_BOAT));
    }

    private WoodRenderers() {}
}
