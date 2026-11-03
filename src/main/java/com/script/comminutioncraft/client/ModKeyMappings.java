package com.script.comminutioncraft.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.item.WrenchItem;
import com.script.comminutioncraft.common.network.SwitchWrenchModePayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

/** Our key bindings (Options > Controls > Key Binds > Comminution Craft). */
public final class ModKeyMappings {
    public static final KeyMapping.Category CATEGORY = new KeyMapping.Category(ComminutionCraft.id("main"));

    /** Cycles the held wrench's mode. V by default. */
    public static final KeyMapping WRENCH_MODE = new KeyMapping(WrenchItem.MODE_KEY, KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, CATEGORY);

    public static void register(RegisterKeyMappingsEvent event) {
        event.registerCategory(CATEGORY);
        event.register(WRENCH_MODE);
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        while (WRENCH_MODE.consumeClick()) {
            Player player = Minecraft.getInstance().player;
            if (player == null) {
                return;
            }
            for (InteractionHand hand : InteractionHand.values()) {
                if (player.getItemInHand(hand).getItem() instanceof WrenchItem) {
                    ClientPacketDistributor.sendToServer(new SwitchWrenchModePayload(hand));
                    break;
                }
            }
        }
    }

    private ModKeyMappings() {}
}
