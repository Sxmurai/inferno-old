package cope.inferno.asm.impl.render;

import cope.inferno.core.features.module.render.CameraClip;
import net.minecraft.client.renderer.EntityRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {
    @ModifyVariable(method = "orientCamera", at = @At("STORE"), ordinal = 3)
    public double orientCameraX(double distance) {
        return CameraClip.INSTANCE.isToggled() ? CameraClip.distance.getValue() : distance;
    }

    @ModifyVariable(method = "orientCamera", at = @At("STORE"), ordinal = 7)
    public double orientCameraZ(double distance) {
        return CameraClip.INSTANCE.isToggled() ? CameraClip.distance.getValue() : distance;
    }
}
