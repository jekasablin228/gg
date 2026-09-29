package gg.mod.mixin;

import gg.mod.Hacks;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Block.class)
public abstract class BlockMixin {
    // X-Ray: draw every face of interesting blocks and no culled faces of anything else.
    @Inject(method = "shouldRenderFace", at = @At("HEAD"), cancellable = true)
    private static void ggmod$xray(BlockState state, BlockState neighbor, Direction face, CallbackInfoReturnable<Boolean> cir) {
        if (Hacks.xray()) cir.setReturnValue(Hacks.xrayVisible(state));
    }
}
