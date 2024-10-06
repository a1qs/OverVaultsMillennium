package io.iridium.overvaults.millenium.event;

import io.iridium.overvaults.OverVaults;
import io.iridium.overvaults.millenium.ServerConfig;
import io.iridium.overvaults.millenium.util.PortalNbtUtil;
import io.iridium.overvaults.millenium.util.PortalUtil;
import io.iridium.overvaults.millenium.world.BlockEntityChunkSavedData;
import io.iridium.overvaults.millenium.world.PortalData;
import io.iridium.overvaults.millenium.world.PortalSavedData;
import iskallia.vault.block.entity.VaultPortalTileEntity;
import iskallia.vault.core.Version;
import iskallia.vault.core.data.compound.IdentifierList;
import iskallia.vault.core.data.sync.SyncMode;
import iskallia.vault.core.vault.*;
import iskallia.vault.core.vault.modifier.VaultModifierStack;
import iskallia.vault.core.world.storage.VirtualWorld;
import iskallia.vault.init.ModBlocks;
import iskallia.vault.init.ModNetwork;
import iskallia.vault.item.crystal.CrystalData;
import iskallia.vault.network.message.VaultMessage;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class ServerTickEvent {
    public static int counter = 0;
    public static int ticksForPortalSpawn = ServerConfig.SECONDS_UNTIL_PORTAL_SPAWN.get() * 20;
    private static int activePortalTickCounter = 0;
    private static int removeModifierTimer = ServerConfig.SECONDS_UNTIL_MODIFIER_REMOVAL.get() * 20;

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) return;

        List<ServerLevel> dimensions = Arrays.asList(
                server.getLevel(Level.OVERWORLD),
                server.getLevel(Level.NETHER),
                server.getLevel(Level.END)
        );

        List<ServerLevel> validDimensions = dimensions.stream()
                .filter(Objects::nonNull)
                .toList();


        // Check if the counter has reached the limit for portal spawning
        if (shouldSpawnPortal()) {
            if (!validDimensions.isEmpty()) {
                boolean foundPortal = false;

                // Try each dimension until a valid portal is found
                for (ServerLevel level : validDimensions) {
                    PortalSavedData portalSavedData = PortalSavedData.get(level);
                    List<PortalData> portalDataList = portalSavedData.getPortalData();

                    if (portalDataList != null && !portalDataList.isEmpty()) {
                        foundPortal = true;

                        PortalData data = PortalUtil.getRandomPortalData(portalDataList);

                        server.getPlayerList().getPlayers().forEach(player -> {
                            if(ServerConfig.INFORM_PLAYERS_IN_VAULTS.get()) {
                                if (player.getLevel() instanceof VirtualWorld) return; // Skip players inside a vault
                            }

                            if(ServerConfig.BROADCAST_IN_CHAT.get()) {
                                MutableComponent cmp = new TextComponent("A mysterious energy has appeared in " + level.dimension().location().getPath() + " at ")
                                        .withStyle(ChatFormatting.DARK_PURPLE)
                                        .append(PortalUtil.obfuscateLastTwoDigits(data.getPortalFrameCenterPos().getX()))
                                        .append(", ")
                                        .append(PortalUtil.obfuscateLastTwoDigits(data.getPortalFrameCenterPos().getY()))
                                        .append(", ")
                                        .append(PortalUtil.obfuscateLastTwoDigits(data.getPortalFrameCenterPos().getZ()))
                                        .withStyle(ChatFormatting.RESET)
                                        .withStyle(ChatFormatting.LIGHT_PURPLE);


                                player.sendMessage(cmp, ChatType.SYSTEM, Util.NIL_UUID);
                            }

                            if(ServerConfig.PLAY_SOUND_ON_OPEN.get()) {
                                player.getLevel().playSound(null, player.blockPosition(), SoundEvents.END_PORTAL_SPAWN, SoundSource.MASTER, 1.0f, 1.25f);
                            }
                        });


                        List<VaultPortalTileEntity> portalTileEntities = activatePortal(level, data);

                        BlockEntityChunkSavedData entityChunkData = BlockEntityChunkSavedData.get(level);
                        for (VaultPortalTileEntity portalTileEntity : portalTileEntities) {
                            entityChunkData.addPortalTileEntity(portalTileEntity.getBlockPos());
                        }
                        entityChunkData.setDirty();

                        data.setActiveState(true);
                        if(ServerConfig.UPDATE_VAULT_COMPASS.get()) sendCompassInfo(level, data.getPortalFrameCenterPos());
                        portalSavedData.setDirty();

                        counter = 0;
                        break;
                    }
                }

                if (!foundPortal) {
                    OverVaults.LOGGER.error("Could not find a valid OverVaults structure to open a Vault Portal in any dimension.");
                    counter = 0;
                }

            } else {
                OverVaults.LOGGER.error("No valid dimensions found for spawning a portal.");
                counter = 0;
            }
        }

        // Increment the counter only if there's no active portal across all dimensions
        if (validDimensions.stream().noneMatch(level -> PortalSavedData.get(level).hasActiveOverVault()) && counter < Integer.MAX_VALUE) {
            counter++;
        }

        validDimensions.forEach(level -> {
            PortalSavedData portalSavedData = PortalSavedData.get(level);
            if (portalSavedData.hasActiveOverVault()) {
                activePortalTickCounter++;

                if (shouldModifyPortal()) {
                    BlockEntityChunkSavedData entityChunkData = BlockEntityChunkSavedData.get(level);
                    List<BlockPos> portalTilePositions = entityChunkData.getPortalTilePositions();

                    Random random = new Random();
                    AtomicInteger randInt = new AtomicInteger(-1);

                    // Pre-select a random modifier stack index for the first portal
                    if (!portalTilePositions.isEmpty()) {
                        BlockPos firstPos = portalTilePositions.get(0);
                        VaultPortalTileEntity firstPortalTileEntity = (VaultPortalTileEntity) level.getBlockEntity(firstPos);

                        if (firstPortalTileEntity != null && firstPortalTileEntity.getData().isPresent()) {
                            List<VaultModifierStack> modifierList = firstPortalTileEntity.getData().get().getModifiers().getList();
                            if (!modifierList.isEmpty()) {
                                randInt.set(random.nextInt(modifierList.size()));
                            }
                        }
                    }

                    // Get the iterator for the portalTilePositions list
                    Iterator<BlockPos> iterator = portalTilePositions.iterator();

                    while (iterator.hasNext()) {
                        BlockPos pos = iterator.next();

                        if (level.isLoaded(pos)) {  // Only process if chunk is loaded
                            BlockState blockState = level.getBlockState(pos);

                            // Check if the block is still a portal block before accessing the tile entity
                            if (blockState.is(ModBlocks.VAULT_PORTAL)) {
                                VaultPortalTileEntity portalTileEntity = (VaultPortalTileEntity) level.getBlockEntity(pos);
                                if (portalTileEntity != null && portalTileEntity.getData().isPresent()) {
                                    portalTileEntity.getData().ifPresent(data -> handleModifierRemoval(data, randInt));
                                }
                            } else {
                                // Remove the portal tile entity from the data and the chunk position
                                iterator.remove();  // Use the iterator to safely remove the current element
                                entityChunkData.removePortalTileEntityData();
                                entityChunkData.removeChunkPositionData();

                                // Check for active portal data and update accordingly
                                if (PortalUtil.getAllLevelActivePortalData(server) != null) {
                                    Objects.requireNonNull(PortalUtil.getAllLevelActivePortalData(server)).setActiveState(false);
                                    portalSavedData.setDirty();
                                }
                            }
                        }
                    }

                    activePortalTickCounter = 0; // Reset the counter after execution
                }
            }
        });

    }

    /**
     * Method to inform clients in the same Level as the Overvault about it being opened, sending Vault Compass info
     *
     * @param level Used to send the VaultMessage.Sync to each player in the Level
     * @param pos Position where the Vault Compass will point to
     */
    public static void sendCompassInfo(ServerLevel level, BlockPos pos) {
        level.getPlayers(serverPlayer -> true).forEach(serverPlayer -> {
            Vault vault = new Vault();
            WorldManager worldManager = new WorldManager();
            ClassicPortalLogic portalLogic = new ClassicPortalLogic();
            iskallia.vault.core.vault.PortalData.List portalDataList = new iskallia.vault.core.vault.PortalData.List();
            iskallia.vault.core.vault.PortalData portal = new iskallia.vault.core.vault.PortalData();
            IdentifierList entranceIdentifier = IdentifierList.create();
            entranceIdentifier.add(ClassicPortalLogic.ENTRANCE);
            portal.set(iskallia.vault.core.vault.PortalData.TAGS,entranceIdentifier);
            portal.set(iskallia.vault.core.vault.PortalData.MIN, pos.relative(Direction.NORTH,-5));
            portal.set(iskallia.vault.core.vault.PortalData.MAX, pos.relative(Direction.NORTH,-5));
            portalDataList.add(portal);
            portalLogic.set(PortalLogic.DATA, portalDataList);
            worldManager.set(WorldManager.PORTAL_LOGIC, portalLogic);
            worldManager.set(WorldManager.FACING, Direction.NORTH);
            vault.set(Vault.WORLD, worldManager);
            vault.set(Vault.VERSION, Version.latest());
            vault.set(Vault.MODIFIERS, new Modifiers());
            ModNetwork.CHANNEL.sendTo(new VaultMessage.Sync(serverPlayer, vault, SyncMode.FULL), serverPlayer.connection.getConnection(), NetworkDirection.PLAY_TO_CLIENT);
        });
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
     * Activates an OverVault Portal
     *
     * @param level Level used to force-load chunks, fill blocks, get block entities & determine the Crystal Data of the Portal
     * @param data The Portal data which we would like to activate, used to determine size, position & rotation of the Portal
     * @return A List of Vault Portal tile entities that have been filled
     */
    private static List<VaultPortalTileEntity> activatePortal(ServerLevel level, PortalData data) {
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
     * Helper method to determine if a portal should spawn
     * @return true/false whether the portal should spawn
     */
    private static boolean shouldSpawnPortal() {
        return counter >= ticksForPortalSpawn;
    }

    /**
     * Helper method to determine if a portal should be modified
     * @return true/false whether the portal should be modified
     */
    private static boolean shouldModifyPortal() {
        return activePortalTickCounter >= removeModifierTimer;
    }
}
