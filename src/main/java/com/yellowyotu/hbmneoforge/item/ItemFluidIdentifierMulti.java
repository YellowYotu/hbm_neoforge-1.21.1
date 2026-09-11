package com.yellowyotu.hbmneoforge.item;

import com.yellowyotu.hbmneoforge.ModItems;
import com.yellowyotu.hbmneoforge.blockentity.FluidPipeBlockEntity;
import com.yellowyotu.hbmneoforge.blockentity.FluidStorageBlockEntity;
import com.yellowyotu.hbmneoforge.blockentity.FluidStorageDummyBlockEntity;
import com.yellowyotu.hbmneoforge.fluid.NTMFluidType;
import com.yellowyotu.hbmneoforge.menu.FluidIdentifierMenu;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public final class ItemFluidIdentifierMulti extends Item {
    private static final String PRIMARY = "fluid1";
    private static final String SECONDARY = "fluid2";

    public ItemFluidIdentifierMulti(Properties properties) {
        super(properties);
    }

    public static ItemStack configured(NTMFluidType primary) {
        ItemStack stack = new ItemStack(ModItems.FLUID_IDENTIFIER.get());
        setType(stack, primary, true);
        setType(stack, null, false);
        return stack;
    }

    public static NTMFluidType getType(ItemStack stack, boolean primary) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        String key = primary ? PRIMARY : SECONDARY;
        if (!tag.contains(key)) {
            return null;
        }
        return NTMFluidType.byId(tag.getString(key));
    }

    public static void setType(ItemStack stack, NTMFluidType type, boolean primary) {
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        String key = primary ? PRIMARY : SECONDARY;
        if (type == null) {
            tag.remove(key);
        } else {
            tag.putString(key, type.id());
        }
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
    }

    public static void swapTypes(ItemStack stack) {
        NTMFluidType primary = getType(stack, true);
        NTMFluidType secondary = getType(stack, false);
        setType(stack, secondary, true);
        setType(stack, primary, false);
    }

    @Override
    public Component getName(ItemStack stack) {
        NTMFluidType primary = getType(stack, true);
        if (primary == null) {
            return Component.translatable("item.hbm_neoforge.fluid_identifier");
        }
        return Component.translatable("item.hbm_neoforge.fluid_identifier")
                .append(" (")
                .append(primary.displayName())
                .append(")");
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide() && player instanceof ServerPlayer serverPlayer) {
                serverPlayer.openMenu(new SimpleMenuProvider(
                        (id, inventory, ignored) -> new FluidIdentifierMenu(id, inventory, hand),
                        Component.translatable("container.hbm_neoforge.fluid_identifier")),
                        buffer -> buffer.writeByte(hand.ordinal()));
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
        }

        if (!level.isClientSide()) {
            swapTypes(stack);
            NTMFluidType primary = getType(stack, true);
            if (primary != null) {
                player.displayClientMessage(primary.displayName().copy().withStyle(ChatFormatting.YELLOW), true);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        NTMFluidType primary = getType(context.getItemInHand(), true);
        if (primary == null) {
            return InteractionResult.PASS;
        }

        if (context.getLevel().getBlockEntity(context.getClickedPos()) instanceof FluidPipeBlockEntity pipe) {
            if (!context.getLevel().isClientSide()) {
                if (context.getPlayer() != null && context.getPlayer().isShiftKeyDown()) {
                    spreadType(context.getLevel(), context.getClickedPos(), primary, pipe.getFilter(), 256);
                } else {
                    pipe.setFilter(primary);
                }
            }
            return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
        }

        if (context.getLevel().getBlockEntity(context.getClickedPos()) instanceof FluidStorageBlockEntity storage) {
            if (!context.getLevel().isClientSide()) { storage.setFluidType(primary); }
            return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
        }
        if (context.getLevel().getBlockEntity(context.getClickedPos()) instanceof FluidStorageDummyBlockEntity dummy) {
            if (!context.getLevel().isClientSide()) { dummy.setFluidType(primary); }
            return InteractionResult.sidedSuccess(context.getLevel().isClientSide());
        }

        return InteractionResult.PASS;
    }

    private static void spreadType(Level level, BlockPos start, NTMFluidType newType, NTMFluidType oldType, int limit) {
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        Set<BlockPos> visited = new HashSet<>();
        queue.add(start);
        while (!queue.isEmpty() && visited.size() < limit) {
            BlockPos pos = queue.removeFirst();
            if (!visited.add(pos)) {
                continue;
            }
            if (!(level.getBlockEntity(pos) instanceof FluidPipeBlockEntity pipe)) {
                continue;
            }
            if (pipe.getFilter() != oldType) {
                continue;
            }
            pipe.setFilter(newType);
            for (net.minecraft.core.Direction direction : net.minecraft.core.Direction.values()) {
                queue.addLast(pos.relative(direction));
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        NTMFluidType primary = getType(stack, true);
        NTMFluidType secondary = getType(stack, false);
        tooltip.add(Component.translatable("tooltip.hbm_neoforge.fluid_identifier.primary").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("   ").append(primary == null ? Component.translatable("fluid.hbm_neoforge.none") : primary.displayName()).withStyle(ChatFormatting.AQUA));
        tooltip.add(Component.translatable("tooltip.hbm_neoforge.fluid_identifier.secondary").withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.literal("   ").append(secondary == null ? Component.translatable("fluid.hbm_neoforge.none") : secondary.displayName()).withStyle(ChatFormatting.DARK_AQUA));
        tooltip.add(Component.translatable("tooltip.hbm_neoforge.fluid_identifier.controls").withStyle(ChatFormatting.DARK_GRAY));
    }
}
