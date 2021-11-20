package me.sxmurai.inferno.impl.features.module.modules.movement;

import me.sxmurai.inferno.impl.event.entity.PushEvent;
import me.sxmurai.inferno.impl.event.network.PacketEvent;
import me.sxmurai.inferno.impl.features.module.Module;
import me.sxmurai.inferno.impl.settings.Setting;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Module.Define(name = "Velocity", category = Module.Category.Movement)
@Module.Info(description = "Modifies the velocity you take")
public class Velocity extends Module {
    public final Setting<Float> vertical = new Setting<>("Vertical", 0.0f, 0.0f, 100.0f);
    public final Setting<Float> horizontal = new Setting<>("Horizontal", 0.0f, 0.0f, 100.0f);

    public final Setting<Boolean> knockback = new Setting<>("Knockback", true);
    public final Setting<Boolean> explosions = new Setting<>("Explosions", true);
    public final Setting<Boolean> blocks = new Setting<>("Blocks", false);
    public final Setting<Boolean> liquid = new Setting<>("Liquid", false);
    public final Setting<Boolean> push = new Setting<>("NoPush", true);
    public final Setting<Boolean> bobbers = new Setting<>("Bobbers", false);

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive event) {
        if (fullNullCheck()) {
            if (event.getPacket() instanceof SPacketEntityVelocity) {
                if (!this.knockback.getValue()) {
                    return;
                }

                SPacketEntityVelocity packet = event.getPacket();
                if (packet.getEntityID() == mc.player.entityId) {
                    packet.motionX *= this.horizontal.getValue().intValue();
                    packet.motionY *= this.vertical.getValue().intValue();
                    packet.motionZ *= this.horizontal.getValue().intValue();
                }
            } else if (event.getPacket() instanceof SPacketExplosion) {
                if (!this.explosions.getValue()) {
                    return;
                }

                SPacketExplosion packet = event.getPacket();
                packet.motionX *= this.horizontal.getValue();
                packet.motionY *= this.vertical.getValue();
                packet.motionZ *= this.horizontal.getValue();
            } else if (event.getPacket() instanceof SPacketEntityStatus) {
                if (!this.bobbers.getValue()) {
                    return;
                }

                SPacketEntityStatus packet = event.getPacket();
                if (packet.getEntity(mc.world) instanceof EntityFishHook && packet.getOpCode() == 31) {
                    EntityFishHook hook = (EntityFishHook) packet.getEntity(mc.world);
                    if (hook.caughtEntity == mc.player) {
                        event.setCanceled(true);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onPush(PushEvent event) {
        if (event.getEntity() == mc.player) {
            switch (event.getMaterial()) {
                case BLOCKS:
                    event.setCanceled(this.blocks.getValue());
                    break;

                case LIQUID:
                    event.setCanceled(this.liquid.getValue());
                    break;

                case ENTITY: {
                    if (this.push.getValue()) {
                        PushEvent.Entity evt = (PushEvent.Entity) event;
                        if (this.vertical.getValue() == 0.0f && this.horizontal.getValue() == 0.0f) {
                            evt.setCanceled(true);
                            return;
                        }

                        evt.setX(evt.getX() * this.horizontal.getValue().doubleValue());
                        evt.setY(evt.getY() * this.vertical.getValue().doubleValue());
                        evt.setZ(evt.getZ() * this.horizontal.getValue().doubleValue());
                        break;
                    }
                }
            }
        }
    }
}
