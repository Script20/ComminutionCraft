package com.script.comminutioncraft.common.recipe;

import com.mojang.serialization.MapCodec;
import com.script.comminutioncraft.common.registry.ModItems;
import com.script.comminutioncraft.common.registry.ModRecipes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

/** A Coal Drill and a Drill Bit, anywhere in the grid: the same drill back with a fresh bit (its wear reset). */
public class DrillBitRecipe extends CustomRecipe {
    public static final DrillBitRecipe INSTANCE = new DrillBitRecipe();
    public static final MapCodec<DrillBitRecipe> MAP_CODEC = MapCodec.unit(INSTANCE);
    public static final StreamCodec<RegistryFriendlyByteBuf, DrillBitRecipe> STREAM_CODEC = StreamCodec.unit(INSTANCE);

    @Override
    public boolean matches(CraftingInput input, Level level) {
        return find(input, true) != null && find(input, false) != null && input.ingredientCount() == 2;
    }

    @Override
    public ItemStack assemble(CraftingInput input) {
        ItemStack drill = find(input, true);
        if (drill == null) {
            return ItemStack.EMPTY;
        }
        ItemStack result = drill.copyWithCount(1);
        result.setDamageValue(0);
        return result;
    }

    private static ItemStack find(CraftingInput input, boolean drill) {
        for (int i = 0; i < input.size(); i++) {
            ItemStack stack = input.getItem(i);
            if (drill ? stack.is(ModItems.COAL_DRILL.get()) : stack.is(ModItems.DRILL_BIT.get())) {
                return stack;
            }
        }
        return null;
    }

    @Override
    public RecipeSerializer<DrillBitRecipe> getSerializer() {
        return ModRecipes.DRILL_BIT_SERIALIZER.get();
    }
}
