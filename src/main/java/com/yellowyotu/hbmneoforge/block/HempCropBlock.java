package com.yellowyotu.hbmneoforge.block;

import com.mojang.serialization.MapCodec;
import com.yellowyotu.hbmneoforge.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.FluidState;

public final class HempCropBlock extends BushBlock implements BonemealableBlock {
    public enum Stage implements StringRepresentable {
        SINGLE("single"),
        LOWER("lower"),
        UPPER("upper");

        private final String name;

        Stage(String name) {
            this.name = name;
        }

        @Override
        public String getSerializedName() {
            return name;
        }
    }

    public static final EnumProperty<Stage> STAGE = EnumProperty.create("stage", Stage.class);
    public static final MapCodec<HempCropBlock> CODEC = simpleCodec(HempCropBlock::new);

    public HempCropBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(STAGE, Stage.SINGLE));
    }

    @Override
    public MapCodec<? extends HempCropBlock> codec() {
        return CODEC;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STAGE);
    }

    @Override
    protected boolean mayPlaceOn(BlockState state, BlockGetter level, BlockPos pos) {
        return state.is(Blocks.GRASS_BLOCK)
                || state.is(Blocks.DIRT)
                || state.is(Blocks.COARSE_DIRT)
                || state.is(Blocks.PODZOL)
                || state.is(Blocks.FARMLAND);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        BlockPos pos = context.getClickedPos();
        BlockState below = context.getLevel().getBlockState(pos.below());
        if (below.is(this) && below.getValue(STAGE) == Stage.SINGLE) {
            return defaultBlockState().setValue(STAGE, Stage.UPPER);
        }
        return defaultBlockState();
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, net.minecraft.world.entity.LivingEntity placer, ItemStack stack) {
        if (!level.isClientSide() && state.getValue(STAGE) == Stage.UPPER) {
            BlockPos belowPos = pos.below();
            BlockState below = level.getBlockState(belowPos);
            if (below.is(this) && below.getValue(STAGE) == Stage.SINGLE) {
                level.setBlock(belowPos, below.setValue(STAGE, Stage.LOWER), 3);
            }
        }
    }

    @Override
    protected boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        Stage stage = state.getValue(STAGE);
        if (stage == Stage.UPPER) {
            BlockState below = level.getBlockState(pos.below());
            return below.is(this) && below.getValue(STAGE) == Stage.LOWER;
        }
        return super.canSurvive(state, level, pos);
    }

    @Override
    protected BlockState updateShape(BlockState state, net.minecraft.core.Direction direction, BlockState neighborState,
                                     net.minecraft.world.level.LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        Stage stage = state.getValue(STAGE);
        if (stage == Stage.UPPER && direction == net.minecraft.core.Direction.DOWN) {
            if (!neighborState.is(this) || neighborState.getValue(STAGE) != Stage.LOWER) {
                return Blocks.AIR.defaultBlockState();
            }
        }
        if (stage == Stage.LOWER && direction == net.minecraft.core.Direction.UP) {
            if (!neighborState.is(this) || neighborState.getValue(STAGE) != Stage.UPPER) {
                return state.setValue(STAGE, Stage.SINGLE);
            }
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    protected void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (state.getValue(STAGE) == Stage.SINGLE
                && level.isEmptyBlock(pos.above())
                && random.nextInt(3) == 0
                && random.nextFloat() < 0.33F) {
            growTall(level, pos, state);
        }
    }

    @Override
    public boolean isValidBonemealTarget(LevelReader level, BlockPos pos, BlockState state) {
        return state.getValue(STAGE) == Stage.SINGLE && level.isEmptyBlock(pos.above());
    }

    @Override
    public boolean isBonemealSuccess(Level level, RandomSource random, BlockPos pos, BlockState state) {
        return random.nextFloat() < 0.33F;
    }

    @Override
    public void performBonemeal(ServerLevel level, RandomSource random, BlockPos pos, BlockState state) {
        growTall(level, pos, state);
    }

    private void growTall(ServerLevel level, BlockPos pos, BlockState state) {
        if (!level.isEmptyBlock(pos.above())) {
            return;
        }
        level.setBlock(pos, state.setValue(STAGE, Stage.LOWER), 2);
        level.setBlock(pos.above(), defaultBlockState().setValue(STAGE, Stage.UPPER), 2);
    }
}
