package com.script.comminutioncraft.client.compat.jei;

import com.mojang.blaze3d.platform.InputConstants;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.inputs.IJeiInputHandler;
import mezz.jei.api.gui.inputs.IJeiUserInput;
import mezz.jei.api.gui.widgets.IRecipeExtrasBuilder;
import mezz.jei.api.gui.widgets.IRecipeWidget;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

/**
 * A multiblock's JEI page (owner's design, mock-up design/previews/jei_multiblock.png): the structure as a 3D model
 * (each block drawn as its own 3D item icon, back to front), a ▲ button stepping up one layer at a time (past the top
 * it shows the whole structure again) and ▼ stepping down, the layer shown between them, the blocks needed (for the
 * whole thing or the layer: icon + count, name and where it goes on hover), and a row of what goes in and out.
 */
public abstract class MultiblockCategory<T> extends AbstractRecipeCategory<T> {
    protected static final int WIDTH = 170;
    private static final int MODEL_W = 126;
    private static final int MODEL_H = 96;
    private static final int BUTTON_X = 140;
    private static final int UP_Y = 4;
    private static final int DOWN_Y = 74;
    private static final int BUTTON = 18;
    private static final int LIST_Y = MODEL_H + 4;
    /** Where subclasses put their in -> out row. */
    protected static final int IO_Y = LIST_Y + 22;
    protected static final int HEIGHT = IO_Y + 19;

    private final int sizeX;
    private final int sizeY;
    private final int sizeZ;
    private final Map<BlockPos, ItemLike> blocks;
    private final Map<Item, Component> rules = new LinkedHashMap<>();
    /** Pixel steps of the projection (for an item icon drawn at {@link #scale} x 16 px). */
    private final float scale;
    private final float originX;
    private final float originY;

    protected MultiblockCategory(IRecipeType<T> type, Component title, IDrawable icon, int sizeX, int sizeY, int sizeZ,
                                 Map<BlockPos, ItemLike> blocks) {
        super(type, title, icon, WIDTH, HEIGHT);
        this.sizeX = sizeX;
        this.sizeY = sizeY;
        this.sizeZ = sizeZ;
        this.blocks = blocks;
        // Fit the whole structure in the model area; the layers keep the same scale and place.
        float wide = (sizeX + sizeZ) * 7.07F + 6;
        float tall = (sizeX + sizeZ) * 3.54F + sizeY * 8.66F + 6;
        scale = Math.min(2.0F, Math.min(MODEL_W / wide, MODEL_H / tall));
        float minX = Float.MAX_VALUE, maxX = -Float.MAX_VALUE, minY = Float.MAX_VALUE, maxY = -Float.MAX_VALUE;
        for (BlockPos p : blocks.keySet()) {
            float[] s = project(p.getX(), p.getY(), p.getZ(), 1);
            minX = Math.min(minX, s[0]);
            maxX = Math.max(maxX, s[0]);
            minY = Math.min(minY, s[1]);
            maxY = Math.max(maxY, s[1]);
        }
        originX = MODEL_W / 2F - (minX + maxX) / 2 * scale;
        originY = MODEL_H / 2F - (minY + maxY) / 2 * scale;
    }

    /** What may go where, shown when hovering that block in the list. */
    protected void rule(ItemLike item, Component text) {
        rules.putIfAbsent(item.asItem(), text);
    }

    /** Screen offset of a block's middle, for icons drawn at scale k. */
    private static float[] project(int x, int y, int z, float k) {
        return new float[] {(x - z) * 7.07F * k, (x + z) * 3.54F * k - y * 8.66F * k};
    }

    // ---- Subclasses: the in -> out row ----

    protected abstract void addIoSlots(IRecipeLayoutBuilder builder);

