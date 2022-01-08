package cope.inferno.asm.impl.input;

import cope.inferno.core.events.BlockPlaceEvent;
import cope.inferno.core.events.DamageBlockEvent;
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
import net.minecraftforge.common.MinecraftForge;
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
            return;
        }

        BlockPlaceEvent event = new BlockPlaceEvent(pos, direction, hand, vec);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            info.cancel();
        }
    }

    @Inject(method = "onPlayerDamageBlock", at = @At("HEAD"), cancellable = true)
    public void onPlayerDamageBlock(BlockPos posBlock, EnumFacing directionFacing, CallbackInfoReturnable<Boolean> info) {
        DamageBlockEvent event = new DamageBlockEvent(posBlock, directionFacing);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            info.cancel();
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
