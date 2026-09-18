package com.yellowyotu.hbmneoforge.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class SteelGrateBlock extends Block {
    public static final MapCodec<SteelGrateBlock> CODEC = simpleCodec(SteelGrateBlock::new);
    public static final IntegerProperty HEIGHT = IntegerProperty.create("height", 0, 9);

    public SteelGrateBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(HEIGHT, 0));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HEIGHT);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction face = context.getClickedFace();
        if (face == Direction.DOWN) {
            return defaultBlockState().setValue(HEIGHT, 7);
        }
        if (face == Direction.UP) {
            return defaultBlockState().setValue(HEIGHT, 0);
        }
        double hitY = context.getClickLocation().y - context.getClickedPos().getY();
        int height = Math.max(0, Math.min(7, (int) Math.floor(hitY * 8.0D)));
        return defaultBlockState().setValue(HEIGHT, height);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (!placer.isShiftKeyDown()) {
            return;
        }
        int height = state.getValue(HEIGHT);
        if (height == 0 && !level.getBlockState(pos.below()).isAir()) {
            level.setBlock(pos, state.setValue(HEIGHT, 9), 3);
        } else if (height == 7 && !level.getBlockState(pos.above()).isAir()) {
            level.setBlock(pos, state.setValue(HEIGHT, 8), 3);
        }
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeFor(state.getValue(HEIGHT));
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return shapeFor(state.getValue(HEIGHT));
    }

    private static VoxelShape shapeFor(int height) {
        double y = height == 9 ? -2.0D : height * 2.0D;
        return box(0.0D, y, 0.0D, 16.0D, y + 2.0D, 16.0D);
    }
}
