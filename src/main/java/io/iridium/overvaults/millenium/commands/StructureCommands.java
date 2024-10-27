package io.iridium.overvaults.millenium.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.iridium.overvaults.millenium.util.TextUtil;
import io.iridium.overvaults.millenium.world.PortalData;
import io.iridium.overvaults.millenium.world.PortalSavedData;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.DimensionArgument;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;

import java.util.List;

public class StructureCommands extends BaseCommand {
    public String getName() {
        return "structures";
    }

    public void build(LiteralArgumentBuilder<CommandSourceStack> builder) {
        builder.then(Commands.literal("getStructureWithIndex").then(Commands.argument("dimension", DimensionArgument.dimension()).then(Commands.argument("index", IntegerArgumentType.integer()).executes(this::getStructureWithIndex))));
        builder.then(Commands.literal("removeStructureWithIndex").then(Commands.argument("dimension", DimensionArgument.dimension()).then(Commands.argument("index", IntegerArgumentType.integer()).executes(this::removeStructureWithIndex))));
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
}
