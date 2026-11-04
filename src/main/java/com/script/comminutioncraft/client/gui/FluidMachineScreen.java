package com.script.comminutioncraft.client.gui;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.machine.FluidMachineMenu;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;

/**
 * GUI for the fluid machines: energy bar (left), one gauge per tank, an arrow that runs while it's working, and the
 * rate under the arrow. Sheet layouts: see tools/gui_dark_chassis.py ({@code fluid_machine_sheet}).
 */
public class FluidMachineScreen extends MachineScreen<FluidMachineMenu> {
    private static final int LABEL_COLOR = 0xFF9EA4B0;
    private static final int ARROW_TICKS = 20;

    /** Which tank a gauge shows, where (x on the GUI), its fill sprite (u on the sheet) and its tooltip key. */
    public record Gauge(int tank, int x, int u, String tooltipKey) {}

    private final List<Gauge> gauges;
    private final String rateKey;

    public FluidMachineScreen(FluidMachineMenu menu, Inventory inventory, Component title, Identifier texture,
                              List<Gauge> gauges, String rateKey) {
        this(menu, inventory, title, texture, gauges, rateKey, Tier.PLASMA);
    }

    public FluidMachineScreen(FluidMachineMenu menu, Inventory inventory, Component title, Identifier texture,
                              List<Gauge> gauges, String rateKey, Tier tier) {
        super(menu, inventory, title, texture, tier);
        this.gauges = gauges;
        this.rateKey = rateKey;
    }

    /** Water (in) -> hydrogen + oxygen (out). */
    public static FluidMachineScreen electrolyser(FluidMachineMenu menu, Inventory inventory, Component title) {
        return new FluidMachineScreen(menu, inventory, title, ComminutionCraft.id("textures/gui/container/electrolyser.png"),
                List.of(new Gauge(0, 50, WATER_U, "gui.comminutioncraft.water"),
                        new Gauge(1, 131, HYDROGEN_U, "gui.comminutioncraft.hydrogen"),
                        new Gauge(2, PLASMA_X, OXYGEN_U, "gui.comminutioncraft.oxygen")),
                "gui.comminutioncraft.fluid_rate");
    }

    /** Hydrogen (in) -> plasma (out). */
    public static FluidMachineScreen plasmaGenerator(FluidMachineMenu menu, Inventory inventory, Component title) {
        return new FluidMachineScreen(menu, inventory, title, ComminutionCraft.id("textures/gui/container/plasma_generator.png"),
                List.of(new Gauge(0, 50, HYDROGEN_U, "gui.comminutioncraft.hydrogen"),
                        new Gauge(1, PLASMA_X, PLASMA_U, "gui.comminutioncraft.plasma")),
                "gui.comminutioncraft.fluid_rate");
    }

    /** Plasma (in) -> FE (the energy bar). */
    public static FluidMachineScreen mhdGenerator(FluidMachineMenu menu, Inventory inventory, Component title) {
        return new FluidMachineScreen(menu, inventory, title, ComminutionCraft.id("textures/gui/container/mhd_generator.png"),
                List.of(new Gauge(0, 50, PLASMA_U, "gui.comminutioncraft.plasma")),
                "gui.comminutioncraft.generating");
    }

    /** Water (in) -> deuterium (out). */
    public static FluidMachineScreen isotopeSeparator(FluidMachineMenu menu, Inventory inventory, Component title) {
        return new FluidMachineScreen(menu, inventory, title, ComminutionCraft.id("textures/gui/container/isotope_separator.png"),
                List.of(new Gauge(0, 50, WATER_U, "gui.comminutioncraft.water"),
                        new Gauge(1, PLASMA_X, DEUTERIUM_U, "gui.comminutioncraft.deuterium")),
                "gui.comminutioncraft.fluid_rate", Tier.FUSION);
    }

    /** Deuterium (in) -> tritium (out). */
    public static FluidMachineScreen tritiumBreeder(FluidMachineMenu menu, Inventory inventory, Component title) {
        return new FluidMachineScreen(menu, inventory, title, ComminutionCraft.id("textures/gui/container/tritium_breeder.png"),
                List.of(new Gauge(0, 50, DEUTERIUM_U, "gui.comminutioncraft.deuterium"),
                        new Gauge(1, PLASMA_X, TRITIUM_U, "gui.comminutioncraft.tritium")),
                "gui.comminutioncraft.fluid_rate", Tier.FUSION);
    }

    @Override
    protected boolean isActive() {
        return menu.getRate() > 0;
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        super.extractBackground(graphics, mouseX, mouseY, partialTick);
        drawEnergyBar(graphics, menu.getEnergy(), menu.getEnergyCapacity());
        for (Gauge gauge : gauges) {
            drawGauge(graphics, gauge.x(), gauge.u(), menu.getTankAmount(gauge.tank()), menu.getTankCapacity(gauge.tank()));
        }
        if (isActive() && minecraft != null && minecraft.level != null) {
            drawArrow(graphics, 79, 34, (minecraft.level.getGameTime() % ARROW_TICKS + partialTick) / ARROW_TICKS);
        }
    }

    @Override
    protected void extractLabels(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractLabels(graphics, mouseX, mouseY);
        Component rate = Component.translatable(rateKey, formatFe(menu.getRate()));
        graphics.text(font, rate, 91 - font.width(rate) / 2, 56, LABEL_COLOR, false);
    }

    @Override
    protected void extractTooltip(GuiGraphicsExtractor graphics, int mouseX, int mouseY) {
        super.extractTooltip(graphics, mouseX, mouseY);
        energyTooltip(graphics, mouseX, mouseY, menu.getEnergy(), menu.getEnergyCapacity());
        for (Gauge gauge : gauges) {
            gaugeTooltip(graphics, mouseX, mouseY, gauge.x(), gauge.tooltipKey(),
                    menu.getTankAmount(gauge.tank()), menu.getTankCapacity(gauge.tank()));
        }
    }
}
