package io.iridium.overvaults.mixin.accessor;


import iskallia.vault.item.crystal.theme.PoolCrystalTheme;
import iskallia.vault.item.crystal.theme.ValueCrystalTheme;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = PoolCrystalTheme.class, remap = false)
public interface AccessorPoolCrystalTheme {

    @Accessor("id")
    ResourceLocation getId();
}
