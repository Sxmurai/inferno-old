package cope.inferno.asm.impl.input;

import cope.inferno.core.features.module.player.Interact;
import cope.inferno.core.features.module.player.Reach;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.util.EnumActionResult;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerControllerMP.class)
public class MixinPlayerControllerMP {
    @Inject(method = "processRightClickBlock", at = @At("HEAD"), cancellable = true)
    public void processRightClickBlock(EntityPlayerSP player, WorldClient worldIn, BlockPos pos, EnumFacing direction, Vec3d vec, EnumHand hand, CallbackInfoReturnable<EnumActionResult> info) {
        if (Interact.INSTANCE.isToggled() && Interact.shouldNoInteract(pos)) {
            info.setReturnValue(EnumActionResult.FAIL);
        }
    }

    @Inject(method = "getBlockReachDistance", at = @At("HEAD"), cancellable = true)
    public void getBlockReachDistance(CallbackInfoReturnable<Float> info) {
        if (Reach.INSTANCE.isToggled()) {
            info.setReturnValue(Reach.distance.getValue());
        }
    }

    @Inject(method = "extendedReach", at = @At("RETURN"), cancellable = true)
    public void extendedReach(CallbackInfoReturnable<Boolean> info) {
        info.setReturnValue(info.getReturnValue() || Reach.INSTANCE.isToggled());
    }
}
