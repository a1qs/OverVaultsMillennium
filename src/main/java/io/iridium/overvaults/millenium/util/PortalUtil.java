package io.iridium.overvaults.millenium.util;

import io.iridium.overvaults.millenium.world.BlockEntityChunkSavedData;
import io.iridium.overvaults.millenium.world.PortalData;
import io.iridium.overvaults.millenium.world.PortalSavedData;
import iskallia.vault.block.entity.VaultPortalTileEntity;
import iskallia.vault.init.ModBlocks;
import iskallia.vault.item.crystal.CrystalData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkStatus;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PortalUtil {
    public static PortalData getRandomPortalData(List<PortalData> portalDataList) {
        return portalDataList.get(new Random().nextInt(portalDataList.size()));
    }



    public static List<PortalData> getAllLevelPortalData(@NotNull MinecraftServer server) {
        List<ServerLevel> dimensions = Arrays.asList(
                server.getLevel(Level.OVERWORLD),
                server.getLevel(Level.NETHER),
                server.getLevel(Level.END)
        );

        List<ServerLevel> validDimensions = dimensions.stream()
                .filter(Objects::nonNull)
                .toList();

        List<PortalData> unifiedPortalDataList = new ArrayList<>();

        for (ServerLevel level : validDimensions) {
            List<PortalData> portalDataList = PortalSavedData.get(level).getPortalData();
            if(!portalDataList.isEmpty())
                unifiedPortalDataList.addAll(portalDataList);
        }

        if (unifiedPortalDataList.isEmpty()) {
            return null;
        }

        return unifiedPortalDataList;
    }

    public static PortalData getAllLevelActivePortalData(@NotNull MinecraftServer server) {
        List<PortalData> portalDataList = getAllLevelPortalData(server);
        if(portalDataList == null) return null;

        return portalDataList.stream()
                .filter(PortalData::getActiveState)
                .findFirst()
                .orElse(null);
    }

    /**
     * Activates an OverVault Portal
     *
     * @param level Level used to force-load chunks, fill blocks, get block entities & determine the Crystal Data of the Portal
     * @param data The Portal data which we would like to activate, used to determine size, position & rotation of the Portal
     * @return A List of Vault Portal tile entities that have been filled
     */
    public static List<VaultPortalTileEntity> activatePortal(ServerLevel level, PortalData data) {
        List<VaultPortalTileEntity> vaultPortalTileEntities = new ArrayList<>();
        BlockEntityChunkSavedData entityChunkData = BlockEntityChunkSavedData.get(level);
        Iterable<BlockPos> blocksToFill = data.getSize().getBlockPositions(data.getPortalFrameCenterPos(), data.getRotation());

        for(BlockPos pos : blocksToFill) {
            ChunkPos chunkPos = new ChunkPos(pos);

            if(!entityChunkData.getForceloadedChunks().contains(chunkPos)) {
                level.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.FULL, true);
                entityChunkData.addForceloadedChunk(chunkPos.x, chunkPos.z);

                level.getChunkSource().addRegionTicket(TicketType.FORCED, chunkPos, 1, chunkPos);
            }
        }


        CrystalData originalCrystalData = PortalNbtUtil.getRandomCrystalData(level.dimension());
        blocksToFill.forEach(pos -> {
            level.setBlock(pos, ModBlocks.VAULT_PORTAL.defaultBlockState().rotate(data.getRotation()), 3);
            BlockEntity te = level.getBlockEntity(pos);
            if (te instanceof VaultPortalTileEntity portalTE) {
                CrystalData crystalDataCopy = originalCrystalData.copy();
                portalTE.setCrystalData(crystalDataCopy);
                vaultPortalTileEntities.add(portalTE);
            }
        });

        return vaultPortalTileEntities;
    }
}
