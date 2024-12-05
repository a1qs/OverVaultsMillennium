package io.iridium.overvaults.config.vault.entry;

import com.google.gson.annotations.Expose;
import net.minecraft.resources.ResourceLocation;

public class EntityDataEntry {

    @Expose
    public ResourceLocation entityId;

    @Expose
    public float entityHitpoints;

    @Expose
    public float entityAttackDamage;


    public EntityDataEntry(ResourceLocation entityId, float entityHitpoints, float entityAttackDamage) {
        this.entityId = entityId;
        this.entityHitpoints = entityHitpoints;
        this.entityAttackDamage = entityAttackDamage;
    }
}
