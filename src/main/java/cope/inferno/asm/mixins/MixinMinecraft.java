package cope.inferno.asm.mixins;

import cope.inferno.impl.features.module.modules.player.MultiTask;
import cope.inferno.Inferno;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MixinMinecraft {
    @Redirect(method = "sendClickBlockToController", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/EntityPlayerSP;isHandActive()Z"))
    public boolean hookIsHandActive(EntityPlayerSP player) {
        return MultiTask.INSTANCE.isOff() && player.isHandActive();
    }

    @Redirect(method = "rightClickMouse", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/PlayerControllerMP;getIsHittingBlock()Z"))
    public boolean hookGetIsHittingBlock(PlayerControllerMP controller) {
        return MultiTask.INSTANCE.isOff() && controller.getIsHittingBlock();
    }

    @Inject(method = "shutdown", at = @At("HEAD"), cancellable = true)
    public void shutdown(CallbackInfo info) {
        Inferno.configManager.saveConfigs();
    }
}
