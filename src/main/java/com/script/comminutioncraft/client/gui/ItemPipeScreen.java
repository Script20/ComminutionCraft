package com.script.comminutioncraft.client.gui;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.pipe.ItemPipeBlock;
import com.script.comminutioncraft.common.pipe.ItemPipeMenu;
import com.script.comminutioncraft.common.pipe.PipeSide;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Item Pipe GUI (owner-approved mock-up design/previews/item_pipe_gui.png): one side at a time, picked with the tabs on
 * the left (each shows the block on that side). Vanilla buttons whose icon follows the setting: Mode, Whitelist /
 * Blacklist and, on Extract sides, Closest first / Round robin (both need a circuit). Extract sides also get the
 * machines' redstone tab on the right.
 */
public class ItemPipeScreen extends AbstractContainerScreen<ItemPipeMenu> {
    private static final Identifier TEXTURE = ComminutionCraft.id("textures/gui/container/item_pipe.png");
    private static final int BLUE = 0xFF40C4FF;
    private static final int TITLE_COLOR = 0xFFE4E6EC;
    private static final int LABEL_COLOR = 0xFF9EA4B0;
    private static final int LOCKED = 0xC01E2025;
    /** Tab order down the left edge. */
    private static final List<Direction> TABS = List.of(Direction.NORTH, Direction.SOUTH, Direction.WEST, Direction.EAST,
            Direction.UP, Direction.DOWN);

    private MachineTab redstoneTab;
    private IconButton spreadButton;

