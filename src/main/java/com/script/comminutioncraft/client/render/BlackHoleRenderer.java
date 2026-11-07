package com.script.comminutioncraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.machine.EventHorizonReactorBlockEntity;
import com.script.comminutioncraft.common.machine.MachineBlock;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

/**
 * Draws a running Event Horizon Reactor's black hole and its field, from the layers of the approved mock-up
 * (design/previews/singularity_mockup.py; textures from tools/gen_black_hole_textures.py):
 * <ol>
 *   <li>a soft glow and the lensed halo arch (camera-facing), a little behind the hole;</li>
 *   <li>the shadow: a camera-facing black disc that writes depth, so the far half of the disc hides behind it;</li>
 *   <li>the accretion disc: flat rings round the hole, each spinning at its own (Kepler) speed, brighter on the side
 *       moving towards the camera;</li>
 *   <li>the field: a faint gold shimmer drifting up the inner 5x5x5's faces.</li>
 * </ol>
 * The light layers use the "eyes" render type (normal see-through blending, full bright, no depth writes) with
 * textures whose alpha is their brightness, so they keep their gold over a bright sky. {@link #drawHole} is shared
 * with the summonable {@link BlackHoleEntityRenderer} (no field) for looking at it on its own.
 */
public class BlackHoleRenderer implements BlockEntityRenderer<EventHorizonReactorBlockEntity, BlackHoleRenderer.State> {
    private static final Identifier HALO = ComminutionCraft.id("textures/entity/black_hole/halo.png");
    private static final Identifier GLOW = ComminutionCraft.id("textures/entity/black_hole/glow.png");
    private static final Identifier DISC = ComminutionCraft.id("textures/entity/black_hole/disc.png");
    private static final Identifier SHADOW_TEXTURE = ComminutionCraft.id("textures/entity/black_hole/shadow.png");
    private static final Identifier FIELD = ComminutionCraft.id("textures/entity/black_hole/field.png");

    private static final float SHADOW = 0.44F;      // shadow radius, blocks
    private static final float HALO_EXTENT = 2.4F;  // halo quad half-size, in shadow radii (as in the texture)
    private static final float DISC_INNER = 1.2F;   // inside the halo's sides, so the disc and the arch join
    private static final float DISC_OUTER = 5.4F;
    private static final float GLOW_SIZE = 2.6F;    // glow quad half-size, blocks
    private static final int DISC_BANDS = 8;
    private static final int DISC_SEGMENTS = 72;

    public static class State extends BlockEntityRenderState {
        boolean lit;
        /** The hole's middle, relative to the controller block's corner. */
        float cx;
        float cy;
        float cz;
        /** From the hole to the camera. */
        float camX;
        float camY;
        float camZ;
        /** Seconds, for the spin and shimmer. */
        float time;
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(EventHorizonReactorBlockEntity reactor, State state, float partialTicks, Vec3 cameraPosition,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(reactor, state, partialTicks, cameraPosition, breakProgress);
        state.lit = reactor.getBlockState().getValue(MachineBlock.LIT);
        BlockPos pos = reactor.getBlockPos();
        BlockPos centre = reactor.getCentre();
        state.cx = centre.getX() - pos.getX() + 0.5F;
        state.cy = centre.getY() - pos.getY() + 0.5F;
        state.cz = centre.getZ() - pos.getZ() + 0.5F;
        state.camX = (float) (cameraPosition.x - (centre.getX() + 0.5));
        state.camY = (float) (cameraPosition.y - (centre.getY() + 0.5));
        state.camZ = (float) (cameraPosition.z - (centre.getZ() + 0.5));
        state.time = reactor.getLevel() == null ? 0 : ((reactor.getLevel().getGameTime() % 24000) + partialTicks) / 20F;
    }

    @Override
    public AABB getRenderBoundingBox(EventHorizonReactorBlockEntity reactor) {
        return new AABB(reactor.getCentre()).inflate(EventHorizonReactorBlockEntity.HALF + 1).minmax(new AABB(reactor.getBlockPos()));
    }

