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
    public static final ForgeConfigSpec.ConfigValue<Boolean> SPAWN_ENTITY_MODIFIER_REMOVAL;

    public static final ForgeConfigSpec.ConfigValue<Integer> MIN_SECONDS_UNTIL_PORTAL_SPAWN;
    public static final ForgeConfigSpec.ConfigValue<Integer> MIN_SECONDS_UNTIL_MODIFIER_REMOVAL;
    public static final ForgeConfigSpec.ConfigValue<Integer> MAX_SECONDS_UNTIL_PORTAL_SPAWN;
    public static final ForgeConfigSpec.ConfigValue<Integer> MAX_SECONDS_UNTIL_MODIFIER_REMOVAL;

    public static final ForgeConfigSpec.ConfigValue<Integer> RAW_VAULT_WEIGHT;
    public static final ForgeConfigSpec.ConfigValue<Integer> NORMAL_VAULT_WEIGHT;
    public static final ForgeConfigSpec.ConfigValue<Integer> UBER_VAULT_WEIGHT;
    public static final ForgeConfigSpec.ConfigValue<Integer> ENTITY_STEP;
    public static final ForgeConfigSpec.ConfigValue<Double> CHANCE_OF_SPECIAL_THEME_NETHER;
    public static final ForgeConfigSpec.ConfigValue<Double> CHANCE_OF_SPECIAL_THEME_END;

    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> NORMAL_VAULT_MODIFIERS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> UBER_VAULT_MODIFIERS;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> NETHER_VAULT_THEMES;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> VOID_VAULT_THEMES;
    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> BOSS_ENTITIES;




    static {
        BUILDER.comment("ServerConfigs for OverVaults - Millennium Edition");

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

        SPAWN_ENTITY_MODIFIER_REMOVAL = BUILDER.comment("Whether an entity should be spawned upon removing a Modifier from an Overvault")
                .define("SPAWN_ENTITY_MODIFIER_REMOVAL", true);

        BUILDER.pop();


        // Timing Settings
        BUILDER.push("Timing Settings");

        MIN_SECONDS_UNTIL_PORTAL_SPAWN = BUILDER.comment("Minimum Time in seconds until a OverVault is attempted to be opened.")
                .define("MIN_SECONDS_UNTIL_PORTAL_SPAWN", 3000);

        MAX_SECONDS_UNTIL_PORTAL_SPAWN = BUILDER.comment("Maximum Time in seconds until a OverVault is attempted to be opened.")
                .define("MAX_SECONDS_UNTIL_PORTAL_SPAWN", 9000);

        MIN_SECONDS_UNTIL_MODIFIER_REMOVAL = BUILDER.comment("Minimum Time in seconds until a Modifier is removed from an OverVault")
                .define("MIN_SECONDS_UNTIL_MODIFIER_REMOVAL", 1200);

        MAX_SECONDS_UNTIL_MODIFIER_REMOVAL = BUILDER.comment("Maximum Time in seconds until a Modifier is removed from an OverVault")
                .define("MAX_SECONDS_UNTIL_MODIFIER_REMOVAL", 1800);

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
                .defineInRange("CHANCE_OF_SPECIAL_THEME_NETHER", 0.15, 0.0, 1.0);
        CHANCE_OF_SPECIAL_THEME_END = BUILDER.comment("The chance of getting a special theme defined in 'VOID_VAULT_THEMES' as a decimal.")
                .defineInRange("CHANCE_OF_SPECIAL_THEME_END", 0.10, 0.0, 1.0);

        ENTITY_STEP = BUILDER.comment("The value chosen to 'tier-up' a spawned Entity from an active Vault Portal\nAfter 4 tier-ups, a Boss-Entity is spawned and it starts over")
                        .defineInRange("ENTITY_STEP", 5, 1, Integer.MAX_VALUE);

        BUILDER.pop();

        // List Settings
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

        BOSS_ENTITIES = BUILDER
                .comment("List of entity Ids that can be selected as a Boss monster when summoning a Vault enemy after modifier removal")
                .defineList("BOSS_ENTITIES",
                        Arrays.asList(
                                "the_vault:robot",
                                "the_vault:boogieman"
                        ), obj -> obj instanceof String);

        BUILDER.pop();
        SPEC = BUILDER.build();
    }
}
