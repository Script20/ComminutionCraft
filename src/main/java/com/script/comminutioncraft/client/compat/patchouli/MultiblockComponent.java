package com.script.comminutioncraft.client.compat.patchouli;

import com.mojang.blaze3d.platform.InputConstants;
import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.client.multiblock.MultiblockStructure;
import com.script.comminutioncraft.client.multiblock.MultiblockView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.UnaryOperator;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.Nullable;
import vazkii.patchouli.api.ICustomComponent;
import vazkii.patchouli.api.IComponentRenderContext;
import vazkii.patchouli.api.IMultiblock;
import vazkii.patchouli.api.IStateMatcher;
import vazkii.patchouli.api.IVariable;
import vazkii.patchouli.api.PatchouliAPI;
import vazkii.patchouli.client.base.PersistentData;
import vazkii.patchouli.client.book.gui.GuiBook;
import vazkii.patchouli.client.book.gui.GuiBookEntry;
import vazkii.patchouli.client.book.gui.button.GuiButtonBookEye;
import vazkii.patchouli.client.handler.MultiblockVisualizationHandler;

/**
 * The guide book's multiblock view (template {@code comminutioncraft:multiblock}), used instead of Patchouli's own
 * page, which in its 26.1 beta neither centres nor sizes the structure. Same frame and Visualize button as Patchouli's,
 * drawn by our {@link MultiblockView}: it turns slowly, right-drag turns it by hand, hovering a block names it.
 * Only loaded by Patchouli, so it may use Patchouli's classes.
 */
public class MultiblockComponent implements ICustomComponent {
    private static final int FRAME = 106;
    private static final int MODEL = 99;
    private static final int MODEL_INSET = 4;

    /** From the template: which {@link MultiblockStructure} and its name (for the Visualize overlay). */
    String structure = "";
    String name = "";

    private transient int x;
    private transient int y;
    private transient int pageNum;
    private transient @Nullable MultiblockView view;
    private transient boolean turning;
    private transient double lastX;
    private transient double lastY;

    @Override
    public void build(int componentX, int componentY, int pageNum) {
        x = componentX;
        y = componentY;
        this.pageNum = pageNum;
    }

    @Override
    public void onVariablesAvailable(UnaryOperator<IVariable> lookup, HolderLookup.Provider registries) {
        structure = lookup.apply(IVariable.wrap(structure)).asString();
        name = lookup.apply(IVariable.wrap(name)).asString();
    }

    private @Nullable MultiblockView view() {
        if (view == null) {
            MultiblockStructure found = MultiblockStructure.byId(structure);
            if (found != null) {
                view = new MultiblockView(found, 150, true);
            }
        }
        return view;
    }

    @Override
    public void onDisplayed(IComponentRenderContext context) {
        view = null; // every visit starts spinning again
        turning = false;
        if (context.getGui() instanceof GuiBook book) {
            context.addWidget(new GuiButtonBookEye(book, x + 7, y + 90 + GuiBook.TOP_PADDING, button -> visualize(book)), pageNum);
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, IComponentRenderContext context, float partialTicks,
                                   int mouseX, int mouseY) {
        if (context.getGui() instanceof GuiBook book) {
            GuiBook.drawFromTexture(graphics, book.book, x, y, 405, 149, FRAME, FRAME);
        }
        MultiblockView view = view();
        if (view == null) {
            return;
        }
        if (turning) {
            if (Minecraft.getInstance().mouseHandler.isRightPressed()) {
                view.rotate(mouseX - lastX, mouseY - lastY);
                lastX = mouseX;
                lastY = mouseY;
            } else {
                turning = false;
            }
        }
        int modelX = x + MODEL_INSET;
        int modelY = y + MODEL_INSET;
        view.extract(graphics, modelX, modelY, MODEL, MODEL);
        if (!turning && context.isAreaHovered(mouseX, mouseY, modelX, modelY, MODEL, MODEL)) {
            BlockPos block = view.blockAt(mouseX - modelX, mouseY - modelY, MODEL, MODEL);
            if (block != null) {
                List<Component> lines = new ArrayList<>(view.tooltip(block));
                lines.add(MultiblockView.hint());
                context.setHoverTooltipComponents(lines);
            }
        }
    }

    @Override
    public boolean mouseClicked(IComponentRenderContext context, MouseButtonEvent event, boolean doubleClick) {
        int modelX = x + MODEL_INSET;
        int modelY = y + MODEL_INSET;
        boolean inside = event.x() >= modelX && event.y() >= modelY && event.x() < modelX + MODEL && event.y() < modelY + MODEL;
        if (!inside || event.button() != InputConstants.MOUSE_BUTTON_RIGHT || view() == null) {
            return false;
        }
        turning = true; // and the right click doesn't turn the page back
        lastX = event.x();
        lastY = event.y();
        return true;
    }

    /** Patchouli's Visualize: the structure as ghost blocks in the world, placed where the player looks. */
    private void visualize(GuiBook book) {
        MultiblockStructure found = MultiblockStructure.byId(structure);
        if (found == null || !(book instanceof GuiBookEntry entry)) {
            return;
        }
        var api = PatchouliAPI.get();
        Map<BlockPos, IStateMatcher> matchers = new HashMap<>();
        found.blocks().forEach((pos, state) -> matchers.put(pos, api.stateMatcher(state)));
        found.mustBeEmpty().forEach(pos -> matchers.put(pos, api.airMatcher()));
        IMultiblock multiblock = api.makeSparseMultiblock(matchers)
                .offset(found.sizeX() / 2, found.sizeY() / 2, found.sizeZ() / 2)
                .setId(ComminutionCraft.id(found.id()));
        PersistentData.Bookmark bookmark = new PersistentData.Bookmark(entry.getEntry().getId(), pageNum / 2);
        MultiblockVisualizationHandler.INSTANCE.setMultiblock(multiblock, Component.literal(name), bookmark, true);
        entry.addBookmarkButtons();
    }
}
