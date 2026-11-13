package com.script.comminutioncraft.client.sound;

import com.script.comminutioncraft.client.ClientConfig;
import com.script.comminutioncraft.common.machine.EventHorizonReactorBlockEntity;
import com.script.comminutioncraft.common.registry.ModSounds;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.jspecify.annotations.Nullable;

/**
 * Client side of the machine sounds: every few ticks it looks at the block entities near the player. A machine whose
 * block is LIT (working) gets its loop ({@link ModSounds#machineLoop}), which fades out by itself once the machine
 * stops. The Event Horizon Reactor's black hole plays {@code form} when its controller lights up, then the
 * {@code hum} loop (once {@code form} has finished, so the two join seamlessly), and {@code collapse} when it goes out
 * (starved, cage broken, put to sleep by redstone, or the controller broken). Coming into range of a hole that's
 * already running just starts the hum. Nothing here touches the server.
 */
public final class MachineSounds {
    private static final int SCAN_INTERVAL = 5;
    private static final double MACHINE_RANGE = 24;
    private static final double HOLE_RANGE = 40;
    private static final int MAX_LOOPS = 40;
    private static final float MACHINE_VOLUME = 0.45F;
    private static final float HOLE_VOLUME = 1.0F;
    /** black_hole/form.ogg is 9.4 s; the hum starts as it ends. */
    private static final int FORM_TICKS = 188;

    private static final Map<BlockPos, LoopSound> LOOPS = new HashMap<>();
    private static final Map<BlockPos, Hole> HOLES = new HashMap<>();
    private static @Nullable ClientLevel lastLevel;
    private static int scanTimer;

    /** One Event Horizon Reactor's sounds, keyed by its controller; they play at the hole's centre. */
    private static final class Hole {
        BlockPos centre;
        boolean lit;
        @Nullable OneShotSound form;
        int formTicks;
        @Nullable LoopSound hum;

        Hole(BlockPos centre) {
            this.centre = centre;
        }
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        Player player = mc.player;
        if (level != lastLevel) {
            reset(mc);
            lastLevel = level;
        }
        if (level == null || player == null) {
            return;
        }
        for (Hole hole : HOLES.values()) {
            if (hole.lit && hole.form != null && ++hole.formTicks >= FORM_TICKS) {
                hole.form = null;
                startHum(mc, level, hole);
            }
        }
        if (--scanTimer > 0) {
            return;
        }
        scanTimer = SCAN_INTERVAL;
        LOOPS.values().removeIf(LoopSound::isStopped);

        Set<BlockPos> seenHoles = new HashSet<>();
        int radius = (int) Math.ceil(HOLE_RANGE / 16);
        int pcx = player.blockPosition().getX() >> 4;
        int pcz = player.blockPosition().getZ() >> 4;
        for (int cx = pcx - radius; cx <= pcx + radius; cx++) {
            for (int cz = pcz - radius; cz <= pcz + radius; cz++) {
                if (!level.hasChunk(cx, cz)) {
                    continue;
                }
                for (BlockEntity be : level.getChunk(cx, cz).getBlockEntities().values()) {
                    BlockPos pos = be.getBlockPos();
                    BlockState state = be.getBlockState();
                    if (be instanceof EventHorizonReactorBlockEntity reactor) {
                        if (inRange(player, reactor.getCentre(), HOLE_RANGE)) {
                            seenHoles.add(pos.immutable());
                            updateHole(mc, level, pos.immutable(), reactor.getCentre(), isLit(state));
                        }
                    } else if (isLit(state) && inRange(player, pos, MACHINE_RANGE)) {
                        startLoop(mc, level, pos.immutable(), state.getBlock());
                    }
                }
            }
        }
        // holes no longer seen: broken (in range, so play the collapse) or simply out of range
        Iterator<Map.Entry<BlockPos, Hole>> it = HOLES.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<BlockPos, Hole> entry = it.next();
            if (!seenHoles.contains(entry.getKey())) {
                Hole hole = entry.getValue();
                goOut(mc, hole, hole.lit && inRange(player, hole.centre, HOLE_RANGE));
                it.remove();
            }
        }
    }

    private static boolean isLit(BlockState state) {
        return state.hasProperty(BlockStateProperties.LIT) && state.getValue(BlockStateProperties.LIT);
    }

    private static boolean inRange(Player player, BlockPos pos, double range) {
        return player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= range * range;
    }

    private static void startLoop(Minecraft mc, ClientLevel level, BlockPos pos, Block block) {
        LoopSound existing = LOOPS.get(pos);
        if (existing != null && !existing.isStopped()) {
            existing.wake(); // it started again before its fade-out finished
            return;
        }
        SoundEvent sound = ModSounds.machineLoop(block);
        if (sound == null || LOOPS.size() >= MAX_LOOPS) {
            return;
        }
        LoopSound loop = new LoopSound(sound, pos, MACHINE_VOLUME, () -> Minecraft.getInstance().level == level
                && level.getBlockState(pos).is(block) && isLit(level.getBlockState(pos))
                && Minecraft.getInstance().player != null && inRange(Minecraft.getInstance().player, pos, MACHINE_RANGE + 8));
        LOOPS.put(pos, loop);
        mc.getSoundManager().play(loop);
    }

    private static void updateHole(Minecraft mc, ClientLevel level, BlockPos controller, BlockPos centre, boolean lit) {
        Hole hole = HOLES.get(controller);
        if (hole == null) {
            hole = new Hole(centre);
            HOLES.put(controller, hole);
            hole.lit = lit;
            if (lit) {
                startHum(mc, level, hole); // already running when it came into range: no forming sound
            }
            return;
        }
        hole.centre = centre;
        if (lit && !hole.lit) { // ignited (or woken by redstone)
            hole.lit = true;
            if (hole.hum != null) {
                hole.hum.fadeOut();
                hole.hum = null;
            }
            hole.form = new OneShotSound(ModSounds.BLACK_HOLE_FORM.get(), centre, HOLE_VOLUME * ClientConfig.machineVolume());
            hole.formTicks = 0;
            mc.getSoundManager().play(hole.form);
        } else if (!lit && hole.lit) { // starved, cage broken or put to sleep
            goOut(mc, hole, true);
        }
    }

    private static void startHum(Minecraft mc, ClientLevel level, Hole hole) {
        LoopSound hum = new LoopSound(ModSounds.BLACK_HOLE_HUM.get(), hole.centre, HOLE_VOLUME,
                () -> hole.lit && Minecraft.getInstance().level == level);
        hole.hum = hum;
        mc.getSoundManager().play(hum);
    }

    private static void goOut(Minecraft mc, Hole hole, boolean collapse) {
        hole.lit = false;
        if (hole.form != null) {
            mc.getSoundManager().stop(hole.form);
            hole.form = null;
        }
        if (hole.hum != null) {
            hole.hum.fadeOut();
            hole.hum = null;
        }
        if (collapse) {
            mc.getSoundManager().play(new OneShotSound(ModSounds.BLACK_HOLE_COLLAPSE.get(), hole.centre,
                    HOLE_VOLUME * ClientConfig.machineVolume()));
        }
    }

    private static void reset(Minecraft mc) {
        LOOPS.values().forEach(LoopSound::fadeOut);
        LOOPS.clear();
        for (Hole hole : HOLES.values()) {
            goOut(mc, hole, false);
        }
        HOLES.clear();
        scanTimer = 0;
    }

    private MachineSounds() {}
}
