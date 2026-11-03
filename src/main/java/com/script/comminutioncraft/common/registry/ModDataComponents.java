package com.script.comminutioncraft.common.registry;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.item.WrenchMode;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.util.ExtraCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModDataComponents {
    public static final DeferredRegister.DataComponents DATA_COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, ComminutionCraft.MODID);

    /** The wrench's mode; absent = Rotate. The item model selects its texture on this. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<WrenchMode>> WRENCH_MODE =
            DATA_COMPONENTS.registerComponentType("wrench_mode", builder -> builder
                    .persistent(WrenchMode.CODEC)
                    .networkSynchronized(WrenchMode.STREAM_CODEC));

    /** FE kept in a broken Energy Battery; placing the item puts it back. */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Integer>> STORED_ENERGY =
            DATA_COMPONENTS.registerComponentType("stored_energy", builder -> builder
                    .persistent(ExtraCodecs.NON_NEGATIVE_INT)
                    .networkSynchronized(ByteBufCodecs.VAR_INT));

    private ModDataComponents() {}
}
