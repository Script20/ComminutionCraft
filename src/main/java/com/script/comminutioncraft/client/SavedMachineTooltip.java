package com.script.comminutioncraft.client;

import com.script.comminutioncraft.common.item.EnergyBatteryItem;
import com.script.comminutioncraft.common.registry.ModDataComponents;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/** Tooltip lines for a machine picked up with the wrench: its stored FE and that it kept its settings. */
public final class SavedMachineTooltip {
    public static void onTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        List<Component> lines = event.getToolTip();
        int at = Math.min(1, lines.size());
        Integer energy = stack.get(ModDataComponents.STORED_ENERGY.get());
        // The battery's own tooltip already shows its charge.
        if (energy != null && !(stack.getItem() instanceof EnergyBatteryItem)) {
            lines.add(at++, Component.translatable("gui.comminutioncraft.battery.stored", String.format(Locale.ROOT, "%,d", energy))
                    .withStyle(ChatFormatting.GRAY));
        }
        if (stack.has(ModDataComponents.MACHINE_SETTINGS.get())) {
            lines.add(at, Component.translatable("tooltip.comminutioncraft.machine_settings").withStyle(ChatFormatting.GRAY));
        }
    }

    private SavedMachineTooltip() {}
}
