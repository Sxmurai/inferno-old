package cope.inferno.asm.mixins.entity;

import cope.inferno.impl.event.entity.PushEvent;
import net.minecraft.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Entity.class)
public class MixinEntity {
    @Redirect(method = "applyEntityCollision", at = @At(value = "INVOKE", target = "Lnet/minecraft/entity/Entity;addVelocity(DDD)V"))
    public void hookAddVelocity(Entity entity, double x, double y, double z) {
        PushEvent.Entity event = new PushEvent.Entity(entity, x, y, z, entity.isAirBorne);
        MinecraftForge.EVENT_BUS.post(event);
        if (!event.isCanceled()) {
            entity.motionX += event.getX();
            entity.motionY += event.getY();
            entity.motionZ += event.getZ();
            entity.isAirBorne = event.isAirborne();
        }
    }
}
