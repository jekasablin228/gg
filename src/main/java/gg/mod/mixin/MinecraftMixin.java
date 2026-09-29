package gg.mod.mixin;

import gg.mod.Hacks;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin {
    @Inject(method = "tick", at = @At("TAIL"), require = 1)
    private void ggmod$tick(CallbackInfo ci) {
        Hacks.onClientTick((Minecraft) (Object) this);
    }

    // ESP: other players get the vanilla glowing outline, visible through walls.
    @Inject(method = "shouldEntityAppearGlowing", at = @At("HEAD"), cancellable = true)
    private void ggmod$glow(Entity entity, CallbackInfoReturnable<Boolean> cir) {
        if (Hacks.esp && entity instanceof Player && entity != ((Minecraft) (Object) this).player) {
            cir.setReturnValue(true);
        }
    }
}
