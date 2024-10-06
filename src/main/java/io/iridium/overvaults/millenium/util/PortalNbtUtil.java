package io.iridium.overvaults.millenium.util;

import io.iridium.overvaults.config.ServerConfig;
import iskallia.vault.VaultMod;
import iskallia.vault.core.random.JavaRandom;
import iskallia.vault.core.vault.modifier.VaultModifierStack;
import iskallia.vault.core.vault.modifier.registry.VaultModifierRegistry;
import iskallia.vault.core.vault.modifier.spi.VaultModifier;
import iskallia.vault.init.ModConfigs;
import iskallia.vault.init.ModItems;
import iskallia.vault.item.crystal.CrystalData;
import iskallia.vault.item.crystal.model.RawCrystalModel;
import iskallia.vault.item.crystal.objective.EmptyCrystalObjective;
import iskallia.vault.item.crystal.theme.PoolCrystalTheme;
import iskallia.vault.item.crystal.theme.ValueCrystalTheme;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.Random;



public class PortalNbtUtil {
    private static final Random RANDOM = new Random();
    private static final List<? extends String> NETHER_THEMES = ServerConfig.NETHER_VAULT_THEMES.get();
    private static final List<? extends String> VOID_THEMES = ServerConfig.VOID_VAULT_THEMES.get();

    private static CrystalData rawCrystalBase() {
        ItemStack stack = new ItemStack(ModItems.VAULT_CRYSTAL);
        CrystalData crystal = CrystalData.read(stack);
        crystal.getProperties().setLevel(0);
        crystal.setModel(new RawCrystalModel());
        crystal.setObjective(new EmptyCrystalObjective());
        crystal.setTheme(new PoolCrystalTheme(VaultMod.id("raw")));

        for (VaultModifier<?> modifier : ModConfigs.VAULT_MODIFIER_POOLS.getRandom(VaultMod.id("raw"), 0, JavaRandom.ofNanoTime())) {
            crystal.getModifiers().add(VaultModifierStack.of(modifier, 1));
        }

        crystal.getModifiers().setRandomModifiers(false);

        return crystal;
    }

    private static CrystalData betterCrystalBase(ResourceKey<Level> dimension) {
        List<? extends String> normalVaultModifiers = ServerConfig.NORMAL_VAULT_MODIFIERS.get();
        ItemStack stack = new ItemStack(ModItems.VAULT_CRYSTAL);
        CrystalData crystal = CrystalData.read(stack);
        crystal.getProperties().setLevel(0);

        if(!normalVaultModifiers.isEmpty()) {
            for(String modifier : normalVaultModifiers) {
                crystal.getModifiers().add(VaultModifierStack.of(VaultModifierRegistry.get(new ResourceLocation(modifier)), 1));
            }
        }


        if(dimension.equals(Level.NETHER) && RANDOM.nextFloat() <= ServerConfig.CHANCE_OF_SPECIAL_THEME_NETHER.get()) {
            crystal.setTheme(new ValueCrystalTheme(
                    new ResourceLocation(NETHER_THEMES.get(RANDOM.nextInt(NETHER_THEMES.size())))
            ));
        } else if (dimension.equals(Level.END) && RANDOM.nextFloat() <= ServerConfig.CHANCE_OF_SPECIAL_THEME_END.get()) {

            crystal.setTheme(new ValueCrystalTheme(
                    new ResourceLocation(VOID_THEMES.get(RANDOM.nextInt(VOID_THEMES.size())))
            ));
        }

        crystal.getModifiers().setRandomModifiers(false);

        return crystal;
    }

    private static CrystalData uberCrystalBase(ResourceKey<Level> dimension) {
        List<? extends String> uberVaultModifiers = ServerConfig.UBER_VAULT_MODIFIERS.get();
        ItemStack stack = new ItemStack(ModItems.VAULT_CRYSTAL);
        CrystalData crystal = CrystalData.read(stack);
        crystal.getProperties().setLevel(0);

        if(!uberVaultModifiers.isEmpty()) {
            for(String modifier : uberVaultModifiers) {
                crystal.getModifiers().add(VaultModifierStack.of(VaultModifierRegistry.get(new ResourceLocation(modifier)), 1));
            }
        }

        if(dimension.equals(Level.NETHER) && RANDOM.nextFloat() <= ServerConfig.CHANCE_OF_SPECIAL_THEME_NETHER.get()) {
            crystal.setTheme(new ValueCrystalTheme(
                    new ResourceLocation(NETHER_THEMES.get(RANDOM.nextInt(NETHER_THEMES.size())))
            ));
        } else if (dimension.equals(Level.END) && RANDOM.nextFloat() <= ServerConfig.CHANCE_OF_SPECIAL_THEME_END.get()) {
            crystal.setTheme(new ValueCrystalTheme(
                    new ResourceLocation(VOID_THEMES.get(RANDOM.nextInt(VOID_THEMES.size())))
            ));
        }

        return crystal;
    }

    public static CrystalData getRandomCrystalData(ResourceKey<Level> dimension) {
        int rawVaultWeight = ServerConfig.RAW_VAULT_WEIGHT.get();
        int normalVaultWeight = ServerConfig.NORMAL_VAULT_WEIGHT.get();
        int uberVaultWeight = ServerConfig.UBER_VAULT_WEIGHT.get();

        int totalWeight = rawVaultWeight + normalVaultWeight + uberVaultWeight;
        int randomValue = RANDOM.nextInt(totalWeight);

        if (randomValue < rawVaultWeight) {
            return rawCrystalBase();
        } else if (randomValue < rawVaultWeight + normalVaultWeight) {
            return betterCrystalBase(dimension);
        } else {
            return uberCrystalBase(dimension);
        }
    }
}
