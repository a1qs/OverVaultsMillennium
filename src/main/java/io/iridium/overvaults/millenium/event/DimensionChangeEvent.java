package io.iridium.overvaults.millenium.event;


import io.iridium.overvaults.millenium.util.PortalUtil;
import io.iridium.overvaults.millenium.world.PortalData;
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

        if (event.getPlayer().getLevel() instanceof ServerLevel level) {
            PortalData data = PortalUtil.getAllLevelActivePortalData(ServerLifecycleHooks.getCurrentServer());
            if (data == null) return;
            if (data.getDimension() == event.getTo()) {
                ServerTickEvent.sendCompassInfo(level, data.getPortalFrameCenterPos());
            }
        }
    }
}
