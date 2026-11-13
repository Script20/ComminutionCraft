package com.script.comminutioncraft.client.sound;

import com.script.comminutioncraft.client.ClientConfig;
import java.util.function.BooleanSupplier;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

/**
 * A looping sound at a block that plays while {@code keepPlaying} says so, then fades out and stops. It fades in too,
 * so machines starting and stopping never click. Volume = {@code baseVolume} x the client config's machine volume.
 */
public class LoopSound extends AbstractTickableSoundInstance {
    private static final int FADE_IN_TICKS = 6;
    private static final int FADE_OUT_TICKS = 10;

    private final float baseVolume;
    private final BooleanSupplier keepPlaying;
    private boolean fading;

    public LoopSound(SoundEvent event, BlockPos pos, float baseVolume, BooleanSupplier keepPlaying) {
        super(event, SoundSource.BLOCKS, SoundInstance.createUnseededRandom());
        this.baseVolume = baseVolume;
        this.keepPlaying = keepPlaying;
        this.looping = true;
        this.delay = 0;
        this.volume = 0.0F;
        this.x = pos.getX() + 0.5;
        this.y = pos.getY() + 0.5;
        this.z = pos.getZ() + 0.5;
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    /** Still playing and not on its way out. */
    public boolean isRunning() {
        return !isStopped() && !fading;
    }

    /** Starts fading out (it stops itself once silent). */
    public void fadeOut() {
        fading = true;
    }

    /** Cancels a fade-out that hasn't finished yet. */
    public void wake() {
        if (!isStopped()) {
            fading = false;
        }
    }

    @Override
    public void tick() {
        float target = baseVolume * ClientConfig.machineVolume();
        if (!fading && !keepPlaying.getAsBoolean()) {
            fading = true;
        }
        if (fading) {
            volume -= Math.max(baseVolume, 0.05F) / FADE_OUT_TICKS;
            if (volume <= 0) {
                volume = 0;
                stop();
            }
        } else {
            volume = Math.min(target, volume + Math.max(target, 0.01F) / FADE_IN_TICKS);
        }
    }
}
