package com.script.comminutioncraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.script.comminutioncraft.ComminutionCraft;
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
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.client.renderer.block.BlockAndTintGetter;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.fluid.FluidTintSource;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jspecify.annotations.Nullable;

/**
 * Draws a Fluid Tank's inside: its own walls facing inwards (the block model is a cube with see-through windows, so
 * without them you'd look straight through the tank), and the contents as a box filling the tank, as high as the
 * window shows it full, using the fluid's own still texture and tint (water is blue, plasma glows). Fluids that give
 * off light are drawn full bright.
 */
public class FluidTankRenderer implements BlockEntityRenderer<FluidTankBlockEntity, FluidTankRenderer.State> {
    private static final float WALL = 0.002F;   // inner walls, just inside the block faces
    private static final float FLUID = 0.01F;   // the fluid, just inside the walls
    // The window spans texture rows 1-14: an empty tank shows nothing, a full one fills the window.
    private static final float WINDOW_BOTTOM = 1F / 16;
    private static final float WINDOW_HEIGHT = 14F / 16;

    private final SpriteGetter sprites;

    public FluidTankRenderer(SpriteGetter sprites) {
        this.sprites = sprites;
    }

    public static class State extends BlockEntityRenderState {
        /** The tank block's own side / end textures (Fluid Tank or Creative Fluid Tank) for the inner walls. */
        @Nullable SpriteId side;
        @Nullable SpriteId end;
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
        String block = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(tank.getBlockState().getBlock()).getPath();
        state.side = new SpriteId(TextureAtlas.LOCATION_BLOCKS, ComminutionCraft.id("block/" + block + "_side"));
        state.end = new SpriteId(TextureAtlas.LOCATION_BLOCKS, ComminutionCraft.id("block/" + block + "_top"));
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
        if (state.side == null || state.end == null) {
            return;
        }
        TextureAtlasSprite side = sprites.get(state.side);
        TextureAtlasSprite end = sprites.get(state.end);
        TextureAtlasSprite fluid = state.sprite;
        int light = state.lightCoords;
        collector.submitCustomGeometry(poseStack, Sheets.translucentBlockSheet(), (pose, buffer) -> {
            box(new Face(buffer, pose, end, 0xFFFFFFFF, light), new Face(buffer, pose, side, 0xFFFFFFFF, light),
                    WALL, WALL, 1 - WALL, 1 - WALL, 0, true);
            if (fluid != null && state.fill > 0) {
                float top = WINDOW_BOTTOM + WINDOW_HEIGHT * state.fill;
                Face face = new Face(buffer, pose, fluid, state.color, state.glows ? LightCoordsUtil.FULL_BRIGHT : light);
                box(face, face, FLUID, FLUID, 1 - FLUID, top, 1 - (top - FLUID), false);
            }
        });
    }

    /**
     * A box from (min, bottom, min) to (max, top, max): `ends` on the top and bottom, `sides` round it, faces pointing
     * out (or in, for the walls). The sides show the sprite from `vTop` down, so a part-full box isn't squashed.
     */
    private static void box(Face ends, Face sides, float min, float bottom, float max, float top, float vTop,
                            boolean inward) {
        ends.quad(inward, 0, 1, 0, min, top, min, 0, 0, min, top, max, 0, 1, max, top, max, 1, 1, max, top, min, 1, 0);
        ends.quad(inward, 0, -1, 0, min, bottom, min, 0, 0, max, bottom, min, 1, 0, max, bottom, max, 1, 1, min, bottom, max, 0, 1);
        // north (-z), south (+z), west (-x), east (+x)
        sides.quad(inward, 0, 0, -1, max, top, min, 0, vTop, max, bottom, min, 0, 1, min, bottom, min, 1, 1, min, top, min, 1, vTop);
        sides.quad(inward, 0, 0, 1, min, top, max, 0, vTop, min, bottom, max, 0, 1, max, bottom, max, 1, 1, max, top, max, 1, vTop);
        sides.quad(inward, -1, 0, 0, min, top, min, 0, vTop, min, bottom, min, 0, 1, min, bottom, max, 1, 1, min, top, max, 1, vTop);
        sides.quad(inward, 1, 0, 0, max, top, max, 0, vTop, max, bottom, max, 0, 1, max, bottom, min, 1, 1, max, top, min, 1, vTop);
    }

    /** Emits textured quads (four corners as x, y, z, u, v with u/v from 0 to 1 across the sprite). */
    private record Face(VertexConsumer buffer, PoseStack.Pose pose, TextureAtlasSprite sprite, int color, int light) {
        void quad(boolean inward, float nx, float ny, float nz,
                  float x0, float y0, float z0, float u0, float v0,
                  float x1, float y1, float z1, float u1, float v1,
                  float x2, float y2, float z2, float u2, float v2,
                  float x3, float y3, float z3, float u3, float v3) {
            if (inward) { // reversed winding faces the other way (back faces are culled)
                vertex(-nx, -ny, -nz, x3, y3, z3, u3, v3);
                vertex(-nx, -ny, -nz, x2, y2, z2, u2, v2);
                vertex(-nx, -ny, -nz, x1, y1, z1, u1, v1);
                vertex(-nx, -ny, -nz, x0, y0, z0, u0, v0);
            } else {
                vertex(nx, ny, nz, x0, y0, z0, u0, v0);
                vertex(nx, ny, nz, x1, y1, z1, u1, v1);
                vertex(nx, ny, nz, x2, y2, z2, u2, v2);
                vertex(nx, ny, nz, x3, y3, z3, u3, v3);
            }
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
