package cope.inferno.asm.mixins.input;

import cope.inferno.impl.event.world.DamageBlockEvent;
import cope.inferno.impl.event.world.DestroyBlockEvent;
import cope.inferno.impl.features.module.modules.player.Reach;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerControllerMP.class)
public class MixinPlayerControllerMP {
    @Inject(method = "getBlockReachDistance", at = @At("HEAD"), cancellable = true)
    public void getBlockReachDistance(CallbackInfoReturnable<Float> info) {
        if (Reach.INSTANCE.isOn()) {
            info.setReturnValue(Reach.range.getValue());
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

    @Inject(method = "onPlayerDestroyBlock", at = @At("HEAD"), cancellable = true)
    public void onPlayerDestroyBlock(BlockPos pos, CallbackInfoReturnable<Boolean> info) {
        MinecraftForge.EVENT_BUS.post(new DestroyBlockEvent(pos));
    }
}