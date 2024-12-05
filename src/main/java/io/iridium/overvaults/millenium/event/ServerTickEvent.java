package io.iridium.overvaults.millenium.event;

import io.iridium.overvaults.OverVaults;
import io.iridium.overvaults.config.ServerConfig;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerTickEvent {
    private static final Random random = new Random();
    public static int counter = 0; // Counter used to track the time to the next Portal Spawn

    public static int actlTicksForPortalSpawn = -1; // Field to track which time is required for the next Vault Portal Spawn
    private static int actlRemoveModifierTimer = -1; // Field to track which time is required for the next Modifer Portal Removal
    private static int activePortalTickCounter = 0; // Field to track the time an OverVault is active

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

            if (portalDataList != null && !portalDataList.isEmpty()) {
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
                    OverVaults.LOGGER.warn("No valid portals were found to activate.");
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
                AtomicInteger randInt = new AtomicInteger(-1);

                // Pre-select a random modifier stack index for the first portal
                if (!portalTilePositions.isEmpty()) {
                    BlockPos firstPos = portalTilePositions.get(0);
                    VaultPortalTileEntity firstPortalTileEntity = (VaultPortalTileEntity) portalLevel.getBlockEntity(firstPos);

                    if (firstPortalTileEntity != null && firstPortalTileEntity.getData().isPresent()) {
                        List<VaultModifierStack> modifierList = firstPortalTileEntity.getData().get().getModifiers().getList();
                        if (!modifierList.isEmpty()) {
                            randInt.set(random.nextInt(modifierList.size()));
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
                                portalTileEntity.getData().ifPresent(data -> handleModifierRemoval(data, randInt));
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
                    int actStep = removed / ServerConfig.ENTITY_STEP.get(); // Technical step, ignoring max cap, for bosses
                    int step = Math.min(actStep, 4); // used for spawning dwellers

                    EntityType<?> entityType;
                    if (actStep == 5) {
                        String str = ServerConfig.BOSS_ENTITIES.get().get(new Random().nextInt(ServerConfig.BOSS_ENTITIES.get().size()));
                        entityType = EntityType.byString(str).orElse(null); // Spawn boss
                        portalData.setModifiersRemoved(0); // Reset modifiers removed after spawning boss
                    } else {
                        // Spawn fighters for steps 0-4
                        entityType = EntityType.byString("the_vault:vault_fighter_" + step).orElse(null);
                    }

                    if (entityType != null) {
                        Entity e = entityType.create(portalLevel);
                        if (e != null) {
                            e.moveTo(portalTilePositions.get(0).getX() + 0.5, portalTilePositions.get(0).getY(), portalTilePositions.get(0).getZ() + 0.5);

                            if (e instanceof LivingEntity livingEntity) {
                                // Modify health based on step
                                var healthAttribute = livingEntity.getAttribute(Attributes.MAX_HEALTH);
                                if (healthAttribute != null) {
                                    double baseHealth = actStep == 5 ? 150.0 : 12.5 + (step * 7.5);
                                    healthAttribute.setBaseValue(baseHealth);
                                    livingEntity.setHealth((float) baseHealth);
                                }

                                // Modify attack damage based on step
                                var attackAttribute = livingEntity.getAttribute(Attributes.ATTACK_DAMAGE);
                                if (attackAttribute != null) {
                                    double baseDamage = actStep == 5 ? 4.0 : 2.0 + (step * 0.5); // Boss or fighters' damage
                                    attackAttribute.setBaseValue(baseDamage);
                                }
                            }
                            if(e instanceof Mob) ((Mob) e).setPersistenceRequired();
                            portalLevel.addFreshEntity(e);
                        }
                    }
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
    private static void handleModifierRemoval(CrystalData data, AtomicInteger randInt) {
        List<VaultModifierStack> modifierList = data.getModifiers().getList();

        if (!modifierList.isEmpty()) {
            VaultModifierStack modifierStack = modifierList.get(randInt.get());

            if (modifierStack.shrink(1).isEmpty()) {
                modifierList.remove(modifierStack);
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
        int minTicksForPortalSpawn = ServerConfig.MIN_SECONDS_UNTIL_PORTAL_SPAWN.get() * 20;
        int maxTicksForPortalSpawn = ServerConfig.MAX_SECONDS_UNTIL_PORTAL_SPAWN.get() * 20;
        return random.nextInt(maxTicksForPortalSpawn - minTicksForPortalSpawn + 1) + minTicksForPortalSpawn;
    }

    /**
     * Returns a random value for the ticks until modifier removal.
     */
    private static int getRandomRemoveModifierTimer() {
        int minRemoveModifierTimer = ServerConfig.MIN_SECONDS_UNTIL_MODIFIER_REMOVAL.get() * 20;
        int maxRemoveModifierTimer = ServerConfig.MAX_SECONDS_UNTIL_MODIFIER_REMOVAL.get() * 20;
        return random.nextInt(maxRemoveModifierTimer - minRemoveModifierTimer + 1) + minRemoveModifierTimer;
    }


}
