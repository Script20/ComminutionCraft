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
 *   <li>a soft glow and the lensed halo arch (camera-facing, additive), a little behind the hole;</li>
 *   <li>the shadow: a camera-facing black disc that writes depth, so the far half of the disc hides behind it;</li>
 *   <li>the accretion disc: flat rings round the hole, each spinning at its own (Kepler) speed, brighter on the side
 *       moving towards the camera;</li>
 *   <li>the field: a faint, scrolling gold shimmer on the inner 5x5x5's faces (seen from both sides).</li>
 * </ol>
 * Sizes are in shadow radii, like the textures: the shadow is {@link #SHADOW} blocks across its radius.
 */
public class BlackHoleRenderer implements BlockEntityRenderer<EventHorizonReactorBlockEntity, BlackHoleRenderer.State> {
    private static final Identifier HALO = ComminutionCraft.id("textures/entity/black_hole/halo.png");
    private static final Identifier GLOW = ComminutionCraft.id("textures/entity/black_hole/glow.png");
    private static final Identifier DISC = ComminutionCraft.id("textures/entity/black_hole/disc.png");
    private static final Identifier SHADOW_TEXTURE = ComminutionCraft.id("textures/entity/black_hole/shadow.png");
    private static final Identifier FIELD = ComminutionCraft.id("textures/entity/black_hole/field.png");

    private static final float SHADOW = 0.44F;      // shadow radius, blocks
    private static final float HALO_EXTENT = 2.4F;  // halo quad half-size, in shadow radii (as in the texture)
    private static final float DISC_INNER = 1.45F;
    private static final float DISC_OUTER = 5.4F;
    private static final float GLOW_SIZE = 3.4F;    // glow quad half-size, blocks
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
        Vector3f toCamera = new Vector3f(state.camX, state.camY, state.camZ);
        if (toCamera.lengthSquared() < 1.0E-4F) {
            toCamera.set(0, 0, 1);
        }
        toCamera.normalize();
        Quaternionf facing = camera.orientation;

        poseStack.pushPose();
        poseStack.translate(state.cx, state.cy, state.cz);

        // 1. glow and halo, nudged a little behind the hole (away from the camera) so the shadow covers their middles
        billboard(poseStack, collector, RenderTypes.energySwirl(GLOW, 0, 0), facing, toCamera, -0.3F, GLOW_SIZE, 0xFFFFFFFF);
        billboard(poseStack, collector, RenderTypes.energySwirl(HALO, 0, 0), facing, toCamera, -0.05F, SHADOW * HALO_EXTENT, 0xFFFFFFFF);

        // 2. the shadow (writes depth)
        billboard(poseStack, collector, RenderTypes.entityCutout(SHADOW_TEXTURE), facing, toCamera, 0, SHADOW, 0xFFFFFFFF);

        // 3. the accretion disc
        collector.submitCustomGeometry(poseStack, RenderTypes.energySwirl(DISC, 0, 0), (pose, buffer) -> disc(buffer, pose, state, toCamera));

        // 4. the field: the inner 5x5x5's faces, a faint gold shimmer drifting upwards
        float pulse = 0.75F + 0.25F * (float) Math.sin(state.time * 1.7);
        int c = Math.round(70 * pulse);
        int fieldColor = 0xFF000000 | c << 16 | c << 8 | c;
        collector.submitCustomGeometry(poseStack, RenderTypes.energySwirl(FIELD, 0, -state.time * 0.08F),
                (pose, buffer) -> field(buffer, pose, fieldColor));
        poseStack.popPose();
    }

    /** A camera-facing square of half-size {@code size}, moved {@code depth} blocks towards the camera, both sides. */
    private static void billboard(PoseStack poseStack, SubmitNodeCollector collector, net.minecraft.client.renderer.rendertype.RenderType type,
                                  Quaternionf facing, Vector3f toCamera, float depth, float size, int color) {
        poseStack.pushPose();
        poseStack.translate(toCamera.x * depth, toCamera.y * depth, toCamera.z * depth);
        poseStack.mulPose(facing);
        collector.submitCustomGeometry(poseStack, type, (pose, buffer) -> {
            quad(buffer, pose, color, -size, -size, 0, size, -size, 0, size, size, 0, -size, size, 0, 0, 1, 1, 0);
            quad(buffer, pose, color, -size, size, 0, size, size, 0, size, -size, 0, -size, -size, 0, 0, 0, 1, 1);
        });
        poseStack.popPose();
    }

    /** The disc as rings, each spinning at its own speed (inner faster), brighter where it moves towards the camera. */
    private static void disc(VertexConsumer buffer, PoseStack.Pose pose, State state, Vector3f toCamera) {
        float step = (DISC_OUTER - DISC_INNER) / DISC_BANDS;
        for (int band = 0; band < DISC_BANDS; band++) {
            float r0 = DISC_INNER + band * step;
            float r1 = r0 + step;
            float mid = (r0 + r1) / 2;
            float spin = state.time * 1.6F * (float) Math.pow(DISC_INNER / mid, 1.5);
            for (int i = 0; i < DISC_SEGMENTS; i++) {
                float a0 = (float) (i * Math.PI * 2 / DISC_SEGMENTS);
                float a1 = (float) ((i + 1) * Math.PI * 2 / DISC_SEGMENTS);
                int c0 = discColor(a0, mid, toCamera);
                int c1 = discColor(a1, mid, toCamera);
                // top side and underside (wound the other way)
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

    /** Doppler beaming: brighter where the orbit moves towards the camera. */
    private static int discColor(float angle, float radius, Vector3f toCamera) {
        float vx = (float) -Math.sin(angle);
        float vz = (float) Math.cos(angle);
        float px = (float) Math.cos(angle) * radius * SHADOW;
        float pz = (float) Math.sin(angle) * radius * SHADOW;
        // direction from this point to the camera ~ toCamera (the camera is far compared with the disc), nudged by the offset
        float dx = toCamera.x * 8 - px;
        float dz = toCamera.z * 8 - pz;
        float len = (float) Math.sqrt(dx * dx + dz * dz + toCamera.y * toCamera.y * 64);
        float beam = 1 + 0.85F * (vx * dx + vz * dz) / Math.max(len, 1.0E-3F);
        int c = Math.min(255, Math.round(150 * Math.max(0.25F, beam)));
        return 0xFF000000 | c << 16 | c << 8 | c;
    }

    private static void discVertex(VertexConsumer buffer, PoseStack.Pose pose, float radius, float angle, float spin, int color, float normalY) {
        float x = (float) Math.cos(angle) * radius * SHADOW;
        float z = (float) Math.sin(angle) * radius * SHADOW;
        float t = radius / DISC_OUTER * 0.5F;
        float u = 0.5F + t * (float) Math.cos(angle - spin);
        float v = 0.5F + t * (float) Math.sin(angle - spin);
        vertex(buffer, pose, color, x, 0, z, u, v, 0, normalY, 0);
    }

    /** The field's six faces, from both sides, two tiles of the shimmer per face. */
    private static void field(VertexConsumer b, PoseStack.Pose pose, int color) {
        float h = 2.5F;
        float[][] faces = {
                {-h, h, -h, -h, h, h, h, h, h, h, h, -h},       // top
                {-h, -h, -h, h, -h, -h, h, -h, h, -h, -h, h},   // bottom
                {h, h, -h, h, -h, -h, -h, -h, -h, -h, h, -h},   // north
                {-h, h, h, -h, -h, h, h, -h, h, h, h, h},       // south
                {-h, h, -h, -h, -h, -h, -h, -h, h, -h, h, h},   // west
                {h, h, h, h, -h, h, h, -h, -h, h, h, -h},       // east
        };
        for (float[] f : faces) {
            quad(b, pose, color, f[0], f[1], f[2], f[3], f[4], f[5], f[6], f[7], f[8], f[9], f[10], f[11], 0, 0, 2, 2);
            quad(b, pose, color, f[9], f[10], f[11], f[6], f[7], f[8], f[3], f[4], f[5], f[0], f[1], f[2], 0, 0, 2, 2);
        }
    }

    private static void quad(VertexConsumer b, PoseStack.Pose pose, int color,
                             float ax, float ay, float az, float bx, float by, float bz,
                             float cx, float cy, float cz, float dx, float dy, float dz,
                             float u0, float v0, float u1, float v1) {
        vertex(b, pose, color, ax, ay, az, u0, v1, 0, 0, 1);
        vertex(b, pose, color, bx, by, bz, u1, v1, 0, 0, 1);
        vertex(b, pose, color, cx, cy, cz, u1, v0, 0, 0, 1);
        vertex(b, pose, color, dx, dy, dz, u0, v0, 0, 0, 1);
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
