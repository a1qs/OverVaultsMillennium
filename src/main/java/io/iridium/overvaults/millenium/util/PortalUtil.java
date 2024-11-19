package io.iridium.overvaults.millenium.util;

import com.mojang.datafixers.util.Pair;
import io.iridium.overvaults.config.ServerConfig;
import io.iridium.overvaults.millenium.world.BlockEntityChunkSavedData;
import io.iridium.overvaults.millenium.world.PortalData;
import io.iridium.overvaults.millenium.world.PortalSavedData;
import iskallia.vault.block.entity.VaultPortalTileEntity;
import iskallia.vault.init.ModBlocks;
import iskallia.vault.item.crystal.CrystalData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.chunk.ChunkStatus;

import java.util.*;

public class PortalUtil {
    public static PortalData getRandomPortalData(List<PortalData> portalDataList) {
        return portalDataList.get(new Random().nextInt(portalDataList.size()));
    }

    /**
     * Activates an OverVault Portal
     *
     * @param level Level used to force-load chunks, fill blocks, get block entities & determine the Crystal Data of the Portal
     * @param data The Portal data which we would like to activate, used to determine size, position & rotation of the Portal
     * @return A List of Vault Portal tile entities that have been filled
     */
    public static List<VaultPortalTileEntity> portalTileActivation(ServerLevel level, PortalData data) {
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

    /**
     * Helper method to get the Closest Portal to the player
     *
     * @param player the Player which the distance gets checked of
     * @return a Pair containing an Integer (index of the Portal) & a Double (the distance between the Portal & the Player)
     */
    public static Pair<Integer, Double> getClosestPortalDataIndexAndDistance(ServerPlayer player) {
        BlockPos playerPos = player.blockPosition();

        List<PortalData> filteredPortalList = filteredPortalList(player.getLevel());
        int closestIndex = -1;
        double closestDistance = Double.MAX_VALUE;

        for (int i = 0; i < filteredPortalList.size(); i++) {
            PortalData data = filteredPortalList.get(i);
            double distance = data.getPortalFrameCenterPos().distSqr(playerPos);

            if (distance < closestDistance) {
                closestDistance = distance;
                closestIndex = i;
            }
        }

        return closestIndex == -1 ? null : new Pair<>(closestIndex, Math.sqrt(closestDistance));
    }

    /**
     * Helper method to get all {@code PortalData} objects in the given Level
     *
     * @param level The {@code Level} to filter for
     * @return List of all the Portals in the given Level
     */
    public static List<PortalData> filteredPortalList(Level level) {
        return PortalSavedData.getServer().getPortalData()
                .stream()
                .filter(p -> p.getDimension().equals(level.dimension()))
                .toList();
    }

    private static void notifyPlayers(MinecraftServer server, PortalData data) {
        server.getPlayerList().getPlayers().forEach(player -> {
            if (ServerConfig.PLAY_SOUND_ON_OPEN.get())
                player.getLevel().playSound(null, player.blockPosition(), SoundEvents.END_PORTAL_SPAWN, SoundSource.MASTER, 1.0f, 1.25f);
        });

        if (ServerConfig.BROADCAST_IN_CHAT.get()) {
            MiscUtil.broadcast(TextUtil.getPortalAppearComponent(data, true));

            if (ServerConfig.UPDATE_VAULT_COMPASS.get())
                MiscUtil.sendCompassInfo(server.getLevel(data.getDimension()), data.getPortalFrameCenterPos());
        }
    }

    public static void activatePortal(MinecraftServer server, PortalData data) {
        ServerLevel portalLevel = server.getLevel(data.getDimension());
        if(portalLevel == null) {
            return;
        }
        BlockEntityChunkSavedData entityChunkData = BlockEntityChunkSavedData.getServer();
        PortalSavedData portalSavedData = PortalSavedData.getServer();
        List<VaultPortalTileEntity> portalTileEntities = PortalUtil.portalTileActivation(portalLevel, data);

        for (VaultPortalTileEntity portalTileEntity : portalTileEntities) {
            entityChunkData.addPortalTileEntity(portalTileEntity.getBlockPos());
        }
        data.setActiveState(true);
        entityChunkData.setDirty();
        portalSavedData.setDirty();

        notifyPlayers(server, data);
    }
}
