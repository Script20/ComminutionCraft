package com.script.comminutioncraft.client.sound;

import net.minecraft.client.resources.sounds.AbstractSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;

/** A sound played once in the middle of a block (the black hole forming or collapsing). */
public class OneShotSound extends AbstractSoundInstance {
    public OneShotSound(SoundEvent event, BlockPos pos, float volume) {
        super(event, SoundSource.BLOCKS, SoundInstance.createUnseededRandom());
        this.looping = false;
        this.delay = 0;
        this.volume = volume;
        this.x = pos.getX() + 0.5;
        this.y = pos.getY() + 0.5;
        this.z = pos.getZ() + 0.5;
    }
}