    @Override
    public boolean shouldRenderOffScreen() {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 160;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
        if (!state.lit) {
            return;
        }
        poseStack.pushPose();
        poseStack.translate(state.cx, state.cy, state.cz);
        drawHole(poseStack, collector, camera.orientation, new Vector3f(state.camX, state.camY, state.camZ), state.time);
        drawField(poseStack, collector, state.time);
        poseStack.popPose();
    }

    // ---- Shared drawing (the pose's origin = the hole's middle) ----

    /** The black hole: glow, halo, shadow and disc. {@code toCamera} = from the hole to the camera (any length). */
    public static void drawHole(PoseStack poseStack, SubmitNodeCollector collector, Quaternionf facing, Vector3f toCamera, float time) {
        Vector3f dir = new Vector3f(toCamera);
        if (dir.lengthSquared() < 1.0E-4F) {
            dir.set(0, 0, 1);
        }
        dir.normalize();
        // glow and halo, nudged a little behind the hole (away from the camera) so the shadow covers their middles
        billboard(poseStack, collector, RenderTypes.eyes(GLOW), facing, dir, -0.3F, GLOW_SIZE, 0x90FFFFFF);
        billboard(poseStack, collector, RenderTypes.eyes(HALO), facing, dir, -0.05F, SHADOW * HALO_EXTENT, 0xFFFFFFFF);
        // the shadow (writes depth)
        billboard(poseStack, collector, RenderTypes.entityCutout(SHADOW_TEXTURE), facing, dir, 0, SHADOW, 0xFFFFFFFF);
        // the accretion disc
        collector.submitCustomGeometry(poseStack, RenderTypes.eyes(DISC), (pose, buffer) -> disc(buffer, pose, time, dir));
    }

    /** The deadly 5x5x5 field: a faint gold shimmer drifting up its faces. */
    public static void drawField(PoseStack poseStack, SubmitNodeCollector collector, float time) {
        float pulse = 0.8F + 0.2F * (float) Math.sin(time * 1.7);
        int alpha = Math.round(48 * pulse);
        float scroll = (time * 0.06F) % 1;
        collector.submitCustomGeometry(poseStack, RenderTypes.eyes(FIELD), (pose, buffer) -> field(buffer, pose, alpha << 24 | 0xFFFFFF, scroll));
    }

    /** A camera-facing square of half-size {@code size}, moved {@code depth} blocks towards the camera, both sides. */
    private static void billboard(PoseStack poseStack, SubmitNodeCollector collector, RenderType type, Quaternionf facing,
                                  Vector3f dir, float depth, float size, int color) {
        poseStack.pushPose();
        poseStack.translate(dir.x * depth, dir.y * depth, dir.z * depth);
        poseStack.mulPose(facing);
        // bottom-left first; the texture's top row goes at the top
        collector.submitCustomGeometry(poseStack, type, (pose, buffer) ->
                quad(buffer, pose, color, -size, -size, 0, size, -size, 0, size, size, 0, -size, size, 0, 0, 0, 1, 1));
        poseStack.popPose();
    }

    /** The disc as rings, each spinning at its own speed (inner faster), brighter where it moves towards the camera. */
    private static void disc(VertexConsumer buffer, PoseStack.Pose pose, float time, Vector3f dir) {
        float step = (DISC_OUTER - DISC_INNER) / DISC_BANDS;
        for (int band = 0; band < DISC_BANDS; band++) {
            float r0 = DISC_INNER + band * step;
            float r1 = r0 + step;
            float mid = (r0 + r1) / 2;
            float spin = time * 1.6F * (float) Math.pow(DISC_INNER / mid, 1.5);
            for (int i = 0; i < DISC_SEGMENTS; i++) {
                float a0 = (float) (i * Math.PI * 2 / DISC_SEGMENTS);
                float a1 = (float) ((i + 1) * Math.PI * 2 / DISC_SEGMENTS);
                int c0 = discColor(a0, dir);
                int c1 = discColor(a1, dir);
                // top side, then the underside (same corners reversed, same texture coordinates)
                discVertex(buffer, pose, r0, a0, spin, c0, 1);
                discVertex(buffer, pose, r1, a0, spin, c0, 1);
                discVertex(buffer, pose, r1, a1, spin, c1, 1);
                discVertex(buffer, pose, r0, a1, spin, c1, 1);
                discVertex(buffer, pose, r0, a1, spin, c1, -1);
                discVertex(buffer, pose, r1, a1, spin, c1, -1);
                discVertex(buffer, pose, r1, a0, spin, c0, -1);
                discVertex(buffer, pose, r0, a0, spin, c0, -1);
            }
        }
    }

