package io.iridium.overvaults.config;

import io.iridium.overvaults.config.vault.OverVaultsGeneralConfig;
import io.iridium.overvaults.config.vault.OverVaultsMobConfig;
import io.iridium.overvaults.config.vault.OverVaultsPortalConfig;

public class VaultConfigRegistry {
    public static OverVaultsPortalConfig OVERVAULTS_PORTAL_CONFIG;
    public static OverVaultsGeneralConfig OVERVAULTS_GENERAL_CONFIG;
    public static OverVaultsMobConfig OVERVAULTS_MOB_CONFIG;


    public static void registerCustomConfigs() {
        OVERVAULTS_PORTAL_CONFIG = new OverVaultsPortalConfig().readConfig();
        OVERVAULTS_GENERAL_CONFIG = new OverVaultsGeneralConfig().readConfig();
        OVERVAULTS_MOB_CONFIG = new OverVaultsMobConfig().readConfig();
    }
}
