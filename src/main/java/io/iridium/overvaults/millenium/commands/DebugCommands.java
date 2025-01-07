package io.iridium.overvaults.millenium.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import io.iridium.overvaults.millenium.event.ServerTickEvent;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class DebugCommands extends BaseCommand {
    @Override
    public String getName() {
        return "debug";
    }

    @Override
    public void build(LiteralArgumentBuilder<CommandSourceStack> builder) {
        builder.then(Commands.literal("modifyActivationTimer")
                .then(Commands.argument("timeInTicks", IntegerArgumentType.integer())
                        .executes(this::modifyActivationTimer)
                )
        );
        builder.then(Commands.literal("modifyModifierRemovalTimer")
                .then(Commands.argument("timeInTicks", IntegerArgumentType.integer())
                        .executes(this::modifyModifierRemovalTimer)
                )
        );
    }

    private int modifyActivationTimer(CommandContext<CommandSourceStack> context) {
        int modifyTimer = IntegerArgumentType.getInteger(context, "timeInTicks");

        ServerTickEvent.counter += modifyTimer;

        return 0;
    }

    private int modifyModifierRemovalTimer(CommandContext<CommandSourceStack> context) {
        int modifyTimer = IntegerArgumentType.getInteger(context, "timeInTicks");

        ServerTickEvent.activePortalTickCounter += modifyTimer;

        return 0;
    }







}
