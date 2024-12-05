package io.iridium.overvaults.config.vault;



import com.google.gson.annotations.Expose;
import io.iridium.overvaults.OverVaults;
import io.iridium.overvaults.config.VaultConfigRegistry;
import io.iridium.overvaults.config.vault.ds.ModifierStack;
import iskallia.vault.VaultMod;
import iskallia.vault.config.Config;
import iskallia.vault.core.random.JavaRandom;
import iskallia.vault.core.util.WeightedList;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class OverVaultsPortalConfig extends Config {

    private static final Random RANDOM = new Random();
    @Expose public float CHANCE_OF_RAW_VAULT;
    @Expose public float CHANCE_OF_NETHER_THEME_IN_NETHER;
    @Expose public float CHANCE_OF_VOID_THEME_IN_END;
    @Expose public WeightedList<List<ModifierStack>> MODIFIER_LISTS = new WeightedList<>();
    @Expose public List<ResourceLocation> NETHER_VAULT_THEMES = new ArrayList<>();
    @Expose public List<ResourceLocation> VOID_VAULT_THEMES = new ArrayList<>();

    @Override
    public String getName() {
        return "~overvaults_portals";
    }

    @Override
    protected void reset() {
        CHANCE_OF_RAW_VAULT = 0.75F;

        CHANCE_OF_NETHER_THEME_IN_NETHER = 0.10F;
        CHANCE_OF_VOID_THEME_IN_END = 0.075F;

        MODIFIER_LISTS.put(
                List.of(
                        new ModifierStack(new ResourceLocation("the_vault:energizing"), 1),
                        new ModifierStack(new ResourceLocation("the_vault:soul_boost"), 1),
                        new ModifierStack(new ResourceLocation("the_vault:item_quantity"), 3),
                        new ModifierStack(new ResourceLocation("the_vault:item_rarity"), 3)
                ),
                75
        );

        MODIFIER_LISTS.put(
                List.of(
                        new ModifierStack(new ResourceLocation("the_vault:prosperous"), 1),
                        new ModifierStack(new ResourceLocation("the_vault:haunted"), 3),
                        new ModifierStack(new ResourceLocation("the_vault:coin_pile"), 3),
                        new ModifierStack(new ResourceLocation("the_vault:gilded"), 3),
                        new ModifierStack(new ResourceLocation("the_vault:ornate"), 3),
                        new ModifierStack(new ResourceLocation("the_vault:living"), 3)
                ),
                20
        );
        MODIFIER_LISTS.put(
                List.of(
                        new ModifierStack(new ResourceLocation("the_vault:prosperous"), 25)
                ),
                5
        );

        NETHER_VAULT_THEMES.add(new ResourceLocation("the_vault:classic_vault_nether_crimson"));
        NETHER_VAULT_THEMES.add(new ResourceLocation("the_vault:classic_vault_nether_warped"));
        NETHER_VAULT_THEMES.add(new ResourceLocation("the_vault:classic_vault_nether_blackstone"));
        NETHER_VAULT_THEMES.add(new ResourceLocation("the_vault:classic_vault_nether_soul"));

        VOID_VAULT_THEMES.add(new ResourceLocation("the_vault:classic_vault_void"));
        VOID_VAULT_THEMES.add(new ResourceLocation("the_vault:classic_vault_end_void"));
        VOID_VAULT_THEMES.add(new ResourceLocation("the_vault:classic_vault_factory_void"));
    }


    public static CrystalData getRandomCrystalData(ResourceKey<Level> dimension) {
        OverVaultsPortalConfig cfg = VaultConfigRegistry.OVERVAULTS_PORTAL_CONFIG;

        ItemStack crystalItem = new ItemStack(ModItems.VAULT_CRYSTAL);
        CrystalData crystal = CrystalData.read(crystalItem);
        crystal.getProperties().setLevel(0);

        if(RANDOM.nextFloat() <= cfg.CHANCE_OF_RAW_VAULT) {
            crystal.setModel(new RawCrystalModel());
            crystal.setObjective(new EmptyCrystalObjective());
            crystal.setTheme(new PoolCrystalTheme(VaultMod.id("raw")));

            for (VaultModifier<?> modifier : ModConfigs.VAULT_MODIFIER_POOLS.getRandom(VaultMod.id("raw"), 0, JavaRandom.ofNanoTime())) {
                crystal.getModifiers().add(VaultModifierStack.of(modifier, 1));
            }

            crystal.getModifiers().setRandomModifiers(false);
            return crystal;
        }


        Optional<List<ModifierStack>> d = cfg.MODIFIER_LISTS.getRandom();
        if(d.isPresent()) {
            for(ModifierStack stack : d.get()) {
                crystal.getModifiers().add(VaultModifierStack.of(VaultModifierRegistry.get(stack.getModifierId()), stack.getAmount()));
            }
        } else {
            OverVaults.LOGGER.error("Found invalid modifier list inside of '{}' config!", VaultConfigRegistry.OVERVAULTS_PORTAL_CONFIG.getName());
        }

        if(dimension.equals(Level.NETHER) && RANDOM.nextFloat() <= cfg.CHANCE_OF_NETHER_THEME_IN_NETHER) {
            crystal.setTheme(new ValueCrystalTheme(
                    cfg.NETHER_VAULT_THEMES.get(RANDOM.nextInt(cfg.NETHER_VAULT_THEMES.size()))
            ));
        } else if (dimension.equals(Level.END) && RANDOM.nextFloat() <= cfg.CHANCE_OF_VOID_THEME_IN_END) {
            crystal.setTheme(new ValueCrystalTheme(
                    cfg.VOID_VAULT_THEMES.get(RANDOM.nextInt(cfg.VOID_VAULT_THEMES.size()))
            ));
        }

        return crystal;
    }
}
