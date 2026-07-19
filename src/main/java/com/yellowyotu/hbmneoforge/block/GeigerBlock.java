package com.yellowyotu.hbmneoforge.block;

import com.yellowyotu.hbmneoforge.ModSounds;
import com.yellowyotu.hbmneoforge.radiation.GeigerSounds;
import com.yellowyotu.hbmneoforge.radiation.ChunkRadiationManager;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

public final class GeigerBlock extends Block {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

    public GeigerBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any().setValue(FACING, net.minecraft.core.Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected void onPlace(
            BlockState state,
            Level level,
            BlockPos pos,
            BlockState oldState,
            boolean movedByPiston
    ) {
        super.onPlace(state, level, pos, oldState, movedByPiston);

        if (!level.isClientSide()) {
            level.scheduleTick(pos, this, 5);
        }
    }

    @Override
    protected void tick(
            BlockState state,
            ServerLevel level,
            BlockPos pos,
            RandomSource random
    ) {
        float radiation =
                ChunkRadiationManager.getRadiation(level, pos);

        SoundEvent sound =
                GeigerSounds.select(radiation, random);

        if (sound != null) {
            level.playSound(
                    null,
                    pos,
                    sound,
                    SoundSource.BLOCKS,
                    1.0F,
                    1.0F
            );
        } else if (radiation <= 0.0F
                && random.nextInt(50) == 0) {
            level.playSound(
                    null,
                    pos,
                    ModSounds.GEIGER_1.get(),
                    SoundSource.BLOCKS,
                    1.0F,
                    1.0F
            );
        }

        level.scheduleTick(pos, this, 5);
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hitResult
    ) {
        if (!level.isClientSide()) {
            float radiation =
                    ChunkRadiationManager.getRadiation(level, pos);

            player.sendSystemMessage(
                    Component.literal(
                                    String.format(
                                            java.util.Locale.ROOT,
                                            "Geiger Counter: %.1f RAD/s",
                                            radiation
                                    )
                            )
                            .withStyle(ChatFormatting.YELLOW)
            );
        }

        return InteractionResult.sidedSuccess(
                level.isClientSide()
        );
    }
}
