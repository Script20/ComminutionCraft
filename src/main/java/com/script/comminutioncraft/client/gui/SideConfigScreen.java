package com.script.comminutioncraft.client.gui;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.machine.control.ControllableMenu;
import com.script.comminutioncraft.common.machine.control.MachineControls;
import com.script.comminutioncraft.common.machine.control.RelativeSide;
import com.script.comminutioncraft.common.machine.control.SideMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.AbstractContainerMenu;
import org.joml.Matrix3x2f;
import org.jspecify.annotations.Nullable;

/**
 * Side configuration: the machine drawn as a cube with its own textures, each face tinted by its {@link SideMode}.
 * Left-click a face to cycle Input / Output / None; right-drag to turn the cube (a right-click without dragging turns
 * it a quarter). Changes go to the server as menu button clicks, so the parent machine menu stays open throughout.
 * <p>
 * Each visible face of an orthographically projected cube is a parallelogram, i.e. an affine image of the square
 * texture, so faces are drawn with a 2D affine pose and picked by inverting that same mapping.
 */
public class SideConfigScreen extends Screen {
    private static final int WIDTH = 176;
    private static final int HEIGHT = 166;
    private static final int CUBE_HALF = 34;

    private static final int PANEL_OUTLINE = 0xFF101114;
    private static final int PANEL_TOP = 0xFF3C4049;
    private static final int PANEL_BOTTOM = 0xFF2A2D34;
    private static final int PANEL_HIGHLIGHT = 0xFF545964;
    private static final int TITLE_COLOR = 0xFFE4E6EC;
    private static final int TEXT_COLOR = 0xFF9EA4B0;

    /** A face in machine space (cube from -1 to 1, front towards +Z): texture origin corner, u and v edges, normal. */
    private record Face(RelativeSide side, double[] origin, double[] u, double[] v, double[] normal) {}

    private static final List<Face> FACES = List.of(
            new Face(RelativeSide.FRONT, v(-1, 1, 1), v(2, 0, 0), v(0, -2, 0), v(0, 0, 1)),
            new Face(RelativeSide.BACK, v(1, 1, -1), v(-2, 0, 0), v(0, -2, 0), v(0, 0, -1)),
            new Face(RelativeSide.LEFT, v(-1, 1, -1), v(0, 0, 2), v(0, -2, 0), v(-1, 0, 0)),
            new Face(RelativeSide.RIGHT, v(1, 1, 1), v(0, 0, -2), v(0, -2, 0), v(1, 0, 0)),
            new Face(RelativeSide.TOP, v(-1, 1, -1), v(2, 0, 0), v(0, 0, 2), v(0, 1, 0)),
            new Face(RelativeSide.BOTTOM, v(-1, -1, 1), v(2, 0, 0), v(0, 0, -2), v(0, -1, 0)));

    /** A face as it appears on screen: corner P0 plus edge vectors U and V, and how much it faces the viewer. */
    private record Projected(Face face, double x, double y, double ux, double uy, double vx, double vy, double facing) {
        boolean contains(double px, double py) {
            double det = ux * vy - uy * vx;
            if (Math.abs(det) < 1e-6) {
                return false;
            }
            double dx = px - x;
            double dy = py - y;
            double a = (dx * vy - dy * vx) / det;
            double b = (ux * dy - uy * dx) / det;
            return a >= 0 && a <= 1 && b >= 0 && b <= 1;
        }
    }

    private final Screen parent;
    private final AbstractContainerMenu menu;
    private final ControllableMenu controls;
    private final String machineName;
    private final int accent;

    private int left;
    private int top;
    private float yaw = 35;
    private float pitch = 25;
    private boolean rotating;
    private double dragDistance;

