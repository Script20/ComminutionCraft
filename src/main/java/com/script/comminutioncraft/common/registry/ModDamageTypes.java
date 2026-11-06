package com.script.comminutioncraft.common.registry;

import com.script.comminutioncraft.ComminutionCraft;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;

/**
 * {@code comminutioncraft:event_horizon} - what kills anything that walks into a running Event Horizon Reactor's
 * field. It's in {@code #minecraft:bypasses_invulnerability} (and the armour / effect / enchantment bypass tags), so it
 * kills creative players too (owner's rule). Death message: "death.attack.comminutioncraft.event_horizon".
 */
public final class ModDamageTypes {
    public static final ResourceKey<DamageType> EVENT_HORIZON = ResourceKey.create(Registries.DAMAGE_TYPE, ComminutionCraft.id("event_horizon"));

    public static void bootstrap(BootstrapContext<DamageType> context) {
        context.register(EVENT_HORIZON, new DamageType("comminutioncraft.event_horizon", 0.0F));
    }

    public static DamageSource eventHorizon(ServerLevel level) {
        return new DamageSource(level.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(EVENT_HORIZON));
    }

    private ModDamageTypes() {}
}
