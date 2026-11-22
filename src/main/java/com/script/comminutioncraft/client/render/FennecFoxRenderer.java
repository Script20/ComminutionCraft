package com.script.comminutioncraft.client.render;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.entity.FennecFox;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.renderer.entity.AgeableMobRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Draws the Fennec Fox (adult and baby models), sitting when it's been told to. */
public class FennecFoxRenderer extends AgeableMobRenderer<FennecFox, FennecFoxRenderer.State, FennecFoxModel> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(ComminutionCraft.id("fennec_fox"), "main");
    public static final ModelLayerLocation BABY_LAYER = new ModelLayerLocation(ComminutionCraft.id("fennec_fox_baby"), "main");
    private static final Identifier TEXTURE = ComminutionCraft.id("textures/entity/fennec_fox.png");

    /** What the model needs each frame besides the usual living-entity state. */
    public static class State extends LivingEntityRenderState {
        public boolean sitting;
    }

    public FennecFoxRenderer(EntityRendererProvider.Context context) {
        super(context, new FennecFoxModel(context.bakeLayer(LAYER)), new FennecFoxModel(context.bakeLayer(BABY_LAYER)), 0.3F);
    }

    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(LAYER, FennecFoxModel::createBodyLayer);
        event.registerLayerDefinition(BABY_LAYER, FennecFoxModel::createBabyLayer);
    }

    @Override
    public Identifier getTextureLocation(State state) {
        return TEXTURE;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(FennecFox fox, State state, float partialTicks) {
        super.extractRenderState(fox, state, partialTicks);
        state.sitting = fox.isInSittingPose();
    }
}