    public <M extends AbstractContainerMenu & ControllableMenu> SideConfigScreen(Screen parent, M menu, int accent) {
        super(Component.translatable("gui.comminutioncraft.side_config"));
        this.parent = parent;
        this.menu = menu;
        this.controls = menu;
        // Menu ids match block ids (coal_granulator, electric_generator...), which name the textures.
        this.machineName = BuiltInRegistries.MENU.getKey(menu.getType()).getPath();
        this.accent = accent;
    }

    @Override
    protected void init() {
        left = (width - WIDTH) / 2;
        top = (height - HEIGHT) / 2;
        addRenderableWidget(Button.builder(Component.translatable("gui.done"), b -> onClose())
                .bounds(left + (WIDTH - 70) / 2, top + HEIGHT - 26, 70, 20)
                .build());
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void tick() {
        // The server closed the machine (e.g. it was broken): leave too.
        if (minecraft.player == null || minecraft.player.containerMenu != menu) {
            minecraft.setScreen(null);
        }
    }

    // ---- Drawing ----

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractBackground(graphics, mouseX, mouseY, a);
        int x1 = left + WIDTH;
        int y1 = top + HEIGHT;
        graphics.fill(left + 1, top, x1 - 1, y1, PANEL_OUTLINE);
        graphics.fill(left, top + 1, x1, y1 - 1, PANEL_OUTLINE);
        graphics.fillGradient(left + 1, top + 1, x1 - 1, y1 - 1, PANEL_TOP, PANEL_BOTTOM);
        graphics.fill(left + 2, top + 1, x1 - 2, top + 2, PANEL_HIGHLIGHT);
        // Seams either side of the title, like the machine GUIs
        int titleWidth = font.width(title);
        int seamY = top + 9;
        graphics.fill(left + 8, seamY, left + (WIDTH - titleWidth) / 2 - 4, seamY + 1, accent);
        graphics.fill(left + (WIDTH + titleWidth) / 2 + 4, seamY, x1 - 8, seamY + 1, accent);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        super.extractRenderState(graphics, mouseX, mouseY, a);
        graphics.centeredText(font, title, left + WIDTH / 2, top + 6, TITLE_COLOR);

        List<Projected> faces = visibleFaces();
        Projected hovered = faceAt(faces, mouseX, mouseY);
        for (Projected face : faces) {
            drawFace(graphics, face, face == hovered);
        }

        drawLegend(graphics);
        graphics.centeredText(font, Component.translatable("gui.comminutioncraft.side_config.help"),
                left + WIDTH / 2, top + HEIGHT - 38, TEXT_COLOR);

        if (hovered != null) {
            RelativeSide side = hovered.face().side();
            graphics.setTooltipForNextFrame(Component.translatable("gui.comminutioncraft.side_config.tooltip",
                    side.displayName(), controls.getSideMode(side).displayName()), mouseX, mouseY);
        }
    }

