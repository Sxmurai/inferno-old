package cope.inferno.impl.features.module.modules.client;

import cope.inferno.Inferno;
import cope.inferno.impl.features.hud.HudComponent;
import cope.inferno.impl.features.module.Module;
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
