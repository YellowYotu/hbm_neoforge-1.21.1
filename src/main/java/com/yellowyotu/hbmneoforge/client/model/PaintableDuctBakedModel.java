package com.yellowyotu.hbmneoforge.client.model;

import com.yellowyotu.hbmneoforge.blockentity.FluidPipeBlockEntity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.Nullable;

public final class PaintableDuctBakedModel extends BakedModelWrapper<BakedModel> {
    private final BakedModel facadeOverlay;

    public PaintableDuctBakedModel(BakedModel originalModel, BakedModel facadeOverlay) {
        super(originalModel);
        this.facadeOverlay = facadeOverlay;
    }

    @Override
    public List<BakedQuad> getQuads(BlockState state, @Nullable Direction side, RandomSource rand, ModelData data, @Nullable RenderType renderType) {
        BlockState disguise = data.get(FluidPipeBlockEntity.DISGUISE_STATE);
        if (disguise == null) {
            return originalModel.getQuads(state, side, rand, data, renderType);
        }

        BakedModel disguiseModel = Minecraft.getInstance().getBlockRenderer().getBlockModel(disguise);
        List<BakedQuad> disguiseQuads = disguiseModel.getQuads(disguise, side, rand, ModelData.EMPTY, renderType);
        List<BakedQuad> overlayQuads = facadeOverlay.getQuads(state, side, rand, ModelData.EMPTY, renderType);
        if (overlayQuads.isEmpty()) {
            return disguiseQuads;
        }

        List<BakedQuad> result = new ArrayList<>(disguiseQuads.size() + overlayQuads.size());
        result.addAll(disguiseQuads);
        result.addAll(overlayQuads);
        return result;
    }
}
