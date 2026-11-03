package com.script.comminutioncraft.common.network;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.item.WrenchItem;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Client -> server: the wrench-mode key was pressed while holding a wrench in {@code hand}. */
public record SwitchWrenchModePayload(InteractionHand hand) implements CustomPacketPayload {
    public static final Type<SwitchWrenchModePayload> TYPE = new Type<>(ComminutionCraft.id("switch_wrench_mode"));
    public static final StreamCodec<ByteBuf, SwitchWrenchModePayload> STREAM_CODEC =
            InteractionHand.STREAM_CODEC.map(SwitchWrenchModePayload::new, SwitchWrenchModePayload::hand);

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(TYPE, STREAM_CODEC, SwitchWrenchModePayload::handle);
    }

    /** Runs on the server thread (the registrar's default). */
    private static void handle(SwitchWrenchModePayload payload, IPayloadContext context) {
        Player player = context.player();
        ItemStack stack = player.getItemInHand(payload.hand());
        if (stack.getItem() instanceof WrenchItem) {
            WrenchItem.switchMode(player.level(), player, stack);
        }
    }

    @Override
    public Type<SwitchWrenchModePayload> type() {
        return TYPE;
    }
}
