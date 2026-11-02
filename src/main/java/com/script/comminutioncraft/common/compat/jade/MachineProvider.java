package com.script.comminutioncraft.common.compat.jade;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.machine.ElectricMachineBlockEntity;
import com.script.comminutioncraft.common.machine.FuelMachineBlockEntity;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.StreamServerDataProvider;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.JadeUI;

/**
 * Jade view for processing machines (both tiers), like Jade's own furnace view:
 * input (+ fuel for tier 1) -> progress arrow -> output. Energy is shown by Jade itself from the FE capability.
 */
public class MachineProvider implements StreamServerDataProvider<BlockAccessor, MachineProvider.Data> {
    public static final MachineProvider INSTANCE = new MachineProvider();
    public static final Identifier UID = ComminutionCraft.id("machine");

    @Override
    public boolean shouldRequestData(BlockAccessor accessor) {
        return accessor.getBlockEntity() instanceof FuelMachineBlockEntity || accessor.getBlockEntity() instanceof ElectricMachineBlockEntity;
    }

    @Override
    public @Nullable Data streamData(BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof FuelMachineBlockEntity machine) {
            return new Data(machine.getProgress(), machine.getTotalProcessTime(), List.of(
                    machine.getItem(FuelMachineBlockEntity.SLOT_INPUT),
                    machine.getItem(FuelMachineBlockEntity.SLOT_FUEL),
                    machine.getItem(FuelMachineBlockEntity.SLOT_OUTPUT)));
        }
        if (accessor.getBlockEntity() instanceof ElectricMachineBlockEntity machine) {
            return new Data(machine.getProgress(), machine.getTotalProcessTime(), List.of(
                    machine.getItem(ElectricMachineBlockEntity.SLOT_INPUT),
                    machine.getItem(ElectricMachineBlockEntity.SLOT_OUTPUT)));
        }
        return null;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, Data> streamCodec() {
        return Data.STREAM_CODEC;
    }

    @Override
    public Identifier getUid() {
        return UID;
    }

    /** Inventory is [input, fuel, output] for tier 1 and [input, output] for tier 2. */
    public record Data(int progress, int total, List<ItemStack> inventory) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Data> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Data::progress,
                ByteBufCodecs.VAR_INT, Data::total,
                ItemStack.OPTIONAL_LIST_STREAM_CODEC, Data::inventory,
                Data::new);
    }

    public static class Client implements IBlockComponentProvider {
        public static final Client INSTANCE = new Client();

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            Data data = MachineProvider.INSTANCE.decodeFromData(accessor).orElse(null);
            if (data == null || data.inventory().stream().allMatch(ItemStack::isEmpty)) {
                return;
            }
            List<ItemStack> items = data.inventory();
            tooltip.add(JadeUI.item(items.getFirst()).alignSelfCenter());
            for (ItemStack middle : items.subList(1, items.size() - 1)) {
                tooltip.append(JadeUI.item(middle).alignSelfCenter());
            }
            float progress = data.total() == 0 ? 0 : (float) data.progress() / data.total();
            tooltip.append(JadeUI.progressArrow(progress).alignSelfCenter().settings(s -> s.paddingHorizontal(3)));
            tooltip.append(JadeUI.item(items.getLast()).alignSelfCenter());
        }

        @Override
        public Identifier getUid() {
            return UID;
        }
    }
}
