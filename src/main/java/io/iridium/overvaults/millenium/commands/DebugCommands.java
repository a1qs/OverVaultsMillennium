package io.iridium.overvaults.millenium.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.iridium.overvaults.millenium.util.PortalUtil;
import io.iridium.overvaults.millenium.util.TextUtil;
import io.iridium.overvaults.millenium.world.BlockEntityChunkSavedData;
import io.iridium.overvaults.millenium.world.PortalData;
import io.iridium.overvaults.millenium.world.PortalSavedData;
import iskallia.vault.block.entity.VaultPortalTileEntity;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.stream.Stream;

public class DebugCommands extends BaseCommand {

    @Override
    public String getName() {
        return "debug";
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSourceStack> builder) {
        builder.then(Commands.literal("getRandomStructure").executes(this::getRandomStructure));
        builder.then(Commands.literal("activateAllPortals").executes(this::activateAllPortals));
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
}
