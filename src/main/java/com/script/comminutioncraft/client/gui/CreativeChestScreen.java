package com.script.comminutioncraft.client.gui;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.machine.CreativeChestMenu;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

/** Creative Chest GUI: the picked item's slot between two rainbow seams, its name or a hint under it. */
public class CreativeChestScreen extends AbstractContainerScreen<CreativeChestMenu> {
    private static final Identifier TEXTURE = ComminutionCraft.id("textures/gui/container/creative_chest.png");
    private static final int TITLE_COLOR = 0xFFE4E6EC;
    private static final int LABEL_COLOR = 0xFF9EA4B0;

    public CreativeChestScreen(CreativeChestMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        graphics.text(font, title, titleLabelX, titleLabelY, TITLE_COLOR, false);
        graphics.text(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, LABEL_COLOR, false);
        ItemStack picked = menu.getSlot(0).getItem();
        Component line = picked.isEmpty() ? Component.translatable("gui.comminutioncraft.creative_chest.empty")
                : Component.translatable("gui.comminutioncraft.creative_chest.endless", picked.getHoverName());
        graphics.text(font, line, (imageWidth - font.width(line)) / 2, 60, LABEL_COLOR, false);
    }

    /** Where JEI may drop an item: the slot, in screen coordinates. */
    public int slotScreenX() {
        return leftPos + CreativeChestMenu.SLOT_X;
    }

    public int slotScreenY() {
        return topPos + CreativeChestMenu.SLOT_Y;
    }
}
