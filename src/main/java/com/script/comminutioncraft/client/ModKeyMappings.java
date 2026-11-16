package com.script.comminutioncraft.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.item.WrenchItem;
import com.script.comminutioncraft.common.item.tool.DrillItem;
import com.script.comminutioncraft.common.network.SwitchDrillModePayload;
import com.script.comminutioncraft.common.network.SwitchWrenchModePayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.settings.IKeyConflictContext;
import net.neoforged.neoforge.client.settings.KeyConflictContext;
import org.lwjgl.glfw.GLFW;

/** Our key bindings (Options > Controls > Key Binds > Comminution Craft). */
public final class ModKeyMappings {
    public static final KeyMapping.Category CATEGORY = new KeyMapping.Category(ComminutionCraft.id("main"));

    /** Cycles the held wrench's mode. V by default. */
    public static final KeyMapping WRENCH_MODE = new KeyMapping(WrenchItem.MODE_KEY, Held.WRENCH,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, CATEGORY);
    /** Switches the held Fusion or better drill between 3x3 and 1x1. Also V by default: it only works while holding one. */
    public static final KeyMapping DRILL_MODE = new KeyMapping(DrillItem.MODE_KEY, Held.DRILL,
            InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_V, CATEGORY);

    /** A key that only works in game while holding its tool; the two tool keys don't conflict with each other. */
    private enum Held implements IKeyConflictContext {
        WRENCH, DRILL;

        @Override
        public boolean isActive() {
            Player player = Minecraft.getInstance().player;
            return KeyConflictContext.IN_GAME.isActive() && player != null && (holding(player, this) != null);
        }

        @Override
        public boolean conflicts(IKeyConflictContext other) {
            return other == this || (!(other instanceof Held) && KeyConflictContext.IN_GAME.conflicts(other));
        }
    }

    /** The hand holding this key's tool, or null. */
    private static InteractionHand holding(Player player, Held tool) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (tool == Held.WRENCH ? stack.getItem() instanceof WrenchItem
                    : stack.getItem() instanceof DrillItem drill && drill.hasModes()) {
                return hand;
            }
        }
        return null;
    }

    public static void register(RegisterKeyMappingsEvent event) {
        event.registerCategory(CATEGORY);
        event.register(WRENCH_MODE);
        event.register(DRILL_MODE);
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        Player player = Minecraft.getInstance().player;
        while (WRENCH_MODE.consumeClick()) {
            InteractionHand hand = player == null ? null : holding(player, Held.WRENCH);
            if (hand != null) {
                ClientPacketDistributor.sendToServer(new SwitchWrenchModePayload(hand));
            }
        }
        while (DRILL_MODE.consumeClick()) {
            InteractionHand hand = player == null ? null : holding(player, Held.DRILL);
            if (hand != null) {
                ClientPacketDistributor.sendToServer(new SwitchDrillModePayload(hand));
            }
        }
    }

    private ModKeyMappings() {}
}
