package com.script.comminutioncraft.common.block;

import com.script.comminutioncraft.common.registry.ModBlocks;
import java.lang.reflect.Method;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import net.neoforged.neoforge.event.BlockEntityTypeAddBlocksEvent;

/**
 * What a {@link WoodSet} needs besides its registrations: the signs join vanilla's sign block entity types (so they
 * keep their text and use vanilla's sign renderers), the wood burns like vanilla wood, and the sapling fits in a
 * flower pot. Stripping with an axe is the {@code neoforge:strippables} data map (datagen, ModDataMapProvider).
 */
public final class ModWoods {
    public static void register(IEventBus modEventBus) {
        modEventBus.addListener(ModWoods::commonSetup);
        modEventBus.addListener(ModWoods::addSignBlocks);
    }

    private static void addSignBlocks(BlockEntityTypeAddBlocksEvent event) {
        WoodSet wood = ModBlocks.PURPLEHEART;
        event.modify(BlockEntityType.SIGN, wood.sign.get(), wood.wallSign.get());
        event.modify(BlockEntityType.HANGING_SIGN, wood.hangingSign.get(), wood.wallHangingSign.get());
    }

    private static void commonSetup(FMLCommonSetupEvent event) {
        event.enqueueWork(() -> {
            WoodSet wood = ModBlocks.PURPLEHEART;
            // vanilla's odds (FireBlock.bootStrap): planks and their shapes 5/20, logs 5/5, leaves 30/60
            for (var block : java.util.List.of(wood.planks, wood.stairs, wood.slab, wood.fence, wood.fenceGate)) {
                setFlammable(block.get(), 5, 20);
            }
            wood.logs().forEach(block -> setFlammable(block.get(), 5, 5));
            setFlammable(wood.leaves.get(), 30, 60);
            ((FlowerPotBlock) Blocks.FLOWER_POT).addPlant(wood.sapling.getId(), wood.pottedSapling);
        });
    }

    /** FireBlock.setFlammable is private; reflection instead of an access transformer (which re-patches Minecraft). */
    private static void setFlammable(Block block, int igniteOdds, int burnOdds) {
        try {
            SET_FLAMMABLE.invoke(Blocks.FIRE, block, igniteOdds, burnOdds);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException("Can't make " + block + " flammable", e);
        }
    }

    private static final Method SET_FLAMMABLE =
            ObfuscationReflectionHelper.findMethod(FireBlock.class, "setFlammable", Block.class, int.class, int.class);

    private ModWoods() {}
}
