package io.iridium.overvaults.config.vault;

import com.google.gson.annotations.Expose;
import iskallia.vault.config.Config;
import iskallia.vault.config.entry.IntRangeEntry;

public class OverVaultsGeneralConfig extends Config {
    @Expose public boolean RESPECT_WORLD_BORDER;
    @Expose public boolean SET_LEVEL_OF_ENTERING_PLAYER_OVERVAULT;
    @Expose public boolean BROADCAST_IN_CHAT;
    @Expose public boolean PLAY_SOUND_ON_OPEN;
    @Expose public boolean UPDATE_VAULT_COMPASS;
    @Expose public boolean SPAWN_ENTITY_MODIFIER_REMOVAL;

    @Expose public IntRangeEntry SECONDS_UNTIL_PORTAL_SPAWN;
    @Expose public IntRangeEntry SECONDS_UNTIL_MODIFIER_REMOVAL;


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

        SECONDS_UNTIL_PORTAL_SPAWN = new IntRangeEntry(3000, 9000);
        SECONDS_UNTIL_MODIFIER_REMOVAL = new IntRangeEntry(1200, 1800);
    }
}
