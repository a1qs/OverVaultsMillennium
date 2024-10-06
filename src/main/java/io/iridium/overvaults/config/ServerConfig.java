package io.iridium.overvaults.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Arrays;
import java.util.List;

public class ServerConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Boolean> RESPECT_WORLD_BORDER;
    public static final ForgeConfigSpec.ConfigValue<Boolean> SET_LEVEL_OF_ENTERING_PLAYER;
    public static final ForgeConfigSpec.ConfigValue<Boolean> BROADCAST_IN_CHAT;
    public static final ForgeConfigSpec.ConfigValue<Boolean> PLAY_SOUND_ON_OPEN;
    public static final ForgeConfigSpec.ConfigValue<Boolean> UPDATE_VAULT_COMPASS;
    public static final ForgeConfigSpec.ConfigValue<Boolean> INFORM_PLAYERS_IN_VAULTS;


    public static final ForgeConfigSpec.ConfigValue<Integer> SECONDS_UNTIL_PORTAL_SPAWN;
    public static final ForgeConfigSpec.ConfigValue<Integer> SECONDS_UNTIL_MODIFIER_REMOVAL;

    public static final ForgeConfigSpec.ConfigValue<Integer> RAW_VAULT_WEIGHT;
    public static final ForgeConfigSpec.ConfigValue<Integer> NORMAL_VAULT_WEIGHT;
    public static final ForgeConfigSpec.ConfigValue<Integer> UBER_VAULT_WEIGHT;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> NORMAL_VAULT_MODIFIERS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> UBER_VAULT_MODIFIERS;

    public static final ForgeConfigSpec.ConfigValue<Double> CHANCE_OF_SPECIAL_THEME_NETHER;
    public static final ForgeConfigSpec.ConfigValue<Double> CHANCE_OF_SPECIAL_THEME_END;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> NETHER_VAULT_THEMES;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> VOID_VAULT_THEMES;



    static {
        BUILDER.comment("ServerConfigs for OverVaults - Millenium Edition");

        //General Settings
        BUILDER.push("General Settings");

        RESPECT_WORLD_BORDER = BUILDER.comment("Whether the World Border is respected when checking for valid portal structures.")
                .define("RESPECT_WORLD_BORDER", true);
        SET_LEVEL_OF_ENTERING_PLAYER = BUILDER.comment("Whether an active OverVault should use the Level of the Player entering when generating a Vault (Otherwise 0).")
                .define("SET_LEVEL_OF_ENTERING_PLAYER", true);

        BROADCAST_IN_CHAT = BUILDER.comment("Whether it should be broadcast in chat whenever an OverVault opens")
                .define("BROADCAST_IN_CHAT", true);

        PLAY_SOUND_ON_OPEN = BUILDER.comment("Whether it should play a sound for every player whenever an OverVault opens")
                .define("PLAY_SOUND_ON_OPEN", true);

        UPDATE_VAULT_COMPASS = BUILDER.comment("Whether the Vault Compass should be updated upon switching Dimensions/an OverVault opening")
                .define("UPDATE_VAULT_COMPASS", true);

        INFORM_PLAYERS_IN_VAULTS = BUILDER.comment("Whether Players inside of Vaults should also be informed about an OverVault opening.")
                .define("INFORM_PLAYERS_IN_VAULTS", true);

        BUILDER.pop();


        // Timing Settings
        BUILDER.push("Timing Settings");

        SECONDS_UNTIL_PORTAL_SPAWN = BUILDER.comment("Time in seconds until a OverVault is attempted to be opened.")
                .define("SECONDS_UNTIL_PORTAL_SPAWN", 30);

        SECONDS_UNTIL_MODIFIER_REMOVAL = BUILDER.comment("Time in seconds until a Modifier is removed from an OverVault")
                .define("SECONDS_UNTIL_MODIFIER_REMOVAL", 30);

        BUILDER.pop();

        // Weight/Chances Settings
        BUILDER.push("Weight & Chance Settings");

        RAW_VAULT_WEIGHT = BUILDER.comment("The weight of getting a Raw Vault as an OverVault\nChance is calculated based off combination of 'RAW_VAULT_WEIGHT', 'NORMAL_VAULT_WEIGHT' & 'UBER_VAULT_WEIGHT'")
                .define("RAW_VAULT_WEIGHT", 70);

        NORMAL_VAULT_WEIGHT = BUILDER.comment("The weight of getting a Normal Vault as an OverVault\nChance is calculated based off combination of 'RAW_VAULT_WEIGHT', 'NORMAL_VAULT_WEIGHT' & 'UBER_VAULT_WEIGHT'")
                .define("NORMAL_VAULT_WEIGHT", 20);

        UBER_VAULT_WEIGHT = BUILDER.comment("The weight of getting an Uber Vault as an OverVault\nChance is calculated based off combination of 'RAW_VAULT_WEIGHT', 'NORMAL_VAULT_WEIGHT' & 'UBER_VAULT_WEIGHT'")
                .define("UBER_VAULT_WEIGHT", 5);

        CHANCE_OF_SPECIAL_THEME_NETHER = BUILDER.comment("The chance of getting a special theme defined in 'NETHER_VAULT_THEMES' as a decimal.")
                .defineInRange("CHANCE_OF_SPECIAL_THEME_NETHER", 0.2, 0.0, 1.0);
        CHANCE_OF_SPECIAL_THEME_END = BUILDER.comment("The chance of getting a special theme defined in 'VOID_VAULT_THEMES' as a decimal.")
                .defineInRange("CHANCE_OF_SPECIAL_THEME_END", 0.15, 0.0, 1.0);

        BUILDER.pop();

        // Weight/Chances Settings
        BUILDER.push("List Settings");

        NORMAL_VAULT_MODIFIERS = BUILDER
                .comment("List of Modifiers for Normal OverVaults (can be empty)")
                .defineListAllowEmpty(List.of("NORMAL_VAULT_MODIFIERS"),
                        () -> Arrays.asList("the_vault:energizing", "the_vault:soul_boost", "the_vault:item_quantity", "the_vault:item_rarity")
                        , obj -> obj instanceof String);

        UBER_VAULT_MODIFIERS = BUILDER
                .comment("List of Modifiers for Uber OverVaults (can be empty)")
                .defineListAllowEmpty(List.of("UBER_VAULT_MODIFIERS"),
                        () -> Arrays.asList("the_vault:prosperous", "the_vault:haunted", "the_vault:coin_pile", "the_vault:gilded", "the_vault:living", "the_vault:ornate")
                        , obj -> obj instanceof String);


        NETHER_VAULT_THEMES = BUILDER
                .comment("List of Theme Ids that are of type NETHER to be used in the Nether Dimension OverVaults")
                .defineList("NETHER_VAULT_THEMES",
                        Arrays.asList(
                                "the_vault:classic_vault_nether_crimson",
                                "the_vault:classic_vault_nether_warped",
                                "the_vault:classic_vault_nether_blackstone",
                                "the_vault:classic_vault_nether_soul"
                        ), obj -> obj instanceof String);

        VOID_VAULT_THEMES = BUILDER
                .comment("List of Theme Ids that are of type VOID to be used in the End Dimension OverVaults")
                .defineList("VOID_VAULT_TYPES",
                        Arrays.asList(
                                "the_vault:classic_vault_void",
                                "the_vault:classic_vault_end_void",
                                "the_vault:classic_vault_factory_void"
                        ), obj -> obj instanceof String);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
