package com.script.comminutioncraft.common.compat.jade;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.machine.AtmosphericWaterGeneratorBlockEntity;
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

/** Jade view for the Atmospheric Water Generator: "Intake 4/6 · Humidity 65% · 2.6 mB/t", and a note without an Air Filter (the water bar is Jade's own). */
public class WaterGeneratorProvider implements StreamServerDataProvider<BlockAccessor, WaterGeneratorProvider.Data> {
    public static final WaterGeneratorProvider INSTANCE = new WaterGeneratorProvider();
    public static final Identifier UID = ComminutionCraft.id("water_generator");

    @Override
    public boolean shouldRequestData(BlockAccessor accessor) {
        return accessor.getBlockEntity() instanceof AtmosphericWaterGeneratorBlockEntity;
    }

    @Override
    public @Nullable Data streamData(BlockAccessor accessor) {
        if (!(accessor.getBlockEntity() instanceof AtmosphericWaterGeneratorBlockEntity generator)) {
            return null;
        }
        return new Data(generator.getIntake(), Math.round(generator.getHumidity() * 100), Math.round(generator.getRate() * 10),
                generator.hasFilter());
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, Data> streamCodec() {
        return Data.STREAM_CODEC;
    }

    @Override
    public Identifier getUid() {
        return UID;
    }

    /** Humidity in percent, rate in tenths of a mB per tick. */
    public record Data(int intake, int humidity, int rate, boolean filter) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Data> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Data::intake,
                ByteBufCodecs.VAR_INT, Data::humidity,
                ByteBufCodecs.VAR_INT, Data::rate,
                ByteBufCodecs.BOOL, Data::filter,
                Data::new);
    }

    public static class Client implements IBlockComponentProvider {
        public static final Client INSTANCE = new Client();

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            Data data = WaterGeneratorProvider.INSTANCE.decodeFromData(accessor).orElse(null);
            if (data == null) {
                return;
            }
            tooltip.add(Component.translatable("jade.comminutioncraft.water_generator", data.intake(), data.humidity(),
                            String.format(Locale.ROOT, "%.1f", data.rate() / 10.0F))
                    .withStyle(data.rate() > 0 ? ChatFormatting.AQUA : ChatFormatting.GRAY));
            if (!data.filter()) {
                tooltip.add(Component.translatable("jade.comminutioncraft.no_filter").withStyle(ChatFormatting.GOLD));
            }
        }

        @Override
        public Identifier getUid() {
            return UID;
        }
    }
}
