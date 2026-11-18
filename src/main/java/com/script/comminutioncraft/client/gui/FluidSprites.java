package com.script.comminutioncraft.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.client.fluid.FluidTintSource;

/** A fluid's own still texture and tint (water takes the biome's colour), for drawing it in GUIs. */
public final class FluidSprites {
    private static final int TILE = 16;

    private FluidSprites() {
    }

    public static TextureAtlasSprite sprite(Fluid fluid) {
        FluidModel model = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(fluid.defaultFluidState());
        return model.stillMaterial().sprite();
    }

    public static int tint(Fluid fluid) {
        Minecraft minecraft = Minecraft.getInstance();
        FluidState state = fluid.defaultFluidState();
        FluidTintSource tint = minecraft.getModelManager().getFluidStateModelSet().get(state).fluidTintSource();
        if (tint == null || minecraft.level == null || minecraft.player == null) {
            return 0xFFFFFFFF;
        }
        return 0xFF000000 | tint.colorInWorld(state, minecraft.level.getBlockState(minecraft.player.blockPosition()),
                minecraft.level, minecraft.player.blockPosition());
    }

    /** Fills the rectangle with the fluid's texture, tiled up from the bottom edge (so it stays put as the level changes). */
    public static void fill(GuiGraphicsExtractor graphics, Fluid fluid, int x, int y, int width, int height) {
        if (width <= 0 || height <= 0) {
            return;
        }
        TextureAtlasSprite sprite = sprite(fluid);
        int color = tint(fluid);
        int bottom = y + height;
        graphics.enableScissor(x, y, x + width, bottom);
        for (int tileY = bottom - TILE; tileY > y - TILE; tileY -= TILE) {
            for (int tileX = x; tileX < x + width; tileX += TILE) {
                graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, tileX, tileY, TILE, TILE, color);
            }
        }
        graphics.disableScissor();
    }
}
