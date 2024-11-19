package io.iridium.overvaults.millenium.event;

import io.iridium.overvaults.config.ServerConfig;
import io.iridium.overvaults.millenium.util.MiscUtil;
import io.iridium.overvaults.millenium.util.TextUtil;
import io.iridium.overvaults.millenium.world.PortalData;
import io.iridium.overvaults.millenium.world.PortalSavedData;
import net.minecraft.Util;
import net.minecraft.network.chat.ChatType;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;


public class OnPlayerLogin {
    @SubscribeEvent
    public static void onPlayerLoginEvent(PlayerEvent.PlayerLoggedInEvent event) {
        if(!ServerConfig.BROADCAST_IN_CHAT.get()) return;

        if(event.getPlayer() instanceof ServerPlayer player) {
            PortalData data = PortalSavedData.getServer().getFirstActivePortalData();
            if (data == null) return;

            if (data.getDimension() == player.getLevel().dimension()) {
                MiscUtil.sendCompassInfo(player.getLevel(), data.getPortalFrameCenterPos());
            }

            player.sendMessage(TextUtil.loginComponent(data), ChatType.SYSTEM, Util.NIL_UUID);
        }
    }
}
