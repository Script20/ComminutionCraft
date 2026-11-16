package com.script.comminutioncraft.common.network;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.item.tool.DrillItem;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** Client -> server: the drill-mode key was pressed while holding a Fusion or better drill in {@code hand}. */
public record SwitchDrillModePayload(InteractionHand hand) implements CustomPacketPayload {
    public static final Type<SwitchDrillModePayload> TYPE = new Type<>(ComminutionCraft.id("switch_drill_mode"));
    public static final StreamCodec<ByteBuf, SwitchDrillModePayload> STREAM_CODEC =
            InteractionHand.STREAM_CODEC.map(SwitchDrillModePayload::new, SwitchDrillModePayload::hand);

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(TYPE, STREAM_CODEC, SwitchDrillModePayload::handle);
    }

    /** Runs on the server thread (the registrar's default). */
    private static void handle(SwitchDrillModePayload payload, IPayloadContext context) {
        Player player = context.player();
        ItemStack stack = player.getItemInHand(payload.hand());
        if (stack.getItem() instanceof DrillItem drill && drill.hasModes()) {
            DrillItem.switchMode(player.level(), player, stack);
        }
    }

    @Override
    public Type<SwitchDrillModePayload> type() {
        return TYPE;
    }
}
