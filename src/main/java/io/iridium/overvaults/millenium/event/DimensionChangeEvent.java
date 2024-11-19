package io.iridium.overvaults.millenium.event;


import io.iridium.overvaults.config.ServerConfig;
import io.iridium.overvaults.millenium.util.MiscUtil;
import io.iridium.overvaults.millenium.util.PortalUtil;
import io.iridium.overvaults.millenium.world.PortalData;
import io.iridium.overvaults.millenium.world.PortalSavedData;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

public class DimensionChangeEvent {

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onDimensionChange(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (event.getPlayer() == null) return;
        if (ServerLifecycleHooks.getCurrentServer() == null) return;
        if (!ServerConfig.UPDATE_VAULT_COMPASS.get()) return;

        if (event.getPlayer().getLevel() instanceof ServerLevel level) {
            PortalData data = PortalSavedData.getServer().getFirstActivePortalData();
            if (data == null) return;
            if (data.getDimension() == event.getTo()) {
                MiscUtil.sendCompassInfo(level, data.getPortalFrameCenterPos());
            }
        }
    }
}
