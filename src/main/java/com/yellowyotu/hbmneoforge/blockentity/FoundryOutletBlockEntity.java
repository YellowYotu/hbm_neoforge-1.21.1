package com.yellowyotu.hbmneoforge.blockentity;

import com.yellowyotu.hbmneoforge.ModBlockEntities;
import com.yellowyotu.hbmneoforge.ModBlocks;
import com.yellowyotu.hbmneoforge.block.FoundryOutletBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public final class FoundryOutletBlockEntity extends FoundryBaseBlockEntity {
    private String filter = "";
    private boolean invertFilter;
    private boolean invertRedstone;
    private String visualMaterial = "";
    private int visualTicks;

    public FoundryOutletBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FOUNDRY_OUTLET.get(), pos, state);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FoundryOutletBlockEntity outlet) {
        if (outlet.visualTicks > 0) {
            outlet.visualTicks--;
            if (outlet.visualTicks == 0) {
                outlet.visualMaterial = "";
                outlet.syncIfChanged();
            }
        }
    }

    private boolean isClosed() {
        return level != null && (invertRedstone ^ level.hasNeighborSignal(worldPosition));
    }

    private boolean passesFilter(String incomingMaterial) {
        if (filter.isBlank()) {
            return true;
        }
        boolean matches = filter.equals(incomingMaterial);
        return invertFilter ? !matches : matches;
    }

    @Override
    public boolean canReceiveFrom(Direction side, String incomingMaterial) {
        if (level == null || isClosed() || !passesFilter(incomingMaterial)) {
            return false;
        }
        Direction facing = getBlockState().getValue(FoundryOutletBlock.FACING);
        if (side != facing.getOpposite()) {
            return false;
        }
        return findPourTarget(incomingMaterial) != null;
    }

    @Override
    public int insertMolten(String incomingMaterial, int incomingAmount) {
        if (incomingAmount <= 0 || !passesFilter(incomingMaterial) || isClosed()) {
            return 0;
        }
        PourTarget target = findPourTarget(incomingMaterial);
        if (target == null) {
            return 0;
        }
        int accepted;
        if (target.entity instanceof FoundryCastingBlockEntity casting) {
            accepted = casting.insertPour(incomingMaterial, incomingAmount);
        } else if (target.entity instanceof CrucibleBlockEntity crucible) {
            accepted = crucible.pourIntoCrucible(incomingMaterial, incomingAmount);
        } else if (target.entity instanceof FoundryBaseBlockEntity foundry) {
            accepted = foundry.insertMolten(incomingMaterial, incomingAmount);
        } else {
            return 0;
        }
        if (accepted > 0) {
            visualMaterial = incomingMaterial;
            visualTicks = 8;
            syncIfChanged();
        }
        return accepted;
    }

    private PourTarget findPourTarget(String incomingMaterial) {
        if (level == null) {
            return null;
        }
        boolean slagtap = getBlockState().is(ModBlocks.FOUNDRY_SLAGTAP.get());
        // HBM CE: normal foundry outlet raycasts exactly 4 blocks down; slagtap exactly 15.
        int range = slagtap ? 15 : 4;
        for (int distance = 1; distance <= range; distance++) {
            BlockPos targetPos = worldPosition.below(distance);
            BlockEntity blockEntity = level.getBlockEntity(targetPos);
            if (blockEntity instanceof FoundryCastingBlockEntity casting && casting.canReceivePour(Direction.UP, incomingMaterial)) {
                return new PourTarget(casting, targetPos);
            }
            if (blockEntity instanceof CrucibleBlockEntity crucible && crucible.canAcceptPour(incomingMaterial)) {
                return new PourTarget(crucible, targetPos);
            }
            if (blockEntity instanceof FoundryBaseBlockEntity foundry && !(foundry instanceof FoundryOutletBlockEntity)
                    && foundry.canReceiveFrom(Direction.UP, incomingMaterial)) {
                return new PourTarget(foundry, targetPos);
            }
            if (!level.getBlockState(targetPos).isAir()) {
                return null;
            }
        }
        return null;
    }


    public BlockPos getPourTargetPos(String incomingMaterial) {
        PourTarget target = findPourTarget(incomingMaterial);
        return target == null ? null : target.pos();
    }

    @Override
    public int getCapacity() {
        return 0;
    }

    public void setFilter(String filter) {
        this.filter = filter == null ? "" : filter;
        syncIfChanged();
    }

    public void setInvertFilter(boolean invertFilter) {
        this.invertFilter = invertFilter;
        syncIfChanged();
    }

    public void setInvertRedstone(boolean invertRedstone) {
        this.invertRedstone = invertRedstone;
        syncIfChanged();
    }

    public String getVisualMaterial() {
        return visualMaterial;
    }

    public int getVisualTicks() {
        return visualTicks;
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putString("Filter", filter);
        tag.putBoolean("InvertFilter", invertFilter);
        tag.putBoolean("InvertRedstone", invertRedstone);
        tag.putString("VisualMaterial", visualMaterial);
        tag.putInt("VisualTicks", visualTicks);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        filter = tag.getString("Filter");
        invertFilter = tag.getBoolean("InvertFilter");
        invertRedstone = tag.getBoolean("InvertRedstone");
        visualMaterial = tag.getString("VisualMaterial");
        visualTicks = tag.getInt("VisualTicks");
    }

    private record PourTarget(BlockEntity entity, BlockPos pos) {
    }
}
