package com.script.comminutioncraft.registry;

import com.script.comminutioncraft.ComminutionCraft;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Unit;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModDataComponents {
    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ComminutionCraft.MODID);

    /** Present on a wrench in Cable mode; absent = Rotate mode. (A marker also lets the item model switch on it.) */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Unit>> WRENCH_CABLE_MODE =
            DATA_COMPONENTS.registerComponentType("wrench_cable_mode", builder -> builder
                    .persistent(Unit.CODEC)
                    .networkSynchronized(StreamCodec.unit(Unit.INSTANCE)));

    private ModDataComponents() {}
}
