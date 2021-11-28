package cope.inferno.asm.mixins.input;

import cope.inferno.impl.features.Wrapper;
import cope.inferno.impl.features.module.modules.movement.NoSlow;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.settings.KeyBinding;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyBinding.class)
public class MixinKeyBinding {
    @Shadow
    public boolean pressed;

    @Inject(method = "isKeyDown", at = @At("HEAD"), cancellable = true)
    public void hookIsKeyDown(CallbackInfoReturnable<Boolean> info) {
        if (NoSlow.INSTANCE != null && NoSlow.INSTANCE.isOn()) {
            if (!(Wrapper.mc.currentScreen instanceof GuiChat) && NoSlow.INSTANCE.guiMove.getValue()) {
                info.setReturnValue(this.pressed);
            }
        }
    }
}
