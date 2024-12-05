package io.iridium.overvaults.config.vault.entry;

import com.google.gson.annotations.Expose;
import net.minecraft.resources.ResourceLocation;


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
}
