package com.script.comminutioncraft.common.data;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.registry.ModDamageTypes;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.KeyTagProvider;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageType;

/** The event horizon gets through everything: invulnerability (creative), armour, effects, enchantments, totems. */
public class ModDamageTypeTagsProvider extends KeyTagProvider<DamageType> {
    public ModDamageTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, Registries.DAMAGE_TYPE, lookupProvider, ComminutionCraft.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(DamageTypeTags.BYPASSES_INVULNERABILITY).add(ModDamageTypes.EVENT_HORIZON);
        tag(DamageTypeTags.BYPASSES_ARMOR).add(ModDamageTypes.EVENT_HORIZON);
        tag(DamageTypeTags.BYPASSES_EFFECTS).add(ModDamageTypes.EVENT_HORIZON);
        tag(DamageTypeTags.BYPASSES_RESISTANCE).add(ModDamageTypes.EVENT_HORIZON);
        tag(DamageTypeTags.BYPASSES_ENCHANTMENTS).add(ModDamageTypes.EVENT_HORIZON);
        tag(DamageTypeTags.BYPASSES_SHIELD).add(ModDamageTypes.EVENT_HORIZON);
        tag(DamageTypeTags.NO_KNOCKBACK).add(ModDamageTypes.EVENT_HORIZON);
    }
}
