package io.iridium.overvaults.config;

import net.minecraftforge.common.ForgeConfigSpec;

import java.util.Arrays;
import java.util.List;

public class ServerConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.ConfigValue<Integer> MIN_SECONDS_UNTIL_PORTAL_SPAWN;
    public static final ForgeConfigSpec.ConfigValue<Integer> MIN_SECONDS_UNTIL_MODIFIER_REMOVAL;
    public static final ForgeConfigSpec.ConfigValue<Integer> MAX_SECONDS_UNTIL_PORTAL_SPAWN;
    public static final ForgeConfigSpec.ConfigValue<Integer> MAX_SECONDS_UNTIL_MODIFIER_REMOVAL;


    public static final ForgeConfigSpec.ConfigValue<Integer> ENTITY_STEP;



    public static final ForgeConfigSpec.ConfigValue<List<? extends String>> BOSS_ENTITIES;




    static {
        BUILDER.comment("ServerConfigs for OverVaults - Millennium Edition");

        //General Settings
        BUILDER.push("General Settings");

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

        ENTITY_STEP = BUILDER.comment("The value chosen to 'tier-up' a spawned Entity from an active Vault Portal\nAfter 4 tier-ups, a Boss-Entity is spawned and it starts over")
                        .defineInRange("ENTITY_STEP", 5, 1, Integer.MAX_VALUE);

        BUILDER.pop();

        // List Settings
        BUILDER.push("List Settings");

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
