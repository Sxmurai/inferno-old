package me.sxmurai.inferno.asm.mixins.render;

import me.sxmurai.inferno.impl.features.module.modules.render.NoRender;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public class MixinItemRenderer {
    @Inject(method = "renderSuffocationOverlay", at = @At("HEAD"), cancellable = true)
    public void renderSuffocationOverlay(TextureAtlasSprite sprite, CallbackInfo info) {
        if (NoRender.INSTANCE.isOn() && NoRender.blocks.getValue()) {
            info.cancel();
        }
    }

    @Inject(method = "renderWaterOverlayTexture", at = @At("HEAD"), cancellable = true)
    public void renderWaterOverlayTexture(float partialTicks, CallbackInfo info) {
        if (NoRender.INSTANCE.isOn() && NoRender.blocks.getValue()) {
            info.cancel();
        }
    }

    @Inject(method = "renderFireInFirstPerson", at = @At("HEAD"), cancellable = true)
    public void renderFireInFirstPerson(CallbackInfo info) {
        if (NoRender.INSTANCE.isOn() && NoRender.fire.getValue()) {
            info.cancel();
        }
    }
}