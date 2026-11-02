package com.script.comminutioncraft.common.data;

import com.script.comminutioncraft.common.block.StoneSet;
import java.util.List;
import net.minecraft.data.BlockFamily;

/**
 * A {@link StoneSet} as vanilla {@link BlockFamily}s, which the model and recipe providers know how to expand
 * (stairs, slabs, walls, buttons, pressure plates, polished, chiseled, cracked).
 * <p>
 * Full blocks that are the base of their own family (cobbled, polished, bricks...) are deliberately not listed as
 * variants of another family, or vanilla's model generator would create their model twice.
 */
public record StoneFamilies(BlockFamily stone, BlockFamily cobbled, BlockFamily mossyCobbled, BlockFamily polished,
                            BlockFamily smooth, BlockFamily bricks, BlockFamily mossyBricks) {
    public static StoneFamilies of(StoneSet set) {
        return new StoneFamilies(
                new BlockFamily.Builder(set.stone.get())
                        .stairs(set.stairs.get()).slab(set.slab.get())
                        .polished(set.polished.get())
                        .getFamily(),
                new BlockFamily.Builder(set.cobbled.get())
                        .stairs(set.cobbledStairs.get()).slab(set.cobbledSlab.get()).wall(set.cobbledWall.get())
                        .getFamily(),
                new BlockFamily.Builder(set.mossyCobbled.get())
                        .stairs(set.mossyCobbledStairs.get()).slab(set.mossyCobbledSlab.get()).wall(set.mossyCobbledWall.get())
                        .getFamily(),
                new BlockFamily.Builder(set.polished.get())
                        .stairs(set.polishedStairs.get()).slab(set.polishedSlab.get())
                        .getFamily(),
                new BlockFamily.Builder(set.smooth.get())
                        .slab(set.smoothSlab.get())
                        .getFamily(),
                new BlockFamily.Builder(set.bricks.get())
                        .stairs(set.brickStairs.get()).slab(set.brickSlab.get()).wall(set.brickWall.get())
                        .chiseled(set.chiseled.get()).cracked(set.crackedBricks.get())
                        .getFamily(),
                new BlockFamily.Builder(set.mossyBricks.get())
                        .stairs(set.mossyBrickStairs.get()).slab(set.mossyBrickSlab.get()).wall(set.mossyBrickWall.get())
                        .getFamily());
    }

    public List<BlockFamily> all() {
        return List.of(stone, cobbled, mossyCobbled, polished, smooth, bricks, mossyBricks);
    }
}
