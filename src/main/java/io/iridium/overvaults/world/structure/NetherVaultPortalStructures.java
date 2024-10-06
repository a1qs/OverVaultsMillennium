package io.iridium.overvaults.world.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.BlockColumn;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class NetherVaultPortalStructures extends StructureFeature<JigsawConfiguration> {

    public NetherVaultPortalStructures() {
        super(JigsawConfiguration.CODEC, NetherVaultPortalStructures::createPiecesGenerator, NetherVaultPortalStructures::afterPlace);
    }


    @Override
    public GenerationStep.Decoration step() {
        return GenerationStep.Decoration.SURFACE_STRUCTURES;
    }

    private static boolean isValidLand(PieceGeneratorSupplier.Context<JigsawConfiguration> context) {
        ChunkPos chunkPos = context.chunkPos();
        ChunkGenerator chunkGenerator = context.chunkGenerator();
        LevelHeightAccessor heightAccessor = context.heightAccessor();

        int baseX = chunkPos.getMiddleBlockX();
        int baseZ = chunkPos.getMiddleBlockZ();

        // Define the offsets for the middle and the corners
        int[][] offsets = {
                {0, 0},    // Middle
                {-7, -7},  // Top-left corner
                {7, -7},   // Top-right corner
                {-7, 7},   // Bottom-left corner
                {7, 7}     // Bottom-right corner
        };

        boolean hasAirExposure = false; // Track whether there's air exposure

        // For each position (middle and corners), check if there's solid ground
        for (int[] offset : offsets) {
            int x = baseX + offset[0];
            int z = baseZ + offset[1];

            // Get the vertical column of blocks at this X/Z coordinate
            BlockColumn column = chunkGenerator.getBaseColumn(x, z, heightAccessor);

            // Iterate downward from sea level to find the first solid block
            for (int y = heightAccessor.getMaxBuildHeight() - 5; y >= heightAccessor.getMinBuildHeight(); y--) {
                BlockState blockState = column.getBlock(y);

                // Ensure the block is solid but not bedrock or lava
                if (!blockState.isAir() && blockState.getMaterial().isSolid() &&
                        !blockState.is(Blocks.BEDROCK) && !blockState.is(Blocks.LAVA)) {

                    BlockPos pos = new BlockPos(x, y, z);
                    if (isExposedToAir(context.chunkGenerator(), pos, heightAccessor)) {
                        hasAirExposure = true;
                    }

                    break;
                }

                if (y == heightAccessor.getMinBuildHeight()) {
                    return false;
                }
            }
        }

        return hasAirExposure; // All positions are on solid ground
    }


    public static Optional<PieceGenerator<JigsawConfiguration>> createPiecesGenerator(PieceGeneratorSupplier.Context<JigsawConfiguration> context) {
        ChunkPos chunkPos = context.chunkPos();
        BlockPos blockpos = chunkPos.getMiddleBlockPosition(0);



        // Determine the Y coordinate based on the dimension without using heightmaps
        Random random = new Random();
        int targetY = findValidY(context, blockpos, 11, random);

        // If no valid Y coordinate is found, return empty
        if (targetY == -1) {
            return Optional.empty();
        }

        if (!NetherVaultPortalStructures.isValidLand(context)) {
            return Optional.empty();
        }

        BlockPos targetPos = new BlockPos(blockpos.getX(), targetY, blockpos.getZ());

        Optional<PieceGenerator<JigsawConfiguration>> structurePiecesGenerator =
                JigsawPlacement.addPieces(
                        context, // Context for JigsawPlacement.
                        PoolElementStructurePiece::new, // List of jigsaw pieces.
                        targetPos, // Position to place the structure.
                        false,  // Special boundary adjustments for villages.
                        false // Adds terrain height's y value to blockpos's y value.
                );

        return structurePiecesGenerator;
    }

    private static Block CHROMATIC_IRON_ORE = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("the_vault:chromatic_iron_ore"));
    private static Block RAW_CHROMATIC_IRON_BLOCK = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("the_vault:raw_chromatic_iron_block"));
    private static Block GILDED_SCONCE = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("the_vault:gilded_sconce"));

    private static void afterPlace(WorldGenLevel level, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, PiecesContainer piecesContainer) {

        for (int x = boundingBox.minX(); x <= boundingBox.maxX(); x++) {
            for (int y = boundingBox.minY(); y <= boundingBox.maxY(); y++) {
                for (int z = boundingBox.minZ(); z <= boundingBox.maxZ(); z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState currentState = level.getBlockState(pos);

                    if (currentState.is(CHROMATIC_IRON_ORE) || currentState.is(GILDED_SCONCE)) {

                        boolean isSubmerged =
                                level.getBlockState(pos.above()).getFluidState().isSource() ||
                                level.getBlockState(pos.north()).getFluidState().isSource() ||
                                level.getBlockState(pos.south()).getFluidState().isSource() ||
                                level.getBlockState(pos.east()).getFluidState().isSource() ||
                                level.getBlockState(pos.west()).getFluidState().isSource();

                        if (!isSubmerged) continue;

                        if (currentState.is(CHROMATIC_IRON_ORE) && random.nextFloat() < 0.2f) {
                            level.setBlock(pos, RAW_CHROMATIC_IRON_BLOCK.defaultBlockState(), 3);
                        }
                    }
                }
            }
        }
    }

    private static int findValidY(PieceGeneratorSupplier.Context<JigsawConfiguration> context, BlockPos blockpos, int structureHeight, Random random) {
        int startY = 80;
        int endY = 32;
        List<Integer> validYLevels = new ArrayList<>();


        for (int y = startY; y >= endY; y--) {
            BlockPos currentPos = new BlockPos(blockpos.getX(), y, blockpos.getZ());


            boolean hasEnoughSpace = true;
            for (int offset = 0; offset < structureHeight; offset++) {
                BlockPos posToCheck = currentPos.above(offset);
                BlockState blockState = context.chunkGenerator().getBaseColumn(posToCheck.getX(), posToCheck.getZ(), context.heightAccessor()).getBlock(posToCheck.getY());

                if (!blockState.isAir() && !blockState.getFluidState().isEmpty()) {
                    hasEnoughSpace = false;
                    break;
                }
            }

            // If we found a valid Y-level with enough space, add it to the list
            if (hasEnoughSpace) {
                validYLevels.add(y);
            }
        }

        // If there are valid Y-levels, return a random one; otherwise, return -1
        if (!validYLevels.isEmpty()) {
            return validYLevels.get(random.nextInt(validYLevels.size()));
        }

        // Return -1 if no suitable Y-level is found
        return -1;
    }

    private static boolean isExposedToAir(ChunkGenerator chunkGenerator, BlockPos pos, LevelHeightAccessor accessor) {
        int[] dx = {-1, 1, 0, 0, 0, 0};
        int[] dy = {0, 0, -1, 1, 0, 0};
        int[] dz = {0, 0, 0, 0, -1, 1};

        for (int i = 0; i < 6; i++) {
            BlockPos checkPos = pos.offset(dx[i], dy[i], dz[i]);
            BlockState state = chunkGenerator.getBaseColumn(checkPos.getX(), checkPos.getZ(), accessor).getBlock(checkPos.getY());
            if (state.isAir()) {
                return true; // Exposed to air
            }
        }
        return false;
    }
}