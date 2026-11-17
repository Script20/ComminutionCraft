package com.script.comminutioncraft.client.multiblock;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Matrix3x2f;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

/**
 * A multiblock shown in 3D in a GUI area (JEI page or guide book): its turn (right-drag), zoom, the layer shown, and
 * which block is under the mouse. Drawn by {@link MultiblockRenderer}.
 */
public final class MultiblockView {
    private static final float DEFAULT_PITCH = 30;
    /** Degrees per second while it spins by itself. */
    private static final float SPIN_SPEED = 10;

    private final MultiblockStructure structure;
    private float yaw;
    private float pitch = DEFAULT_PITCH;
    private float zoom = 1;
    /** -1 = every layer, else only that one (0 = bottom). */
    private int layer = -1;
    /** Turns slowly by itself until someone turns it by hand. */
    private boolean spinning;
    private long spinStart;

    /** {@code yaw} 0 looks at the north face; 180 at the front (south). */
    public MultiblockView(MultiblockStructure structure, float yaw, boolean spinning) {
        this.structure = structure;
        this.yaw = yaw;
        this.spinning = spinning;
        spinStart = Util.getMillis();
    }

    public MultiblockStructure structure() {
        return structure;
    }

    public int layer() {
        return layer;
    }

    public void setLayer(int layer) {
        this.layer = layer;
    }

    public boolean shows(BlockPos pos) {
        return layer < 0 || pos.getY() == layer;
    }

    private float currentYaw() {
        return spinning ? (yaw + (Util.getMillis() - spinStart) / 1000F * SPIN_SPEED) % 360 : yaw;
    }

    /** Turns it by a mouse drag of (dx, dy) GUI pixels: sideways spins it, up/down tilts it. */
    public void rotate(double dx, double dy) {
        yaw = (currentYaw() + (float) dx * 1.5F) % 360;
        spinning = false;
        pitch = Mth.clamp(pitch + (float) dy * 1.5F, -90, 90);
    }

    /** Scroll: zoom in (up) or out. */
    public void zoom(double scroll) {
        zoom = Mth.clamp(zoom * (float) Math.pow(1.15, scroll), 0.5F, 4);
    }

    /** View transform in the area's own GUI units, around its middle (x right, y down, the viewer at -z). */
    private Matrix4f view(float width, float height) {
        float across = Mth.sqrt(structure.sizeX() * structure.sizeX() + structure.sizeZ() * structure.sizeZ());
        float tall = structure.sizeY() * Mth.cos(DEFAULT_PITCH * Mth.DEG_TO_RAD) + across * Mth.sin(DEFAULT_PITCH * Mth.DEG_TO_RAD);
        float perBlock = 0.9F * Math.min(width / across, height / tall) * zoom;
        return new Matrix4f()
                .rotateZ(Mth.PI) // world up = screen up
                .rotateX(-pitch * Mth.DEG_TO_RAD)
                .rotateY(currentYaw() * Mth.DEG_TO_RAD)
                .scale(perBlock)
                .translate(-structure.sizeX() / 2F, -structure.sizeY() / 2F, -structure.sizeZ() / 2F);
    }

    /** Draws it filling (x, y, width, height) in the graphics' current pose. */
    public void extract(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
        Matrix3x2f pose = graphics.pose();
        Vector2f from = pose.transformPosition(new Vector2f(x, y));
        Vector2f to = pose.transformPosition(new Vector2f(x + width, y + height));
        int x0 = Math.round(from.x);
        int y0 = Math.round(from.y);
        int x1 = Math.round(to.x);
        int y1 = Math.round(to.y);
        if (x1 <= x0 || y1 <= y0) {
            return;
        }
        graphics.submitPictureInPictureRenderState(new MultiblockRenderState(structure, layer, view(width, height),
                x0, y0, x1, y1, (float) (x1 - x0) / width, graphics.peekScissorStack()));
    }

    /** The block under (mouseX, mouseY), both relative to the area's top left; null over empty space. */
    public @Nullable BlockPos blockAt(double mouseX, double mouseY, int width, int height) {
        if (mouseX < 0 || mouseY < 0 || mouseX >= width || mouseY >= height) {
            return null;
        }
        Matrix4f inverse = view(width, height).invert();
        float mx = (float) mouseX - width / 2F;
        float my = (float) mouseY - height / 2F;
        Vector3f start = inverse.transformPosition(new Vector3f(mx, my, -10_000));
        Vector3f dir = inverse.transformPosition(new Vector3f(mx, my, 10_000)).sub(start);
        BlockPos best = null;
        float bestT = Float.MAX_VALUE;
        for (Map.Entry<BlockPos, BlockState> e : structure.blocks().entrySet()) {
            BlockPos p = e.getKey();
            if (!shows(p)) {
                continue;
            }
            float t = hit(start, dir, p);
            if (t < bestT) {
                bestT = t;
                best = p;
            }
        }
        return best;
    }

    /** Where along the ray it first enters the block's cube (slab test), or MAX_VALUE if it misses. */
    private static float hit(Vector3f start, Vector3f dir, BlockPos p) {
        float near = -Float.MAX_VALUE;
        float far = Float.MAX_VALUE;
        float[] s = {start.x, start.y, start.z};
        float[] d = {dir.x, dir.y, dir.z};
        int[] lo = {p.getX(), p.getY(), p.getZ()};
        for (int axis = 0; axis < 3; axis++) {
            if (Math.abs(d[axis]) < 1e-7F) {
                if (s[axis] < lo[axis] || s[axis] > lo[axis] + 1) {
                    return Float.MAX_VALUE;
                }
                continue;
            }
            float a = (lo[axis] - s[axis]) / d[axis];
            float b = (lo[axis] + 1 - s[axis]) / d[axis];
            near = Math.max(near, Math.min(a, b));
            far = Math.min(far, Math.max(a, b));
        }
        return near <= far && far >= 0 ? near : Float.MAX_VALUE;
    }

    /** The block's name and what it's for. */
    public List<Component> tooltip(BlockPos pos) {
        List<Component> lines = new ArrayList<>();
        BlockState state = structure.blocks().get(pos);
        if (state == null) {
            return lines;
        }
        lines.add(state.getBlock().getName());
        Component rule = structure.rule(state.getBlock());
        if (rule != null) {
            lines.add(rule.copy().withStyle(ChatFormatting.GRAY));
        }
        return lines;
    }

    /** The "how to turn it" line for tooltips. */
    public static Component hint() {
        return Component.translatable("gui.comminutioncraft.multiblock.rotate").withStyle(ChatFormatting.DARK_GRAY);
    }
}
