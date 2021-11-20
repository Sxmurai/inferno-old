package me.sxmurai.inferno.asm.mixins.render;

import me.sxmurai.inferno.Inferno;
import me.sxmurai.inferno.impl.features.Wrapper;
import me.sxmurai.inferno.impl.features.module.modules.render.Nametags;
import me.sxmurai.inferno.impl.features.module.modules.render.PopChams;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.RenderPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(RenderPlayer.class)
public class MixinRenderPlayer {
    private float renderPitch;
    private float renderYaw;
    private float renderHeadYaw;
    private float prevRenderHeadYaw;
    private float lastRenderHeadYaw = 0.0f;
    private float prevRenderPitch;
    private float lastRenderPitch = 0.0f;

    @Inject(method = "doRender", at = @At("HEAD"))
    public void doRenderPre(AbstractClientPlayer player, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo info) {
        if (player == Wrapper.mc.player && Inferno.rotationManager.isValid()) {
            this.renderPitch = player.rotationPitch;
            this.renderYaw = player.rotationYaw;

            this.renderHeadYaw = player.rotationYawHead;

            this.prevRenderHeadYaw = player.prevRotationYawHead;
            this.prevRenderPitch = player.prevRotationPitch;

            player.rotationPitch = Inferno.rotationManager.getPitch();
            player.rotationYaw = Inferno.rotationManager.getYaw();

            player.rotationYawHead = Inferno.rotationManager.getYaw();
            player.renderYawOffset = Inferno.rotationManager.getYaw();

            player.prevRotationYawHead = this.lastRenderHeadYaw;
            player.prevRotationPitch = this.lastRenderPitch;
        }
    }

    @Inject(method = "doRender", at = @At("RETURN"))
    public void doRenderPost(AbstractClientPlayer player, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo info) {
        if (player == Wrapper.mc.player && Inferno.rotationManager.isValid()) {
            this.lastRenderHeadYaw = player.rotationYawHead;
            this.lastRenderPitch = player.rotationPitch;

            player.rotationPitch = this.renderPitch;
            player.rotationYaw = this.renderYaw;

            player.prevRotationPitch = this.prevRenderPitch;

            player.rotationYawHead = this.renderHeadYaw;
            player.prevRotationYawHead = this.prevRenderHeadYaw;
        }
    }

    @Inject(method = "renderEntityName", at = @At("HEAD"), cancellable = true)
    protected void renderEntityName(AbstractClientPlayer entityIn, double x, double y, double z, String name, double distanceSq, CallbackInfo info) {
        if (PopChams.INSTANCE.isOn() && PopChams.INSTANCE.pops.containsKey(entityIn.entityId)) {
            info.cancel();
            return;
        }

        if (Nametags.INSTANCE.isOn()) {
            info.cancel();
        }
    }
}