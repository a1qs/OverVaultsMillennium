package io.iridium.overvaults.millenium.util;

import io.iridium.overvaults.millenium.world.PortalData;
import io.iridium.overvaults.millenium.world.PortalSavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class PortalUtil {
    public static PortalData getRandomPortalData(List<PortalData> portalDataList) {
        return portalDataList.get(new Random().nextInt(portalDataList.size()));
    }

    public static MutableComponent obfuscateLastTwoDigits(int number) {
        // Convert the number to a string
        String numberStr = Integer.toString(number);
        char[] charArray = numberStr.toCharArray();
        if(numberStr.length() == 1) {
            return new TextComponent(numberStr).withStyle(ChatFormatting.OBFUSCATED);
        }
        // Check the length of the string
        char lastDigit = charArray[charArray.length-1];
        char secondToLastDigit = charArray[charArray.length-2];

        String prefix = numberStr.substring(0, numberStr.length() - 2);


        MutableComponent obfuscatedPart = new TextComponent("" + secondToLastDigit + lastDigit).withStyle(ChatFormatting.OBFUSCATED);

        return new TextComponent(prefix).append(obfuscatedPart);
    }

    public static List<PortalData> getAllLevelPortalData(@NotNull MinecraftServer server) {
        List<ServerLevel> dimensions = Arrays.asList(
                server.getLevel(Level.OVERWORLD),
                server.getLevel(Level.NETHER),
                server.getLevel(Level.END)
        );

        List<ServerLevel> validDimensions = dimensions.stream()
                .filter(Objects::nonNull)
                .toList();

        List<PortalData> unifiedPortalDataList = new ArrayList<>();

        for (ServerLevel level : validDimensions) {
            List<PortalData> portalDataList = PortalSavedData.get(level).getPortalData();
            if(!portalDataList.isEmpty())
                unifiedPortalDataList.addAll(portalDataList);
        }

        if (unifiedPortalDataList.isEmpty()) {
            return null;
        }

        return unifiedPortalDataList;
    }

    public static PortalData getAllLevelActivePortalData(@NotNull MinecraftServer server) {
        List<PortalData> portalDataList = getAllLevelPortalData(server);
        if(portalDataList == null) return null;

        return portalDataList.stream()
                .filter(PortalData::getActiveState)
                .findFirst()
                .orElse(null);
    }
}