    protected abstract void addIoExtras(IRecipeExtrasBuilder builder);

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, T recipe, IFocusGroup focuses) {
        addIoSlots(builder);
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, T recipe, IFocusGroup focuses) {
        Model model = new Model();
        builder.addWidget(model);
        builder.addInputHandler(model);
        addIoExtras(builder);
    }

    /** The model, the layer buttons and the block list; remembers the layer shown on this page. */
    private final class Model implements IRecipeWidget, IJeiInputHandler {
        /** -1 = the whole structure, else the layer (0 = bottom). */
        private int layer = -1;

        @Override
        public ScreenPosition getPosition() {
            return new ScreenPosition(0, 0);
        }

        @Override
        public ScreenRectangle getArea() {
            return new ScreenRectangle(0, 0, WIDTH, LIST_Y + 18);
        }

        private boolean shown(BlockPos p) {
            return layer < 0 || p.getY() == layer;
        }

        /** Item -> how many, for the layer shown (most first). */
        private List<Map.Entry<Item, Integer>> counts() {
            Map<Item, Integer> counts = new LinkedHashMap<>();
            blocks.forEach((p, item) -> {
                if (shown(p)) {
                    counts.merge(item.asItem(), 1, Integer::sum);
                }
            });
            List<Map.Entry<Item, Integer>> list = new ArrayList<>(counts.entrySet());
            list.sort(Map.Entry.<Item, Integer>comparingByValue().reversed());
            return list;
        }

        @Override
        public void drawWidget(GuiGraphicsExtractor graphics, double mouseX, double mouseY) {
            Font font = Minecraft.getInstance().font;
            graphics.fill(0, 0, MODEL_W, MODEL_H, 0x30000000);
            // the model, back to front
            List<BlockPos> order = new ArrayList<>(blocks.keySet());
            order.sort(Comparator.<BlockPos>comparingInt(p -> p.getX() + p.getZ()).thenComparingInt(BlockPos::getY));
            for (BlockPos p : order) {
                if (!shown(p)) {
                    continue;
                }
                float[] s = project(p.getX(), p.getY(), p.getZ(), scale);
                graphics.pose().pushMatrix();
                graphics.pose().translate(originX + s[0] - 8 * scale, originY + s[1] - 8 * scale);
                graphics.pose().scale(scale, scale);
                graphics.fakeItem(new ItemStack(blocks.get(p)), 0, 0);
                graphics.pose().popMatrix();
            }
            // layer buttons and the layer between them
            button(graphics, UP_Y, true, inside(mouseX, mouseY, BUTTON_X, UP_Y));
            button(graphics, DOWN_Y, false, inside(mouseX, mouseY, BUTTON_X, DOWN_Y));
            Component label = Component.translatable("jei.comminutioncraft.multiblock.layer");
            Component which = layer < 0 ? Component.translatable("jei.comminutioncraft.multiblock.all")
                    : Component.literal((layer + 1) + "/" + sizeY);
            int cx = BUTTON_X + BUTTON / 2;
            graphics.text(font, label, cx - font.width(label) / 2, 36, 0xFF404040, false);
            graphics.text(font, which, cx - font.width(which) / 2, 48, 0xFF404040, false);
            // blocks needed: icon + count
            int x = 0;
            for (Map.Entry<Item, Integer> e : counts()) {
                graphics.fakeItem(new ItemStack(e.getKey()), x, LIST_Y);
                Component n = Component.literal("x" + e.getValue());
                graphics.text(font, n, x + 18, LIST_Y + 4, 0xFF404040, false);
                x += 18 + font.width(n) + 6;
            }
        }

        private void button(GuiGraphicsExtractor graphics, int y, boolean up, boolean hover) {
            int x = BUTTON_X;
            graphics.fill(x, y, x + BUTTON, y + BUTTON, 0xFF373737);
            graphics.fill(x, y, x + BUTTON - 1, y + BUTTON - 1, 0xFFFFFFFF);
            graphics.fill(x + 1, y + 1, x + BUTTON - 1, y + BUTTON - 1, hover ? 0xFFA0A8C0 : 0xFF8B8B8B);
            for (int row = 0; row < 5; row++) { // a little triangle
                int half = up ? row : 4 - row;
                graphics.fill(x + 9 - half - 1, y + 7 + row, x + 9 + half, y + 8 + row, 0xFF202020);
            }
        }

        private static boolean inside(double mx, double my, int x, int y) {
            return mx >= x && mx < x + BUTTON && my >= y && my < y + BUTTON;
        }

        @Override
        public boolean handleInput(double mouseX, double mouseY, IJeiUserInput input) {
            InputConstants.Key key = input.getKey();
            if (key.getType() != InputConstants.Type.MOUSE || key.getValue() != InputConstants.MOUSE_BUTTON_LEFT) {
                return false;
            }
            boolean up = inside(mouseX, mouseY, BUTTON_X, UP_Y);
            boolean down = inside(mouseX, mouseY, BUTTON_X, DOWN_Y);
            if (!up && !down) {
                return false;
            }
            if (!input.isSimulate()) {
                // up: all -> 1 -> 2 -> ... -> top -> all; down the other way
                if (up) {
                    layer = layer + 1 >= sizeY ? -1 : layer + 1;
                } else {
                    layer = layer < 0 ? sizeY - 1 : layer - 1;
                }
                Minecraft.getInstance().getSoundManager().play(net.minecraft.client.resources.sounds.SimpleSoundInstance.forUI(
                        net.minecraft.sounds.SoundEvents.UI_BUTTON_CLICK, 1.0F));
            }
            return true;
        }

        @Override
        public void getTooltip(ITooltipBuilder tooltip, double mouseX, double mouseY) {
            if (inside(mouseX, mouseY, BUTTON_X, UP_Y)) {
                tooltip.add(Component.translatable("jei.comminutioncraft.multiblock.up"));
                return;
            }
            if (inside(mouseX, mouseY, BUTTON_X, DOWN_Y)) {
                tooltip.add(Component.translatable("jei.comminutioncraft.multiblock.down"));
                return;
            }
            if (mouseY < LIST_Y || mouseY >= LIST_Y + 16) {
                return;
            }
            Font font = Minecraft.getInstance().font;
            int x = 0;
            for (Map.Entry<Item, Integer> e : counts()) {
                int width = 18 + font.width("x" + e.getValue());
                if (mouseX >= x && mouseX < x + width) {
                    tooltip.add(Component.literal(e.getValue() + " x ").append(new ItemStack(e.getKey()).getHoverName()));
                    Component rule = rules.get(e.getKey());
                    if (rule != null) {
                        tooltip.add(rule.copy().withStyle(ChatFormatting.GRAY));
                    }
                    return;
                }
                x += width + 6;
            }
        }
    }

    /** Helper for subclasses: an empty block map to fill. */
    protected static Map<BlockPos, ItemLike> structure() {
        return new LinkedHashMap<>();
    }

    protected static IDrawable energyIcon(IGuiHelper guiHelper) {
        return guiHelper.drawableBuilder(com.script.comminutioncraft.ComminutionCraft.id("textures/gui/jei_energy.png"), 0, 0, 16, 16)
                .setTextureSize(16, 16)
                .build();
    }
}
