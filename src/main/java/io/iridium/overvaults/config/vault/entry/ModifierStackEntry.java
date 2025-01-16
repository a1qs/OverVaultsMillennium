package io.iridium.overvaults.config.vault.entry;

import com.google.gson.annotations.Expose;
import iskallia.vault.core.vault.modifier.VaultModifierStack;
import iskallia.vault.core.vault.modifier.registry.VaultModifierRegistry;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;


public class ModifierStackEntry {
    @Expose
    private ResourceLocation modifierId;
    @Expose
    private int amount;

    public ModifierStackEntry(ResourceLocation modifierId, int amount) {
        this.modifierId = modifierId;
        this.amount = amount;
    }

    public ResourceLocation getModifierId() {
        return modifierId;
    }

    public int getAmount() {
        return amount;
    }

    public static VaultModifierStack getModifier(ModifierStackEntry entry) {
        return VaultModifierStack.of(VaultModifierRegistry.get(entry.getModifierId()), entry.getAmount());
    }

    public static List<VaultModifierStack> getModifiers(ModifierStackEntry... entries) {
        List<VaultModifierStack> mod = new ArrayList<>();

        for(ModifierStackEntry entry : entries) {
            mod.add(VaultModifierStack.of(VaultModifierRegistry.get(entry.getModifierId()), entry.getAmount()));
        }

        return mod;
    }

    public static List<VaultModifierStack> getModifiers(List<ModifierStackEntry> entries) {
        List<VaultModifierStack> mod = new ArrayList<>();

        for(ModifierStackEntry entry : entries) {
            mod.add(VaultModifierStack.of(VaultModifierRegistry.get(entry.getModifierId()), entry.getAmount()));
        }

        return mod;
    }
}
