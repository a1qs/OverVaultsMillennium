package io.iridium.overvaults.world.structure;

import io.iridium.overvaults.OverVaults;
import net.minecraft.world.level.levelgen.feature.StructureFeature;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModStructures {

    public static final DeferredRegister<StructureFeature<?>> DEFERRED_REGISTRY_STRUCTURE = DeferredRegister.create(ForgeRegistries.STRUCTURE_FEATURES, OverVaults.MOD_ID);
    public static final RegistryObject<StructureFeature<?>> VAULT_PORTAL_STRUCTURES = DEFERRED_REGISTRY_STRUCTURE.register("vault_portal_structures", VaultPortalStructures::new);


    public static void register(IEventBus eventBus){
        DEFERRED_REGISTRY_STRUCTURE.register(eventBus);
    }

}