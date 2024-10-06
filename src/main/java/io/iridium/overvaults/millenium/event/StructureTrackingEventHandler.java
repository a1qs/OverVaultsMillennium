package io.iridium.overvaults.millenium.event;


import com.mojang.datafixers.util.Pair;
import io.iridium.overvaults.OverVaults;
import io.iridium.overvaults.config.ServerConfig;
import io.iridium.overvaults.millenium.world.StructureSize;
import io.iridium.overvaults.millenium.world.PortalData;
import io.iridium.overvaults.millenium.world.PortalSavedData;
import io.iridium.overvaults.world.structure.ModStructures;
import iskallia.vault.core.world.storage.VirtualWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.feature.ConfiguredStructureFeature;
import net.minecraft.world.level.levelgen.structure.PoolElementStructurePiece;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Mod.EventBusSubscriber(modid = OverVaults.MOD_ID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class StructureTrackingEventHandler {

    private static final List<ResourceLocation> validStructures = new ArrayList<>(
            Arrays.asList(
                    new ResourceLocation(OverVaults.MOD_ID, "portal1_leafy_ruin0"),
                    new ResourceLocation(OverVaults.MOD_ID, "portal1_desert_ruin0"),
                    new ResourceLocation(OverVaults.MOD_ID, "portal1_mesa_ruin0"),
                    new ResourceLocation(OverVaults.MOD_ID, "portal1_ruin0"),
                    new ResourceLocation(OverVaults.MOD_ID, "portal2_desert_ruin0"),
                    new ResourceLocation(OverVaults.MOD_ID, "portal2_leafy_ruin0"),
                    new ResourceLocation(OverVaults.MOD_ID, "portal2_mesa_ruin0"),
                    new ResourceLocation(OverVaults.MOD_ID, "portal2_ruin0"),
                    new ResourceLocation(OverVaults.MOD_ID, "portal2_end_ruin0"),
                    new ResourceLocation(OverVaults.MOD_ID, "portal2_nether_ruin0")
            ));

    private static final Map<ResourceLocation, Pair<StructureSize, Map<Rotation, BlockPos>>> structureRotationOffsets = Map.of(
            new ResourceLocation("overvaults:portal2_ruin0"), new Pair<>(StructureSize.SMALL, Map.of(
                    Rotation.NONE, new BlockPos(0, -1, -1),
                    Rotation.CLOCKWISE_90, new BlockPos(0, -1, 0),
                    Rotation.CLOCKWISE_180, new BlockPos(0, -1, 0),
                    Rotation.COUNTERCLOCKWISE_90, new BlockPos(-1, -1, 0)
            )),
            new ResourceLocation("overvaults:portal2_leafy_ruin0"), new Pair<>(StructureSize.SMALL, Map.of(
                    Rotation.NONE, new BlockPos(0, -1, -1),
                    Rotation.CLOCKWISE_90, new BlockPos(0, -1, 0),
                    Rotation.CLOCKWISE_180, new BlockPos(0, -1, 0),
                    Rotation.COUNTERCLOCKWISE_90, new BlockPos(-1, -1, 0)
            )),
            new ResourceLocation("overvaults:portal1_ruin0"), new Pair<>(StructureSize.LARGE, Map.of(
                    Rotation.NONE, new BlockPos(0, -1, -1),
                    Rotation.CLOCKWISE_90, new BlockPos(0, -1, -1),
                    Rotation.CLOCKWISE_180, new BlockPos(-1, -1, 0),
                    Rotation.COUNTERCLOCKWISE_90, new BlockPos(-1, -1, 0)
            )),
            new ResourceLocation("overvaults:portal2_desert_ruin0"), new Pair<>(StructureSize.SMALL, Map.of(
                    Rotation.NONE, new BlockPos(0, -1, -1),
                    Rotation.CLOCKWISE_90, new BlockPos(0, -1, 0),
                    Rotation.CLOCKWISE_180, new BlockPos(0, -1, 0),
                    Rotation.COUNTERCLOCKWISE_90, new BlockPos(-1, -1, 0)
            )),
            new ResourceLocation("overvaults:portal1_desert_ruin0"), new Pair<>(StructureSize.LARGE, Map.of(
                    Rotation.NONE, new BlockPos(0, -1, -1),
                    Rotation.CLOCKWISE_90, new BlockPos(0, -1, 0),
                    Rotation.CLOCKWISE_180, new BlockPos(0, -1, 0),
                    Rotation.COUNTERCLOCKWISE_90, new BlockPos(-1, -1, 0)
            )),
            new ResourceLocation("overvaults:portal2_mesa_ruin0"), new Pair<>(StructureSize.SMALL, Map.of(
                    Rotation.NONE, new BlockPos(0, -1, -1),
                    Rotation.CLOCKWISE_90, new BlockPos(0, -1, 0),
                    Rotation.CLOCKWISE_180, new BlockPos(0, -1, 0),
                    Rotation.COUNTERCLOCKWISE_90, new BlockPos(-1, -1, 0)
            )),
            new ResourceLocation("overvaults:portal1_mesa_ruin0"), new Pair<>(StructureSize.LARGE, Map.of(
                    Rotation.NONE, new BlockPos(0, -1, -1),
                    Rotation.CLOCKWISE_90, new BlockPos(0, -1, -1),
                    Rotation.CLOCKWISE_180, new BlockPos(-1, -1, 0),
                    Rotation.COUNTERCLOCKWISE_90, new BlockPos(-1, -1, 0)
            )),
            new ResourceLocation("overvaults:portal2_end_ruin0"), new Pair<>(StructureSize.SMALL, Map.of(
                    Rotation.NONE, new BlockPos(0, -1, -1),
                    Rotation.CLOCKWISE_90, new BlockPos(0, -1, 0),
                    Rotation.CLOCKWISE_180, new BlockPos(0, -1, 0),
                    Rotation.COUNTERCLOCKWISE_90, new BlockPos(-1, -1, 0)
            )),
            new ResourceLocation("overvaults:portal2_nether_ruin0"), new Pair<>(StructureSize.SMALL, Map.of(
                    Rotation.NONE, new BlockPos(0, -1, -1),
                    Rotation.CLOCKWISE_90, new BlockPos(0, -1, 0),
                    Rotation.CLOCKWISE_180, new BlockPos(0, -1, 0),
                    Rotation.COUNTERCLOCKWISE_90, new BlockPos(-1, -1, 0)
            )),
            new ResourceLocation("overvaults:portal1_leafy_ruin0"), new Pair<>(StructureSize.LARGE, Map.of(
                    Rotation.NONE, new BlockPos(0, -1, -1),
                    Rotation.CLOCKWISE_90, new BlockPos(0, -1, -1),
                    Rotation.CLOCKWISE_180, new BlockPos(-1, -1, 0),
                    Rotation.COUNTERCLOCKWISE_90, new BlockPos(-1, -1, 0)
            ))
    );

    @SubscribeEvent
    public static void onWorldLoad(ChunkEvent.Load event) {
        if (event.getChunk() instanceof LevelChunk chunk) {
            if(chunk.getLevel().isClientSide()) return;
            if(chunk.getLevel() instanceof VirtualWorld) return;

            if(chunk.getLevel() instanceof ServerLevel level) {
                Map<ConfiguredStructureFeature<?, ?>, StructureStart> structureStarts = chunk.getAllStarts();

                for (Map.Entry<ConfiguredStructureFeature<?, ?>, StructureStart> entry : structureStarts.entrySet()) {
                    ConfiguredStructureFeature<?, ?> structureFeature = entry.getKey();
                    StructureStart structureStart = entry.getValue();

                    if (structureFeature.feature == ModStructures.VAULT_PORTAL_STRUCTURES.get() || structureFeature.feature == ModStructures.NETHER_VAULT_PORTAL_STRUCTURES.get()) {
                        trackStructurePositions(structureStart, level);
                    }
                }
            }
        }
    }

    private static void trackStructurePositions(StructureStart structureStart, ServerLevel level) {
        for (StructurePiece piece : structureStart.getPieces()) {
            BlockPos pos = piece.getBoundingBox().getCenter();

            if (piece instanceof PoolElementStructurePiece poolPiece) {
                if (poolPiece.getElement() instanceof SinglePoolElement singleElement) {
                    if(singleElement.template.left().isPresent()) {
                        ResourceLocation templateName = singleElement.template.left().get();
                        if(validStructures.contains(templateName)) {
                            Rotation rotation = poolPiece.getRotation();
                            BlockPos portalFramePos = pos.offset(structureRotationOffsets.get(templateName).getSecond().get(rotation));
                            WorldBorder worldBorder = level.getWorldBorder();
                            PortalSavedData savedData = PortalSavedData.get(level);

                            //Config check whether to respect World Border when adding Valid Structures
                            if(ServerConfig.RESPECT_WORLD_BORDER.get()) {
                                if(worldBorder.isWithinBounds(portalFramePos)) {
                                    PortalData portalData = new PortalData(rotation, portalFramePos, structureRotationOffsets.get(templateName).getFirst(), level.dimension(), false);
                                    savedData.addPortalData(portalData);
                                    //size.getBlockPositions(portalFramePos, rotation).forEach(position -> System.out.println("Block at: " + position));
                                }
                            } else {
                                PortalData portalData = new PortalData(rotation, portalFramePos, structureRotationOffsets.get(templateName).getFirst(), level.dimension(), false);
                                savedData.addPortalData(portalData);
                            }

                        }
                    }
                }
            }
        }
    }

}
