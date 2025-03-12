package io.iridium.overvaults.mixin;

import iskallia.vault.core.vault.ClientVaults;
import iskallia.vault.core.vault.Vault;
import iskallia.vault.network.message.VaultMessage;
import net.minecraftforge.network.NetworkEvent;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;
import java.util.function.Supplier;

@Mixin(value = VaultMessage.Unload.class, remap = false)
public class MixinVaultMessageUnload {
    @Unique
    private static final UUID OVERVAULT_VAULT_ID = UUID.fromString("69c0e010-06d2-4a26-88b9-81f185e193cc");

    @Mutable
    @Final
    @Shadow
    private UUID id;

    @Inject(method = "<init>(Liskallia/vault/core/vault/Vault;)V", at = @At("TAIL"))
    private void replaceNullId(Vault vault, CallbackInfo ci) {
        if(id == null) {
            id = OVERVAULT_VAULT_ID;
        }
    }

    @Inject(method = "handle", at = @At("HEAD"), cancellable = true)
    private static void onHandle(VaultMessage.Unload message, Supplier<NetworkEvent.Context> contextSupplier, CallbackInfo ci) {
        NetworkEvent.Context context = contextSupplier.get();
        context.enqueueWork(() -> {
            ClientVaults.ACTIVE.releaseClient();
            ClientVaults.ACTIVE = new Vault();
        });

        context.setPacketHandled(true);
        ci.cancel();
    }

//    @Mixin(value = VaultMessage.Unload.class, remap = false)
//    public interface Message
}
