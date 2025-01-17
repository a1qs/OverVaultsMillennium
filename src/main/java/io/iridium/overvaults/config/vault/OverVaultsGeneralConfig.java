package io.iridium.overvaults.config.vault;

import com.google.gson.annotations.Expose;
import iskallia.vault.VaultMod;
import iskallia.vault.config.Config;
import iskallia.vault.config.entry.IntRangeEntry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class OverVaultsGeneralConfig extends Config {
    @Expose public boolean RESPECT_WORLD_BORDER;
    @Expose public boolean SET_LEVEL_OF_ENTERING_PLAYER_OVERVAULT;
    @Expose public boolean BROADCAST_IN_CHAT;
    @Expose public boolean PLAY_SOUND_ON_OPEN;
    @Expose public boolean UPDATE_VAULT_COMPASS;
    @Expose public boolean SPAWN_ENTITY_MODIFIER_REMOVAL;

    @Expose public int MAX_RADIUS_PORTAL_SPAWN_OVERWORLD;
    @Expose public int MAX_RADIUS_PORTAL_SPAWN_NETHER;
    @Expose public int MAX_RADIUS_PORTAL_SPAWN_END;

    @Expose public IntRangeEntry SECONDS_UNTIL_PORTAL_SPAWN;
    @Expose public IntRangeEntry SECONDS_UNTIL_MODIFIER_REMOVAL;
    @Expose public List<String> BLACKLISTED_MODIFIERS_TO_REMOVE = new ArrayList<>();


    @Override
    public String getName() {
        return "overvaults_general";
    }

    @Override
    protected void reset() {
        RESPECT_WORLD_BORDER = true;
        SET_LEVEL_OF_ENTERING_PLAYER_OVERVAULT = true;
        BROADCAST_IN_CHAT = true;
        PLAY_SOUND_ON_OPEN = true;
        UPDATE_VAULT_COMPASS = true;
        SPAWN_ENTITY_MODIFIER_REMOVAL = true;

        MAX_RADIUS_PORTAL_SPAWN_OVERWORLD = 100;
        MAX_RADIUS_PORTAL_SPAWN_NETHER = 100;
        MAX_RADIUS_PORTAL_SPAWN_END = 100;

        SECONDS_UNTIL_PORTAL_SPAWN = new IntRangeEntry(3000, 9000);
        SECONDS_UNTIL_MODIFIER_REMOVAL = new IntRangeEntry(1200, 1800);
        BLACKLISTED_MODIFIERS_TO_REMOVE.add(VaultMod.id("locked").toString());

    }

    public int getSpawnRadiusForLevel(ResourceKey<Level> key) {
        if(key.equals(Level.OVERWORLD)) {
            return MAX_RADIUS_PORTAL_SPAWN_OVERWORLD;
        } else if(key.equals(Level.NETHER)) {
            return MAX_RADIUS_PORTAL_SPAWN_NETHER;
        }
        return MAX_RADIUS_PORTAL_SPAWN_END;
    }
}
