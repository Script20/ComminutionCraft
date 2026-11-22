package com.script.comminutioncraft.client.render;

import java.util.Set;
import net.minecraft.client.model.BabyModelTransform;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.util.Mth;

/**
 * The Fennec Fox (owner-approved preview design/previews/fennec_fox.png): head 6x5x5 with a 3x2x2 snout and two big
 * 3x5x1 ears tilted out, body 6x5x9, legs 2x4x2, a 3x3x8 tail hanging back and down. Texture 64 x 32, box UV.
 */
public class FennecFoxModel extends EntityModel<FennecFoxRenderer.State> {
    /** Babies: a bigger head on a half-size body. */
    public static final BabyModelTransform BABY = new BabyModelTransform(true, 11.0F, 1.7F, Set.of("head"));
    private static final float TAIL_DROOP = -0.6F;

    private final ModelPart head;
    private final ModelPart body;
    private final ModelPart leftFrontLeg;
    private final ModelPart rightFrontLeg;
    private final ModelPart leftHindLeg;
    private final ModelPart rightHindLeg;
    private final ModelPart tail;

    public FennecFoxModel(ModelPart root) {
        super(root);
        head = root.getChild("head");
        body = root.getChild("body");
        leftFrontLeg = root.getChild("left_front_leg");
        rightFrontLeg = root.getChild("right_front_leg");
        leftHindLeg = root.getChild("left_hind_leg");
        rightHindLeg = root.getChild("right_hind_leg");
        tail = root.getChild("tail");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -3.0F, -5.0F, 6.0F, 5.0F, 5.0F), PartPose.offset(0.0F, 15.0F, -5.0F));
        head.addOrReplaceChild("snout",
                CubeListBuilder.create().texOffs(24, 0).addBox(-1.5F, 0.0F, -7.0F, 3.0F, 2.0F, 2.0F), PartPose.ZERO);
        head.addOrReplaceChild("left_ear", CubeListBuilder.create().texOffs(34, 0).addBox(-1.5F, -5.0F, -0.5F, 3.0F, 5.0F, 1.0F),
                PartPose.offsetAndRotation(-2.0F, -3.0F, -2.5F, 0.0F, 0.0F, -0.3F));
        head.addOrReplaceChild("right_ear", CubeListBuilder.create().texOffs(42, 0).addBox(-1.5F, -5.0F, -0.5F, 3.0F, 5.0F, 1.0F),
                PartPose.offsetAndRotation(2.0F, -3.0F, -2.5F, 0.0F, 0.0F, 0.3F));
        root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 10).addBox(-3.0F, -5.0F, -9.0F, 6.0F, 5.0F, 9.0F), PartPose.offset(0.0F, 20.0F, 4.0F));
        CubeListBuilder leg = CubeListBuilder.create().texOffs(30, 10).addBox(-1.0F, 0.0F, -1.0F, 2.0F, 4.0F, 2.0F);
        root.addOrReplaceChild("left_front_leg", leg, PartPose.offset(-2.0F, 20.0F, -3.0F));
        root.addOrReplaceChild("right_front_leg", leg, PartPose.offset(2.0F, 20.0F, -3.0F));
        root.addOrReplaceChild("left_hind_leg", leg, PartPose.offset(-2.0F, 20.0F, 3.0F));
        root.addOrReplaceChild("right_hind_leg", leg, PartPose.offset(2.0F, 20.0F, 3.0F));
        root.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(40, 16).addBox(-1.5F, 0.0F, 0.0F, 3.0F, 3.0F, 8.0F),
                PartPose.offsetAndRotation(0.0F, 16.0F, 4.0F, TAIL_DROOP, 0.0F, 0.0F));
        return LayerDefinition.create(mesh, 64, 32);
    }

    public static LayerDefinition createBabyLayer() {
        return createBodyLayer().apply(BABY);
    }

    @Override
    public void setupAnim(FennecFoxRenderer.State state) {
        super.setupAnim(state);
        head.xRot = state.xRot * Mth.DEG_TO_RAD;
        head.yRot = state.yRot * Mth.DEG_TO_RAD;
        float walk = state.walkAnimationPos * 0.6662F;
        float speed = state.walkAnimationSpeed;
        leftHindLeg.xRot = Mth.cos(walk) * 1.4F * speed;
        rightHindLeg.xRot = Mth.cos(walk + Mth.PI) * 1.4F * speed;
        leftFrontLeg.xRot = Mth.cos(walk + Mth.PI) * 1.4F * speed;
        rightFrontLeg.xRot = Mth.cos(walk) * 1.4F * speed;
        // the tail sways as it walks, and a little when it stands
        tail.yRot = Mth.cos(walk) * 0.4F * speed + Mth.sin(state.ageInTicks * 0.08F) * 0.06F;
        if (state.sitting) {
            sit();
        }
    }

    /** Sitting: the body tips up from the hips, the head rises with it, the hind legs fold forward and the tail lies flat. */
    private void sit() {
        body.xRot = -0.4F;
        head.y -= 3.5F;
        head.z += 0.6F;
        leftFrontLeg.y -= 1.5F;
        rightFrontLeg.y -= 1.5F;
        leftFrontLeg.xRot = -0.15F;
        rightFrontLeg.xRot = -0.15F;
        leftHindLeg.xRot = -1.45F;
        rightHindLeg.xRot = -1.45F;
        leftHindLeg.y += 2.5F;
        rightHindLeg.y += 2.5F;
        tail.y += 3.0F;
        tail.xRot = -0.05F;
        tail.yRot = 0.0F;
    }
}
