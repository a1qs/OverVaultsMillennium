package io.iridium.overvaults.millenium.commands;

import io.iridium.overvaults.OverVaults;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.command.ConfigCommand;

@Mod.EventBusSubscriber(modid = OverVaults.MOD_ID)
public class CommandRegistry {
    @SubscribeEvent
    public static void onCommandsRegister(RegisterCommandsEvent event) {
        new StructureCommand(event.getDispatcher());

        ConfigCommand.register(event.getDispatcher());
    }

}
