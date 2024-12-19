package io.iridium.overvaults.millenium.event;

import io.iridium.overvaults.OverVaults;
import io.iridium.overvaults.config.VaultConfigRegistry;
import io.iridium.overvaults.millenium.util.PortalUtil;
import io.iridium.overvaults.millenium.world.BlockEntityChunkSavedData;
import io.iridium.overvaults.millenium.world.PortalData;
import io.iridium.overvaults.millenium.world.PortalSavedData;
import iskallia.vault.block.entity.VaultPortalTileEntity;
import iskallia.vault.core.vault.modifier.VaultModifierStack;
import iskallia.vault.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.*;

public class ServerTickEvent {
    public static int counter = 0; // Counter used to track the time to the next Portal Spawn

    public static int actlTicksForPortalSpawn = -1; // Field to track which time is required for the next Vault Portal Spawn
    public static int activePortalTickCounter = 0; // Field to track the time an OverVault is active
    public static int actlRemoveModifierTimer = -1; // Field to track which time is required for the next Modifer Portal Removal

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;
        ServerLevel level = server.getLevel(Level.OVERWORLD);
        if(level == null) return;

        PortalSavedData portalSavedData = PortalSavedData.get(level);
        if(actlTicksForPortalSpawn == -1) actlTicksForPortalSpawn = getRandomTicksForPortalSpawn();

        // Check if the counter has reached the limit for portal spawning
        if (shouldSpawnPortal(actlTicksForPortalSpawn)) {
            List<PortalData> portalDataList = new ArrayList<>(portalSavedData.getPortalData());
            Collections.shuffle(portalDataList);

            if (!portalDataList.isEmpty()) {
                boolean portalActivated = false;

                for (PortalData data : portalDataList) {
                    ServerLevel portalLevel = server.getLevel(data.getDimension());
                    if (portalLevel == null) {
                        OverVaults.LOGGER.error("Level {} equals null. Please report this.", data.getDimension());
                        continue; // Skip this portal
                    }

                    if(VaultConfigRegistry.OVERVAULTS_GENERAL_CONFIG.RESPECT_WORLD_BORDER) {
                        WorldBorder worldBorder = level.getWorldBorder();
                        if(!worldBorder.isWithinBounds(data.getPortalFrameCenterPos())) {
                            continue; // If the border is not within the portal bounds; skip this portal
                        }
                    }

                    if (PortalUtil.activatePortal(server, data)) {
                        actlTicksForPortalSpawn = getRandomTicksForPortalSpawn();
                        counter = 0;
                        portalActivated = true;
                        break; // Successfully activated a portal, no need to check others
                    }
                }

                if (!portalActivated) {
                    OverVaults.LOGGER.warn("No valid portals were found to activate. Reset Counter");
                    counter = 0;
                }
            }
        }


        // Increment the counter only if there's no active portal across all dimensions
        if (!PortalSavedData.get(level).hasActiveOverVault() && counter < Integer.MAX_VALUE) {
            counter++;
        }

