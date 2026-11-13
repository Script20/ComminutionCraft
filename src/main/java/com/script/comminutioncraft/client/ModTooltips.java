package com.script.comminutioncraft.client;

import com.script.comminutioncraft.common.cable.EnergyCableBlock;
import com.script.comminutioncraft.common.cable.FluidPipeBlock;
import com.script.comminutioncraft.common.item.EnergyBatteryItem;
import com.script.comminutioncraft.common.item.WrenchItem;
import com.script.comminutioncraft.common.item.WrenchMode;
import com.script.comminutioncraft.common.machine.CreativeEnergyBatteryBlock;
import com.script.comminutioncraft.common.machine.CreativeFluidTankBlock;
import com.script.comminutioncraft.common.machine.FluidTankBlock;
import com.script.comminutioncraft.common.machine.FluidTankBlockEntity;
import com.script.comminutioncraft.common.registry.ModDataComponents;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

/**
 * Every tooltip line the mod adds, kept behind Shift: without it an item with details shows only
 * "Hold Shift for Details". Covers the wrench, batteries, cables/pipes, tanks, the creative blocks and anything picked
 * up with the wrench (stored FE / fluid, kept settings).
 */
public final class ModTooltips {
    public static void onTooltip(ItemTooltipEvent event) {
        List<Component> details = details(event.getItemStack());
        if (details.isEmpty()) {
            return;
        }
        List<Component> tooltip = event.getToolTip();
        int at = Math.min(1, tooltip.size());
        if (Minecraft.getInstance().hasShiftDown()) {
            tooltip.addAll(at, details);
        } else {
            tooltip.add(at, Component.translatable("tooltip.comminutioncraft.hold_shift",
                    Component.translatable("tooltip.comminutioncraft.shift").withStyle(ChatFormatting.AQUA))
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    private static List<Component> details(ItemStack stack) {
        List<Component> lines = new ArrayList<>();
        if (stack.getItem() instanceof WrenchItem) {
            WrenchMode mode = WrenchItem.getMode(stack);
            lines.add(mode.displayName().copy().withStyle(ChatFormatting.AQUA));
            lines.add(mode.help().copy().withStyle(ChatFormatting.GRAY));
            lines.add(Component.translatable("item.comminutioncraft.wrench.help.switch", Component.keybind(WrenchItem.MODE_KEY))
                    .withStyle(ChatFormatting.DARK_GRAY));
            return lines;
        }
        com.script.comminutioncraft.common.machine.upgrade.UpgradeType upgrade = com.script.comminutioncraft.common.machine.upgrade.UpgradeType.of(stack);
        if (upgrade != null) {
            lines.add(gray("tooltip.comminutioncraft.upgrade", com.script.comminutioncraft.common.machine.upgrade.UpgradeType.MAX));
            lines.add(Component.translatable("tooltip.comminutioncraft.upgrade." + upgrade.id()).withColor(upgrade.colour()));
            return lines;
        }
        Block block = stack.getItem() instanceof BlockItem item ? item.getBlock() : null;
        SimpleFluidContent fluid = stack.get(ModDataComponents.STORED_FLUID.get());
        FluidStack contents = fluid == null ? FluidStack.EMPTY : fluid.copy();

        if (block instanceof EnergyCableBlock cable) {
            lines.add(gray("tooltip.comminutioncraft.cable_rate", number(cable.getTransferRate())));
        } else if (block instanceof FluidPipeBlock pipe) {
            lines.add(gray("tooltip.comminutioncraft.pipe_rate", number(pipe.getTransferRate())));
            if (pipe.carries(FluidResource.of(com.script.comminutioncraft.common.registry.ModFluids.ANTIMATTER.get()))) {
                lines.add(Component.translatable("tooltip.comminutioncraft.antimatter_only").withStyle(ChatFormatting.WHITE));
            } else if (!pipe.carries(FluidResource.of(Fluids.WATER))) {
                lines.add(Component.translatable("tooltip.comminutioncraft.plasma_only").withStyle(ChatFormatting.LIGHT_PURPLE));
            }
        } else if (block instanceof CreativeFluidTankBlock) {
            lines.add(gray("tooltip.comminutioncraft.creative_tank"));
        } else if (block instanceof FluidTankBlock) {
            boolean cell = block instanceof com.script.comminutioncraft.common.machine.ContainmentCellBlock;
            String capacity = number(cell ? com.script.comminutioncraft.common.machine.ContainmentCellBlockEntity.CAPACITY.get()
                    : FluidTankBlockEntity.CAPACITY.get());
            if (cell) {
                lines.add(Component.translatable("tooltip.comminutioncraft.antimatter_only").withStyle(ChatFormatting.WHITE));
            }
            lines.add(contents.isEmpty()
                    ? gray("tooltip.comminutioncraft.tank_amount", "0", capacity)
                    : gray("tooltip.comminutioncraft.tank_amount_fluid", contents.getHoverName(), number(contents.getAmount()), capacity));
            contents = FluidStack.EMPTY;  // shown already
        } else if (block instanceof com.script.comminutioncraft.common.storage.SoarynChestBlock) {
            lines.add(gray("tooltip.comminutioncraft.soaryn_chest"));
            if (com.script.comminutioncraft.common.config.ComminutionConfig.SOARYN_CHEST_INCLUDE_HOTBAR.get()) {
                lines.add(gray("tooltip.comminutioncraft.soaryn_chest_hotbar"));
            }
        } else if (block instanceof CreativeEnergyBatteryBlock) {
            lines.add(gray("tooltip.comminutioncraft.creative_battery"));
        } else if (block instanceof com.script.comminutioncraft.common.machine.HorizonPortBlock) {
            lines.add(gray("tooltip.comminutioncraft.horizon_port"));
            lines.add(Component.translatable("tooltip.comminutioncraft.accelerator_port.switch").withStyle(ChatFormatting.DARK_GRAY));
        } else if (block instanceof com.script.comminutioncraft.common.machine.HorizonControllerBlock) {
            lines.add(Component.translatable("tooltip.comminutioncraft.horizon_controller").withStyle(ChatFormatting.GOLD));
        } else if (block instanceof com.script.comminutioncraft.common.machine.AcceleratorPortBlock) {
            lines.add(gray("tooltip.comminutioncraft.accelerator_port"));
            lines.add(Component.translatable("tooltip.comminutioncraft.accelerator_port.switch").withStyle(ChatFormatting.DARK_GRAY));
        }

        Integer energy = stack.get(ModDataComponents.STORED_ENERGY.get());
        if (stack.getItem() instanceof EnergyBatteryItem battery) {
            lines.add(gray("gui.comminutioncraft.energy", number(EnergyBatteryItem.stored(stack)), number(battery.capacity())));
        } else if (energy != null) {
            lines.add(gray("gui.comminutioncraft.battery.stored", number(energy)));
        }
        if (!contents.isEmpty()) {
            lines.add(gray("tooltip.comminutioncraft.stored_fluid", contents.getHoverName(), number(contents.getAmount())));
        }
        List<Integer> upgrades = stack.get(ModDataComponents.UPGRADES.get());
        if (upgrades != null) { // a machine picked up with the wrench: "Upgrades: 3 Speed, 8 Energy"
            List<String> parts = new ArrayList<>();
            for (com.script.comminutioncraft.common.machine.upgrade.UpgradeType type : com.script.comminutioncraft.common.machine.upgrade.UpgradeType.values()) {
                int n = type.ordinal() < upgrades.size() ? upgrades.get(type.ordinal()) : 0;
                if (n > 0) {
                    parts.add(n + " " + Component.translatable("gui.comminutioncraft.upgrade." + type.id() + ".name").getString());
                }
            }
            lines.add(gray("tooltip.comminutioncraft.machine_upgrades", String.join(", ", parts)));
        }
        if (stack.has(ModDataComponents.MACHINE_SETTINGS.get())) {
            lines.add(gray("tooltip.comminutioncraft.machine_settings"));
        }
        return lines;
    }

    private static Component gray(String key, Object... args) {
        return Component.translatable(key, args).withStyle(ChatFormatting.GRAY);
    }

    private static String number(int amount) {
        return String.format(Locale.ROOT, "%,d", amount);
    }

    private ModTooltips() {}
}
