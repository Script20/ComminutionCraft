package com.script.comminutioncraft.client.multiblock;

import net.minecraft.client.gui.navigation.ScreenRectangle;
import net.minecraft.client.renderer.state.gui.pip.PictureInPictureRenderState;
import org.joml.Matrix4f;
import org.jspecify.annotations.Nullable;

/**
 * One frame of a {@link MultiblockView}: which blocks to draw and how they're turned. {@code view} works in the
 * view's own GUI units around the middle of the area; {@code scale} turns those into screen GUI units.
 */
public record MultiblockRenderState(MultiblockStructure structure, int layer, Matrix4f view,
                                    int x0, int y0, int x1, int y1, float scale,
                                    @Nullable ScreenRectangle scissorArea, @Nullable ScreenRectangle bounds)
        implements PictureInPictureRenderState {
    public MultiblockRenderState(MultiblockStructure structure, int layer, Matrix4f view, int x0, int y0, int x1, int y1,
                                 float scale, @Nullable ScreenRectangle scissorArea) {
        this(structure, layer, view, x0, y0, x1, y1, scale, scissorArea,
                PictureInPictureRenderState.getBounds(x0, y0, x1, y1, scissorArea));
    }
}
