package io.iridium.overvaults.millenium.commands;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.iridium.overvaults.millenium.event.ServerTickEvent;
import io.iridium.overvaults.millenium.util.MiscUtil;
import io.iridium.overvaults.millenium.util.PortalUtil;
import io.iridium.overvaults.millenium.util.TextUtil;
import io.iridium.overvaults.millenium.world.PortalData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.List;
import java.util.OptionalInt;
import java.util.stream.IntStream;

public class InfoCommands extends BaseCommand {

    @Override
    public String getName() {
        return "info";
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSourceStack> builder) {
        builder.then(Commands.literal("getActiveOverVault").executes(this::getActiveOverVault));
        builder.then(Commands.literal("getNextOverVaultSpawn").executes(this::getNextOverVaultSpawn));
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
}
