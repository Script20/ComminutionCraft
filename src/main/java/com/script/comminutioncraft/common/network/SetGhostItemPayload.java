package com.script.comminutioncraft.common.network;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.menu.GhostItemMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/** An item dragged from JEI onto a ghost slot (client -> server): the open {@link GhostItemMenu} decides what to do. */
public record SetGhostItemPayload(int slot, ItemStack stack) implements CustomPacketPayload {
    public static final Type<SetGhostItemPayload> TYPE = new Type<>(ComminutionCraft.id("set_ghost_item"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SetGhostItemPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SetGhostItemPayload::slot,
            ItemStack.OPTIONAL_STREAM_CODEC, SetGhostItemPayload::stack,
            SetGhostItemPayload::new);

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(TYPE, STREAM_CODEC, SetGhostItemPayload::handle);
    }

    /** Runs on the server thread (the registrar's default). */
    private static void handle(SetGhostItemPayload payload, IPayloadContext context) {
        Player player = context.player();
        if (player.containerMenu instanceof GhostItemMenu menu && player.containerMenu.stillValid(player)) {
            menu.setGhost(player, payload.slot(), payload.stack());
        }
    }

    @Override
    public Type<SetGhostItemPayload> type() {
        return TYPE;
    }
}
