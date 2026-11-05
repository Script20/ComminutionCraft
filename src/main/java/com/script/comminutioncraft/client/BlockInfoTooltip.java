package com.script.comminutioncraft.client;

import com.script.comminutioncraft.common.cable.EnergyCableBlock;
import com.script.comminutioncraft.common.cable.FluidPipeBlock;
import com.script.comminutioncraft.common.machine.CreativeEnergyBatteryBlock;
import com.script.comminutioncraft.common.machine.CreativeFluidTankBlock;
import com.script.comminutioncraft.common.machine.FluidTankBlock;
import com.script.comminutioncraft.common.machine.FluidTankBlockEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

/** Tooltip lines saying what a cable, pipe or tank can carry or hold (and what the creative blocks do). */
public final class BlockInfoTooltip {
    public static void onTooltip(ItemTooltipEvent event) {
        if (!(event.getItemStack().getItem() instanceof BlockItem item)) {
            return;
        }
        List<Component> info = info(item.getBlock());
        if (!info.isEmpty()) {
            event.getToolTip().addAll(Math.min(1, event.getToolTip().size()), info);
        }
    }

    private static List<Component> info(Block block) {
        List<Component> lines = new ArrayList<>();
        if (block instanceof EnergyCableBlock cable) {
            lines.add(line("tooltip.comminutioncraft.cable_rate", cable.getTransferRate()));
        } else if (block instanceof FluidPipeBlock pipe) {
            lines.add(line("tooltip.comminutioncraft.pipe_rate", pipe.getTransferRate()));
            if (!pipe.carries(FluidResource.of(Fluids.WATER))) {
                lines.add(Component.translatable("tooltip.comminutioncraft.plasma_only").withStyle(ChatFormatting.LIGHT_PURPLE));
            }
        } else if (block instanceof CreativeFluidTankBlock) {
            lines.add(Component.translatable("tooltip.comminutioncraft.creative_tank").withStyle(ChatFormatting.GRAY));
        } else if (block instanceof FluidTankBlock) {
            lines.add(line("tooltip.comminutioncraft.tank_capacity", FluidTankBlockEntity.CAPACITY));
        } else if (block instanceof CreativeEnergyBatteryBlock) {
            lines.add(Component.translatable("tooltip.comminutioncraft.creative_battery").withStyle(ChatFormatting.GRAY));
        }
        return lines;
    }

    private static Component line(String key, int amount) {
        return Component.translatable(key, String.format(Locale.ROOT, "%,d", amount)).withStyle(ChatFormatting.GRAY);
    }

    private BlockInfoTooltip() {}
}
