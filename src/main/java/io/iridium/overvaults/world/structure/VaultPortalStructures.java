package io.iridium.overvaults.world.structure;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureFeatureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraft.world.level.levelgen.feature.configurations.JigsawConfiguration;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGenerator;
import net.minecraft.world.level.levelgen.structure.pieces.PieceGeneratorSupplier;
import net.minecraft.world.level.levelgen.structure.pieces.PiecesContainer;
import net.minecraft.world.level.levelgen.structure.pools.JigsawPlacement;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Optional;
import java.util.Random;

public class VaultPortalStructures extends StructureFeature<JigsawConfiguration> {

    public VaultPortalStructures() {
        super(JigsawConfiguration.CODEC, VaultPortalStructures::createPiecesGenerator, VaultPortalStructures::afterPlace);
    }


    @Override
    public GenerationStep.Decoration step() {
        return GenerationStep.Decoration.SURFACE_STRUCTURES;
    }

    private static boolean isValidFlatLand(PieceGeneratorSupplier.Context<JigsawConfiguration> context) {

        // Checks to make sure our structure does not spawn on cliff edges or partially in the air
        ChunkPos chunkPos = context.chunkPos();
        ChunkGenerator chunkGenerator = context.chunkGenerator();
        Heightmap.Types heightmapType = Heightmap.Types.WORLD_SURFACE_WG;
        int maxHeightDifference = 8; // Maximum allowed height difference

        int baseX = chunkPos.getMiddleBlockX();
        int baseZ = chunkPos.getMiddleBlockZ();
        int baseY = chunkGenerator.getBaseHeight(baseX, baseZ, heightmapType, context.heightAccessor());

        // Define the offsets for the middle and the corners
        int[][] offsets = {
                {0, 0}, // Middle
                {-7, -7}, // Top-left corner
                {7, -7}, // Top-right corner
                {-7, 7}, // Bottom-left corner
                {7, 7} // Bottom-right corner
        };

        // Check only the middle and corners
        for (int[] offset : offsets) {
            int x = baseX + offset[0];
            int z = baseZ + offset[1];
            int currentY = chunkGenerator.getBaseHeight(x, z, heightmapType, context.heightAccessor());
            if (Math.abs(currentY - baseY) > maxHeightDifference) {
                return false;
            }
        }

        return true;
    }


    public static Optional<PieceGenerator<JigsawConfiguration>> createPiecesGenerator(PieceGeneratorSupplier.Context<JigsawConfiguration> context) {
        BlockPos blockpos = context.chunkPos().getMiddleBlockPosition(0);

        // Get the height of the surface
        int surfaceY = context.chunkGenerator().getBaseHeight(blockpos.getX(), blockpos.getZ(), Heightmap.Types.WORLD_SURFACE_WG, context.heightAccessor());
        BlockPos surfacePos = new BlockPos(blockpos.getX(), surfaceY, blockpos.getZ());

        // Get the block state at the surface position
        BlockState blockState = context.chunkGenerator().getBaseColumn(surfacePos.getX(), surfacePos.getZ(), context.heightAccessor()).getBlock(surfacePos.getY() - 1);

        // Check if the block at the surface is water
        boolean isWater = blockState.getFluidState().isSource();

        if (surfaceY < 16) {
            return Optional.empty();
        }

        if (!isWater && !VaultPortalStructures.isValidFlatLand(context)) {
            return Optional.empty();
        }

        blockpos = new BlockPos(blockpos.getX(), surfaceY, blockpos.getZ());

        // If the block is water, adjust the block position to the ocean floor or river bed
        if (isWater) {
            int oceanFloorY = context.chunkGenerator().getBaseHeight(blockpos.getX(), blockpos.getZ(), Heightmap.Types.OCEAN_FLOOR_WG, context.heightAccessor());
            blockpos = new BlockPos(blockpos.getX(), oceanFloorY, blockpos.getZ());
        }

        Optional<PieceGenerator<JigsawConfiguration>> structurePiecesGenerator =
                JigsawPlacement.addPieces(
                        context, // Used for JigsawPlacement to get all the proper behaviors done.
                        PoolElementStructurePiece::new, // Needed in order to create a list of jigsaw pieces when making the structure's layout.
                        blockpos, // Position of the structure. Y value is ignored if last parameter is set to true.
                        false,  // Special boundary adjustments for villages. It's... hard to explain. Keep this false and make your pieces not be partially intersecting.
                        // Either not intersecting or fully contained will make children pieces spawn just fine. It's easier that way.
                        false // Adds the terrain height's y value to the passed in blockpos's y value. (This uses WORLD_SURFACE_WG heightmap which stops at top water too)
                );

        return structurePiecesGenerator;
    }

    private static Block CUT_VINE = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("quark:cut_vine"));
    private static Block CHROMATIC_IRON_ORE = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("the_vault:chromatic_iron_ore"));
    private static Block RAW_CHROMATIC_IRON_BLOCK = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("the_vault:raw_chromatic_iron_block"));
    private static Block GILDED_SCONCE = ForgeRegistries.BLOCKS.getValue(new ResourceLocation("the_vault:gilded_sconce"));

    private static void afterPlace(WorldGenLevel level, StructureFeatureManager structureFeatureManager, ChunkGenerator chunkGenerator, Random random, BoundingBox boundingBox, ChunkPos chunkPos, PiecesContainer piecesContainer) {

        for (int x = boundingBox.minX(); x <= boundingBox.maxX(); x++) {
            for (int y = boundingBox.minY(); y <= boundingBox.maxY(); y++) {
                for (int z = boundingBox.minZ(); z <= boundingBox.maxZ(); z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState currentState = level.getBlockState(pos);

                    if (currentState.is(Blocks.VINE) || currentState.is(CUT_VINE) || currentState.is(CHROMATIC_IRON_ORE) || currentState.is(GILDED_SCONCE)) {

                        boolean isSubmerged =
                                level.getBlockState(pos.above()).getFluidState().isSource() ||
                                level.getBlockState(pos.north()).getFluidState().isSource() ||
                                level.getBlockState(pos.south()).getFluidState().isSource() ||
                                level.getBlockState(pos.east()).getFluidState().isSource() ||
                                level.getBlockState(pos.west()).getFluidState().isSource();

                        if (!isSubmerged) continue;

                        if (currentState.is(Blocks.VINE) || currentState.is(CUT_VINE)) {
                            level.setBlock(pos, Blocks.WATER.defaultBlockState(), 3);
                        }

                        if (currentState.is(CHROMATIC_IRON_ORE) && random.nextFloat() < 0.2f) {
                            level.setBlock(pos, RAW_CHROMATIC_IRON_BLOCK.defaultBlockState(), 3);
                        }
                    }


                }
            }
        }

    }
}