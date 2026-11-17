package com.script.comminutioncraft.client.compat.jei;

import com.mojang.blaze3d.platform.InputConstants;
import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.client.multiblock.MultiblockStructure;
import com.script.comminutioncraft.client.multiblock.MultiblockView;
import java.util.ArrayList;
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
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;
import mezz.jei.api.recipe.types.IRecipeType;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.navigation.ScreenPosition;
import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.Nullable;

/**
 * A multiblock's JEI page (owner's design, mock-up design/previews/jei_multiblock.png): the structure in 3D with the
 * blocks' real models ({@link MultiblockView}: right-drag turns it, scroll zooms, hovering a block names it), a ▲ button
 * stepping up one layer at a time (past the top it shows the whole structure again) and ▼ stepping down, the layer
 * shown between them, the blocks needed (for the whole thing or the layer: icon + count, name and where it goes on
 * hover), and a row of what goes in and out.
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

    private final MultiblockStructure structure;

    protected MultiblockCategory(IRecipeType<T> type, Component title, IDrawable icon, MultiblockStructure structure) {
        super(type, title, icon, WIDTH, HEIGHT);
        this.structure = structure;
    }

    // ---- Subclasses: the in -> out row ----

    protected abstract void addIoSlots(IRecipeLayoutBuilder builder);

    protected abstract void addIoExtras(IRecipeExtrasBuilder builder);

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, T recipe, IFocusGroup focuses) {
        addIoSlots(builder);
        // The model isn't made of slots, so list its blocks as hidden inputs: U on any of them finds this page.
        var hidden = builder.addInvisibleIngredients(RecipeIngredientRole.INPUT);
        structure.blocks().values().stream().map(state -> state.getBlock().asItem()).distinct().forEach(hidden::add);
    }

    @Override
    public void createRecipeExtras(IRecipeExtrasBuilder builder, T recipe, IFocusGroup focuses) {
        Model model = new Model();
        builder.addWidget(model);
        builder.addInputHandler(model);
        addIoExtras(builder);
    }

    /** The model, the layer buttons and the block list; remembers the turn and layer shown on this page. */
    private final class Model implements IRecipeWidget, IJeiInputHandler {
        /** Starts looking at the front (south) and a little of the east side. */
        private final MultiblockView view = new MultiblockView(structure, 150, false);
        /** {@link #counts()} for {@link #countsLayer} (the list only changes with the layer). */
        private @Nullable List<Map.Entry<Item, Integer>> counts;
        private int countsLayer;

        @Override
        public ScreenPosition getPosition() {
            return new ScreenPosition(0, 0);
        }

        @Override
        public ScreenRectangle getArea() {
            return new ScreenRectangle(0, 0, WIDTH, LIST_Y + 18);
        }

        /** Item -> how many, for the layer shown (most first). */
        private List<Map.Entry<Item, Integer>> counts() {
            if (this.counts == null || countsLayer != view.layer()) {
                this.counts = countFor();
                countsLayer = view.layer();
            }
            return this.counts;
        }

        private List<Map.Entry<Item, Integer>> countFor() {
            Map<Item, Integer> counts = new LinkedHashMap<>();
            structure.blocks().forEach((p, state) -> {
                if (view.shows(p)) {
                    counts.merge(state.getBlock().asItem(), 1, Integer::sum);
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
            view.extract(graphics, 0, 0, MODEL_W, MODEL_H);
            // layer buttons and the layer between them
            button(graphics, UP_Y, true, inside(mouseX, mouseY, BUTTON_X, UP_Y));
            button(graphics, DOWN_Y, false, inside(mouseX, mouseY, BUTTON_X, DOWN_Y));
            Component label = Component.translatable("jei.comminutioncraft.multiblock.layer");
            Component which = view.layer() < 0 ? Component.translatable("jei.comminutioncraft.multiblock.all")
                    : Component.literal((view.layer() + 1) + "/" + structure.sizeY());
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
            if (key.getType() != InputConstants.Type.MOUSE) {
                return false;
            }
            if (key.getValue() == InputConstants.MOUSE_BUTTON_RIGHT) {
                return inModel(mouseX, mouseY); // starts a turn (handleMouseDragged)
            }
            if (key.getValue() != InputConstants.MOUSE_BUTTON_LEFT) {
                return false;
            }
            boolean up = inside(mouseX, mouseY, BUTTON_X, UP_Y);
            boolean down = inside(mouseX, mouseY, BUTTON_X, DOWN_Y);
            if (!up && !down) {
                return false;
            }
            if (!input.isSimulate()) {
                // up: all -> 1 -> 2 -> ... -> top -> all; down the other way
                int layer = view.layer();
                int top = structure.sizeY();
                view.setLayer(up ? (layer + 1 >= top ? -1 : layer + 1) : (layer < 0 ? top - 1 : layer - 1));
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(
                        SoundEvents.UI_BUTTON_CLICK, 1.0F));
            }
            return true;
        }

        private static boolean inModel(double mouseX, double mouseY) {
            return mouseX >= 0 && mouseY >= 0 && mouseX < MODEL_W && mouseY < MODEL_H;
        }

        @Override
        public boolean handleMouseDragged(double mouseX, double mouseY, InputConstants.Key key, double dragX, double dragY) {
            if (key.getType() != InputConstants.Type.MOUSE || key.getValue() != InputConstants.MOUSE_BUTTON_RIGHT) {
                return false;
            }
            view.rotate(dragX, dragY);
            return true;
        }

        @Override
        public boolean handleMouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
            if (!inModel(mouseX, mouseY)) {
                return false;
            }
            view.zoom(scrollY);
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
            if (inModel(mouseX, mouseY)) {
                BlockPos block = view.blockAt(mouseX, mouseY, MODEL_W, MODEL_H);
                if (block != null) {
                    view.tooltip(block).forEach(tooltip::add);
                    tooltip.add(MultiblockView.hint());
                }
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
                    Component rule = structure.rule(Block.byItem(e.getKey()));
                    if (rule != null) {
                        tooltip.add(rule.copy().withStyle(ChatFormatting.GRAY));
                    }
                    return;
                }
                x += width + 6;
            }
        }
    }

    protected static IDrawable energyIcon(IGuiHelper guiHelper) {
        return guiHelper.drawableBuilder(ComminutionCraft.id("textures/gui/jei_energy.png"), 0, 0, 16, 16)
                .setTextureSize(16, 16)
                .build();
    }
}
