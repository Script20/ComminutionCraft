package com.script.comminutioncraft.common.compat.jade;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.machine.ElectricGeneratorBlockEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
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

/** Jade view for the Electric Generator: its fuel, and "Generating 40 FE/t (12s left)" while burning. */
public class GeneratorProvider implements StreamServerDataProvider<BlockAccessor, GeneratorProvider.Data> {
    public static final GeneratorProvider INSTANCE = new GeneratorProvider();
    public static final Identifier UID = ComminutionCraft.id("generator");

    @Override
    public boolean shouldRequestData(BlockAccessor accessor) {
        return accessor.getBlockEntity() instanceof ElectricGeneratorBlockEntity;
    }

    @Override
    public @Nullable Data streamData(BlockAccessor accessor) {
        if (!(accessor.getBlockEntity() instanceof ElectricGeneratorBlockEntity generator)) {
            return null;
        }
        return new Data(generator.getItem(ElectricGeneratorBlockEntity.SLOT_FUEL), generator.getLitTimeRemaining());
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, Data> streamCodec() {
        return Data.STREAM_CODEC;
    }

    @Override
    public Identifier getUid() {
        return UID;
    }

    public record Data(ItemStack fuel, int burnTicksLeft) {
        public static final StreamCodec<RegistryFriendlyByteBuf, Data> STREAM_CODEC = StreamCodec.composite(
                ItemStack.OPTIONAL_STREAM_CODEC, Data::fuel,
                ByteBufCodecs.VAR_INT, Data::burnTicksLeft,
                Data::new);
    }

    public static class Client implements IBlockComponentProvider {
        public static final Client INSTANCE = new Client();

        @Override
        public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
            Data data = GeneratorProvider.INSTANCE.decodeFromData(accessor).orElse(null);
            if (data == null) {
                return;
            }
            if (data.burnTicksLeft() > 0) {
                tooltip.add(Component.translatable("jade.comminutioncraft.generating",
                        ElectricGeneratorBlockEntity.ENERGY_PER_TICK, data.burnTicksLeft() / 20).withStyle(ChatFormatting.GOLD));
            } else {
                tooltip.add(Component.translatable("jade.comminutioncraft.idle").withStyle(ChatFormatting.GRAY));
            }
            if (!data.fuel().isEmpty()) {
                tooltip.add(JadeUI.item(data.fuel()).alignSelfCenter());
                tooltip.append(JadeUI.text(data.fuel().getHoverName()).alignSelfCenter());
            }
        }

        @Override
        public Identifier getUid() {
            return UID;
        }
    }
}
