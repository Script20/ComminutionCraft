package com.script.comminutioncraft.client.registry;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.client.multiblock.MultiblockRenderState;
import com.script.comminutioncraft.client.multiblock.MultiblockRenderer;
import com.script.comminutioncraft.client.render.BlackHoleEntityRenderer;
import com.script.comminutioncraft.client.render.BlackHoleRenderer;
import com.script.comminutioncraft.client.render.FluidTankRenderer;
import com.script.comminutioncraft.client.render.FusionReactorRenderer;
import com.script.comminutioncraft.client.render.ItemPipeRenderer;
import com.script.comminutioncraft.client.render.TankItemRenderer;
import com.script.comminutioncraft.common.registry.ModBlockEntities;
import com.script.comminutioncraft.common.registry.ModEntities;
import com.script.comminutioncraft.common.registry.ModFluids;
import java.util.function.Supplier;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.blockentity.ChestRenderer;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterFluidModelsEvent;
import net.neoforged.neoforge.client.event.RegisterPictureInPictureRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterSpecialModelRendererEvent;
import net.neoforged.neoforge.client.fluid.FluidTintSource;

/** Everything drawn by our own code: block entity and entity renderers, fluid models, item and GUI renderers. */
public final class ModRenderers {
    private ModRenderers() {
    }

    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        // tanks draw their inner walls and contents
        event.registerBlockEntityRenderer(ModBlockEntities.FLUID_TANK.get(), context -> new FluidTankRenderer(context.sprites()));
        event.registerBlockEntityRenderer(ModBlockEntities.CONTAINMENT_CELL.get(), context -> new FluidTankRenderer(context.sprites()));
        event.registerBlockEntityRenderer(ModBlockEntities.CREATIVE_FLUID_TANK.get(), context -> new FluidTankRenderer(context.sprites()));
        // items moving through Item Pipes
        event.registerBlockEntityRenderer(ModBlockEntities.ITEM_PIPE.get(), ItemPipeRenderer::new);
        // the Fusion Reactor's glowing core, the Event Horizon Reactor's black hole (and /summon's)
        event.registerBlockEntityRenderer(ModBlockEntities.FUSION_REACTOR.get(), context -> new FusionReactorRenderer(context.sprites()));
        event.registerBlockEntityRenderer(ModBlockEntities.EVENT_HORIZON_REACTOR.get(), context -> new BlackHoleRenderer());
        event.registerEntityRenderer(ModEntities.BLACK_HOLE.get(), BlackHoleEntityRenderer::new);
        event.registerBlockEntityRenderer(ModBlockEntities.SOARYN_CHEST.get(), ChestRenderer::new);
    }

    /** Our fluids' textures (they have no world blocks, but tanks, GUIs and JEI draw them from these models). */
    public static void registerFluidModels(RegisterFluidModelsEvent event) {
        fluidModel(event, "plasma", ModFluids.PLASMA, ModFluids.FLOWING_PLASMA);
        fluidModel(event, "hydrogen", ModFluids.HYDROGEN, ModFluids.FLOWING_HYDROGEN);
        fluidModel(event, "oxygen", ModFluids.OXYGEN, ModFluids.FLOWING_OXYGEN);
        fluidModel(event, "deuterium", ModFluids.DEUTERIUM, ModFluids.FLOWING_DEUTERIUM);
        fluidModel(event, "tritium", ModFluids.TRITIUM, ModFluids.FLOWING_TRITIUM);
        fluidModel(event, "antimatter", ModFluids.ANTIMATTER, ModFluids.FLOWING_ANTIMATTER);
    }

    /** Tank items draw the fluid they hold (wrench pickup). */
    public static void registerItemRenderers(RegisterSpecialModelRendererEvent event) {
        event.register(TankItemRenderer.TYPE, TankItemRenderer.Unbaked.MAP_CODEC);
    }

    /** 3D multiblocks on the JEI pages and in the guide book. */
    public static void registerGuiRenderers(RegisterPictureInPictureRenderersEvent event) {
        event.register(MultiblockRenderState.class, MultiblockRenderer::new);
    }

    private static void fluidModel(RegisterFluidModelsEvent event, String name,
                                   Supplier<? extends Fluid> still, Supplier<? extends Fluid> flowing) {
        event.register(new FluidModel.Unbaked(new Material(ComminutionCraft.id("block/" + name + "_still")),
                        new Material(ComminutionCraft.id("block/" + name + "_flow")), null, (FluidTintSource) null),
                still, flowing);
    }
}
