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
import iskallia.vault.item.crystal.CrystalData;
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

                Random random = new Random();
                int randInt = -1;

                if(portalLevel == null) {
                    OverVaults.LOGGER.error("OverVault Portal Level is null!");
                    return;
                }

                // Pre-select a random modifier stack index for the first portal
                if (!portalTilePositions.isEmpty()) {
                    BlockPos firstPos = portalTilePositions.get(0);
                    VaultPortalTileEntity firstPortalTileEntity = (VaultPortalTileEntity) portalLevel.getBlockEntity(firstPos);

                    if (firstPortalTileEntity != null && firstPortalTileEntity.getData().isPresent()) {
                        List<VaultModifierStack> modifierList = firstPortalTileEntity.getData().get().getModifiers().getList();
                        if (!modifierList.isEmpty()) {
                            randInt = random.nextInt(modifierList.size());

                            hasModified = true;
                            if(portalSavedData.getFirstActivePortalData().getModifiersRemoved() == -1) {
                                portalSavedData.getFirstActivePortalData().setModifiersRemoved(0);
                            } else {
                                portalSavedData.getFirstActivePortalData().addModifiersRemoved(1);
                            }
                        }
                    }
                }

                // Get the iterator for the portalTilePositions list
                Iterator<BlockPos> iterator = portalTilePositions.iterator();

                while (iterator.hasNext()) {
                    BlockPos pos = iterator.next();


                    if (portalLevel.isLoaded(pos)) {  // Only process if chunk is loaded
                        BlockState blockState = portalLevel.getBlockState(pos);

                        // Check if the block is still a portal block before accessing the tile entity
                        if (blockState.is(ModBlocks.VAULT_PORTAL)) {
                            VaultPortalTileEntity portalTileEntity = (VaultPortalTileEntity) portalLevel.getBlockEntity(pos);
                            if (portalTileEntity != null && portalTileEntity.getData().isPresent()) {
                                handleModifierRemoval(portalTileEntity.getData().get(), randInt);
                            }
                        } else {
                            OverVaults.LOGGER.error("Activated portal was invalidated. Removing.");
                            // Remove the portal tile entity from the data and the chunk position
                            iterator.remove();  // Use the iterator to safely remove the current element
                            entityChunkData.removePortalTileEntityData();
                            entityChunkData.removeChunkPositionData();
                            portalData.setActiveState(false);
                            portalSavedData.setDirty();
                        }
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
     * Handles the removal of modifiers of Vault Portals
     *
     * @param data The CrystalData used to get the list of modifiers of the portal
     * @param randInt The random value used to remove a modifier from the modifierList via index
     */
    private static void handleModifierRemoval(CrystalData data, int randInt) {
        List<VaultModifierStack> modifierList = data.getModifiers().getList();

        if (!modifierList.isEmpty()) {
            if (randInt >= 0 && randInt < modifierList.size()) {
                VaultModifierStack modifierStack = modifierList.get(randInt);
                if (modifierStack.shrink(1).isEmpty()) {
                    modifierList.remove(modifierStack);
                }
            } else {
                OverVaults.LOGGER.error("Random index '{}' is out of bounds for the modifier list size '{}'.", randInt, modifierList.size());
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
        return VaultConfigRegistry.OVERVAULTS_GENERAL_CONFIG.SECONDS_UNTIL_PORTAL_SPAWN.getRandom();
    }

    /**
     * Returns a random value for the ticks until modifier removal.
     */
    private static int getRandomRemoveModifierTimer() {
        return VaultConfigRegistry.OVERVAULTS_GENERAL_CONFIG.SECONDS_UNTIL_MODIFIER_REMOVAL.getRandom();
    }


}
