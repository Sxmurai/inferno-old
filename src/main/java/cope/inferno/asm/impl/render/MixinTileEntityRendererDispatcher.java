package cope.inferno.asm.impl.render;

import cope.inferno.core.events.TileEntityRenderEvent;
import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(TileEntityRendererDispatcher.class)
public class MixinTileEntityRendererDispatcher {
    @Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/tileentity/TileEntityRendererDispatcher;render(Lnet/minecraft/tileentity/TileEntity;DDDFIF)V"))
    public void render(TileEntityRendererDispatcher rendererDispatcher, TileEntity tileEntityIn, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        TileEntityRenderEvent event = new TileEntityRenderEvent(rendererDispatcher, tileEntityIn, x, y, z, partialTicks, destroyStage, alpha);
        MinecraftForge.EVENT_BUS.post(event);

        if (!event.isCanceled()) {
            rendererDispatcher.render(tileEntityIn, x, y, z, partialTicks, destroyStage, alpha);
        }
    }
}
