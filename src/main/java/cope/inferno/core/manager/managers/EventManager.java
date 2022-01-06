package cope.inferno.core.manager.managers;

import cope.inferno.core.events.PacketEvent;
import cope.inferno.core.events.TotemPopEvent;
import cope.inferno.core.features.module.Module;
import cope.inferno.util.internal.Wrapper;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import org.lwjgl.opengl.GL11;

public class EventManager implements Wrapper {
    public static EventManager INSTANCE;

    public EventManager() {
        INSTANCE = this;
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof SPacketEntityStatus) {
            SPacketEntityStatus packet = event.getPacket();
            if (packet.getOpCode() == 35 && packet.getEntity(mc.world) instanceof EntityPlayer) {
                MinecraftForge.EVENT_BUS.post(new TotemPopEvent((EntityPlayer) packet.getEntity(mc.world)));
            }
        }
    }

    @SubscribeEvent
    public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        if (nullCheck() && event.getEntity().equals(mc.player)) {
            for (Module module : getInferno().getModuleManager().getModules()) {
                if (module.isToggled()) {
                    mc.profiler.startSection("update_" + module.getName());

                    module.onUpdate();

                    mc.profiler.endSection();
                }
            }

            getInferno().getHoleManager().onUpdate();
        }
    }

    @SubscribeEvent
    public void onClientTick(TickEvent.ClientTickEvent event) {
        if (nullCheck()) {
            for (Module module : getInferno().getModuleManager().getModules()) {
                if (module.isToggled()) {
                    mc.profiler.startSection("clientTick_" + module.getName());

                    module.onTick();

                    mc.profiler.endSection();
                }
            }

            getInferno().getTickManager().onTick();
        }
    }

    @SubscribeEvent
    public void onRenderWorldLast(RenderWorldLastEvent event) {
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.disableDepth();

        for (Module module : getInferno().getModuleManager().getModules()) {
            if (module.isToggled()) {
                mc.profiler.startSection("renderworld_" + module.getName());

                module.onRender3D();

                mc.profiler.endSection();
            }
        }

        GlStateManager.glLineWidth(1.0f);
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
        GlStateManager.enableDepth();
        GlStateManager.enableCull();
    }
}
