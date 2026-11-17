package com.script.comminutioncraft.common.compat.jade;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.pipe.ItemPipeBlockEntity;
import com.script.comminutioncraft.common.pipe.TravellingItem;
import java.util.ArrayList;
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
 * Item Pipe: what's moving through the pipes joined to it and, when it's stuffed, says so and shows what's stuck. Both come with the
 * pipe's own client sync, so this needs no server data.
 */
public final class ItemPipeProvider implements IBlockComponentProvider {
    public static final ItemPipeProvider INSTANCE = new ItemPipeProvider();
    public static final Identifier UID = ComminutionCraft.id("item_pipe");

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        if (!(accessor.getBlockEntity() instanceof ItemPipeBlockEntity pipe)) {
            return;
        }
        // everything moving through the pipes joined to this one, not just this pipe
        List<ItemStack> moving = ItemPipeBlockEntity.network(accessor.getLevel(), accessor.getPosition()).stream()
                .flatMap(p -> p.items().stream()).map(TravellingItem::stack).toList();
        if (!moving.isEmpty()) {
            tooltip.add(Component.translatable("jade.comminutioncraft.item_pipe_moving").withStyle(ChatFormatting.AQUA));
            items(tooltip, moving);
        }
        List<ItemStack> stuck = pipe.stuck();
        if (!stuck.isEmpty()) {
            tooltip.add(Component.translatable("jade.comminutioncraft.item_pipe_stuffed").withStyle(ChatFormatting.RED));
            items(tooltip, stuck);
        }
    }

    /** One row of icons with counts, the same item merged into one. */
    private static void items(ITooltip tooltip, List<ItemStack> stacks) {
        List<ItemStack> merged = new ArrayList<>();
        for (ItemStack stack : stacks) {
            ItemStack same = merged.stream().filter(m -> ItemStack.isSameItemSameComponents(m, stack)).findFirst().orElse(null);
            if (same != null) {
                same.grow(stack.getCount());
            } else {
                merged.add(stack.copy());
            }
        }
        for (int i = 0; i < merged.size(); i++) {
            if (i == 0) {
                tooltip.add(JadeUI.item(merged.get(i)));
            } else {
                tooltip.append(JadeUI.item(merged.get(i)));
            }
        }
    }

    @Override
    public Identifier getUid() {
        return UID;
    }
}
