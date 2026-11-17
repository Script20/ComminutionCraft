package com.script.comminutioncraft.common.network;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.machine.CreativeChestMenu;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * An item dragged from JEI onto the Creative Chest's slot (client -> server). Only creative-mode players may pick an
 * item this way (anyone can still pick an item they hold by clicking the slot with it).
 */
public record SetCreativeChestItemPayload(ItemStack stack) implements CustomPacketPayload {
    public static final Type<SetCreativeChestItemPayload> TYPE = new Type<>(ComminutionCraft.id("set_creative_chest_item"));
    public static final StreamCodec<RegistryFriendlyByteBuf, SetCreativeChestItemPayload> STREAM_CODEC =
            ItemStack.OPTIONAL_STREAM_CODEC.map(SetCreativeChestItemPayload::new, SetCreativeChestItemPayload::stack);

    public static void register(RegisterPayloadHandlersEvent event) {
        event.registrar("1").playToServer(TYPE, STREAM_CODEC, SetCreativeChestItemPayload::handle);
    }

    /** Runs on the server thread (the registrar's default). */
    private static void handle(SetCreativeChestItemPayload payload, IPayloadContext context) {
        Player player = context.player();
        if (player.isCreative() && player.containerMenu instanceof CreativeChestMenu menu && menu.stillValid(player)) {
            menu.pick(payload.stack());
        }
    }

    @Override
    public Type<SetCreativeChestItemPayload> type() {
        return TYPE;
    }
}
