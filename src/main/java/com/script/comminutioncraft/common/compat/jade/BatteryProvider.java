package com.script.comminutioncraft.common.compat.jade;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.machine.EnergyBatteryBlockEntity;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.Nullable;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.StreamServerDataProvider;
import snownee.jade.api.config.IPluginConfig;

/** Jade view for the Energy Battery: "In 40 FE/t · Out 27 FE/t" (the stored FE bar is Jade's own). */
public class BatteryProvider implements StreamServerDataProvider<BlockAccessor, BatteryProvider.Data> {
    public static final BatteryProvider INSTANCE = new BatteryProvider();
    public static final Identifier UID = ComminutionCraft.id("battery");

    @Override
    public boolean shouldRequestData(BlockAccessor accessor) {
        return accessor.getBlockEntity() instanceof EnergyBatteryBlockEntity;
    }

    @Override
    public @Nullable Data streamData(BlockAccessor accessor) {
        if (!(accessor.getBlockEntity() instanceof EnergyBatteryBlockEntity battery)) {
            return null;
        }
        return new Data(battery.getReceived(), battery.getSent());
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, Data> streamCodec() {
        return Data.STREAM_CODEC;
    }

    @Override
    public Identifier getUid() {
        return UID;
    }

    public record Data(int received, int sent) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Data> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Data::received,
                ByteBufCodecs.VAR_INT, Data::sent,
                Data::new);
    }

    public static class Client implements IBlockComponentProvider {
        public static final Client INSTANCE = new Client();

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            Data data = BatteryProvider.INSTANCE.decodeFromData(accessor).orElse(null);
            if (data == null) {
                return;
            }
            boolean flowing = data.received() > 0 || data.sent() > 0;
            tooltip.add(Component.translatable("jade.comminutioncraft.battery_flow",
                            String.format(Locale.ROOT, "%,d", data.received()), String.format(Locale.ROOT, "%,d", data.sent()))
                    .withStyle(flowing ? ChatFormatting.AQUA : ChatFormatting.GRAY));
        }

        @Override
        public Identifier getUid() {
            return UID;
        }
    }
}
