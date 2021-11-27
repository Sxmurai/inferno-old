package me.sxmurai.inferno.impl.manager;

import com.google.common.base.Strings;
import me.sxmurai.inferno.Inferno;
import me.sxmurai.inferno.impl.event.entity.DeathEvent;
import me.sxmurai.inferno.impl.event.entity.TotemPopEvent;
import me.sxmurai.inferno.impl.event.network.ConnectionEvent;
import me.sxmurai.inferno.impl.event.network.PacketEvent;
import me.sxmurai.inferno.impl.event.network.SelfConnectionEvent;
import me.sxmurai.inferno.impl.features.Wrapper;
import me.sxmurai.inferno.impl.features.module.Module;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.SPacketEntityMetadata;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;
import org.lwjgl.opengl.GL11;

import java.util.UUID;

public class EventManager implements Wrapper {
    @SubscribeEvent
    public void onClientConnection(FMLNetworkEvent.ClientConnectedToServerEvent event) {
        MinecraftForge.EVENT_BUS.post(new SelfConnectionEvent(SelfConnectionEvent.Type.Connect));
    }

    @SubscribeEvent
    public void onClientDisconnection(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        MinecraftForge.EVENT_BUS.post(new SelfConnectionEvent(SelfConnectionEvent.Type.Disconnect));
    }

    @SubscribeEvent
    public void onUpdate(LivingEvent.LivingUpdateEvent event) {
        if (fullNullCheck() && event.getEntityLiving() == mc.player) {
            for (Module module : Inferno.moduleManager.getModules()) {
                if (module.isOff()) {
                    continue;
                }

                module.onUpdate();
            }

            Inferno.holeManager.onUpdate();
            Inferno.tickManager.onUpdate();
        }
    }

    @SubscribeEvent
    public void onTick(TickEvent.ClientTickEvent event) {
        if (fullNullCheck()) {
            for (Module module : Inferno.moduleManager.getModules()) {
                if (module.isOff()) {
                    continue;
                }

                module.onTick();
            }
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive event) {
        if (fullNullCheck()) {
            if (event.getPacket() instanceof SPacketEntityStatus) {
                SPacketEntityStatus packet = (SPacketEntityStatus) event.getPacket();
                if (packet.getOpCode() == 35 && packet.getEntity(mc.world) instanceof EntityPlayer) {
                    MinecraftForge.EVENT_BUS.post(new TotemPopEvent((EntityPlayer) packet.getEntity(mc.world)));
                }
            } else if (event.getPacket() instanceof SPacketPlayerListItem) {
                SPacketPlayerListItem packet = (SPacketPlayerListItem) event.getPacket();
                if (packet.getAction() != SPacketPlayerListItem.Action.ADD_PLAYER && packet.getAction() != SPacketPlayerListItem.Action.REMOVE_PLAYER) {
                    return;
                }

                if (!packet.getEntries().isEmpty()) {
                    for (SPacketPlayerListItem.AddPlayerData data : packet.getEntries()) {
                        if (Strings.isNullOrEmpty(data.getProfile().getName()) || data.getProfile().getId() == null) {
                            continue;
                        }

                        UUID id = data.getProfile().getId();
                        EntityPlayer player = mc.world.getPlayerEntityByUUID(id);
                        ConnectionEvent.Type type = packet.getAction() == SPacketPlayerListItem.Action.ADD_PLAYER ? ConnectionEvent.Type.JOIN : ConnectionEvent.Type.LEAVE;

                        MinecraftForge.EVENT_BUS.post(new ConnectionEvent(type, data.getProfile().getName(), id, player));
                    }
                }
            } else if (event.getPacket() instanceof SPacketEntityMetadata) {
                SPacketEntityMetadata packet = (SPacketEntityMetadata) event.getPacket();
                Entity entity = mc.world.getEntityByID(packet.getEntityId());
                if (!(entity instanceof EntityPlayer)) {
                    return;
                }

                EntityPlayer player = (EntityPlayer) entity;
                if (player.getHealth() <= 0.0f || player.isDead) {
                    MinecraftForge.EVENT_BUS.post(new DeathEvent(player));
                }
            }
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

            for (Module module : Inferno.moduleManager.getModules()) {
                if (module.isOn()) {
                    mc.profiler.startSection("renderworld_" + module.getName());
                    module.onRenderWorld();
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

    @SubscribeEvent
    public void onRenderHud(RenderGameOverlayEvent.Text event) {
        for (Module module : Inferno.moduleManager.getModules()) {
            if (module.isOn()) {
                mc.profiler.startSection("renderhud_" + module.getName());
                module.onRenderHud();
                mc.profiler.endSection();
            }
        }
    }
}
