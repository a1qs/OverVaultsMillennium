package io.iridium.overvaults.config.vault;



import com.google.gson.annotations.Expose;
import io.iridium.overvaults.config.vault.ds.ModifierStack;
import iskallia.vault.config.Config;
import iskallia.vault.core.util.WeightedList;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

public class OverVaultsPortalConfig extends Config {

    @Expose public float CHANCE_OF_RAW_VAULT;
    @Expose public float CHANCE_OF_NETHER_THEME_IN_NETHER;
    @Expose public float CHANCE_OF_VOID_THEME_IN_END;
    @Expose public WeightedList<List<ModifierStack>> MODIFIER_LISTS = new WeightedList<>();
    @Expose public List<ResourceLocation> NETHER_VAULT_THEMES = new ArrayList<>();
    @Expose public List<ResourceLocation> VOID_VAULT_THEMES = new ArrayList<>();


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

    @Override
    public String getName() {
        return "~overvaults_portals";
    }
}
