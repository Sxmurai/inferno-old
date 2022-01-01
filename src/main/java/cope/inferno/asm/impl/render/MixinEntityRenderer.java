package cope.inferno.asm.impl.render;

import com.google.common.base.Predicate;
import cope.inferno.core.features.module.player.Interact;
import cope.inferno.core.features.module.render.CameraClip;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.ArrayList;
import java.util.List;

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

    @Redirect(method = "getMouseOver", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;getEntitiesInAABBexcluding(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;Lcom/google/common/base/Predicate;)Ljava/util/List;"))
    public List<Entity> getEntitiesInAABBexcluding(WorldClient world, Entity entityIn, AxisAlignedBB boundingBox, Predicate<? super Entity> predicate) {
        return Interact.INSTANCE.isToggled() && Interact.shouldNoEntityTrace() ?
                new ArrayList<>() :
                world.getEntitiesInAABBexcluding(entityIn, boundingBox, predicate);
    }
}
