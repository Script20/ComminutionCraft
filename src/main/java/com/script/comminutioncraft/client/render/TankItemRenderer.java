package com.script.comminutioncraft.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.script.comminutioncraft.ComminutionCraft;
import com.script.comminutioncraft.common.machine.ContainmentCellBlockEntity;
import com.script.comminutioncraft.common.machine.FluidTankBlockEntity;
import com.script.comminutioncraft.common.registry.ModDataComponents;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.FluidModel;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.sprite.SpriteGetter;
import net.minecraft.client.resources.model.sprite.SpriteId;
import net.minecraft.resources.Identifier;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.client.fluid.FluidTintSource;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.SimpleFluidContent;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

/**
 * The inside of a Fluid Tank / Containment Cell as an item (owner, 2026-11-14): the item model is the block model plus
 * this, which draws the same inner walls and fluid box as the placed tank ({@link FluidTankRenderer}), filled to what the
 * item's {@code stored_fluid} component holds (a wrench pickup keeps the fluid). Without it you'd look straight through
 * the window of an item tank.
 */
public class TankItemRenderer implements SpecialModelRenderer<TankItemRenderer.Contents> {
    public static final Identifier TYPE = ComminutionCraft.id("tank_contents");

    /** What's in the item: the fluid's still sprite and tint, how full, whether it glows. */
    public record Contents(TextureAtlasSprite sprite, int color, float fill, boolean glows) {}

    private final SpriteGetter sprites;
    private final SpriteId side;
    private final SpriteId end;
    private final boolean cell;

    private TankItemRenderer(SpriteGetter sprites, Identifier block) {
        this.sprites = sprites;
        this.side = new SpriteId(TextureAtlas.LOCATION_BLOCKS, block.withPath(p -> "block/" + p + "_side"));
        this.end = new SpriteId(TextureAtlas.LOCATION_BLOCKS, block.withPath(p -> "block/" + p + "_top"));
        this.cell = block.getPath().equals("containment_cell");
    }

    @Override
    public @Nullable Contents extractArgument(ItemStack stack) {
        SimpleFluidContent content = stack.get(ModDataComponents.STORED_FLUID.get());
        FluidStack fluid = content == null ? FluidStack.EMPTY : content.copy();
        if (fluid.isEmpty()) {
            return null;
        }
        FluidState state = fluid.getFluid().defaultFluidState();
        FluidModel model = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(state);
        FluidTintSource tint = model.fluidTintSource();
        int color = tint == null ? 0xFFFFFFFF : 0xFF000000 | tint.colorAsStack(fluid);
        int capacity = cell ? ContainmentCellBlockEntity.CAPACITY.get() : FluidTankBlockEntity.CAPACITY.get();
        return new Contents(model.stillMaterial().sprite(), color, Math.min(1.0F, (float) fluid.getAmount() / capacity),
                fluid.getFluidType().getLightLevel(fluid) > 0);
    }

    @Override
    public void submit(@Nullable Contents contents, PoseStack poseStack, SubmitNodeCollector collector, int light, int overlay,
                       boolean hasFoil, int outlineColor) {
        TextureAtlasSprite sideSprite = sprites.get(side);
        TextureAtlasSprite endSprite = sprites.get(end);
        collector.submitCustomGeometry(poseStack, Sheets.translucentBlockItemSheet(), (pose, buffer) -> {
            FluidTankRenderer.box(new FluidTankRenderer.Face(buffer, pose, endSprite, 0xFFFFFFFF, light),
                    new FluidTankRenderer.Face(buffer, pose, sideSprite, 0xFFFFFFFF, light),
                    FluidTankRenderer.WALL, FluidTankRenderer.WALL, 1 - FluidTankRenderer.WALL, 1 - FluidTankRenderer.WALL, 0, true);
            if (contents != null && contents.fill() > 0) {
                float top = FluidTankRenderer.WINDOW_BOTTOM + FluidTankRenderer.WINDOW_HEIGHT * contents.fill();
                var face = new FluidTankRenderer.Face(buffer, pose, contents.sprite(), contents.color(),
                        contents.glows() ? LightCoordsUtil.FULL_BRIGHT : light);
                float f = FluidTankRenderer.FLUID;
                FluidTankRenderer.box(face, face, f, f, 1 - f, top, 1 - (top - f), false);
            }
        });
    }

    @Override
    public void getExtents(Consumer<Vector3fc> output) {
        for (int i = 0; i < 8; i++) {
            output.accept(new Vector3f(i & 1, (i >> 1) & 1, (i >> 2) & 1));
        }
    }

    /** {@code "type": "comminutioncraft:tank_contents", "block": "<tank block id>"} (walls use that block's textures). */
    public record Unbaked(Identifier block) implements SpecialModelRenderer.Unbaked<Contents> {
        public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                Identifier.CODEC.fieldOf("block").forGetter(Unbaked::block)
        ).apply(i, Unbaked::new));

        @Override
        public MapCodec<Unbaked> type() {
            return MAP_CODEC;
        }

        @Override
        public TankItemRenderer bake(SpecialModelRenderer.BakingContext context) {
            return new TankItemRenderer(context.sprites(), block);
        }
    }
}
