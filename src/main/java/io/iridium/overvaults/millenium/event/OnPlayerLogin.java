package io.iridium.overvaults.millenium.event;

import io.iridium.overvaults.config.ServerConfig;
import io.iridium.overvaults.millenium.util.PortalUtil;
import io.iridium.overvaults.millenium.world.PortalData;
import iskallia.vault.core.world.storage.VirtualWorld;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.server.ServerLifecycleHooks;

public class OnPlayerLogin {
    @SubscribeEvent
    public static void onPlayerLoginEvent(PlayerEvent.PlayerLoggedInEvent event) {
        if(!ServerConfig.BROADCAST_IN_CHAT.get()) return;

        if(event.getPlayer() instanceof ServerPlayer player) {
            PortalData data = PortalUtil.getAllLevelActivePortalData(ServerLifecycleHooks.getCurrentServer());
            if (data == null) return;

            if (data.getDimension() == player.getLevel().dimension()) {
                ServerTickEvent.sendCompassInfo(player.getLevel(), data.getPortalFrameCenterPos());
            }

            MutableComponent cmp = new TextComponent("You sense that a mysterious energy is resonating in " + player.getLevel().dimension().location().getPath() + " at ")
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
    }
}
