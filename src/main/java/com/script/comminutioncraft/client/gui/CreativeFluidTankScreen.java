package com.script.comminutioncraft.client.gui;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.machine.CreativeFluidTankBlockEntity;
import com.script.comminutioncraft.common.machine.CreativeFluidTankMenu;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.client.fluid.FluidTintSource;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import org.jspecify.annotations.Nullable;

/**
 * Creative Fluid Tank's fluid picker: 8 buttons a page (the first one on page 1 is "no fluid"), each drawn with its
 * fluid's own texture and tint, the picked one framed in gold; arrows page through them. The tube on the right shows
 * the picked fluid. Sheet: tools/gui_dark_chassis.py ({@code creative_tank_sheet}).
 */
public class CreativeFluidTankScreen extends AbstractContainerScreen<CreativeFluidTankMenu> {
    private static final Identifier TEXTURE = ComminutionCraft.id("textures/gui/container/creative_fluid_tank.png");
    private static final int PER_PAGE = 8;
    private static final int BUTTON_X = 13;
    private static final int BUTTON_Y = 21;
    private static final int BUTTON_STEP = 22;
    private static final int TUBE_X = 155;
    private static final int TUBE_Y = 17;
    private static final int TUBE_W = 10;
    private static final int TUBE_H = 52;
    private static final int PAGE_X = 104;
    private static final int TITLE_COLOR = 0xFFE4E6EC;
    private static final int LABEL_COLOR = 0xFF9EA4B0;
    private static final int PICKED_COLOR = 0xFFFFBA38;

    private int page;

    public CreativeFluidTankScreen(CreativeFluidTankMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    /** All the choices: null ("no fluid") first, then every fluid. */
    private static List<@Nullable Fluid> choices() {
        List<@Nullable Fluid> list = new java.util.ArrayList<>();
        list.add(null);
        list.addAll(CreativeFluidTankBlockEntity.fluids());
        return list;
    }

    private int pages() {
        return Math.max(1, Math.ceilDiv(choices().size(), PER_PAGE));
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        List<@Nullable Fluid> choices = choices();
        Fluid picked = menu.getFluid();
        for (int i = 0; i < PER_PAGE; i++) {
            int index = page * PER_PAGE + i;
            if (index >= choices.size()) {
                break;
            }
            int x = leftPos + BUTTON_X + (i % 4) * BUTTON_STEP;
            int y = topPos + BUTTON_Y + (i / 4) * BUTTON_STEP;
            Fluid fluid = choices.get(index);
            if (fluid != null) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite(fluid), x, y, 16, 16, tint(fluid));
            } else {
                graphics.fill(x + 3, y + 7, x + 13, y + 9, LABEL_COLOR); // a dash for "no fluid"
            }
            if (fluid == picked) {
                graphics.fill(x - 1, y - 1, x + 17, y, PICKED_COLOR);
                graphics.fill(x - 1, y + 16, x + 17, y + 17, PICKED_COLOR);
                graphics.fill(x - 1, y, x, y + 16, PICKED_COLOR);
                graphics.fill(x + 16, y, x + 17, y + 16, PICKED_COLOR);
            }
        }
        if (picked != null) { // the tube, tiled with the fluid's texture
            TextureAtlasSprite sprite = sprite(picked);
            int color = tint(picked);
            for (int y = 0; y < TUBE_H; y += 16) {
                int h = Math.min(16, TUBE_H - y);
                graphics.enableScissor(leftPos + TUBE_X, topPos + TUBE_Y + y, leftPos + TUBE_X + TUBE_W, topPos + TUBE_Y + y + h);
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, leftPos + TUBE_X, topPos + TUBE_Y + y, 16, 16, color);
                graphics.disableScissor();
            }
        }
    }

    private static TextureAtlasSprite sprite(Fluid fluid) {
        FluidModel model = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(fluid.defaultFluidState());
        return model.stillMaterial().sprite();
    }

    private static int tint(Fluid fluid) {
        Minecraft minecraft = Minecraft.getInstance();
        FluidState state = fluid.defaultFluidState();
        FluidTintSource tint = minecraft.getModelManager().getFluidStateModelSet().get(state).fluidTintSource();
        if (tint == null || minecraft.level == null || minecraft.player == null) {
            return 0xFFFFFFFF;
        }
        return 0xFF000000 | tint.colorInWorld(state, minecraft.level.getBlockState(minecraft.player.blockPosition()),
                minecraft.level, minecraft.player.blockPosition());
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(font, title, titleLabelX, titleLabelY, TITLE_COLOR, false);
        graphics.text(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, LABEL_COLOR, false);
        if (pages() > 1) {
            graphics.text(font, Component.literal("<"), PAGE_X, 30, LABEL_COLOR, false);
            graphics.text(font, Component.literal((page + 1) + "/" + pages()), PAGE_X + 10, 30, LABEL_COLOR, false);
            graphics.text(font, Component.literal(">"), PAGE_X + 34, 30, LABEL_COLOR, false);
        }
        Fluid picked = menu.getFluid();
        Component name = picked == null ? Component.translatable("gui.comminutioncraft.creative_tank.none")
                : FluidResource.of(picked).getHoverName();
        graphics.text(font, name, BUTTON_X, BUTTON_Y + 2 * BUTTON_STEP - 4, LABEL_COLOR, false);
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);
        int index = buttonAt(mouseX, mouseY);
        if (index >= 0) {
            Fluid fluid = choices().get(index);
            graphics.setTooltipForNextFrame(fluid == null ? Component.translatable("gui.comminutioncraft.creative_tank.none")
                    : FluidResource.of(fluid).getHoverName(), mouseX, mouseY);
        }
    }

    /** The choice index under the mouse, or -1. */
    private int buttonAt(double mouseX, double mouseY) {
        for (int i = 0; i < PER_PAGE; i++) {
            int index = page * PER_PAGE + i;
            int x = leftPos + BUTTON_X + (i % 4) * BUTTON_STEP;
            int y = topPos + BUTTON_Y + (i / 4) * BUTTON_STEP;
            if (index < choices().size() && mouseX >= x && mouseX < x + 16 && mouseY >= y && mouseY < y + 16) {
                return index;
            }
        }
        return -1;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (event.button() == 0 && minecraft != null && minecraft.gameMode != null) {
            int index = buttonAt(event.x(), event.y());
            if (index >= 0) {
                minecraft.gameMode.handleInventoryButtonClick(menu.containerId, index);
                return true;
            }
            double y = event.y() - topPos;
            double x = event.x() - leftPos;
            if (pages() > 1 && y >= 28 && y < 40) {
                if (x >= PAGE_X - 2 && x < PAGE_X + 8) {
                    page = (page + pages() - 1) % pages();
                    return true;
                }
                if (x >= PAGE_X + 32 && x < PAGE_X + 42) {
                    page = (page + 1) % pages();
                    return true;
                }
            }
        }
        return super.mouseClicked(event, doubleClick);
    }
}
