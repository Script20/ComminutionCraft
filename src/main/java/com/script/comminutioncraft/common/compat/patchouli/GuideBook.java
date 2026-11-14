package com.script.comminutioncraft.common.compat.patchouli;

import com.mojang.serialization.Codec;
import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.config.ComminutionConfig;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/**
 * The guide book (owner, 2026-11-14): a Patchouli book, {@code comminutioncraft:guide}, defined by data files
 * ({@code data/comminutioncraft/patchouli_books/guide/book.json} and {@code assets/.../patchouli_books/guide/en_us}).
 * Patchouli is optional: without it nothing here does anything. With it, every player is given the book the first
 * time they join a world or server (remembered per player, kept through death; the common config can turn it off),
 * and a book + a Wrench crafts another.
 */
public final class GuideBook {
    public static final Identifier BOOK = ComminutionCraft.id("guide");

    public static final DeferredRegister<AttachmentType<?>> ATTACHMENTS =
            DeferredRegister.create(NeoForgeRegistries.Keys.ATTACHMENT_TYPES, ComminutionCraft.MODID);

    /** Whether this player has been given the guide book already. */
    public static final DeferredHolder<AttachmentType<?>, AttachmentType<Boolean>> RECEIVED = ATTACHMENTS.register("received_guide_book",
            () -> AttachmentType.builder(() -> false).serialize(Codec.BOOL.fieldOf("received")).copyOnDeath().build());

    public static boolean patchouliLoaded() {
        return ModList.get().isLoaded("patchouli");
    }

    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !patchouliLoaded()
                || !ComminutionConfig.GIVE_GUIDE_BOOK.get() || player.getData(RECEIVED)) {
            return;
        }
        PatchouliBridge.give(player, BOOK); // only loaded when Patchouli is there
        player.setData(RECEIVED, true);
    }

    private GuideBook() {}
}
