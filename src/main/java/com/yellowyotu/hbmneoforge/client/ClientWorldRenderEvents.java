package com.yellowyotu.hbmneoforge.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.yellowyotu.hbmneoforge.HBMsNuclearTechModUnofficialNeoForgeEdition;
import com.yellowyotu.hbmneoforge.block.AbstractHeaterBlock;
import com.yellowyotu.hbmneoforge.block.AirIntakeBlock;
import com.yellowyotu.hbmneoforge.block.ArcWelderBlock;
import com.yellowyotu.hbmneoforge.block.AssemblyMachineBlock;
import com.yellowyotu.hbmneoforge.block.BatterySocketBlock;
import com.yellowyotu.hbmneoforge.block.ChemicalPlantBlock;
import com.yellowyotu.hbmneoforge.block.CrucibleBlock;
import com.yellowyotu.hbmneoforge.block.FluidTankMultiblockBlock;
import com.yellowyotu.hbmneoforge.block.MachinePressBlock;
import com.yellowyotu.hbmneoforge.block.MixerBlock;
import com.yellowyotu.hbmneoforge.block.OilDerrickBlock;
import com.yellowyotu.hbmneoforge.block.SolderingStationBlock;
import com.yellowyotu.hbmneoforge.block.WoodBurnerBlock;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderHighlightEvent;

@EventBusSubscriber(modid = HBMsNuclearTechModUnofficialNeoForgeEdition.MODID, value = Dist.CLIENT)
public final class ClientWorldRenderEvents {

    private ClientWorldRenderEvents() {
    }

    @SubscribeEvent
    public static void onBlockHighlight(RenderHighlightEvent.Block event) {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        Level level = minecraft.level;

        if (player == null || level == null) {
            return;
        }

        BlockHitResult hit = event.getTarget();
        Preview preview = createPlacementPreview(player, level, hit);
        if (preview != null) {
            event.setCanceled(true);
            renderPlacementPreview(event, preview);
            return;
        }

        BlockPos pos = hit.getBlockPos();
        BlockState state = level.getBlockState(pos);
        ResourceLocation id = BuiltInRegistries.BLOCK.getKey(state.getBlock());

        if (!HBMsNuclearTechModUnofficialNeoForgeEdition.MODID.equals(id.getNamespace())) {
            return;
        }

        if (state.getRenderShape() == RenderShape.INVISIBLE && state.getShape(level, pos).isEmpty()) {
            return;
        }

        VoxelShape shape = state.getShape(level, pos);
        if (shape.isEmpty()) {
            return;
        }

        event.setCanceled(true);
        renderSimpleBlackOutline(event, pos, shape);
    }

    private static Preview createPlacementPreview(Player player, Level level, BlockHitResult hit) {
        ItemStack stack = player.getMainHandItem();
        InteractionHand hand = InteractionHand.MAIN_HAND;

        if (!(stack.getItem() instanceof BlockItem)) {
            stack = player.getOffhandItem();
            hand = InteractionHand.OFF_HAND;
        }

        if (!(stack.getItem() instanceof BlockItem blockItem)) {
            return null;
        }

        Block block = blockItem.getBlock();
        if (!hasOriginalStylePlacementPreview(block)) {
            return null;
        }

        BlockPlaceContext context = new BlockPlaceContext(player, hand, stack, hit);
        BlockPos origin = context.getClickedPos();
        List<BlockPos> positions = getPreviewPositions(block, context, origin);

        if (positions.isEmpty()) {
            return null;
        }

        boolean valid = true;
        for (BlockPos position : positions) {
            if (!level.getWorldBorder().isWithinBounds(position)
                    || !level.getBlockState(position).canBeReplaced(context)) {
                valid = false;
                break;
            }
        }

        return new Preview(List.copyOf(new LinkedHashSet<>(positions)), valid);
    }

    private static boolean hasOriginalStylePlacementPreview(Block block) {
        return block instanceof AbstractHeaterBlock
                || block instanceof AirIntakeBlock
                || block instanceof ArcWelderBlock
                || block instanceof AssemblyMachineBlock
                || block instanceof BatterySocketBlock
                || block instanceof ChemicalPlantBlock
                || block instanceof CrucibleBlock
                || block instanceof FluidTankMultiblockBlock
                || block instanceof MachinePressBlock
                || block instanceof MixerBlock
                || block instanceof OilDerrickBlock
                || block instanceof SolderingStationBlock
                || block instanceof WoodBurnerBlock;
    }

    private static List<BlockPos> getPreviewPositions(Block block, BlockPlaceContext context, BlockPos origin) {
        Direction playerDirection = context.getHorizontalDirection();

        if (block instanceof AbstractHeaterBlock) {
            return AbstractHeaterBlock.getAllPositions(origin);
        }

        if (block instanceof AirIntakeBlock) {
            return AirIntakeBlock.getFootprint(origin, playerDirection.getOpposite());
        }

        if (block instanceof ArcWelderBlock) {
            return ArcWelderBlock.getAllPositions(origin, playerDirection.getOpposite());
        }

        if (block instanceof AssemblyMachineBlock || block instanceof ChemicalPlantBlock) {
            return centeredBox(origin, 3, 3, 3);
        }

        if (block instanceof BatterySocketBlock) {
            return positiveBox(origin, 2, 2, 2);
        }

        if (block instanceof CrucibleBlock) {
            return CrucibleBlock.getAllPositions(origin);
        }

        if (block instanceof FluidTankMultiblockBlock tank) {
            BlockState state = tank.defaultBlockState().setValue(FluidTankMultiblockBlock.FACING, playerDirection);
            return tank.getStructurePositions(origin, state);
        }

        if (block instanceof MachinePressBlock || block instanceof MixerBlock) {
            return List.of(origin, origin.above(), origin.above(2));
        }

        if (block instanceof OilDerrickBlock) {
            return OilDerrickBlock.getMachinePositions(origin);
        }

        if (block instanceof SolderingStationBlock) {
            return SolderingStationBlock.getAllPositions(origin, playerDirection.getOpposite());
        }

        if (block instanceof WoodBurnerBlock) {
            Direction facing = playerDirection;
            Direction right = facing.getClockWise();
            List<BlockPos> positions = new ArrayList<>(8);
            for (int y = 0; y < 2; y++) {
                for (int forward = 0; forward < 2; forward++) {
                    for (int side = 0; side < 2; side++) {
                        positions.add(origin.relative(facing, forward).relative(right, side).above(y));
                    }
                }
            }
            return positions;
        }

        return List.of();
    }

