package com.yellowyotu.hbmneoforge.blockentity;

import com.yellowyotu.hbmneoforge.ModBlockEntities;
import com.yellowyotu.hbmneoforge.ModSounds;
import com.yellowyotu.hbmneoforge.radiation.ChunkRadiationManager;
import com.yellowyotu.hbmneoforge.radiation.GeigerSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class GeigerBlockEntity extends BlockEntity {

    private int timer;
    private float ticker;

    public GeigerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GEIGER.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, GeigerBlockEntity blockEntity) {
        if (level.isClientSide()) {
            return;
        }

        blockEntity.timer++;

        if (blockEntity.timer == 10) {
            blockEntity.timer = 0;
            blockEntity.ticker = ChunkRadiationManager.getRadiation(level, pos);
            level.updateNeighbourForOutputSignal(pos, state.getBlock());
        }

        if (blockEntity.timer % 5 == 0) {
            if (blockEntity.ticker > 0.0F) {
                SoundEvent sound = GeigerSounds.select(blockEntity.ticker, level.random);

                if (sound != null) {
                    level.playSound(null, pos, sound, SoundSource.BLOCKS, 1.0F, 1.0F);
                }
            } else if (level.random.nextInt(50) == 0) {
                level.playSound(null, pos, ModSounds.GEIGER_1.get(), SoundSource.BLOCKS, 1.0F, 1.0F);
            }
        }
    }

    public float check() {
        return ticker;
    }
}