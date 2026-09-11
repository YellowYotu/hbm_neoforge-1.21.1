package com.yellowyotu.hbmneoforge.client.model;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.world.entity.LivingEntity;

public final class GasMaskArmorModel extends HumanoidModel<LivingEntity> {
    public GasMaskArmorModel(ModelPart root) {
        super(root);
        setAllVisible(false);
        head.visible = true;
        hat.visible = false;
        applyOriginalScale();
    }

    public void applyOriginalScale() {
        head.xScale = 1.15F;
        head.yScale = 1.15F;
        head.zScale = 1.15F;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);

        head.addOrReplaceChild("mask_shell",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(-4.0F, -7.9625F, -4.0F, 8.0F, 8.0F, 3.0F),
                PartPose.ZERO);
        head.addOrReplaceChild("lens_left",
                CubeListBuilder.create().texOffs(22, 0).mirror()
                        .addBox(-3.0F, -4.9625F, -4.75F, 2.0F, 2.0F, 1.25F),
                PartPose.ZERO);
        head.addOrReplaceChild("lens_right",
                CubeListBuilder.create().texOffs(22, 0).mirror()
                        .addBox(1.0F, -4.9625F, -4.75F, 2.0F, 2.0F, 1.25F),
                PartPose.ZERO);
        head.addOrReplaceChild("nose",
                CubeListBuilder.create().texOffs(0, 11).mirror()
                        .addBox(0.0F, 0.0F, 0.0F, 2.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(-1.0F, -2.9625F, -4.0F, -0.7853982F, 0.0F, 0.0F));
        head.addOrReplaceChild("filter",
                CubeListBuilder.create().texOffs(0, 15).mirror()
                        .addBox(0.0F, 2.0F, -0.5F, 3.0F, 4.0F, 3.0F),
                PartPose.offsetAndRotation(-1.5F, -2.9625F, -4.0F, -0.7853982F, 0.0F, 0.0F));
        head.addOrReplaceChild("strap",
                CubeListBuilder.create().texOffs(0, 22).mirror()
                        .addBox(-4.0F, -4.9625F, -1.0F, 8.0F, 1.0F, 5.0F),
                PartPose.ZERO);

        return LayerDefinition.create(mesh, 64, 32);
    }
}
