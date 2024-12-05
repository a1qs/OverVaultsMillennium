package io.iridium.overvaults.millenium.util;

import com.mojang.datafixers.util.Pair;
import io.iridium.overvaults.OverVaults;
import io.iridium.overvaults.config.VaultConfigRegistry;
import io.iridium.overvaults.millenium.world.BlockEntityChunkSavedData;
import io.iridium.overvaults.millenium.world.PortalData;
import io.iridium.overvaults.millenium.world.PortalSavedData;
import io.iridium.overvaults.millenium.world.StructureSize;
import iskallia.vault.block.entity.VaultPortalTileEntity;
import iskallia.vault.init.ModBlocks;
import iskallia.vault.init.ModConfigs;
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
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
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
     * Helper method to get the Closest Portal to the player.
     *
     * @param player the Player whose distance is being checked.
     * @return a Pair containing the PortalData of the closest Portal & the distance between the Portal and the Player.
     */
    public static Pair<PortalData, Double> getClosestPortalData(ServerPlayer player) {
        BlockPos playerPos = player.blockPosition();

        // Get the filtered list of portals in the player's level
        List<PortalData> filteredPortalList = filteredPortalList(player.getLevel());

        PortalData closestPortal = null;
        double closestDistance = Double.MAX_VALUE;

        // Iterate through the portal list to find the closest one
        for (PortalData data : filteredPortalList) {
            double distance = data.getPortalFrameCenterPos().distSqr(playerPos);

            if (distance < closestDistance) {
                closestDistance = distance;
                closestPortal = data;
            }
        }

        // Return the closest PortalData and the square root of the distance (Euclidean distance)
        return closestPortal == null ? null : new Pair<>(closestPortal, Math.sqrt(closestDistance));
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
            if (VaultConfigRegistry.OVERVAULTS_GENERAL_CONFIG.PLAY_SOUND_ON_OPEN)
                player.getLevel().playSound(null, player.blockPosition(), SoundEvents.END_PORTAL_SPAWN, SoundSource.MASTER, 1.0f, 1.25f);
        });

        if (VaultConfigRegistry.OVERVAULTS_GENERAL_CONFIG.BROADCAST_IN_CHAT) {
            MiscUtil.broadcast(TextUtil.getPortalAppearComponent(data, true));

            if (VaultConfigRegistry.OVERVAULTS_GENERAL_CONFIG.UPDATE_VAULT_COMPASS)
                MiscUtil.sendCompassInfo(server.getLevel(data.getDimension()), data.getPortalFrameCenterPos());
        }
    }

    public static boolean activatePortal(MinecraftServer server, PortalData data) {
        ServerLevel portalLevel = server.getLevel(data.getDimension());
        if(portalLevel == null) {
            OverVaults.LOGGER.error("Attempted to activate an Overvault portal, but the Level of the portal equals null.");
            return false;
        }
        boolean valid = PortalUtil.hasValidFrameBlocks(portalLevel, data);
        if(!valid) {
            OverVaults.LOGGER.error("Attempted to activate Overvault portal, but the given portal had an invalid frame.");
            return false;
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
        return true;
    }

    public static boolean hasValidFrameBlocks(ServerLevel level, PortalData data) {
        Block[] validFrameBlocks = ModConfigs.VAULT_PORTAL.getValidFrameBlocks();

        // Positions to check around the portal center
        BlockPos[] positions;
        if (Objects.requireNonNull(data.getSize()) == StructureSize.SMALL) {
            positions = new BlockPos[]{
                    data.getPortalFrameCenterPos().east(2),
                    data.getPortalFrameCenterPos().west(2),
                    data.getPortalFrameCenterPos().north(2),
                    data.getPortalFrameCenterPos().south(2)
            };
        } else {
            positions = new BlockPos[]{
                    data.getPortalFrameCenterPos().east(3),
                    data.getPortalFrameCenterPos().west(3),
                    data.getPortalFrameCenterPos().north(3),
                    data.getPortalFrameCenterPos().south(3)
            };
        }

        int validCount = 0;
        for (BlockPos pos : positions) {
            BlockState blockState = level.getBlockState(pos);

            // Check if the block matches any of the valid frame blocks
            for (Block validBlock : validFrameBlocks) {
                if (blockState.getBlock() == validBlock) {
                    validCount++;
                    break;
                } else {
                    OverVaults.LOGGER.warn("Expected valid frame block at {} but found {}", pos, blockState.getBlock());
                }
            }
        }


        return validCount >= 2; // Return true if at least 2 valid blocks are found
    }
}
