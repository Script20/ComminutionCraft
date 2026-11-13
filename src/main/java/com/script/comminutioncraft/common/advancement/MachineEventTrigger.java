package com.script.comminutioncraft.common.advancement;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.script.comminutioncraft.common.registry.ModTriggers;
import java.util.Optional;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.criterion.ContextAwarePredicate;
import net.minecraft.advancements.criterion.EntityPredicate;
import net.minecraft.advancements.criterion.SimpleCriterionTrigger;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * {@code comminutioncraft:machine_event}: a moment in a machine that an advancement waits for (a Fusion Reactor
 * lighting up, antimatter made, a black hole ignited...). The machine calls {@link #fireNear}; every player close
 * enough to see it gets it. JSON: {@code {"trigger": "comminutioncraft:machine_event", "conditions": {"event": "..."}}}.
 */
public class MachineEventTrigger extends SimpleCriterionTrigger<MachineEventTrigger.TriggerInstance> {
    public static final String FUSION_REACTOR_LIT = "fusion_reactor_lit";
    public static final String ANTIMATTER_MADE = "antimatter_made";
    public static final String BLACK_HOLE_IGNITED = "black_hole_ignited";
    public static final String PURIFIER_BYPRODUCT = "purifier_byproduct";
    public static final String SINGULARITY_PROCESSED = "singularity_processed";
    public static final String SPAGHETTIFIED = "spaghettified";

    @Override
    public Codec<TriggerInstance> codec() {
        return TriggerInstance.CODEC;
    }

    public void trigger(ServerPlayer player, String event) {
        trigger(player, instance -> instance.event().equals(event));
    }

    /** Gives the event to every player within {@code radius} blocks of {@code pos}. */
    public static void fireNear(ServerLevel level, BlockPos pos, String event, double radius) {
        for (ServerPlayer player : level.players()) {
            if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) <= radius * radius) {
                ModTriggers.MACHINE_EVENT.get().trigger(player, event);
            }
        }
    }

    public record TriggerInstance(Optional<ContextAwarePredicate> player, String event) implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<TriggerInstance> CODEC = RecordCodecBuilder.create(i -> i.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player").forGetter(TriggerInstance::player),
                Codec.STRING.fieldOf("event").forGetter(TriggerInstance::event)
        ).apply(i, TriggerInstance::new));

        public static Criterion<TriggerInstance> of(String event) {
            return ModTriggers.MACHINE_EVENT.get().createCriterion(new TriggerInstance(Optional.empty(), event));
        }
    }
}