    /** Doppler beaming: the side orbiting towards the camera is brighter (gently - both halves stay visible). */
    private static int discColor(float angle, Vector3f dir) {
        float vx = (float) -Math.sin(angle);
        float vz = (float) Math.cos(angle);
        float flat = (float) Math.sqrt(dir.x * dir.x + dir.z * dir.z);
        float towards = flat < 1.0E-3F ? 0 : (vx * dir.x + vz * dir.z) / flat * flat;
        float beam = 0.75F + 0.35F * towards;
        int a = Math.min(255, Math.round(255 * beam));
        return a << 24 | 0xFFFFFF;
    }

    private static void discVertex(VertexConsumer buffer, PoseStack.Pose pose, float radius, float angle, float spin, int color, float normalY) {
        float x = (float) Math.cos(angle) * radius * SHADOW;
        float z = (float) Math.sin(angle) * radius * SHADOW;
        float t = radius / DISC_OUTER * 0.5F;
        float u = 0.5F + t * (float) Math.cos(angle - spin);
        float v = 0.5F + t * (float) Math.sin(angle - spin);
        vertex(buffer, pose, color, x, 0, z, u, v, 0, normalY, 0);
    }

    /** The field's six faces (both sides), two tiles of the shimmer per face, drifting by {@code scroll}. */
    private static void field(VertexConsumer b, PoseStack.Pose pose, int color, float scroll) {
        float h = 2.5F;
        float[][] faces = {
                {-h, h, -h, -h, h, h, h, h, h, h, h, -h},       // top
                {-h, -h, -h, h, -h, -h, h, -h, h, -h, -h, h},   // bottom
                {h, -h, -h, -h, -h, -h, -h, h, -h, h, h, -h},   // north
                {-h, -h, h, h, -h, h, h, h, h, -h, h, h},       // south
                {-h, -h, -h, -h, -h, h, -h, h, h, -h, h, -h},   // west
                {h, -h, h, h, -h, -h, h, h, -h, h, h, h},       // east
        };
        for (float[] f : faces) {
            quad(b, pose, color, f[0], f[1], f[2], f[3], f[4], f[5], f[6], f[7], f[8], f[9], f[10], f[11], 0, scroll, 2, 2 + scroll);
        }
    }

    /**
     * A quad seen from both sides: a-b-c-d (a = bottom-left of the texture, then round anticlockwise), then the same
     * corners in reverse for the back - each corner keeps its texture coordinate, so the two sides line up exactly.
     */
    private static void quad(VertexConsumer b, PoseStack.Pose pose, int color,
                             float ax, float ay, float az, float bx, float by, float bz,
                             float cx, float cy, float cz, float dx, float dy, float dz,
                             float u0, float v0, float u1, float v1) {
        vertex(b, pose, color, ax, ay, az, u0, v1, 0, 0, 1);
        vertex(b, pose, color, bx, by, bz, u1, v1, 0, 0, 1);
        vertex(b, pose, color, cx, cy, cz, u1, v0, 0, 0, 1);
        vertex(b, pose, color, dx, dy, dz, u0, v0, 0, 0, 1);
        vertex(b, pose, color, dx, dy, dz, u0, v0, 0, 0, -1);
        vertex(b, pose, color, cx, cy, cz, u1, v0, 0, 0, -1);
        vertex(b, pose, color, bx, by, bz, u1, v1, 0, 0, -1);
        vertex(b, pose, color, ax, ay, az, u0, v1, 0, 0, -1);
    }

    private static void vertex(VertexConsumer b, PoseStack.Pose pose, int color, float x, float y, float z,
                               float u, float v, float nx, float ny, float nz) {
        b.addVertex(pose, x, y, z)
                .setColor(color)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightCoordsUtil.FULL_BRIGHT)
                .setNormal(pose, nx, ny, nz);
    }
}
