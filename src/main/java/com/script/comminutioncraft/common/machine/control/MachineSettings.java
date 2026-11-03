package com.script.comminutioncraft.common.machine.control;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * A machine's redstone mode and packed side modes (see {@link MachineControls#packSides()}), kept on the item when a
 * machine is picked up with the wrench ({@code comminutioncraft:machine_settings}).
 */
public record MachineSettings(RedstoneMode redstone, int sides) {
    public static final Codec<MachineSettings> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.INT.xmap(RedstoneMode::byOrdinal, RedstoneMode::ordinal).fieldOf("redstone").forGetter(MachineSettings::redstone),
            Codec.INT.fieldOf("sides").forGetter(MachineSettings::sides)
    ).apply(instance, MachineSettings::new));

    public static final StreamCodec<ByteBuf, MachineSettings> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT.map(RedstoneMode::byOrdinal, RedstoneMode::ordinal), MachineSettings::redstone,
            ByteBufCodecs.VAR_INT, MachineSettings::sides,
            MachineSettings::new);
}