    public ItemPipeScreen(ItemPipeMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    private static Identifier icon(String name) {
        return ComminutionCraft.id("item_pipe/" + name);
    }

    private static Component sideName(Direction side) {
        return Component.translatable("gui.comminutioncraft.direction." + side.getSerializedName());
    }

    private static Component withDesc(Component name, String descKey) {
        return name.copy().append(CommonComponents.NEW_LINE).append(Component.translatable(descKey).withStyle(ChatFormatting.GRAY));
    }

    @Override
    protected void init() {
        super.init();
        for (int i = 0; i < TABS.size(); i++) {
            Direction side = TABS.get(i);
            addRenderableWidget(new SideTab(leftPos - SideTab.SIZE + 2, topPos + 6 + i * (SideTab.SIZE + 2),
                    () -> live(side) ? neighbourIcon(side) : ItemStack.EMPTY,
                    Component.literal(side.getSerializedName().substring(0, 1).toUpperCase(Locale.ROOT)),
                    () -> Component.translatable("gui.comminutioncraft.item_pipe.side", sideName(side), live(side)
                            ? neighbourIcon(side).getHoverName() : Component.translatable("gui.comminutioncraft.item_pipe.no_block")),
                    () -> live(side),
                    () -> menu.side() == side,
                    BLUE,
                    () -> clickButton(ItemPipeMenu.BUTTON_SIDE + side.ordinal())));
        }
        addRenderableWidget(new IconButton(leftPos + 8, topPos + 18,
                () -> icon(menu.mode().name().toLowerCase(Locale.ROOT)),
                () -> withDesc(Component.translatable("gui.comminutioncraft.item_pipe.mode", menu.mode().displayName()),
                        "gui.comminutioncraft.item_pipe.mode." + menu.mode().name().toLowerCase(Locale.ROOT) + ".desc"),
                () -> true,
                () -> clickButton(ItemPipeMenu.BUTTON_MODE)));
        addRenderableWidget(new IconButton(leftPos + 31, topPos + 18,
                () -> icon(menu.blacklist() ? "blacklist" : "whitelist"),
                () -> circuitHint(withDesc(Component.translatable("gui.comminutioncraft.item_pipe." + (menu.blacklist() ? "blacklist" : "whitelist")),
                        "gui.comminutioncraft.item_pipe." + (menu.blacklist() ? "blacklist" : "whitelist") + ".desc")),
                menu::hasCircuit,
                () -> clickButton(ItemPipeMenu.BUTTON_LIST)));
        spreadButton = addRenderableWidget(new IconButton(leftPos + 54, topPos + 18,
                () -> icon(spreads() ? "round_robin" : "closest"),
                () -> circuitHint(withDesc(Component.translatable("gui.comminutioncraft.item_pipe." + (spreads() ? "round_robin" : "closest")),
                        "gui.comminutioncraft.item_pipe." + (spreads() ? "round_robin" : "closest") + ".desc")),
                menu::hasCircuit,
                () -> clickButton(ItemPipeMenu.BUTTON_SPREAD)));
        redstoneTab = addRenderableWidget(new MachineTab(leftPos + imageWidth - 2, topPos + 6,
                () -> new ItemStack(MachineScreen.redstoneIcon(menu.redstone())),
                () -> Component.translatable("gui.comminutioncraft.redstone", menu.redstone().displayName())
                        .append(CommonComponents.NEW_LINE).append(menu.redstone().description().copy().withStyle(ChatFormatting.GRAY)),
                BLUE,
                () -> clickButton(ItemPipeMenu.BUTTON_REDSTONE)));
    }

    /** Without a circuit the shown setting doesn't apply: closest first, and the list / spread buttons say why. */
    private boolean spreads() {
        return menu.hasCircuit() && menu.roundRobin();
    }

    private Component circuitHint(Component text) {
        return menu.hasCircuit() ? text : text.copy().append(CommonComponents.NEW_LINE)
                .append(Component.translatable("gui.comminutioncraft.item_pipe.needs_circuit",
                        menu.circuit().getName(new ItemStack(menu.circuit()))).withStyle(ChatFormatting.RED));
    }

    private boolean live(Direction side) {
        return (menu.inventorySides() & (1 << side.ordinal())) != 0;
    }

    private ItemStack neighbourIcon(Direction side) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) {
            return ItemStack.EMPTY;
        }
        BlockState state = minecraft.level.getBlockState(menu.pos().relative(side));
        return new ItemStack(state.getBlock().asItem());
    }

    private void clickButton(int id) {
        if (minecraft != null && minecraft.gameMode != null) {
            minecraft.gameMode.handleInventoryButtonClick(menu.containerId, id);
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        boolean extract = menu.mode() == ItemPipeBlock.Mode.EXTRACT;
        spreadButton.visible = extract;
        redstoneTab.visible = extract;
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TEXTURE, leftPos, topPos, 0, 0, imageWidth, imageHeight, 256, 256);
        if (!menu.hasCircuit()) {
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, ComminutionCraft.id(menu.isInstant() ? "item_pipe/circuit_ghost_fusion" : "item_pipe/circuit_ghost"),
                    leftPos + ItemPipeMenu.CIRCUIT_X, topPos + ItemPipeMenu.CIRCUIT_Y, 16, 16);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        Component heading = Component.translatable("gui.comminutioncraft.item_pipe.title", title, sideName(menu.side()));
        int x = (imageWidth - font.width(heading)) / 2;
        graphics.text(font, heading, x, titleLabelY, TITLE_COLOR, false);
        graphics.fill(8, titleLabelY + 4, x - 4, titleLabelY + 5, BLUE);
        graphics.fill(x + font.width(heading) + 4, titleLabelY + 4, imageWidth - 8, titleLabelY + 5, BLUE);
        graphics.text(font, playerInventoryTitle, inventoryLabelX, inventoryLabelY, LABEL_COLOR, false);
        graphics.text(font, Component.translatable(menu.hasCircuit() ? "gui.comminutioncraft.item_pipe.filter"
                : "gui.comminutioncraft.item_pipe.filter_locked"), ItemPipeMenu.FILTER_X, ItemPipeMenu.FILTER_Y - 10, LABEL_COLOR, false);
        if (!menu.hasCircuit()) { // the filter only works with a circuit in
            graphics.nextStratum();
            for (int i = 0; i < PipeSide.FILTER_SLOTS; i++) {
                int sx = ItemPipeMenu.FILTER_X + i * 18;
                graphics.fill(sx, ItemPipeMenu.FILTER_Y, sx + 16, ItemPipeMenu.FILTER_Y + 16, LOCKED);
            }
        }
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);
        int x = mouseX - leftPos - ItemPipeMenu.CIRCUIT_X;
        int y = mouseY - topPos - ItemPipeMenu.CIRCUIT_Y;
        if (x >= 0 && x < 16 && y >= 0 && y < 16 && !menu.hasCircuit()) {
            graphics.setTooltipForNextFrame(Component.translatable("gui.comminutioncraft.item_pipe.circuit",
                    menu.circuit().getName(new ItemStack(menu.circuit()))), mouseX, mouseY);
        }
    }

    /** Where JEI may drop items: the filter slots, in screen coordinates. */
    public int filterScreenX(int index) {
        return leftPos + ItemPipeMenu.FILTER_X + index * 18;
    }

    public int filterScreenY() {
        return topPos + ItemPipeMenu.FILTER_Y;
    }

    /** The side tabs stick out of the GUI's left edge, the redstone tab out of its right (for JEI's exclusion areas). */
    public List<Rect2i> extraAreas() {
        return List.of(new Rect2i(leftPos - SideTab.SIZE + 2, topPos + 6, SideTab.SIZE, TABS.size() * (SideTab.SIZE + 2)),
                new Rect2i(leftPos + imageWidth - 2, topPos + 6, MachineTab.SIZE, MachineTab.SIZE));
    }
}
