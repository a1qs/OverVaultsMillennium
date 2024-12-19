package io.iridium.overvaults;

import net.minecraft.server.level.TicketType;
import net.minecraft.world.level.ChunkPos;

import java.util.Comparator;

public class OverVaultConstants {
    public static final TicketType<ChunkPos> OVERVAULT_TICKET = TicketType.create("overvaults", Comparator.comparingLong(ChunkPos::toLong));
}
