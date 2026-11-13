package com.script.comminutioncraft.common.data;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.registry.ModSounds;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.SoundDefinitionsProvider;

/** {@code assets/comminutioncraft/sounds.json}: each sound event, its .ogg and its subtitle key. */
public class ModSoundsProvider extends SoundDefinitionsProvider {
    public ModSoundsProvider(PackOutput output) {
        super(output, ComminutionCraft.MODID);
    }

    public static String subtitle(String event) {
        return "subtitles." + ComminutionCraft.MODID + "." + event;
    }

    @Override
    public void registerSounds() {
        ModSounds.MACHINE.forEach((name, holder) -> add(holder, definition()
                .subtitle(subtitle("machine." + name))
                .with(sound(ComminutionCraft.id("machine/" + name)))));
        int range = (int) ModSounds.BLACK_HOLE_RANGE;
        add(ModSounds.BLACK_HOLE_FORM, definition()
                .subtitle(subtitle("black_hole.form"))
                .with(sound(ComminutionCraft.id("black_hole/form")).stream().attenuationDistance(range)));
        add(ModSounds.BLACK_HOLE_HUM, definition()
                .subtitle(subtitle("black_hole.hum"))
                .with(sound(ComminutionCraft.id("black_hole/hum")).attenuationDistance(range)));
        add(ModSounds.BLACK_HOLE_COLLAPSE, definition()
                .subtitle(subtitle("black_hole.collapse"))
                .with(sound(ComminutionCraft.id("black_hole/collapse")).attenuationDistance(range)));
    }
}