    private void drawFace(GuiGraphicsExtractor graphics, Projected face, boolean hovered) {
        RelativeSide side = face.face().side();
        // Faces turned away from the light are darker, so the cube reads as 3D.
        int light = Mth.clamp((int) (255 * (0.55 + 0.45 * face.facing())), 0, 255);
        int tint = 0xFF000000 | light << 16 | light << 8 | light;
        SideMode mode = controls.getSideMode(side);

        graphics.pose().pushMatrix();
        graphics.pose().mul(new Matrix3x2f(
                (float) face.ux() / 16, (float) face.uy() / 16,
                (float) face.vx() / 16, (float) face.vy() / 16,
                (float) face.x(), (float) face.y()));
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture(side), 0, 0, 0, 0, 16, 16, 16, 16, tint);
        graphics.fill(0, 0, 16, 16, (mode.color & 0x00FFFFFF) | (mode == SideMode.NONE ? 0x50000000 : 0x70000000));
        if (hovered) {
            graphics.fill(0, 0, 16, 16, 0x40FFFFFF);
        }
        graphics.pose().popMatrix();
    }

    private Identifier texture(RelativeSide side) {
        String suffix = switch (side) {
            case FRONT -> "_front";
            case TOP, BOTTOM -> "_top";
            default -> "_side";
        };
        return ComminutionCraft.id("textures/block/" + machineName + suffix + ".png");
    }

    private void drawLegend(GuiGraphicsExtractor graphics) {
        SideMode[] modes = SideMode.values();
        int[] widths = new int[modes.length];
        int total = 0;
        for (int i = 0; i < modes.length; i++) {
            widths[i] = 10 + font.width(modes[i].displayName()) + 8;
            total += widths[i];
        }
        int x = left + (WIDTH - total) / 2;
        int y = top + HEIGHT - 50;
        for (int i = 0; i < modes.length; i++) {
            graphics.fill(x, y + 1, x + 7, y + 8, modes[i].color);
            graphics.text(font, modes[i].displayName(), x + 10, y, TEXT_COLOR, false);
            x += widths[i];
        }
    }

    // ---- Projection ----

    private List<Projected> visibleFaces() {
        double cx = left + WIDTH / 2.0;
        double cy = top + 66;
        List<Projected> result = new ArrayList<>();
        for (Face face : FACES) {
            double[] n = rotate(face.normal());
            if (n[2] <= 0.01) {
                continue;
            }
            double[] o = rotate(face.origin());
            double[] u = rotate(face.u());
            double[] w = rotate(face.v());
            result.add(new Projected(face,
                    cx + o[0] * CUBE_HALF, cy - o[1] * CUBE_HALF,
                    u[0] * CUBE_HALF, -u[1] * CUBE_HALF,
                    w[0] * CUBE_HALF, -w[1] * CUBE_HALF,
                    n[2]));
        }
        // Far faces first (a convex cube's visible faces never overlap, but keep the order sensible).
        result.sort(Comparator.comparingDouble(Projected::facing));
        return result;
    }

    /** Yaw around Y, then pitch around X. +Z points out of the screen. */
    private double[] rotate(double[] p) {
        double ry = Math.toRadians(yaw);
        double rx = Math.toRadians(pitch);
        double x1 = p[0] * Math.cos(ry) + p[2] * Math.sin(ry);
        double z1 = -p[0] * Math.sin(ry) + p[2] * Math.cos(ry);
        double y2 = p[1] * Math.cos(rx) - z1 * Math.sin(rx);
        double z2 = p[1] * Math.sin(rx) + z1 * Math.cos(rx);
        return v(x1, y2, z2);
    }

    private static @Nullable Projected faceAt(List<Projected> faces, double mouseX, double mouseY) {
        Projected best = null;
        for (Projected face : faces) {
            if (face.contains(mouseX, mouseY) && (best == null || face.facing() > best.facing())) {
                best = face;
            }
        }
        return best;
    }

    private static double[] v(double x, double y, double z) {
        return new double[] {x, y, z};
    }

    // ---- Input ----

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0) {
            Projected face = faceAt(visibleFaces(), event.x(), event.y());
            if (face != null) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId,
                        MachineControls.BUTTON_FIRST_SIDE + face.face().side().ordinal());
                minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                return true;
            }
        } else if (event.button() == 1) {
            rotating = true;
            dragDistance = 0;
            return true;
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dx, double dy) {
        if (rotating && event.button() == 1) {
            yaw = Mth.wrapDegrees(yaw + (float) dx * 1.5F);
            pitch = Mth.clamp(pitch + (float) dy * 1.5F, -85, 85);
            dragDistance += Math.abs(dx) + Math.abs(dy);
            return true;
        }
        return super.mouseDragged(event, dx, dy);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (rotating && event.button() == 1) {
            rotating = false;
            if (dragDistance < 2) {
                // A plain right-click turns the machine a quarter.
                yaw = Mth.wrapDegrees(yaw + 90);
            }
            return true;
        }
        return super.mouseReleased(event);
    }
}
