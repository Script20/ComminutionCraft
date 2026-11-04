package com.script.comminutioncraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.machine.FusionReactorBlockEntity;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Draws a burning Fusion Reactor's core: a glowing ring of tritium-green plasma in the empty block behind the
 * controller, seen through the Reactor Glass. Full bright; it pulses a little.
 */
public class FusionReactorRenderer implements BlockEntityRenderer<FusionReactorBlockEntity, FusionReactorRenderer.State> {
    private static final SpriteId CORE = new SpriteId(TextureAtlas.LOCATION_BLOCKS, ComminutionCraft.id("block/tritium_still"));

    private final SpriteGetter sprites;

    public FusionReactorRenderer(SpriteGetter sprites) {
        this.sprites = sprites;
    }

    public static class State extends BlockEntityRenderState {
        boolean burning;
        int dx;
        int dy;
        int dz;
        float time;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(FusionReactorBlockEntity reactor, State state, float partialTicks, Vec3 cameraPosition,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(reactor, state, partialTicks, cameraPosition, breakProgress);
        state.burning = reactor.isBurning();
        BlockPos core = reactor.getCorePos().subtract(reactor.getBlockPos());
        state.dx = core.getX();
        state.dy = core.getY();
        state.dz = core.getZ();
        state.time = reactor.getLevel() == null ? 0 : (reactor.getLevel().getGameTime() % 80 + partialTicks) / 80;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!state.burning) {
            return;
        }
        TextureAtlasSprite sprite = sprites.get(CORE);
        float pulse = 0.06F * (float) Math.sin(state.time * Math.PI * 2);
        poseStack.pushPose();
        poseStack.translate(state.dx, state.dy, state.dz);
        collector.submitCustomGeometry(poseStack, Sheets.translucentBlockSheet(), (pose, buffer) -> {
            // A square ring (four bars) round the middle of the core block, and a dimmer heart.
            float lo = 0.18F - pulse;
            float hi = 0.82F + pulse;
            float w = 0.14F;
            float y0 = 0.44F;
            float y1 = 0.56F;
            box(buffer, pose, sprite, 0xFFFFFFFF, lo, y0, lo, hi, y1, lo + w);
            box(buffer, pose, sprite, 0xFFFFFFFF, lo, y0, hi - w, hi, y1, hi);
            box(buffer, pose, sprite, 0xFFFFFFFF, lo, y0, lo + w, lo + w, y1, hi - w);
            box(buffer, pose, sprite, 0xFFFFFFFF, hi - w, y0, lo + w, hi, y1, hi - w);
            box(buffer, pose, sprite, 0xA0FFFFFF, 0.42F, 0.42F, 0.42F, 0.58F, 0.58F, 0.58F);
        });
        poseStack.popPose();
    }

    /** An outward-facing box (all six faces), the sprite stretched over each face. */
    private static void box(VertexConsumer b, PoseStack.Pose pose, TextureAtlasSprite s, int color,
                            float x0, float y0, float z0, float x1, float y1, float z1) {
        quad(b, pose, s, color, 0, 1, 0, x0, y1, z0, x0, y1, z1, x1, y1, z1, x1, y1, z0);
        quad(b, pose, s, color, 0, -1, 0, x0, y0, z0, x1, y0, z0, x1, y0, z1, x0, y0, z1);
        quad(b, pose, s, color, 0, 0, -1, x1, y1, z0, x1, y0, z0, x0, y0, z0, x0, y1, z0);
        quad(b, pose, s, color, 0, 0, 1, x0, y1, z1, x0, y0, z1, x1, y0, z1, x1, y1, z1);
        quad(b, pose, s, color, -1, 0, 0, x0, y1, z0, x0, y0, z0, x0, y0, z1, x0, y1, z1);
        quad(b, pose, s, color, 1, 0, 0, x1, y1, z1, x1, y0, z1, x1, y0, z0, x1, y1, z0);
    }

    private static void quad(VertexConsumer b, PoseStack.Pose pose, TextureAtlasSprite s, int color, float nx, float ny, float nz,
                             float ax, float ay, float az, float bx, float by, float bz,
                             float cx, float cy, float cz, float dx, float dy, float dz) {
        vertex(b, pose, s, color, nx, ny, nz, ax, ay, az, 0, 0);
        vertex(b, pose, s, color, nx, ny, nz, bx, by, bz, 0, 1);
        vertex(b, pose, s, color, nx, ny, nz, cx, cy, cz, 1, 1);
        vertex(b, pose, s, color, nx, ny, nz, dx, dy, dz, 1, 0);
    }

    private static void vertex(VertexConsumer b, PoseStack.Pose pose, TextureAtlasSprite s, int color, float nx, float ny, float nz,
                               float x, float y, float z, float u, float v) {
        b.addVertex(pose, x, y, z)
                .setColor(color)
                .setUv(s.getU(u), s.getV(v))
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightCoordsUtil.FULL_BRIGHT)
                .setNormal(pose, nx, ny, nz);
    }
}
