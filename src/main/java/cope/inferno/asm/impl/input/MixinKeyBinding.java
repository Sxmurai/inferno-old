package cope.inferno.asm.impl.input;

import cope.inferno.core.events.IsKeyPressedEvent;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyBinding.class)
public class MixinKeyBinding {
    @Shadow private boolean pressed;
    @Shadow private int keyCode;

    @Inject(method = "isKeyDown", at = @At("HEAD"), cancellable = true)
    public void isKeyDown(CallbackInfoReturnable<Boolean> info) {
        IsKeyPressedEvent event = new IsKeyPressedEvent(keyCode, pressed);
        MinecraftForge.EVENT_BUS.post(event);

        if (event.isCanceled()) {
            info.setReturnValue(event.isPressed());
        }
    }
}
