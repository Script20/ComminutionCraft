package com.script.comminutioncraft.common.registry;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.advancement.MachineEventTrigger;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Advancement triggers. */
public final class ModTriggers {
    public static final DeferredRegister<CriterionTrigger<?>> TRIGGERS = DeferredRegister.create(Registries.TRIGGER_TYPE, ComminutionCraft.MODID);

    public static final DeferredHolder<CriterionTrigger<?>, MachineEventTrigger> MACHINE_EVENT =
            TRIGGERS.register("machine_event", MachineEventTrigger::new);

    private ModTriggers() {}
}
