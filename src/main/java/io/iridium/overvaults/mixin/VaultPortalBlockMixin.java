package io.iridium.overvaults.mixin;

import io.iridium.overvaults.config.ServerConfig;
import io.iridium.overvaults.config.VaultConfigRegistry;
import io.iridium.overvaults.millenium.world.BlockEntityChunkSavedData;
import io.iridium.overvaults.millenium.world.PortalData;
import io.iridium.overvaults.millenium.world.PortalSavedData;
import iskallia.vault.block.VaultPortalBlock;
import iskallia.vault.block.entity.VaultPortalTileEntity;
import iskallia.vault.item.crystal.CrystalData;
import iskallia.vault.world.data.PlayerVaultStatsData;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = VaultPortalBlock.class)
public class VaultPortalBlockMixin {
    @Inject(method = "entityInside", at = @At(value = "INVOKE", target = "Liskallia/vault/block/entity/VaultPortalTileEntity;getData()Ljava/util/Optional;", shift = At.Shift.AFTER))
    public void removeOverVaultPortal(BlockState state, Level level, BlockPos pos, Entity entity, CallbackInfo ci) {
        if (level instanceof ServerLevel serverLevel) {
            PortalSavedData savedData = PortalSavedData.get(serverLevel);
            if (savedData.hasActiveOverVault()) {
                BlockEntityChunkSavedData entityChunkData = BlockEntityChunkSavedData.get(serverLevel);
                PortalData data = savedData.getFirstActivePortalData();
                BlockPos portalFrameCenter = data.getPortalFrameCenterPos();
                if (isPortalBlock(pos, portalFrameCenter, data)) {
                    data.setActiveState(false);
                    savedData.setDirty();
                    entityChunkData.removePortalTileEntityData();
                    for (ChunkPos chunkPos : entityChunkData.getForceloadedChunks()) {
                        serverLevel.getChunkSource().removeRegionTicket(TicketType.FORCED, chunkPos, 1, chunkPos);
                        //entityChunkData.removeChunkPositionData();
                    }
                    entityChunkData.removeChunkPositionData();
                }
            }
        }
    }


    @Inject(method = "entityInside", at = @At(value = "INVOKE", target = "Liskallia/vault/block/entity/VaultPortalTileEntity;getData()Ljava/util/Optional;"))
    public void setPortalToPlayerLevel(BlockState state, Level level, BlockPos pos, Entity entity, CallbackInfo ci) {
        if (level instanceof ServerLevel serverLevel) {
            PortalSavedData savedData = PortalSavedData.get(serverLevel);
            if (savedData.hasActiveOverVault()) {
                PortalData data = savedData.getFirstActivePortalData();
                BlockPos portalFrameCenter = data.getPortalFrameCenterPos();
                if (isPortalBlock(pos, portalFrameCenter, data)) {
                    BlockEntity te = level.getBlockEntity(pos);
                    VaultPortalTileEntity portal = te instanceof VaultPortalTileEntity ? (VaultPortalTileEntity)te : null;
                    if(portal != null && portal.getData().isPresent()) {
                        CrystalData crystalData = portal.getData().get();
                        int vaultLevel;
                        if (entity instanceof Player player && VaultConfigRegistry.OVERVAULTS_GENERAL_CONFIG.SET_LEVEL_OF_ENTERING_PLAYER_OVERVAULT) {
                            vaultLevel = PlayerVaultStatsData.get((ServerLevel)player.level).getVaultStats(player).getVaultLevel();
                        } else {
                            vaultLevel = 0;

                        }


                        crystalData.getProperties().setLevel(vaultLevel);
                    }
                }
            }
        }
    }

    @Unique
    // Utility method to check if the BlockPos is part of a valid Portal object
    private boolean isPortalBlock(BlockPos pos, BlockPos portalCenter, PortalData data) {
        Iterable<BlockPos> portalBlocks = data.getSize().getBlockPositions(portalCenter, data.getRotation());
        for (BlockPos blockPos : portalBlocks) {
            if (pos.equals(blockPos)) {
                return true;
            }
        }
        return false;
    }
}
