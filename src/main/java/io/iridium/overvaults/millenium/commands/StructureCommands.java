package io.iridium.overvaults.millenium.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.datafixers.util.Pair;
import io.iridium.overvaults.millenium.event.ServerTickEvent;
import io.iridium.overvaults.millenium.util.MiscUtil;
import io.iridium.overvaults.millenium.util.PortalUtil;
import io.iridium.overvaults.millenium.util.TextUtil;
import io.iridium.overvaults.millenium.world.BlockEntityChunkSavedData;
import io.iridium.overvaults.millenium.world.PortalData;
import io.iridium.overvaults.millenium.world.PortalSavedData;
import iskallia.vault.block.entity.VaultPortalTileEntity;
import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.List;
import java.util.Objects;
import java.util.OptionalInt;
import java.util.Random;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class StructureCommands extends BaseCommand {
    public String getName() {
        return "structures";
    }

    public void build(LiteralArgumentBuilder<CommandSourceStack> builder) {
        builder.then(Commands.literal("removeStructureWithIndex").then(Commands.argument("dimension", DimensionArgument.dimension()).then(Commands.argument("index", IntegerArgumentType.integer()).executes(this::removeStructureWithIndex))));
        builder.then(Commands.literal("getStructureWithIndex").then(Commands.argument("dimension", DimensionArgument.dimension()).then(Commands.argument("index", IntegerArgumentType.integer()).executes(this::getStructureWithIndex))));
        builder.then(Commands.literal("getClosestStructure").executes(this::getClosestStructure));
        builder.then(Commands.literal("getRandomStructure").executes(this::getRandomStructure));
        builder.then(Commands.literal("getActiveOverVault").executes(this::getActiveOverVault));
        builder.then(Commands.literal("getNextOverVaultSpawn").executes(this::getNextOverVaultSpawn));
        builder.then(Commands.literal("activateAllPortals").executes(this::activateAllPortals));
        builder.then(Commands.literal("activateRandomPortal").executes(this::activateRandomPortal));
        builder.then(Commands.literal("deactivateActivePortal").executes(this::deactivateActivePortal));
    }



    private int getNextOverVaultSpawn(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        if(PortalUtil.getAllLevelActivePortalData(ServerLifecycleHooks.getCurrentServer()) != null) {
            source.sendFailure(new TextComponent("An OverVaults portal is already active! Cannot spawn additional."));
            return 1;
        }

        int[] timeRemaining = MiscUtil.convertTime(ServerTickEvent.actlTicksForPortalSpawn - ServerTickEvent.counter);
        int[] timeRequired = MiscUtil.convertTime(ServerTickEvent.actlTicksForPortalSpawn);

        String timeFormatted = MiscUtil.formatTime(timeRemaining);
        String defTimeFormatted = MiscUtil.formatTime(timeRequired);

        MutableComponent cmp = new TextComponent("")
                .append(new TextComponent("Time until next OverVault portal spawn: ").withStyle(ChatFormatting.GREEN))
                .append(new TextComponent(defTimeFormatted + "/")).withStyle(ChatFormatting.GRAY)
                .append(new TextComponent(timeFormatted).withStyle(ChatFormatting.AQUA))
                .append(new TextComponent(" (HH:MM:SS)").withStyle(ChatFormatting.GRAY));



        source.sendSuccess(cmp, true);
        return 0;
    }

    private int getActiveOverVault(CommandContext<CommandSourceStack> context) {
        List<PortalData> unifiedPortalDataList = PortalUtil.getAllLevelPortalData(context.getSource().getServer());
        if(unifiedPortalDataList == null) {
            context.getSource().sendFailure(new TextComponent("No valid portal data found across all dimensions!"));
            return 1;
        }

        OptionalInt indexOpt = IntStream.range(0, unifiedPortalDataList.size())
                .filter(i -> unifiedPortalDataList.get(i).getActiveState())
                .findFirst();

        PortalData data = indexOpt.isPresent() ? unifiedPortalDataList.get(indexOpt.getAsInt()) : null;
        int index = indexOpt.orElse(-1);

        if(data == null) {
            context.getSource().sendFailure(new TextComponent("No active OverVault found!"));
            return 1;
        }

        context.getSource().sendSuccess(TextUtil.getPortalTpComponent(index, data), true);
        return 0;
    }

    private int getStructureWithIndex(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerLevel level = DimensionArgument.getDimension(context, "dimension");
        int index = IntegerArgumentType.getInteger(context, "index");
        List<PortalData> portalDataList = PortalSavedData.get(level).getPortalData();

        if (index >= portalDataList.size()) {
            context.getSource().sendFailure(new TextComponent("Index: " + index + " is out of bounds! Max allowed value is: " + (portalDataList.size() - 1)));
            return 1;
        }

        PortalData data = PortalSavedData.get(level).getPortalData().get(index);
        context.getSource().sendSuccess(TextUtil.getPortalTpComponent(index, data), true);
        return 0;
    }

    private int removeStructureWithIndex(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerLevel level = DimensionArgument.getDimension(context, "dimension");
        int index = IntegerArgumentType.getInteger(context, "index");
        List<PortalData> portalDataList = PortalSavedData.get(level).getPortalData();

        if (index >= portalDataList.size()) {
            context.getSource().sendFailure(new TextComponent("Index: " + index + " is out of bounds! Max allowed value is: " + (portalDataList.size() - 1)));
            return 1;
        }

        portalDataList.remove(index);
        MutableComponent cmp = new TextComponent("Removed Portal with index: " + index + "\n")
                .append(new TextComponent("(Note: This portal will be re-added to the List when the chunks are re-loaded)").withStyle(ChatFormatting.GRAY));

        context.getSource().sendSuccess(cmp, true);
        return 0;
    }

    public int getClosestStructure(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        Pair<Integer, Double> closestPortalData = PortalUtil.getClosestPortalDataIndexAndDistance(player);

        if (closestPortalData == null) {
            context.getSource().sendFailure(new TextComponent("No valid portal data found in the dimension of the Player."));
            return 1;
        }

        int index = closestPortalData.getFirst();
        double distance = closestPortalData.getSecond();

        MutableComponent cmp = TextUtil.getPortalTpComponent(index, PortalSavedData.get(player.getLevel()).getPortalData().get(index));
        cmp.append(new TextComponent("    Distance to closest Portal: " + (int) distance + " Block" + (distance <= 1 ? "" : "s")).withStyle(ChatFormatting.YELLOW));

        context.getSource().sendSuccess(cmp, true);
        return 0;
    }

    public int deactivateActivePortal(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        ServerPlayer player = context.getSource().getPlayerOrException();
        PortalData data = PortalUtil.getAllLevelActivePortalData(context.getSource().getServer());
        MinecraftServer srv = context.getSource().getServer();

        if (data == null) {
            context.getSource().sendFailure(new TextComponent("Found no active Portal!"));
            return 1;
        }

        ServerLevel level = srv.getLevel(data.getDimension());

        if (level == null) {
            context.getSource().sendFailure(new TextComponent("Invalid dimension...?"));
            return 1;
        }


        BlockEntityChunkSavedData entityChunkData = BlockEntityChunkSavedData.get(level);
        PortalSavedData savedData = PortalSavedData.get(level);

        entityChunkData.removePortalTileEntityData();
        data.setActiveState(false);
        savedData.setDirty();
        for (ChunkPos chunkPos : entityChunkData.getForceloadedChunks()) {
            level.getChunkSource().removeRegionTicket(TicketType.FORCED, chunkPos, 1, chunkPos);
        }
        entityChunkData.removeChunkPositionData();


        context.getSource().sendSuccess(new TextComponent("Removed data, did not remove tile entities (lazy) "), true);
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
        context.getSource().sendSuccess(TextUtil.getPortalTpComponent(index, data), true);
        return 0;
    }

    private int activateAllPortals(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        MinecraftServer server = context.getSource().getServer();

        List<ServerLevel> validDimensions = Stream.of(
                server.getLevel(Level.OVERWORLD),
                server.getLevel(Level.NETHER),
                server.getLevel(Level.END)
        ).filter(Objects::nonNull).toList();


        for(ServerLevel level : validDimensions) {
            PortalSavedData portalSavedData = PortalSavedData.get(level);
            for(PortalData data : portalSavedData.getPortalData()) {

                List<VaultPortalTileEntity> portalTileEntities = PortalUtil.activatePortal(level, data);


                BlockEntityChunkSavedData entityChunkData = BlockEntityChunkSavedData.get(level);
                for (VaultPortalTileEntity portalTileEntity : portalTileEntities) {
                    entityChunkData.addPortalTileEntity(portalTileEntity.getBlockPos());
                }
                entityChunkData.setDirty();

                data.setActiveState(true);
                portalSavedData.setDirty();
                context.getSource().getPlayerOrException().sendMessage(TextUtil.getPortalAppearComponent(level, data), ChatType.SYSTEM, Util.NIL_UUID);
            }
        }
        return 0;
    }

    private int activateRandomPortal(CommandContext<CommandSourceStack> context) {
        if(PortalUtil.getAllLevelActivePortalData(ServerLifecycleHooks.getCurrentServer()) != null) {
            context.getSource().sendFailure(new TextComponent("An OverVaults portal is already active! Cannot set activation timer."));
            return 1;
        }

        context.getSource().sendSuccess(new TextComponent("Instantly activated a Portal!").withStyle(ChatFormatting.YELLOW), true);
        ServerTickEvent.counter = ServerTickEvent.actlTicksForPortalSpawn;
        return 0;
    }
}
