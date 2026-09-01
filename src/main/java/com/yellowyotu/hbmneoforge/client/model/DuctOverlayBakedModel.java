package com.yellowyotu.hbmneoforge.client.model;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.jetbrains.annotations.Nullable;

public final class DuctOverlayBakedModel extends BakedModelWrapper<BakedModel> {

    private final TextureAtlasSprite overlaySprite;
    @SuppressWarnings("unchecked")
    private final List<BakedQuad>[] sideCache = new List[Direction.values().length];
    private List<BakedQuad> generalCache;

    public DuctOverlayBakedModel(BakedModel originalModel, TextureAtlasSprite overlaySprite) {
        super(originalModel);
        this.overlaySprite = overlaySprite;
    }

    @Override
    public List<BakedQuad> getQuads(BlockState state, @Nullable Direction side, RandomSource rand, ModelData data, @Nullable RenderType renderType) {
        if (side == null) {
            if (generalCache == null) {
                generalCache = buildOverlayQuads(originalModel.getQuads(state, null, rand, data, renderType));
            }
            return generalCache;
        }

        List<BakedQuad> cached = sideCache[side.ordinal()];
        if (cached == null) {
            cached = buildOverlayQuads(originalModel.getQuads(state, side, rand, data, renderType));
            sideCache[side.ordinal()] = cached;
        }
        return cached;
    }

    private List<BakedQuad> buildOverlayQuads(List<BakedQuad> baseQuads) {
        if (baseQuads.isEmpty()) {
            return baseQuads;
        }

        List<BakedQuad> quads = new ArrayList<>(baseQuads.size() * 2);
        for (BakedQuad baseQuad : baseQuads) {
            quads.add(baseQuad);
            quads.add(copyWithOverlay(baseQuad));
        }
        return List.copyOf(quads);
    }

    private BakedQuad copyWithOverlay(BakedQuad baseQuad) {
        int[] vertices = Arrays.copyOf(baseQuad.getVertices(), baseQuad.getVertices().length);
        TextureAtlasSprite baseSprite = baseQuad.getSprite();
        int stride = vertices.length / 4;

        for (int vertex = 0; vertex < 4; vertex++) {
            int offset = vertex * stride;
            int uIndex = offset + 4;
            int vIndex = offset + 5;

            float u = Float.intBitsToFloat(vertices[uIndex]);
            float v = Float.intBitsToFloat(vertices[vIndex]);
            float normalizedU = (u - baseSprite.getU0()) / (baseSprite.getU1() - baseSprite.getU0());
            float normalizedV = (v - baseSprite.getV0()) / (baseSprite.getV1() - baseSprite.getV0());

            vertices[uIndex] = Float.floatToRawIntBits(overlaySprite.getU0() + normalizedU * (overlaySprite.getU1() - overlaySprite.getU0()));
            vertices[vIndex] = Float.floatToRawIntBits(overlaySprite.getV0() + normalizedV * (overlaySprite.getV1() - overlaySprite.getV0()));
        }

        return new BakedQuad(vertices, 1, baseQuad.getDirection(), overlaySprite, baseQuad.isShade(), baseQuad.hasAmbientOcclusion());
    }
}
