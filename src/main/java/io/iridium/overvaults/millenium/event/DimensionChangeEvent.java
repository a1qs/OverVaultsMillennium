package io.iridium.overvaults.millenium.event;

import io.iridium.overvaults.config.VaultConfigRegistry;
import io.iridium.overvaults.millenium.util.MiscUtil;
import io.iridium.overvaults.millenium.world.PortalData;
import io.iridium.overvaults.millenium.world.PortalSavedData;
import iskallia.vault.init.ModNetwork;
import iskallia.vault.network.message.VaultMessage;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.server.ServerLifecycleHooks;

public class DimensionChangeEvent {

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getPlayer() == null) return;
        if (ServerLifecycleHooks.getCurrentServer() == null) return;
        if (!VaultConfigRegistry.OVERVAULTS_GENERAL_CONFIG.UPDATE_VAULT_COMPASS) return;

        if (event.getPlayer().getLevel() instanceof ServerLevel level) {
            ServerPlayer player = (ServerPlayer) event.getPlayer();
            PortalData data = PortalSavedData.getServer().getFirstActivePortalData();
            if (data == null) return;
            if (data.getDimension() == event.getTo()) {
                MiscUtil.sendCompassInfo(level, data.getPortalFrameCenterPos());
            } else {
                ModNetwork.CHANNEL.sendTo(new VaultMessage.Unload(MiscUtil.OVERVAULT_COMPASS_V), player.connection.connection, NetworkDirection.PLAY_TO_CLIENT);
            }
        }
    }
}
