package io.iridium.overvaults.millenium.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import io.iridium.overvaults.OverVaults;
import io.iridium.overvaults.millenium.event.ServerTickEvent;
import io.iridium.overvaults.millenium.util.PortalUtil;
import io.iridium.overvaults.millenium.world.PortalData;
import io.iridium.overvaults.millenium.world.PortalSavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.*;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.*;

public class StructureCommand {
    public StructureCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal(OverVaults.MOD_ID)
                .requires(sender -> sender.hasPermission(4))
                .then(Commands.literal("getStructureWithIndex")
                        .then(Commands.argument("index", IntegerArgumentType.integer())
                                .executes(this::getStructureWithIndex)
                        )
                )
                .then(Commands.literal("getRandomStructure")
                        .executes(this::getRandomStructure)
                )
                .then(Commands.literal("getNextOverVaultSpawn")
                        .executes(this::getNextOverVaultSpawn)
                )

                .then(Commands.literal("getActiveOverVault")
                        .executes(this::getActiveOverVault)
                )
        );
    }


    private int getStructureWithIndex(CommandContext<CommandSourceStack> context) {
        MinecraftServer server = context.getSource().getServer();
        int index = IntegerArgumentType.getInteger(context, "index");

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
            context.getSource().sendFailure(new TextComponent("No valid portal data found across all dimensions!"));
            return 1;
        }

        if (index >= unifiedPortalDataList.size()) {
            context.getSource().sendFailure(new TextComponent("Index: " + index + " is out of bounds! Max allowed value is: " + (unifiedPortalDataList.size() - 1)));
            return 1;
        }

        PortalData data = unifiedPortalDataList.get(index);
        MutableComponent cmp = new TextComponent("=== ")
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
                .append(new TextComponent(data.getDimension().location().getPath()).withStyle(ChatFormatting.YELLOW));

        context.getSource().sendSuccess(cmp, true);
        return 0;
    }

    private int getRandomStructure(CommandContext<CommandSourceStack> context) {
        MinecraftServer server = context.getSource().getServer();

        List<PortalData> unifiedPortalDataList = PortalUtil.getAllLevelPortalData(server);
        if(unifiedPortalDataList == null) {
            context.getSource().sendFailure(new TextComponent("No valid portal data found across all dimensions!"));
            return 1;
        }

        int index = new Random().nextInt(unifiedPortalDataList.size());
        PortalData data = unifiedPortalDataList.get(index);
        MutableComponent cmp = new TextComponent("=== ")
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
                .append(new TextComponent(data.getDimension().location().getPath()).withStyle(ChatFormatting.YELLOW));

        context.getSource().sendSuccess(cmp, true);
        return 0;
    }

    private int getNextOverVaultSpawn(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        if(PortalUtil.getAllLevelActivePortalData(ServerLifecycleHooks.getCurrentServer()) != null) {
            source.sendFailure(new TextComponent("An OverVaults portal is already active! Cannot spawn additional."));
            return 1;
        }

        int ticksRemaining = ServerTickEvent.ticksForPortalSpawn - ServerTickEvent.counter;
        int totalSecondsRemaining  = ticksRemaining/20;

        int hours = totalSecondsRemaining / 3600;
        int minutes = (totalSecondsRemaining % 3600) / 60;
        int seconds = totalSecondsRemaining % 60;
        String timeFormatted = String.format("%02d:%02d:%02d", hours, minutes, seconds);

        MutableComponent cmp = new TextComponent("")
                .append(new TextComponent("Time until next OverVault portal spawn: ").withStyle(ChatFormatting.GREEN))
                .append(new TextComponent(timeFormatted).withStyle(ChatFormatting.AQUA))
                .append(new TextComponent(" (HH:MM:SS)").withStyle(ChatFormatting.GRAY));



        source.sendSuccess(cmp, true);

        return 0;
    }

    private int getActiveOverVault(CommandContext<CommandSourceStack> context) {
        MinecraftServer server = context.getSource().getServer();

        List<PortalData> unifiedPortalDataList = PortalUtil.getAllLevelPortalData(server);
        if(unifiedPortalDataList == null) {
            context.getSource().sendFailure(new TextComponent("No valid portal data found across all dimensions!"));
            return 1;
        }

        PortalData data = unifiedPortalDataList.stream()
                .filter(PortalData::getActiveState)
                .findFirst()
                .orElse(null);

        if(data == null) {
            context.getSource().sendFailure(new TextComponent("No active OverVault found!"));
            return 1;
        }

        BlockPos offsetPosition =  data.getPortalFrameCenterPos().offset(3.0, 0.0, 3.0);
        String tpCommand = "    /execute as @s in " + data.getDimension().location() + " run tp " + offsetPosition.getX() + " " + offsetPosition.getY() + " " + offsetPosition.getZ();
        MutableComponent acceptTxt = new TextComponent(tpCommand).withStyle(ChatFormatting.AQUA);
        acceptTxt.withStyle((style) -> style.withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new TextComponent("Click to teleport!"))).withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, tpCommand)));

        MutableComponent cmp = new TextComponent("=== ")
                .append(new TextComponent("Portal data of current active OverVault").withStyle(ChatFormatting.AQUA))
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
                .append(new TextComponent(data.getDimension().location().getPath() + "\n").withStyle(ChatFormatting.YELLOW))
                .append(acceptTxt);

        context.getSource().sendSuccess(cmp, true);
        return 0;
    }
}
