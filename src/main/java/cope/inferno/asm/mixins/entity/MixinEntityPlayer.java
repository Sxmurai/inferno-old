package cope.inferno.asm.mixins.entity;

import cope.inferno.impl.event.entity.JumpEvent;
import cope.inferno.impl.event.entity.PushEvent;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityPlayer.class)
public class MixinEntityPlayer {
    @Inject(method = "jump", at = @At("HEAD"), cancellable = true)
    public void onJump(CallbackInfo info) {
        MinecraftForge.EVENT_BUS.post(new JumpEvent((EntityPlayer) (Object) this));
    }

    @Inject(method = "isPushedByWater", at = @At("HEAD"), cancellable = true)
    public void hookIsPushedByWater(CallbackInfoReturnable<Boolean> info) {
        PushEvent event = new PushEvent(PushEvent.Type.LIQUID, (Entity) (Object) this);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            info.setReturnValue(false);
        }
    }
}
