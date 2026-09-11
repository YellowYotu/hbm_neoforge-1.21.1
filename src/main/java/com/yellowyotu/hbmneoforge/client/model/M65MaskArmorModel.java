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

public final class M65MaskArmorModel extends HumanoidModel<LivingEntity> {
    private final ModelPart filter;

    public M65MaskArmorModel(ModelPart root) {
        super(root);
        this.filter = head.getChild("filter");
        setAllVisible(false);
        head.visible = true;
        hat.visible = false;
        applyOriginalScale();
    }

    public void applyOriginalScale() {
        float scale = 18.0F / 16.0F * 1.01F;
        head.xScale = scale;
        head.yScale = scale;
        head.zScale = scale;
    }

    public void setFilterVisible(boolean visible) {
        filter.visible = visible;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0F);
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.ZERO);

        head.addOrReplaceChild("mask",
                CubeListBuilder.create().texOffs(0, 0).mirror()
                        .addBox(-4.0F, -7.5F, -4.0F, 8.0F, 8.0F, 8.0F),
                PartPose.ZERO);
        head.addOrReplaceChild("nose",
                CubeListBuilder.create().texOffs(0, 16).mirror()
                        .addBox(-1.5F, -3.0F, -5.0F, 3.0F, 3.0F, 1.0F),
                PartPose.ZERO);
        head.addOrReplaceChild("outlet",
                CubeListBuilder.create().texOffs(0, 20).mirror()
                        .addBox(0.0F, -2.0F, 0.0F, 2.0F, 2.0F, 1.0F),
                PartPose.offsetAndRotation(-1.0F, -3.0F, -5.0F, -0.4799655F, 0.0F, 0.0F));
        head.addOrReplaceChild("nose_slope",
                CubeListBuilder.create().texOffs(8, 16).mirror()
                        .addBox(0.0F, 0.0F, -2.0F, 3.0F, 2.0F, 2.0F),
                PartPose.offsetAndRotation(-1.5F, -1.5F, -4.0F, 0.6108652F, 0.0F, 0.0F));
        head.addOrReplaceChild("eye_left",
                CubeListBuilder.create().texOffs(0, 23).mirror()
                        .addBox(-3.5F, -5.5F, -4.55F, 3.0F, 3.0F, 0.35F),
                PartPose.ZERO);
        head.addOrReplaceChild("eye_right",
                CubeListBuilder.create().texOffs(0, 26).mirror()
                        .addBox(0.5F, -5.5F, -4.55F, 3.0F, 3.0F, 0.35F),
                PartPose.ZERO);
        head.addOrReplaceChild("front",
                CubeListBuilder.create().texOffs(6, 20).mirror()
                        .addBox(-1.0F, -2.7F, -6.0F, 2.0F, 2.0F, 1.0F),
                PartPose.ZERO);

        PartDefinition filter = head.addOrReplaceChild("filter", CubeListBuilder.create(), PartPose.ZERO);
        filter.addOrReplaceChild("connector",
                CubeListBuilder.create().texOffs(6, 23).mirror()
                        .addBox(0.0F, 0.0F, -3.0F, 2.0F, 2.0F, 1.0F),
                PartPose.offsetAndRotation(-1.0F, -1.5F, -4.0F, 0.6108652F, 0.0F, 0.0F));
        filter.addOrReplaceChild("filter_a",
                CubeListBuilder.create().texOffs(18, 21).mirror()
                        .addBox(0.0F, -1.0F, -5.0F, 3.0F, 4.0F, 2.0F),
                PartPose.offsetAndRotation(-1.5F, -1.5F, -4.0F, 0.6108652F, 0.0F, 0.0F));
        filter.addOrReplaceChild("filter_b",
                CubeListBuilder.create().texOffs(18, 16).mirror()
                        .addBox(0.0F, -0.5F, -5.0F, 4.0F, 3.0F, 2.0F),
                PartPose.offsetAndRotation(-2.0F, -1.5F, -4.0F, 0.6108652F, 0.0F, 0.0F));

        return LayerDefinition.create(mesh, 32, 32);
    }
}