    private static List<BlockPos> centeredBox(BlockPos origin, int width, int height, int depth) {
        List<BlockPos> positions = new ArrayList<>(width * height * depth);
        int halfWidth = width / 2;
        int halfDepth = depth / 2;

        for (int y = 0; y < height; y++) {
            for (int x = -halfWidth; x <= halfWidth; x++) {
                for (int z = -halfDepth; z <= halfDepth; z++) {
                    positions.add(origin.offset(x, y, z));
                }
            }
        }

        return positions;
    }

    private static List<BlockPos> positiveBox(BlockPos origin, int width, int height, int depth) {
        List<BlockPos> positions = new ArrayList<>(width * height * depth);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                for (int z = 0; z < depth; z++) {
                    positions.add(origin.offset(x, y, z));
                }
            }
        }

        return positions;
    }

    private static void renderSimpleBlackOutline(RenderHighlightEvent.Block event, BlockPos pos, VoxelShape shape) {
        Vec3 camera = event.getCamera().getPosition();
        AABB bounds = shape.bounds()
                .move(pos)
                .move(-camera.x, -camera.y, -camera.z)
                .inflate(0.002D);

        VertexConsumer lines = event.getMultiBufferSource().getBuffer(RenderType.lines());
        RenderSystem.lineWidth(2.0F);
        LevelRenderer.renderLineBox(event.getPoseStack(), lines, bounds, 0.0F, 0.0F, 0.0F, 1.0F);
    }

    private static void renderPlacementPreview(RenderHighlightEvent.Block event, Preview preview) {
        Vec3 camera = event.getCamera().getPosition();
        VertexConsumer lines = event.getMultiBufferSource().getBuffer(RenderType.lines());

        double timer = (System.currentTimeMillis() % (long) (1000.0D * Math.PI)) / 250.0D;
        float pulse = (float) (Math.sin(timer) * 0.25D + 0.75D);

        float red = preview.valid() ? 0.0F : pulse;
        float green = preview.valid() ? pulse : 0.0F;
        Set<BlockPos> occupied = Set.copyOf(preview.positions());

        RenderSystem.lineWidth(2.0F);

        for (BlockPos pos : preview.positions()) {
            double minX = pos.getX() - camera.x;
            double minY = pos.getY() - camera.y;
            double minZ = pos.getZ() - camera.z;
            double maxX = minX + 1.0D;
            double maxY = minY + 1.0D;
            double maxZ = minZ + 1.0D;

            // Draw only outer faces of the multiblock preview. Shared internal faces are omitted,
            // matching the original BlockDummyable placement visualization.
            if (!occupied.contains(pos.west())) {
                renderFaceOutline(event, lines, Direction.WEST, minX, minY, minZ, maxX, maxY, maxZ, red, green);
            }
            if (!occupied.contains(pos.east())) {
                renderFaceOutline(event, lines, Direction.EAST, minX, minY, minZ, maxX, maxY, maxZ, red, green);
            }
            if (!occupied.contains(pos.below())) {
                renderFaceOutline(event, lines, Direction.DOWN, minX, minY, minZ, maxX, maxY, maxZ, red, green);
            }
            if (!occupied.contains(pos.above())) {
                renderFaceOutline(event, lines, Direction.UP, minX, minY, minZ, maxX, maxY, maxZ, red, green);
            }
            if (!occupied.contains(pos.north())) {
                renderFaceOutline(event, lines, Direction.NORTH, minX, minY, minZ, maxX, maxY, maxZ, red, green);
            }
            if (!occupied.contains(pos.south())) {
                renderFaceOutline(event, lines, Direction.SOUTH, minX, minY, minZ, maxX, maxY, maxZ, red, green);
            }
        }
    }

    private static void renderFaceOutline(
            RenderHighlightEvent.Block event,
            VertexConsumer lines,
            Direction face,
            double minX,
            double minY,
            double minZ,
            double maxX,
            double maxY,
            double maxZ,
            float red,
            float green
    ) {
        double epsilon = 0.001D;
        AABB box = switch (face) {
            case WEST -> new AABB(minX - epsilon, minY, minZ, minX + epsilon, maxY, maxZ);
            case EAST -> new AABB(maxX - epsilon, minY, minZ, maxX + epsilon, maxY, maxZ);
            case DOWN -> new AABB(minX, minY - epsilon, minZ, maxX, minY + epsilon, maxZ);
            case UP -> new AABB(minX, maxY - epsilon, minZ, maxX, maxY + epsilon, maxZ);
            case NORTH -> new AABB(minX, minY, minZ - epsilon, maxX, maxY, minZ + epsilon);
            case SOUTH -> new AABB(minX, minY, maxZ - epsilon, maxX, maxY, maxZ + epsilon);
        };

        LevelRenderer.renderLineBox(event.getPoseStack(), lines, box, red, green, 0.0F, 1.0F);
    }

    private record Preview(List<BlockPos> positions, boolean valid) {
    }
}
