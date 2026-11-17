package com.script.comminutioncraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.cable.CableConnection;
import com.script.comminutioncraft.common.cable.ConduitBlock;
import com.script.comminutioncraft.common.pipe.ItemPipeBlockEntity;
import com.script.comminutioncraft.common.pipe.TravellingItem;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Item Pipe: the stacks travelling through it (small spinning items, entry face -> middle -> exit face) and the orange
 * plate on its Extract sides (drawn just over the blue Insert plate of the block model).
 */
public class ItemPipeRenderer implements BlockEntityRenderer<ItemPipeBlockEntity, ItemPipeRenderer.State> {
    private static final SpriteId EXTRACT = new SpriteId(TextureAtlas.LOCATION_BLOCKS, ComminutionCraft.id("block/item_pipe_extract"));
    /** Small enough to ride inside the 6x6 tube. */
    private static final float ITEM_SCALE = 0.3F;
    private static final float E = 0.004F; // over the model's plate

    private final ItemModelResolver itemModels;
    private final SpriteGetter sprites;

    public ItemPipeRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModels = context.itemModelResolver();
        this.sprites = context.sprites();
    }

    public static class State extends BlockEntityRenderState {
        final List<Moving> items = new ArrayList<>();
        final List<Direction> extract = new ArrayList<>();
        float time;
    }

    record Moving(ItemStackRenderState item, Vec3 at, int seed) {}

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(ItemPipeBlockEntity pipe, State state, float partialTicks, Vec3 cameraPosition,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(pipe, state, partialTicks, cameraPosition, breakProgress);
        state.items.clear();
        state.extract.clear();
        float step = 1.0F / Math.max(1, ItemPipeBlockEntity.TICKS_PER_BLOCK.get());
        int seed = 0;
        for (TravellingItem moving : pipe.items()) {
            float p = Math.min(1, moving.progress() + partialTicks * step);
            Direction side = p < 0.5F ? moving.from() : moving.to();
            float out = Math.abs(p - 0.5F);
            Vec3 at = new Vec3(0.5 + side.getStepX() * out, 0.5 + side.getStepY() * out, 0.5 + side.getStepZ() * out);
            ItemStackRenderState item = new ItemStackRenderState();
            itemModels.updateForTopItem(item, moving.stack(), ItemDisplayContext.GROUND, pipe.getLevel(), null, seed);
            state.items.add(new Moving(item, at, seed++));
        }
        BlockState block = pipe.getBlockState();
        for (Direction direction : Direction.values()) {
            if (pipe.side(direction).isExtract()
                    && block.getValue(ConduitBlock.CONNECTIONS.get(direction)) == CableConnection.MACHINE) {
                state.extract.add(direction);
            }
        }
        state.time = pipe.getLevel() == null ? 0 : (pipe.getLevel().getGameTime() % 3600) + partialTicks;
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        for (Moving moving : state.items) {
            pose.pushPose();
            pose.translate(moving.at().x, moving.at().y - 0.07, moving.at().z);
            pose.mulPose(Axis.YP.rotationDegrees(state.time * 4 + moving.seed() * 47));
            pose.scale(ITEM_SCALE, ITEM_SCALE, ITEM_SCALE);
            moving.item().submit(pose, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            pose.popPose();
        }
        if (state.extract.isEmpty()) {
            return;
        }
        TextureAtlasSprite sprite = sprites.get(EXTRACT);
        int light = state.lightCoords;
        for (Direction direction : state.extract) {
            pose.pushPose();
            pose.translate(0.5F, 0.5F, 0.5F);
            pose.mulPose(direction.getRotation()); // the plate is built on the top face
            pose.translate(-0.5F, -0.5F, -0.5F);
            collector.submitCustomGeometry(pose, Sheets.cutoutBlockSheet(), (p, buffer) ->
                    plate(new FluidTankRenderer.Face(buffer, p, sprite, 0xFFFFFFFF, light)));
            pose.popPose();
        }
    }

    /** The 10x10x1 plate on the top face (texture: the plate at (0,0), 10 texels square), a hair bigger than the model's. */
    private static void plate(FluidTankRenderer.Face face) {
        float lo = 3 / 16F - E;
        float hi = 13 / 16F + E;
        float bottom = 15 / 16F - E;
        float top = 1 + E;
        float uv = 10 / 16F;
        float rim = 1 / 16F;
        face.quad(false, 0, 1, 0, lo, top, lo, 0, 0, lo, top, hi, 0, uv, hi, top, hi, uv, uv, hi, top, lo, uv, 0);
        face.quad(false, 0, -1, 0, lo, bottom, lo, 0, 0, hi, bottom, lo, uv, 0, hi, bottom, hi, uv, uv, lo, bottom, hi, 0, uv);
        face.quad(false, 0, 0, -1, hi, top, lo, 0, 0, hi, bottom, lo, 0, rim, lo, bottom, lo, uv, rim, lo, top, lo, uv, 0);
        face.quad(false, 0, 0, 1, lo, top, hi, 0, 0, lo, bottom, hi, 0, rim, hi, bottom, hi, uv, rim, hi, top, hi, uv, 0);
        face.quad(false, -1, 0, 0, lo, top, lo, 0, 0, lo, bottom, lo, 0, rim, lo, bottom, hi, uv, rim, lo, top, hi, uv, 0);
        face.quad(false, 1, 0, 0, hi, top, hi, 0, 0, hi, bottom, hi, 0, rim, hi, bottom, lo, uv, rim, hi, top, lo, uv, 0);
    }
}
