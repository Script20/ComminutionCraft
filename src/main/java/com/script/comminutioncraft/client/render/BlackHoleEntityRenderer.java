package com.script.comminutioncraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.script.comminutioncraft.common.entity.BlackHoleEntity;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import org.joml.Vector3f;

/** Draws a {@link BlackHoleEntity}: the same black hole as the Event Horizon Reactor's, without the field. */
public class BlackHoleEntityRenderer extends EntityRenderer<BlackHoleEntity, EntityRenderState> {
    public BlackHoleEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public EntityRenderState createRenderState() {
        return new EntityRenderState();
    }

    @Override
    protected boolean affectedByCulling(BlackHoleEntity entity) {
        return false; // the disc reaches well past its little bounding box
    }

    @Override
    public void submit(EntityRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        poseStack.pushPose();
        poseStack.translate(0, 0.45F, 0); // the middle of its box
        Vector3f toCamera = new Vector3f((float) (camera.pos.x - state.x), (float) (camera.pos.y - state.y - 0.45), (float) (camera.pos.z - state.z));
        BlackHoleRenderer.drawHole(poseStack, collector, camera.orientation, toCamera, state.ageInTicks / 20F);
        poseStack.popPose();
        super.submit(state, poseStack, collector, camera);
    }
}
