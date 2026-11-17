package com.script.comminutioncraft.client.multiblock;

import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.render.pip.PictureInPictureRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.block.BlockModelResolver;
import net.minecraft.client.renderer.block.model.BlockDisplayContext;
import net.minecraft.client.renderer.feature.FeatureRenderDispatcher;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.block.state.BlockState;

/** Draws a {@link MultiblockRenderState} with the blocks' real models, into its own texture at full GUI resolution. */
public class MultiblockRenderer extends PictureInPictureRenderer<MultiblockRenderState> {
    private final BlockDisplayContext displayContext = BlockDisplayContext.create();

    public MultiblockRenderer(MultiBufferSource.BufferSource bufferSource) {
        super(bufferSource);
    }

    @Override
    public Class<MultiblockRenderState> getRenderStateClass() {
        return MultiblockRenderState.class;
    }

    @Override
    protected void renderToTexture(MultiblockRenderState state, PoseStack pose) {
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.gameRenderer.getLighting().setupFor(Lighting.Entry.ITEMS_3D);
        FeatureRenderDispatcher dispatcher = minecraft.gameRenderer.getFeatureRenderDispatcher();
        BlockModelResolver resolver = new BlockModelResolver(minecraft.getModelManager());
        Map<BlockState, BlockModelRenderState> models = new HashMap<>();
        pose.mulPose(state.view());
        state.structure().blocks().forEach((pos, block) -> {
            if (state.layer() >= 0 && pos.getY() != state.layer()) {
                return;
            }
            BlockModelRenderState model = models.computeIfAbsent(block, b -> {
                BlockModelRenderState m = new BlockModelRenderState();
                resolver.update(m, b, displayContext);
                return m;
            });
            pose.pushPose();
            pose.translate(pos.getX(), pos.getY(), pos.getZ());
            model.submitMultiLayer(pose, dispatcher.getSubmitNodeStorage(), LightCoordsUtil.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
            pose.popPose();
        });
        dispatcher.renderAllFeatures();
    }

    @Override
    protected float getTranslateY(int height, int guiScale) {
        return height / 2.0F; // the view is built around the middle of the area
    }

    @Override
    protected String getTextureLabel() {
        return "comminutioncraft multiblock";
    }
}
