package com.script.comminutioncraft.common.data;

import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.block.WoodSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.BlockFamily;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

/**
 * A {@link WoodSet}'s planks family, like vanilla's {@code BlockFamilies.OAK_PLANKS}: the model and recipe providers
 * expand it into stairs, slab, fence, fence gate, door, trapdoor, pressure plate, button and sign. Logs, wood,
 * hanging sign, leaves, sapling and boats are handled one by one by the providers.
 */
public final class WoodFamily {
    public static BlockFamily of(WoodSet set) {
        return new BlockFamily.Builder(set.planks.get())
                .button(set.button.get())
                .fence(set.fence.get())
                .fenceGate(set.fenceGate.get())
                .pressurePlate(set.pressurePlate.get())
                .sign(set.sign.get(), set.wallSign.get())
                .slab(set.slab.get())
                .stairs(set.stairs.get())
                .door(set.door.get())
                .trapdoor(set.trapdoor.get())
                .recipeGroupPrefix("wooden")
                .recipeUnlockedBy("has_planks")
                .getFamily();
    }

    /** {@code comminutioncraft:<name>_logs}: log, wood and their stripped forms (like {@code minecraft:oak_logs}). */
    public static TagKey<Block> logsBlockTag(WoodSet set) {
        return TagKey.create(Registries.BLOCK, ComminutionCraft.id(set.name + "_logs"));
    }

    public static TagKey<Item> logsItemTag(WoodSet set) {
        return TagKey.create(Registries.ITEM, ComminutionCraft.id(set.name + "_logs"));
    }

    private WoodFamily() {}
}
