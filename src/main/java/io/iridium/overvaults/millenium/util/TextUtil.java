package io.iridium.overvaults.millenium.util;

import io.iridium.overvaults.OverVaults;
import io.iridium.overvaults.millenium.world.PortalData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.*;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class TextUtil {
    public static MutableComponent getPortalComponent(int index, PortalData data) {
        return new TextComponent("=== ")
                .append(new TextComponent("Portal data of index " + index).withStyle(ChatFormatting.AQUA))
                .append(new TextComponent(" ===\n").withStyle(ChatFormatting.WHITE))
                .append(new TextComponent("    Portal Frame Center Position: ").withStyle(ChatFormatting.LIGHT_PURPLE))
                .append(new TextComponent(data.getPortalFrameCenterPos() + "\n").withStyle(ChatFormatting.YELLOW))
                .append(new TextComponent("    Portal size: ").withStyle(ChatFormatting.LIGHT_PURPLE))
                .append(new TextComponent(data.getSize() + "\n").withStyle(ChatFormatting.YELLOW))
                .append(new TextComponent("    Portal Rotation: ").withStyle(ChatFormatting.LIGHT_PURPLE))
                .append(new TextComponent(data.getRotation() + "\n").withStyle(ChatFormatting.YELLOW))
                .append(new TextComponent("    Portal Activation State: ").withStyle(ChatFormatting.LIGHT_PURPLE))
                .append(new TextComponent(data.getActiveState() + "\n").withStyle(ChatFormatting.YELLOW))
                .append(new TextComponent("    Portal Dimension: ").withStyle(ChatFormatting.LIGHT_PURPLE))
                .append(new TextComponent(data.getDimension().location().getPath() + "\n").withStyle(ChatFormatting.YELLOW));
    }

    public static MutableComponent getPortalTpComponent(int index, PortalData data) {
        BlockPos offsetPosition =  data.getPortalFrameCenterPos().offset(3.0, 0.0, 3.0);
        String tpCommand = "    /execute as @s in " + data.getDimension().location() + " run tp " + offsetPosition.getX() + " " + offsetPosition.getY() + " " + offsetPosition.getZ();
        MutableComponent tpComponent = new TextComponent(tpCommand).withStyle(ChatFormatting.AQUA);
        tpComponent.withStyle((style) -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent("Click to teleport!"))).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, tpCommand)));

        return TextUtil.getPortalComponent(index, data).append(tpComponent);
    }

    public static MutableComponent getPortalAppearComponent(PortalData data, boolean obfuscation) {
        if (obfuscation) {
            return new TextComponent("A mysterious energy has appeared in ").withStyle(ChatFormatting.LIGHT_PURPLE)
                    .append(dimensionComponent(data.getDimension()))
                    .append(new TextComponent(" at ").withStyle(ChatFormatting.DARK_PURPLE))
                    .append(obfuscateLastTwoDigits(data.getPortalFrameCenterPos().getX()).withStyle(ChatFormatting.LIGHT_PURPLE))
                    .append(new TextComponent(", ").withStyle(ChatFormatting.LIGHT_PURPLE))
                    .append(obfuscateLastTwoDigits(data.getPortalFrameCenterPos().getY()).withStyle(ChatFormatting.LIGHT_PURPLE))
                    .append(new TextComponent(", ").withStyle(ChatFormatting.LIGHT_PURPLE))
                    .append(obfuscateLastTwoDigits(data.getPortalFrameCenterPos().getZ()).withStyle(ChatFormatting.LIGHT_PURPLE));
        } else {
            return new TextComponent("A mysterious energy has appeared in ").withStyle(ChatFormatting.LIGHT_PURPLE)
                    .append(dimensionComponent(data.getDimension()))
                    .append(new TextComponent(" at ").withStyle(ChatFormatting.DARK_PURPLE))
                    .append(new TextComponent("" + data.getPortalFrameCenterPos().getX()).withStyle(ChatFormatting.LIGHT_PURPLE))
                    .append(new TextComponent(", ").withStyle(ChatFormatting.LIGHT_PURPLE))
                    .append(new TextComponent("" + data.getPortalFrameCenterPos().getY()).withStyle(ChatFormatting.LIGHT_PURPLE))
                    .append(new TextComponent(", ").withStyle(ChatFormatting.LIGHT_PURPLE))
                    .append(new TextComponent("" + data.getPortalFrameCenterPos().getZ()).withStyle(ChatFormatting.LIGHT_PURPLE));
        }
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

    public static MutableComponent loginComponent(PortalData data) {
        return new TextComponent("You sense that a mysterious energy is resonating in ").withStyle(ChatFormatting.LIGHT_PURPLE)
                .append(dimensionComponent(data.getDimension()))
                .append(new TextComponent( " at ").withStyle(ChatFormatting.LIGHT_PURPLE))
                .append(TextUtil.obfuscateLastTwoDigits(data.getPortalFrameCenterPos().getX()))
                .append(", ")
                .append(TextUtil.obfuscateLastTwoDigits(data.getPortalFrameCenterPos().getY()))
                .append(", ")
                .append(TextUtil.obfuscateLastTwoDigits(data.getPortalFrameCenterPos().getZ()))
                .withStyle(ChatFormatting.RESET)
                .withStyle(ChatFormatting.LIGHT_PURPLE);
    }

    private static MutableComponent dimensionComponent(ResourceKey<Level> dimension) {
        if(dimension.equals(Level.OVERWORLD)) {
            return new TextComponent("the Overworld").withStyle(ChatFormatting.YELLOW);
        } else if (dimension.equals(Level.NETHER)) {
            return new TextComponent("the Nether").withStyle(ChatFormatting.RED);
        } else if (dimension.equals(Level.END)) {
            return new TextComponent("the End").withStyle(ChatFormatting.DARK_AQUA);
        } else {
            OverVaults.LOGGER.warn("Chosen dimension for a new active portal was not the overworld, nether, or end.");
            return new TextComponent("Report this").withStyle(ChatFormatting.BOLD).withStyle(ChatFormatting.DARK_RED);

        }
    }
}
