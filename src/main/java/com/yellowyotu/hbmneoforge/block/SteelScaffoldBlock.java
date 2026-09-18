package com.yellowyotu.hbmneoforge.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public final class SteelScaffoldBlock extends Block {
    public enum Orientation implements StringRepresentable {
        HORIZONTAL_NS("horizontal_ns"), HORIZONTAL_EW("horizontal_ew"), VERTICAL_NS("vertical_ns"), VERTICAL_EW("vertical_ew");

        private final String name;

        Orientation(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public static final MapCodec<SteelScaffoldBlock> CODEC = simpleCodec(SteelScaffoldBlock::new);
    public static final EnumProperty<Orientation> ORIENTATION = EnumProperty.create("orientation", Orientation.class);
    private static final VoxelShape HORIZONTAL_NS_SHAPE = Block.box(0.0D, 0.0D, 2.0D, 16.0D, 16.0D, 14.0D);
    private static final VoxelShape HORIZONTAL_EW_SHAPE = Block.box(2.0D, 0.0D, 0.0D, 14.0D, 16.0D, 16.0D);
    private static final VoxelShape VERTICAL_SHAPE = Block.box(0.0D, 2.0D, 0.0D, 16.0D, 14.0D, 16.0D);

    public SteelScaffoldBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(ORIENTATION, Orientation.HORIZONTAL_NS));
    }

    @Override
    protected MapCodec<? extends Block> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ORIENTATION);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction clickedFace = context.getClickedFace();
        Orientation orientation;
        if (clickedFace.getAxis() == Direction.Axis.Y) {
            Direction playerDirection = context.getHorizontalDirection();
            orientation = playerDirection.getAxis() == Direction.Axis.X ? Orientation.HORIZONTAL_EW : Orientation.HORIZONTAL_NS;
        } else {
            orientation = clickedFace.getAxis() == Direction.Axis.Z ? Orientation.VERTICAL_NS : Orientation.VERTICAL_EW;
        }
        return defaultBlockState().setValue(ORIENTATION, orientation);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(ORIENTATION)) {
            case HORIZONTAL_NS -> HORIZONTAL_NS_SHAPE;
            case HORIZONTAL_EW -> HORIZONTAL_EW_SHAPE;
            case VERTICAL_NS, VERTICAL_EW -> VERTICAL_SHAPE;
        };
    }

    @Override
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return getShape(state, level, pos, context);
    }
}
