package com.yellowyotu.hbmneoforge.blockentity;

import com.yellowyotu.hbmneoforge.ModBlockEntities;
import com.yellowyotu.hbmneoforge.ModItems;
import com.yellowyotu.hbmneoforge.block.FatManBlock;
import com.yellowyotu.hbmneoforge.menu.FatManMenu;
import java.util.Random;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;

public final class FatManBlockEntity extends BlockEntity implements MenuProvider {
    public static final int EFFECT_DURATION = 1_200;

    private final ItemStackHandler inventory = new ItemStackHandler(6) {
        @Override
        public int getSlotLimit(int slot) {
            return 1;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return switch (slot) {
                case 0 -> stack.is(ModItems.MAN_IGNITER.get());
                case 1, 2, 3, 4 -> stack.is(ModItems.EARLY_EXPLOSIVE_LENSES.get());
                case 5 -> stack.is(ModItems.MAN_CORE.get());
                default -> false;
            };
        }

        @Override
        protected void onContentsChanged(int slot) {
            setChanged();
        }
    };

    private int effectTicks;

    public FatManBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.NUKE_MAN.get(), pos, state);
    }

    public ItemStackHandler getInventory() {
        return inventory;
    }

    public boolean exp1() {
        return inventory.getStackInSlot(1).is(ModItems.EARLY_EXPLOSIVE_LENSES.get());
    }

    public boolean exp2() {
        return inventory.getStackInSlot(2).is(ModItems.EARLY_EXPLOSIVE_LENSES.get());
    }

    public boolean exp3() {
        return inventory.getStackInSlot(3).is(ModItems.EARLY_EXPLOSIVE_LENSES.get());
    }

    public boolean exp4() {
        return inventory.getStackInSlot(4).is(ModItems.EARLY_EXPLOSIVE_LENSES.get());
    }

    public boolean isReady() {
        return exp1()
                && exp2()
                && exp3()
                && exp4()
                && inventory.getStackInSlot(0).is(ModItems.MAN_IGNITER.get())
                && inventory.getStackInSlot(5).is(ModItems.MAN_CORE.get());
    }

    public void clearSlots() {
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            inventory.setStackInSlot(slot, ItemStack.EMPTY);
        }
    }

    public void startVisualEffect() {
        effectTicks = 0;
        clearSlots();
        setChanged();
    }

    public static void tick(Level level, BlockPos pos, BlockState state, FatManBlockEntity nuke) {
        if (!state.getValue(FatManBlock.DETONATED)) {
            return;
        }

        nuke.effectTicks++;
        if (level.isClientSide()) {
            nuke.spawnClientEffect();
            return;
        }

        if (nuke.effectTicks >= EFFECT_DURATION) {
            level.removeBlock(pos, false);
        }
    }

    private void spawnClientEffect() {
        if (level == null) {
            return;
        }

        Random random = new Random((long) effectTicks * 341873128712L + worldPosition.asLong());
        double x = worldPosition.getX() + 0.5D;
        double y = worldPosition.getY() + 0.6D;
        double z = worldPosition.getZ() + 0.5D;

        // Original Torex-style phases: initial flash/fireball, rising stem, torus cap and condensation/shock rings.
        if (effectTicks <= 8) {
            level.addParticle(ParticleTypes.FLASH, x, y + 1.0D, z, 0.0D, 0.0D, 0.0D);
            level.addParticle(ParticleTypes.EXPLOSION_EMITTER, x, y + 0.5D, z, 0.0D, 0.0D, 0.0D);
        }

        if (effectTicks < 120) {
            double fireballRadius = 1.5D + effectTicks * 0.09D;
            int count = 10;
            for (int i = 0; i < count; i++) {
                double theta = random.nextDouble() * Mth.TWO_PI;
                double phi = (random.nextDouble() - 0.5D) * Math.PI;
                double r = fireballRadius * (0.6D + random.nextDouble() * 0.4D);
                double px = x + Math.cos(theta) * Math.cos(phi) * r;
                double py = y + 2.0D + Math.sin(phi) * r * 0.55D;
                double pz = z + Math.sin(theta) * Math.cos(phi) * r;
                level.addParticle(i % 3 == 0 ? ParticleTypes.FLAME : ParticleTypes.LARGE_SMOKE,
                        px, py, pz, 0.0D, 0.04D + random.nextDouble() * 0.04D, 0.0D);
            }
        }

        if (effectTicks >= 20 && effectTicks < 900) {
            double age = effectTicks - 20.0D;
            double stemHeight = Math.min(55.0D, 4.0D + age * 0.075D);
            double stemRadius = 2.0D + Math.min(5.0D, age * 0.006D);
            for (int i = 0; i < 7; i++) {
                double py = y + random.nextDouble() * stemHeight;
                double angle = random.nextDouble() * Mth.TWO_PI;
                double radius = stemRadius * (0.35D + random.nextDouble() * 0.65D);
                level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                        x + Math.cos(angle) * radius,
                        py,
                        z + Math.sin(angle) * radius,
                        Math.cos(angle) * 0.012D,
                        0.05D + random.nextDouble() * 0.04D,
                        Math.sin(angle) * 0.012D);
            }
        }

        if (effectTicks >= 70 && effectTicks < EFFECT_DURATION) {
            double age = effectTicks - 70.0D;
            double capY = y + Math.min(58.0D, 8.0D + age * 0.07D);
            double torusRadius = Math.min(34.0D, 5.0D + age * 0.04D);
            double thickness = 2.0D + Math.min(8.0D, age * 0.008D);
            for (int i = 0; i < 12; i++) {
                double angle = random.nextDouble() * Mth.TWO_PI;
                double localRadius = torusRadius + (random.nextDouble() - 0.5D) * thickness;
                double py = capY + (random.nextDouble() - 0.5D) * thickness * 0.8D;
                level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,
                        x + Math.cos(angle) * localRadius,
                        py,
                        z + Math.sin(angle) * localRadius,
                        Math.cos(angle) * 0.025D,
                        0.025D,
                        Math.sin(angle) * 0.025D);
            }
        }

        if (effectTicks >= 15 && effectTicks <= 220 && effectTicks % 2 == 0) {
            double ringRadius = Math.min(48.0D, 3.0D + effectTicks * 0.22D);
            for (int i = 0; i < 18; i++) {
                double angle = (Mth.TWO_PI * i / 18.0D) + random.nextDouble() * 0.08D;
                level.addParticle(ParticleTypes.CLOUD,
                        x + Math.cos(angle) * ringRadius,
                        y + 0.3D + random.nextDouble() * 1.2D,
                        z + Math.sin(angle) * ringRadius,
                        Math.cos(angle) * 0.12D,
                        0.01D,
                        Math.sin(angle) * 0.12D);
            }
        }
    }

    public void dropContents() {
        if (level == null || level.isClientSide()) {
            return;
        }
        for (int slot = 0; slot < inventory.getSlots(); slot++) {
            ItemStack stack = inventory.getStackInSlot(slot);
            if (!stack.isEmpty()) {
                Containers.dropItemStack(level, worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D, worldPosition.getZ() + 0.5D, stack);
                inventory.setStackInSlot(slot, ItemStack.EMPTY);
            }
        }
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.hbm_neoforge.nuke_man");
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
        return new FatManMenu(id, inventory, this);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Inventory", inventory.serializeNBT(registries));
        tag.putInt("EffectTicks", effectTicks);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        if (tag.contains("Inventory")) {
            inventory.deserializeNBT(registries, tag.getCompound("Inventory"));
        }
        effectTicks = tag.getInt("EffectTicks");
    }
}
