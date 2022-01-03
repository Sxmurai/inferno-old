package cope.inferno.core.events;

import net.minecraft.client.renderer.tileentity.TileEntityRendererDispatcher;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class TileEntityRenderEvent extends Event {
    private final TileEntityRendererDispatcher rendererDispatcher;
    private final TileEntity tileEntity;
    private final double x, y, z;
    private final float partialTicks, alpha;
    private final int destroyStage;

    public TileEntityRenderEvent(TileEntityRendererDispatcher rendererDispatcher, TileEntity tileEntityIn, double x, double y, double z, float partialTicks, int destroyStage, float alpha) {
        this.rendererDispatcher = rendererDispatcher;
        this.tileEntity = tileEntityIn;
        this.x = x;
        this.y = y;
        this.z = z;
        this.partialTicks = partialTicks;
        this.alpha = alpha;
        this.destroyStage = destroyStage;
    }

    public TileEntityRendererDispatcher getRendererDispatcher() {
        return rendererDispatcher;
    }

    public TileEntity getTileEntity() {
        return tileEntity;
    }

    public double getX() {
        return x;
    }

    public double getY() {
        return y;
    }

    public double getZ() {
        return z;
    }

    public float getPartialTicks() {
        return partialTicks;
    }

    public float getAlpha() {
        return alpha;
    }

    public int getDestroyStage() {
        return destroyStage;
    }
}
