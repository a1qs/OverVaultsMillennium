package io.iridium.overvaults.millenium.util;

import iskallia.vault.core.Version;
import iskallia.vault.core.data.compound.IdentifierList;
import iskallia.vault.core.data.sync.SyncMode;
import iskallia.vault.core.vault.*;
import iskallia.vault.init.ModNetwork;
import iskallia.vault.network.message.VaultMessage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.network.NetworkDirection;

public class MiscUtil {
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

    public static int[] convertTime(int totalSeconds) {
        int hours = totalSeconds / 3600;
        int minutes = (totalSeconds % 3600) / 60;
        int seconds = totalSeconds % 60;
        return new int[]{hours, minutes, seconds};
    }

    public static String formatTime(int[] time) {
        return String.format("%02d:%02d:%02d", time[0], time[1], time[2]);
    }
}
