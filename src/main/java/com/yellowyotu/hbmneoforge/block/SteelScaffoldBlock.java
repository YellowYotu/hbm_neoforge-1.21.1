package com.yellowyotu.hbmneoforge.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.Direction;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

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
        Direction direction = context.getHorizontalDirection();
        Orientation orientation = direction.getAxis() == Direction.Axis.X ? Orientation.HORIZONTAL_EW : Orientation.HORIZONTAL_NS;
        return defaultBlockState().setValue(ORIENTATION, orientation);
    }
}
