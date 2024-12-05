package io.iridium.overvaults.config.vault.ds;

import com.google.gson.annotations.Expose;
import net.minecraft.resources.ResourceLocation;


public record ModifierStack(@Expose ResourceLocation modifierId, @Expose int amount) {
}