        if (portalSavedData.hasActiveOverVault()) {
            activePortalTickCounter++;

            if(actlRemoveModifierTimer == -1) actlRemoveModifierTimer = getRandomRemoveModifierTimer();
            if (shouldModifyPortal(actlRemoveModifierTimer)) {
                PortalData portalData = portalSavedData.getFirstActivePortalData();
                ServerLevel portalLevel = server.getLevel(portalData.getDimension());
                BlockEntityChunkSavedData entityChunkData = BlockEntityChunkSavedData.get(level);
                List<BlockPos> portalTilePositions = entityChunkData.getPortalTilePositions();
                boolean hasModified = false;

                if(portalLevel == null) {
                    OverVaults.LOGGER.error("OverVault Portal Level is null!");
                    return;
                }

                List<VaultModifierStack> sharedModifierList = null;

                for (int i = 0; i < portalTilePositions.size(); i++) {
                    BlockPos pos = portalTilePositions.get(i);

                    if (!portalLevel.hasChunkAt(pos)) {
                        OverVaults.LOGGER.warn("Chunk containing position {} is not loaded, attempting to load...", pos);
                        portalLevel.getChunkSource().getChunk(pos.getX() >> 4, pos.getZ() >> 4, true); // Load the chunk if not loaded
                    }

                    if (!portalLevel.isLoaded(pos)) {
                        OverVaults.LOGGER.warn("Position {} is not loaded even after ensuring chunk load, skipping...", pos);
                        continue;
                    }

                    BlockState state = portalLevel.getBlockState(pos);

                    if(!state.is(ModBlocks.VAULT_PORTAL)) {
                        OverVaults.LOGGER.error("Activated portal was invalidated. Removing.");
                        // Remove the portal tile entity from the data and the chunk position
                        portalTilePositions.remove(i--);
                        entityChunkData.removePortalTileEntityData();
                        entityChunkData.removeChunkPositionData();
                        portalData.setActiveState(false);
                        portalData.setModifiersRemoved(-1);
                        portalSavedData.setDirty();
                    }

                    VaultPortalTileEntity portalTileEntity = (VaultPortalTileEntity) portalLevel.getBlockEntity(pos);
                    if(portalTileEntity == null || portalTileEntity.getData().isEmpty()) {
                        OverVaults.LOGGER.warn("Portal BlockEntity doesnt exist or data is empty, skipping");
                        continue;
                    }

                    List<VaultModifierStack> modifierList = portalTileEntity.getData().get().getModifiers().getList();

                    if (i == 0) {
                        // First portal tile entity: shuffle and modify the list
                        if (!modifierList.isEmpty()) {
                            Collections.shuffle(modifierList);
                            if (modifierList.get(0).shrink(1).isEmpty()) {
                                modifierList.remove(0);
                            }
                            sharedModifierList = new ArrayList<>(modifierList); // Clone the modified list
                            hasModified = true;

                            if (portalSavedData.getFirstActivePortalData().getModifiersRemoved() == -1) {
                                portalSavedData.getFirstActivePortalData().setModifiersRemoved(0);
                            } else {
                                portalSavedData.getFirstActivePortalData().addModifiersRemoved(1);
                            }
                        }
                    } else if (sharedModifierList != null) {
                        // Subsequent portal tile entities: apply the shared list
                        modifierList.clear();
                        modifierList.addAll(sharedModifierList);
                    }
                }

                if(hasModified && VaultConfigRegistry.OVERVAULTS_GENERAL_CONFIG.SPAWN_ENTITY_MODIFIER_REMOVAL) {
                    int removed = portalData.getModifiersRemoved();
                    int step = removed / VaultConfigRegistry.OVERVAULTS_MOB_CONFIG.MODIFIERS_TO_REMOVE_UNTIL_TIER_UP; // Technical step, ignoring max cap, for bosses
                    VaultConfigRegistry.OVERVAULTS_MOB_CONFIG.addPortalEntityToWorld(portalLevel, step, portalData);
                }

                activePortalTickCounter = 0; // Reset the counter after execution
                actlRemoveModifierTimer = getRandomRemoveModifierTimer(); //reset the random counter thingy yadda yadda
            }
        }
    }


    /**
     * Helper method to determine if a portal should spawn
     * @return true/false whether the portal should spawn
     */
    private static boolean shouldSpawnPortal(int ticksForPortalSpawn) {
        return counter >= ticksForPortalSpawn;
    }

    /**
     * Helper method to determine if a portal should be modified
     * @return true/false whether the portal should be modified
     */
    private static boolean shouldModifyPortal(int removeModifierTimer) {
        return activePortalTickCounter >= removeModifierTimer;
    }

    /**
     * Returns a random value for the ticks until portal spawn.
     */
    private static int getRandomTicksForPortalSpawn() {
        return VaultConfigRegistry.OVERVAULTS_GENERAL_CONFIG.SECONDS_UNTIL_PORTAL_SPAWN.getRandom() * 20;
    }

    /**
     * Returns a random value for the ticks until modifier removal.
     */
    private static int getRandomRemoveModifierTimer() {
        return VaultConfigRegistry.OVERVAULTS_GENERAL_CONFIG.SECONDS_UNTIL_MODIFIER_REMOVAL.getRandom() * 20;
    }


}
