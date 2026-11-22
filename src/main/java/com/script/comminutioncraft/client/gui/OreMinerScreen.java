package com.script.comminutioncraft.client.gui;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.machine.miner.OreMinerBlockEntity;
import com.script.comminutioncraft.common.machine.miner.OreMinerMenu;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * Ore Miner GUI (owner-approved mock-up design/previews/ore_miner_gui.png): "Mining Chunk: 3/9" and a detail line, the
 * Fill button (every tier), and from tier 3 the Drops button, the circuit slot, Whitelist / Blacklist and the 3x3 filter
 * (dark and locked until the tier's circuit is in); the 3x3 outputs on the right. Seams in the miner's tier colour.
 */
public class OreMinerScreen extends MachineScreen<OreMinerMenu> {
    private static final int TITLE_COLOR = 0xFFE4E6EC;
    private static final int LABEL_COLOR = 0xFF9EA4B0;
    private static final int WAIT_COLOR = 0xFFFF7A6A;
    private static final int SLOT = 0xFF16181C;
    private static final int SLOT_DARK = 0xFF101114;
    private static final int SLOT_LIGHT = 0xFF545964;
    private static final int LOCKED = 0xC01E2025;

    public OreMinerScreen(OreMinerMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title, ComminutionCraft.id("textures/gui/container/ore_miner.png"),
                Tier.values()[menu.tier().ordinal() + 1], 214);
    }

    private static Identifier icon(String name) {
        return ComminutionCraft.id(name);
    }

    private static Component withDesc(String key) {
        return Component.translatable(key).copy().append(CommonComponents.NEW_LINE)
                .append(Component.translatable(key + ".desc").withStyle(ChatFormatting.GRAY));
    }

    @Override
    protected void init() {
        super.init();
        addRenderableWidget(new IconButton(leftPos + 28, topPos + 40,
                () -> icon(menu.fillStone() ? "ore_miner/fill_stone" : "ore_miner/fill_air"),
                () -> withDesc("gui.comminutioncraft.ore_miner.fill." + (menu.fillStone() ? "stone" : "air")),
                () -> true,
                () -> clickButton(OreMinerBlockEntity.BUTTON_FILL)));
        if (menu.tier().hasOptions()) {
            addRenderableWidget(new IconButton(leftPos + 50, topPos + 40,
                    () -> icon(menu.oreBlocks() ? "ore_miner/drops_block" : "ore_miner/drops_raw"),
                    () -> withDesc("gui.comminutioncraft.ore_miner.drops." + (menu.oreBlocks() ? "block" : "raw")),
                    () -> true,
                    () -> clickButton(OreMinerBlockEntity.BUTTON_DROPS)));
            addRenderableWidget(new IconButton(leftPos + 92, topPos + 40,
                    () -> icon(menu.blacklist() ? "item_pipe/blacklist" : "item_pipe/whitelist"),
                    () -> circuitHint(withDesc("gui.comminutioncraft.ore_miner." + (menu.blacklist() ? "blacklist" : "whitelist"))),
                    menu::hasCircuit,
                    () -> clickButton(OreMinerBlockEntity.BUTTON_LIST)));
        }
    }

    private Component circuitHint(Component text) {
        Item circuit = menu.tier().circuit();
        return menu.hasCircuit() || circuit == null ? text : text.copy().append(CommonComponents.NEW_LINE)
                .append(Component.translatable("gui.comminutioncraft.item_pipe.needs_circuit",
                        circuit.getName(new ItemStack(circuit))).withStyle(ChatFormatting.RED));
    }

    @Override
    protected boolean isActive() {
        return menu.waiting() == OreMinerBlockEntity.Waiting.NONE && menu.phase() != OreMinerBlockEntity.Phase.FINISHED;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        drawEnergyBar(graphics, menu.energy(), menu.energyCapacity());
        if (menu.tier().hasOptions()) {
            slotFrame(graphics, leftPos + OreMinerMenu.CIRCUIT_X, topPos + OreMinerMenu.CIRCUIT_Y);
            for (int i = 0; i < OreMinerBlockEntity.FILTER_SLOTS; i++) {
                slotFrame(graphics, leftPos + OreMinerMenu.FILTER_X + (i % 3) * 18, topPos + OreMinerMenu.FILTER_Y + (i / 3) * 18);
            }
        }
    }

    /** A recessed 18x18 slot round an item at (x, y). */
    private static void slotFrame(GuiGraphicsExtractor graphics, int x, int y) {
        graphics.fill(x - 1, y - 1, x + 17, y + 17, SLOT);
        graphics.fill(x - 1, y - 1, x + 16, y, SLOT_DARK);
        graphics.fill(x - 1, y - 1, x, y + 16, SLOT_DARK);
        graphics.fill(x, y + 16, x + 17, y + 17, SLOT_LIGHT);
        graphics.fill(x + 16, y, x + 17, y + 17, SLOT_LIGHT);
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);
        int chunks = menu.tier().chunks();
        Component status = switch (menu.phase()) {
            case SCANNING -> Component.translatable("gui.comminutioncraft.ore_miner.scanning", menu.chunk(), chunks);
            case MINING -> Component.translatable("gui.comminutioncraft.ore_miner.mining", menu.chunk(), chunks);
            case FINISHED -> Component.translatable("gui.comminutioncraft.ore_miner.finished", chunks);
        };
        graphics.text(font, status, 28, 18, TITLE_COLOR, false);
        OreMinerBlockEntity.Waiting waiting = menu.waiting();
        if (waiting != OreMinerBlockEntity.Waiting.NONE) {
            graphics.text(font, Component.translatable("gui.comminutioncraft.ore_miner.waiting." + waiting.name().toLowerCase(Locale.ROOT)),
                    28, 29, WAIT_COLOR, false);
        } else {
            graphics.text(font, Component.translatable("gui.comminutioncraft.ore_miner.detail", menu.y(), menu.mined()), 28, 29, LABEL_COLOR, false);
        }
        Item circuit = menu.tier().circuit();
        if (circuit != null && !menu.hasCircuit()) { // the filter only works with the tier's circuit in
            graphics.item(new ItemStack(circuit), OreMinerMenu.CIRCUIT_X, OreMinerMenu.CIRCUIT_Y);
            graphics.nextStratum();
            graphics.fill(OreMinerMenu.CIRCUIT_X, OreMinerMenu.CIRCUIT_Y, OreMinerMenu.CIRCUIT_X + 16, OreMinerMenu.CIRCUIT_Y + 16, LOCKED);
            for (int i = 0; i < OreMinerBlockEntity.FILTER_SLOTS; i++) {
                int x = OreMinerMenu.FILTER_X + (i % 3) * 18;
                int y = OreMinerMenu.FILTER_Y + (i / 3) * 18;
                graphics.fill(x, y, x + 16, y + 16, LOCKED);
            }
        }
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);
        energyTooltip(graphics, mouseX, mouseY, menu.energy(), menu.energyCapacity());
        Item circuit = menu.tier().circuit();
        if (circuit != null && !menu.hasCircuit() && isHovering(OreMinerMenu.CIRCUIT_X, OreMinerMenu.CIRCUIT_Y, 16, 16, mouseX, mouseY)) {
            graphics.setTooltipForNextFrame(Component.translatable("gui.comminutioncraft.ore_miner.circuit",
                    circuit.getName(new ItemStack(circuit))), mouseX, mouseY);
        }
    }

    /** Where JEI may drop items: the filter slots, in screen coordinates. */
    public int filterScreenX(int index) {
        return leftPos + OreMinerMenu.FILTER_X + (index % 3) * 18;
    }

    public int filterScreenY(int index) {
        return topPos + OreMinerMenu.FILTER_Y + (index / 3) * 18;
    }
}
