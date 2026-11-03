package com.script.comminutioncraft.common.compat.jade;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.machine.PlasmaGeneratorBlockEntity;
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

/** Jade view for the Plasma Generator: "Intake 4/6 · 4 mB/t", and a warning without an Air Filter (FE/plasma bars are Jade's own). */
public class PlasmaGeneratorProvider implements StreamServerDataProvider<BlockAccessor, PlasmaGeneratorProvider.Data> {
    public static final PlasmaGeneratorProvider INSTANCE = new PlasmaGeneratorProvider();
    public static final Identifier UID = ComminutionCraft.id("plasma_generator");

    @Override
    public boolean shouldRequestData(BlockAccessor accessor) {
        return accessor.getBlockEntity() instanceof PlasmaGeneratorBlockEntity;
    }

    @Override
    public @Nullable Data streamData(BlockAccessor accessor) {
        if (!(accessor.getBlockEntity() instanceof PlasmaGeneratorBlockEntity generator)) {
            return null;
        }
        return new Data(generator.getIntake(), generator.getRate(), generator.hasFilter());
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, Data> streamCodec() {
        return Data.STREAM_CODEC;
    }

    @Override
    public Identifier getUid() {
        return UID;
    }

    public record Data(int intake, int rate, boolean filter) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Data> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Data::intake,
                ByteBufCodecs.VAR_INT, Data::rate,
                ByteBufCodecs.BOOL, Data::filter,
                Data::new);
    }

    public static class Client implements IBlockComponentProvider {
        public static final Client INSTANCE = new Client();

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            Data data = PlasmaGeneratorProvider.INSTANCE.decodeFromData(accessor).orElse(null);
            if (data == null) {
                return;
            }
            tooltip.add(Component.translatable("jade.comminutioncraft.plasma_generator", data.intake(), data.rate())
                    .withStyle(data.rate() > 0 ? ChatFormatting.LIGHT_PURPLE : ChatFormatting.GRAY));
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
