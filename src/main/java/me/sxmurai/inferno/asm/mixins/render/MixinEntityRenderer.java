package me.sxmurai.inferno.asm.mixins.render;

import com.google.common.base.Predicate;
import me.sxmurai.inferno.impl.features.module.modules.player.Interact;
import me.sxmurai.inferno.impl.features.module.modules.render.NoRender;
import me.sxmurai.inferno.impl.features.module.modules.render.ViewClip;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.AxisAlignedBB;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {
    @Shadow
    @Final
    private Minecraft mc;

    @Shadow
    private boolean debugView;

    @Shadow
    private ItemStack itemActivationItem;

    @Inject(method = "getFOVModifier", at = @At("HEAD"), cancellable = true)
    public void getFOVModifier(float partialTicks, boolean useFOVSetting, CallbackInfoReturnable<Float> info) {
        if (NoRender.INSTANCE.isOn() && NoRender.fov.getValue()) {
            if (debugView) {
                info.setReturnValue(90.0f);
            } else {
                info.setReturnValue(useFOVSetting ? mc.gameSettings.fovSetting : 70.0f);
            }
        }
    }

    @Inject(method = "hurtCameraEffect", at = @At("HEAD"), cancellable = true)
    public void hurtCameraEffect(float partialTicks, CallbackInfo info) {
        if (NoRender.INSTANCE.isOn() && NoRender.hurtcam.getValue()) {
            info.cancel();
        }
    }

    @ModifyVariable(method = "orientCamera", at = @At("STORE"), ordinal = 3)
    public double orientCameraX(double distance) {
        return ViewClip.INSTANCE.isOn() ? ViewClip.distance.getValue() : distance;
    }

    @ModifyVariable(method = "orientCamera", at = @At("STORE"), ordinal = 7)
    public double orientCameraZ(double distance) {
        return ViewClip.INSTANCE.isOn() ? ViewClip.distance.getValue() : distance;
    }

    @Inject(method = "renderRainSnow", at = @At("HEAD"), cancellable = true)
    public void renderRainSnow(float partialTicks, CallbackInfo info) {
        if (NoRender.INSTANCE.isOn() && NoRender.weather.getValue()) {
            info.cancel();
        }
    }

    @Inject(method = "setupFog", at = @At("HEAD"), cancellable = true)
    public void setupFog(int startCoords, float partialTicks, CallbackInfo info) {
        if (NoRender.INSTANCE.isOn() && NoRender.fog.getValue()) {
            info.cancel();
        }
    }

    @Inject(method = "renderItemActivation", at = @At("HEAD"), cancellable = true)
    public void renderItemActivation(int p_190563_1_, int p_190563_2_, float p_190563_3_, CallbackInfo info) {
        if (NoRender.INSTANCE.isOn() && NoRender.totems.getValue() && this.itemActivationItem != null && this.itemActivationItem.getItem() == Items.TOTEM_OF_UNDYING) {
            info.cancel();
        }
    }

    @Redirect(method = "getMouseOver", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/WorldClient;getEntitiesInAABBexcluding(Lnet/minecraft/entity/Entity;Lnet/minecraft/util/math/AxisAlignedBB;Lcom/google/common/base/Predicate;)Ljava/util/List;"))
    public List<Entity> hookGetEntitiesInAABBExcluding(WorldClient client, Entity entity, AxisAlignedBB box, Predicate predicate) {
        if (Interact.INSTANCE.isOn() && Interact.noEntityTrace.getValue()) {
            return new ArrayList<>();
        }

        return client.getEntitiesInAABBexcluding(entity, box, predicate);
    }
}