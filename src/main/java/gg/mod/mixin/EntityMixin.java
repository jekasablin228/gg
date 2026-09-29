package gg.mod.mixin;

import gg.mod.VanishState;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {
    // Vanish: the server stops sending the vanished player to everyone else's client.
    @Inject(method = "broadcastToPlayer", at = @At("HEAD"), cancellable = true)
    private void ggmod$hide(ServerPlayer viewer, CallbackInfoReturnable<Boolean> cir) {
        UUID vanished = VanishState.vanished;
        Entity self = (Entity) (Object) this;
        if (vanished != null && self != viewer && vanished.equals(self.getUUID())) {
            cir.setReturnValue(false);
        }
    }
}
