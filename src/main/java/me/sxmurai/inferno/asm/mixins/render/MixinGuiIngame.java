package me.sxmurai.inferno.asm.mixins.render;

import me.sxmurai.inferno.impl.features.module.modules.render.NoRender;
import net.minecraft.client.gui.GuiIngame;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.scoreboard.ScoreObjective;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GuiIngame.class)
public class MixinGuiIngame {
    @Inject(method = "renderPotionEffects", at = @At("HEAD"), cancellable = true)
    public void renderPotionEffects(ScaledResolution resolution, CallbackInfo info) {
        if (NoRender.INSTANCE.isOn() && NoRender.potions.getValue()) {
            info.cancel();
        }
    }

    @Inject(method = "renderScoreboard", at = @At("HEAD"), cancellable = true)
    public void renderScoreboard(ScoreObjective objective, ScaledResolution scaledRes, CallbackInfo info) {
        if (NoRender.INSTANCE.isOn() && NoRender.scoreboard.getValue()) {
            info.cancel();
        }
    }

    @Inject(method = "renderPumpkinOverlay", at = @At("HEAD"), cancellable = true)
    public void renderPumpkinOverlay(ScaledResolution scaledRes, CallbackInfo info) {
        if (NoRender.INSTANCE.isOn() && NoRender.pumpkin.getValue()) {
            info.cancel();
        }
    }

    @Inject(method = "renderPortal", at = @At("HEAD"), cancellable = true)
    public void renderPortal(float timeInPortal, ScaledResolution scaledRes, CallbackInfo info) {
        if (NoRender.INSTANCE.isOn() && NoRender.portals.getValue()) {
            info.cancel();
        }
    }
}