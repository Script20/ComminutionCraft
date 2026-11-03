package com.script.comminutioncraft.common.item;

import com.mojang.serialization.Codec;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.StringRepresentable;

/** The wrench's modes, cycled with the mode key (V by default). Stored in the {@code comminutioncraft:wrench_mode} component. */
public enum WrenchMode implements StringRepresentable {
    /** Turn blocks. */
    ROTATE("rotate"),
    /** Cut / restore energy cable connections. */
    CABLE("cable"),
    /** Cycle a machine face between Input, Output and None. */
    CONFIGURE("configure");

    public static final Codec<WrenchMode> CODEC = StringRepresentable.fromEnum(WrenchMode::values);
    public static final StreamCodec<ByteBuf, WrenchMode> STREAM_CODEC =
            ByteBufCodecs.idMapper(i -> values()[i], WrenchMode::ordinal);

    private final String name;

    WrenchMode(String name) {
        this.name = name;
    }

    public WrenchMode next() {
        return values()[(ordinal() + 1) % values().length];
    }

    public Component displayName() {
        return Component.translatable("item.comminutioncraft.wrench.mode." + name);
    }

    public Component help() {
        return Component.translatable("item.comminutioncraft.wrench.help." + name);
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
