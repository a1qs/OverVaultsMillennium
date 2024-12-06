package io.iridium.overvaults.millenium.world;

import iskallia.vault.block.VaultPortalBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public enum StructureSize {
    SMALL(3, 5),   // 3x5 area
    LARGE(5, 9);   // 5x7 area

    private final int width;
    private final int height;

    StructureSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public Iterable<BlockPos> getBlockPositions(BlockPos center, Rotation rotation) {
        // Calculate the corner positions based on width and height
        int halfWidth = (width - 1) / 2;
        int halfHeight = (height - 1) / 2;

        BlockPos minPos = center.below(halfHeight).west(halfWidth);
        BlockPos maxPos = center.above(halfHeight).east(halfWidth);

        Iterable<BlockPos> positions = BlockPos.betweenClosed(minPos, maxPos);

        List<BlockPos> rotatedPositions = new ArrayList<>();
        for (BlockPos pos : positions) {
            int dx = pos.getX() - center.getX();
            int dz = pos.getZ() - center.getZ();

            // Apply rotation transformation based on the rotation parameter
            BlockPos rotatedPos = switch (rotation) {
                case NONE -> pos;
                case CLOCKWISE_90 -> new BlockPos(center.getX() - dz, pos.getY(), center.getZ() + dx);  // 90 degrees
                case CLOCKWISE_180 -> new BlockPos(center.getX() - dx, pos.getY(), center.getZ() - dz); // 180 degrees
                case COUNTERCLOCKWISE_90 -> new BlockPos(center.getX() + dz, pos.getY(), center.getZ() - dx); // -90 degrees
            };

            rotatedPositions.add(rotatedPos.immutable());
        }

        return rotatedPositions;
    }


    public List<BlockPos> getFrameBlockPositions(BlockPos center, Rotation rotation) {
        // Calculate the corner positions based on width and height
        int halfWidth = (width - 1) / 2;
        int halfHeight = (height - 1) / 2;

        // Define the inner bounds
        BlockPos minPos = center.below(halfHeight).west(halfWidth);
        BlockPos maxPos = center.above(halfHeight).east(halfWidth);

        // Define the outer bounds (expand by 1 block in all directions)
        BlockPos minPosD = minPos.below(1).west(1);
        BlockPos maxPosD = maxPos.above(1).east(1);


        Set<BlockPos> inner = StreamSupport.stream(BlockPos.betweenClosed(minPos, maxPos).spliterator(), false)
                .map(BlockPos::immutable) // Convert to immutable BlockPos
                .collect(Collectors.toSet());

        Set<BlockPos> outerAndInner = StreamSupport.stream(BlockPos.betweenClosed(minPosD, maxPosD).spliterator(), false)
                .map(BlockPos::immutable) // Convert to immutable BlockPos
                .collect(Collectors.toSet());


        outerAndInner.removeAll(inner);


        List<BlockPos> rotatedPositions = new ArrayList<>();
        for (BlockPos pos : outerAndInner) {
            int dx = pos.getX() - center.getX();
            int dz = pos.getZ() - center.getZ();

            // Apply rotation transformation based on the rotation parameter
            BlockPos rotatedPos = switch (rotation) {
                case NONE -> pos;
                case CLOCKWISE_90 -> new BlockPos(center.getX() - dz, pos.getY(), center.getZ() + dx);  // 90 degrees
                case CLOCKWISE_180 -> new BlockPos(center.getX() - dx, pos.getY(), center.getZ() - dz); // 180 degrees
                case COUNTERCLOCKWISE_90 -> new BlockPos(center.getX() + dz, pos.getY(), center.getZ() - dx); // -90 degrees
            };

            rotatedPositions.add(rotatedPos.immutable());
        }

        return rotatedPositions;
    }
}
