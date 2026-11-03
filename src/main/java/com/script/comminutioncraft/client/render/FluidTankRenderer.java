package com.script.comminutioncraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.script.comminutioncraft.common.machine.FluidTankBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.fluid.FluidTintSource;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jspecify.annotations.Nullable;

/**
 * Draws a Fluid Tank's contents as a box inside its glass, as high as the tank is full, using the fluid's own
 * still texture and tint (so water is blue, plasma glows). Fluids that give off light are drawn full bright.
 */
public class FluidTankRenderer implements BlockEntityRenderer<FluidTankBlockEntity, FluidTankRenderer.State> {
    // Inside the window (texture columns 3-12, rows 1-14), slightly in from the faces.
    private static final float MIN = 3.05F / 16;
    private static final float MAX = 12.95F / 16;
    private static final float BOTTOM = 1.05F / 16;
    private static final float HEIGHT = 13.9F / 16;

    public static class State extends BlockEntityRenderState {
        @Nullable TextureAtlasSprite sprite;
        int color;
        float fill;
        boolean glows;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(FluidTankBlockEntity tank, State state, float partialTicks, Vec3 cameraPosition,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(tank, state, partialTicks, cameraPosition, breakProgress);
        FluidStack stack = tank.getTank().getStack();
        if (stack.isEmpty() || !(tank.getLevel() instanceof BlockAndTintGetter level)) {
            state.sprite = null;
            return;
        }
        FluidState fluidState = stack.getFluid().defaultFluidState();
        FluidModel model = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(fluidState);
        state.sprite = model.stillMaterial().sprite();
        FluidTintSource tint = model.fluidTintSource();
        state.color = tint == null ? 0xFFFFFFFF
                : 0xFF000000 | tint.colorInWorld(fluidState, tank.getBlockState(), level, tank.getBlockPos());
        state.fill = Math.min(1.0F, (float) stack.getAmount() / FluidTankBlockEntity.CAPACITY);
        state.glows = stack.getFluidType().getLightLevel(stack) > 0;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        TextureAtlasSprite sprite = state.sprite;
        if (sprite == null || state.fill <= 0) {
            return;
        }
        float top = BOTTOM + HEIGHT * state.fill;
        int light = state.glows ? LightCoordsUtil.FULL_BRIGHT : state.lightCoords;
        int color = state.color;
        float vTop = 1 - (top - BOTTOM) / HEIGHT; // the side texture is cropped from the top of the sprite down
        collector.submitCustomGeometry(poseStack, Sheets.translucentBlockSheet(), (pose, buffer) -> {
            Face face = new Face(buffer, pose, sprite, color, light);
            // top
            face.quad(0, 1, 0, MIN, top, MIN, 0, 0, MIN, top, MAX, 0, 1, MAX, top, MAX, 1, 1, MAX, top, MIN, 1, 0);
            // bottom
            face.quad(0, -1, 0, MIN, BOTTOM, MIN, 0, 0, MAX, BOTTOM, MIN, 1, 0, MAX, BOTTOM, MAX, 1, 1, MIN, BOTTOM, MAX, 0, 1);
            // north (-z), south (+z), west (-x), east (+x)
            face.quad(0, 0, -1, MAX, top, MIN, 0, vTop, MAX, BOTTOM, MIN, 0, 1, MIN, BOTTOM, MIN, 1, 1, MIN, top, MIN, 1, vTop);
            face.quad(0, 0, 1, MIN, top, MAX, 0, vTop, MIN, BOTTOM, MAX, 0, 1, MAX, BOTTOM, MAX, 1, 1, MAX, top, MAX, 1, vTop);
            face.quad(-1, 0, 0, MIN, top, MIN, 0, vTop, MIN, BOTTOM, MIN, 0, 1, MIN, BOTTOM, MAX, 1, 1, MIN, top, MAX, 1, vTop);
            face.quad(1, 0, 0, MAX, top, MAX, 0, vTop, MAX, BOTTOM, MAX, 0, 1, MAX, BOTTOM, MIN, 1, 1, MAX, top, MIN, 1, vTop);
        });
    }

    /** Emits textured quads (four corners as x, y, z, u, v with u/v from 0 to 1 across the sprite). */
    private record Face(VertexConsumer buffer, PoseStack.Pose pose, TextureAtlasSprite sprite, int color, int light) {
        void quad(float nx, float ny, float nz,
                  float x0, float y0, float z0, float u0, float v0,
                  float x1, float y1, float z1, float u1, float v1,
                  float x2, float y2, float z2, float u2, float v2,
                  float x3, float y3, float z3, float u3, float v3) {
            vertex(nx, ny, nz, x0, y0, z0, u0, v0);
            vertex(nx, ny, nz, x1, y1, z1, u1, v1);
            vertex(nx, ny, nz, x2, y2, z2, u2, v2);
            vertex(nx, ny, nz, x3, y3, z3, u3, v3);
        }

        private void vertex(float nx, float ny, float nz, float x, float y, float z, float u, float v) {
            buffer.addVertex(pose, x, y, z)
                    .setColor(color)
                    .setUv(sprite.getU(u), sprite.getV(v))
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(light)
                    .setNormal(pose, nx, ny, nz);
        }
    }
}
