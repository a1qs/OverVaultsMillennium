package io.iridium.overvaults.config;

import io.iridium.overvaults.config.vault.OverVaultsPortalConfig;

public class VaultConfigRegistry {
    public static OverVaultsPortalConfig PORTAL_CONFIG;


    public static void registerCustomConfigs() {
        PORTAL_CONFIG = new OverVaultsPortalConfig().readConfig();
    }
}
