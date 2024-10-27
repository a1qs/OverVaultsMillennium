package io.iridium.overvaults.millenium.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.iridium.overvaults.OverVaults;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public abstract class BaseCommand {

    public abstract String getName();

    public int getRequiredPermissionLevel() {
        return 2;
    };

    public void registerCommand(CommandDispatcher<CommandSourceStack> dispatcher) {
        LiteralArgumentBuilder<CommandSourceStack> builder = Commands.literal(this.getName());
        builder.requires((sender) -> sender.hasPermission(this.getRequiredPermissionLevel()));
        this.build(builder);
        dispatcher.register(Commands.literal(OverVaults.MOD_ID).then(builder));
    }

    public abstract void build(LiteralArgumentBuilder<CommandSourceStack> var1);
}
