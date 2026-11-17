package com.script.comminutioncraft.common.compat.jade;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.pipe.ItemPipeBlockEntity;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.JadeUI;

/**
 * Item Pipe: when it's stuffed, says so and shows what's stuck. The stuck items come with the pipe's own client sync,
 * so this needs no server data.
 */
public final class ItemPipeProvider implements IBlockComponentProvider {
    public static final ItemPipeProvider INSTANCE = new ItemPipeProvider();
    public static final Identifier UID = ComminutionCraft.id("item_pipe");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (!(accessor.getBlockEntity() instanceof ItemPipeBlockEntity pipe)) {
            return;
        }
        List<ItemStack> stuck = pipe.stuck();
        if (stuck.isEmpty()) {
            return;
        }
        tooltip.add(Component.translatable("jade.comminutioncraft.item_pipe_stuffed").withStyle(ChatFormatting.RED));
        boolean first = true;
        for (ItemStack stack : stuck) {
            if (first) {
                tooltip.add(JadeUI.item(stack));
                first = false;
            } else {
                tooltip.append(JadeUI.item(stack));
            }
        }
    }

    @Override
    public Identifier getUid() {
        return UID;
    }
}
