package me.sxmurai.inferno.impl.features.module.modules.client;

import me.sxmurai.inferno.Inferno;
import me.sxmurai.inferno.impl.features.hud.HudComponent;
import me.sxmurai.inferno.impl.features.module.Module;
import net.minecraft.client.renderer.GlStateManager;

@Module.Define(name = "HUD", category = Module.Category.Client)
@Module.Info(description = "If to show the HUD")
public class HUD extends Module {
    @Override
    public void onRenderHud() {
        GlStateManager.pushMatrix();

        for (HudComponent component : Inferno.hudManager.getComponents()) {
            if (component.isVisible()) {
                component.render();
            }
        }

        GlStateManager.popMatrix();
    }
}
