package com.script.comminutioncraft.common.registry;

import com.script.comminutioncraft.ComminutionCraft;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jspecify.annotations.Nullable;

/**
 * The mod's sounds (owner-approved 2026-11-13, synthesised by tools/gen_sounds.py and the hand-off in design/sounds):
 * one working loop per machine ({@code machine.<name>}, file {@code sounds/machine/<name>.ogg}), played on the client
 * while the machine is LIT (see {@code client/sound/MachineSounds}), and the Event Horizon Reactor's black hole:
 * {@code black_hole.form} once when it ignites, then the {@code black_hole.hum} loop, and {@code black_hole.collapse}
 * when it goes out.
 */
public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, ComminutionCraft.MODID);

    /** Every machine with a working loop; the name is the block id, except for the multiblocks (see {@link #ALIASES}). */
    public static final List<String> MACHINES = List.of(
            "coal_granulator", "coal_smelter",
            "electric_generator", "electric_granulator", "electric_smelter",
            "atmospheric_water_generator", "circuit_assembler", "electrolyser", "mhd_generator", "ore_washer",
            "plasma_generator", "plasma_granulator", "plasma_smelter",
            "isotope_separator", "ore_purifier", "pellet_press", "tritium_breeder", "fusion_reactor",
            "annihilation_generator", "antimatter_granulator", "antimatter_purifier", "antimatter_smelter",
            "antimatter_washer", "froth_flotation_cell", "particle_accelerator",
            "singularity_processor");
    /** Controller block id -> its multiblock's sound name. */
    private static final Map<String, String> ALIASES = Map.of(
            "reactor_controller", "fusion_reactor",
            "accelerator_controller", "particle_accelerator");

    public static final Map<String, DeferredHolder<SoundEvent, SoundEvent>> MACHINE = new LinkedHashMap<>();

    static {
        for (String name : MACHINES) {
            MACHINE.put(name, SOUND_EVENTS.register("machine." + name, SoundEvent::createVariableRangeEvent));
        }
    }

    /** The black hole is heard further than the machines. */
    public static final float BLACK_HOLE_RANGE = 32;
    public static final DeferredHolder<SoundEvent, SoundEvent> BLACK_HOLE_FORM = blackHole("form");
    public static final DeferredHolder<SoundEvent, SoundEvent> BLACK_HOLE_HUM = blackHole("hum");
    public static final DeferredHolder<SoundEvent, SoundEvent> BLACK_HOLE_COLLAPSE = blackHole("collapse");

    private static DeferredHolder<SoundEvent, SoundEvent> blackHole(String name) {
        return SOUND_EVENTS.register("black_hole." + name, id -> SoundEvent.createFixedRangeEvent(id, BLACK_HOLE_RANGE));
    }

    /** The working loop for this block, or null if it has none. */
    public static @Nullable SoundEvent machineLoop(Block block) {
        String id = BuiltInRegistries.BLOCK.getKey(block).getPath();
        DeferredHolder<SoundEvent, SoundEvent> holder = MACHINE.get(ALIASES.getOrDefault(id, id));
        return holder == null || !BuiltInRegistries.BLOCK.getKey(block).getNamespace().equals(ComminutionCraft.MODID)
                ? null : holder.get();
    }

    /** "mhd_generator" -> "MHD Generator" (for subtitles). */
    public static String displayName(String name) {
        StringBuilder out = new StringBuilder();
        for (String word : name.split("_")) {
            if (!out.isEmpty()) {
                out.append(' ');
            }
            out.append(word.equals("mhd") ? "MHD" : Character.toUpperCase(word.charAt(0)) + word.substring(1));
        }
        return out.toString();
    }

    private ModSounds() {}
}
