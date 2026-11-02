package com.script.comminutioncraft.common.block;

import java.util.List;
import java.util.function.Function;
import java.util.function.UnaryOperator;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ButtonBlock;
import net.minecraft.world.level.block.PressurePlateBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.BlockSetType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Every block of one decorative stone, mirroring vanilla stone: the natural block (drops its cobbled form),
 * cobbled + mossy cobbled, polished, smooth, bricks + cracked/mossy/chiseled, a pillar, and the stairs, slabs,
 * walls, button and pressure plate that go with them. Ids follow vanilla naming, e.g. {@code marble_brick_stairs}.
 * <p>
 * Datagen builds models, loot, tags, lang and recipes for all of it from {@link #all()}
 * (see {@code data/StoneFamilies}), so adding a stone is one line in {@code ModBlocks} plus its textures.
 */
public final class StoneSet {
    public final String name;

    public final DeferredBlock<Block> stone;
    public final DeferredBlock<Block> cobbled;
    public final DeferredBlock<Block> mossyCobbled;
    public final DeferredBlock<Block> polished;
    public final DeferredBlock<Block> smooth;
    public final DeferredBlock<Block> bricks;
    public final DeferredBlock<Block> crackedBricks;
    public final DeferredBlock<Block> mossyBricks;
    public final DeferredBlock<Block> chiseled;
    public final DeferredBlock<RotatedPillarBlock> pillar;

    public final DeferredBlock<StairBlock> stairs;
    public final DeferredBlock<SlabBlock> slab;
    public final DeferredBlock<ButtonBlock> button;

    public final DeferredBlock<StairBlock> cobbledStairs;
    public final DeferredBlock<SlabBlock> cobbledSlab;
    public final DeferredBlock<WallBlock> cobbledWall;

    public final DeferredBlock<StairBlock> mossyCobbledStairs;
    public final DeferredBlock<SlabBlock> mossyCobbledSlab;
    public final DeferredBlock<WallBlock> mossyCobbledWall;

    public final DeferredBlock<StairBlock> polishedStairs;
    public final DeferredBlock<SlabBlock> polishedSlab;

    public final DeferredBlock<SlabBlock> smoothSlab;

    public final DeferredBlock<StairBlock> brickStairs;
    public final DeferredBlock<SlabBlock> brickSlab;
    public final DeferredBlock<WallBlock> brickWall;

    public final DeferredBlock<StairBlock> mossyBrickStairs;
    public final DeferredBlock<SlabBlock> mossyBrickSlab;
    public final DeferredBlock<WallBlock> mossyBrickWall;

    private final DeferredRegister.Blocks blocks;
    private final UnaryOperator<BlockBehaviour.Properties> base;

    public StoneSet(DeferredRegister.Blocks blocks, String name, MapColor color, float hardness, float resistance, SoundType sound) {
        this.blocks = blocks;
        this.name = name;
        this.base = properties -> properties
                .mapColor(color)
                .instrument(NoteBlockInstrument.BASEDRUM)
                .requiresCorrectToolForDrops()
                .strength(hardness, resistance)
                .sound(sound);

        stone = full(name);
        cobbled = full("cobbled_" + name);
        mossyCobbled = full("mossy_cobbled_" + name);
        polished = full("polished_" + name);
        smooth = full("smooth_" + name);
        bricks = full(name + "_bricks");
        crackedBricks = full("cracked_" + name + "_bricks");
        mossyBricks = full("mossy_" + name + "_bricks");
        chiseled = full("chiseled_" + name);
        pillar = blocks.registerBlock(name + "_pillar", RotatedPillarBlock::new, base);

        stairs = stairs(name + "_stairs", stone);
        slab = slab(name + "_slab");
        button = blocks.registerBlock(name + "_button", p -> new ButtonBlock(BlockSetType.STONE, 20, p),
                p -> p.noCollision().strength(0.5F).pushReaction(PushReaction.DESTROY));

        cobbledStairs = stairs("cobbled_" + name + "_stairs", cobbled);
        cobbledSlab = slab("cobbled_" + name + "_slab");
        cobbledWall = wall("cobbled_" + name + "_wall");

        mossyCobbledStairs = stairs("mossy_cobbled_" + name + "_stairs", mossyCobbled);
        mossyCobbledSlab = slab("mossy_cobbled_" + name + "_slab");
        mossyCobbledWall = wall("mossy_cobbled_" + name + "_wall");

        polishedStairs = stairs("polished_" + name + "_stairs", polished);
        polishedSlab = slab("polished_" + name + "_slab");

        smoothSlab = slab("smooth_" + name + "_slab");

        brickStairs = stairs(name + "_brick_stairs", bricks);
        brickSlab = slab(name + "_brick_slab");
        brickWall = wall(name + "_brick_wall");

        mossyBrickStairs = stairs("mossy_" + name + "_brick_stairs", mossyBricks);
        mossyBrickSlab = slab("mossy_" + name + "_brick_slab");
        mossyBrickWall = wall("mossy_" + name + "_brick_wall");
    }

    private DeferredBlock<Block> full(String id) {
        return blocks.registerBlock(id, Block::new, base);
    }

    private DeferredBlock<StairBlock> stairs(String id, DeferredBlock<Block> material) {
        Function<BlockBehaviour.Properties, StairBlock> factory = p -> new StairBlock(material.get().defaultBlockState(), p);
        return blocks.registerBlock(id, factory, base);
    }

    private DeferredBlock<SlabBlock> slab(String id) {
        return blocks.registerBlock(id, SlabBlock::new, base);
    }

    private DeferredBlock<WallBlock> wall(String id) {
        return blocks.registerBlock(id, WallBlock::new, p -> base.apply(p).forceSolidOn());
    }

    /** Every block in the set, in creative-tab order. */
    public List<DeferredBlock<? extends Block>> all() {
        return List.of(
                stone, stairs, slab, button,
                cobbled, cobbledStairs, cobbledSlab, cobbledWall,
                mossyCobbled, mossyCobbledStairs, mossyCobbledSlab, mossyCobbledWall,
                polished, polishedStairs, polishedSlab,
                smooth, smoothSlab,
                bricks, brickStairs, brickSlab, brickWall,
                crackedBricks, chiseled,
                mossyBricks, mossyBrickStairs, mossyBrickSlab, mossyBrickWall,
                pillar);
    }

    public List<DeferredBlock<StairBlock>> allStairs() {
        return List.of(stairs, cobbledStairs, mossyCobbledStairs, polishedStairs, brickStairs, mossyBrickStairs);
    }

    public List<DeferredBlock<SlabBlock>> allSlabs() {
        return List.of(slab, cobbledSlab, mossyCobbledSlab, polishedSlab, smoothSlab, brickSlab, mossyBrickSlab);
    }

    public List<DeferredBlock<WallBlock>> allWalls() {
        return List.of(cobbledWall, mossyCobbledWall, brickWall, mossyBrickWall);
    }
}
