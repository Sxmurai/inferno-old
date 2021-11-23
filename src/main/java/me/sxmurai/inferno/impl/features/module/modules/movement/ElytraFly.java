package me.sxmurai.inferno.impl.features.module.modules.movement;

import me.sxmurai.inferno.Inferno;
import me.sxmurai.inferno.impl.event.entity.MoveEvent;
import me.sxmurai.inferno.impl.event.entity.UpdateWalkingPlayerEvent;
import me.sxmurai.inferno.impl.event.network.PacketEvent;
import me.sxmurai.inferno.impl.features.module.Module;
import me.sxmurai.inferno.impl.settings.Setting;
import me.sxmurai.inferno.util.entity.MovementUtil;
import me.sxmurai.inferno.util.timing.Timer;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Module.Define(name = "ElytraFly", category = Module.Category.Movement)
@Module.Info(description = "Makes flying with an elytra better")
public class ElytraFly extends Module {
    public final Setting<Mode> mode = new Setting<>("Mode", Mode.Strict);
    public final Setting<Boolean> autoStart = new Setting<>("AutoStart", true);
    public final Setting<Double> horizontal = new Setting<>("Horizontal", 2.5, 0.1, 10.0);
    public final Setting<Double> veritcal = new Setting<>("Vertical", 0.5, 0.1, 5.0);
    public final Setting<Boolean> accept = new Setting<>("Accept", true);

    private final Timer timer = new Timer();

    @SubscribeEvent
    public void onMove(MoveEvent event) {
        if (mc.player.isElytraFlying()) {
            event.setX(mc.player.motionX);
            event.setY(mc.player.motionY);
            event.setZ(mc.player.motionZ);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent event) {
        if (this.autoStart.getValue() && !mc.player.isElytraFlying() && !mc.player.onGround) {
            if (this.timer.passedMs(1000L)) {
                this.timer.reset();
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
            }

            return;
        }

        if (mc.player.isElytraFlying()) {
            if (this.mode.getValue() != Mode.Packet) {
                mc.player.setVelocity(0.0, 0.0, 0.0);
            }

            double[] motion = MovementUtil.getDirectionalSpeed(this.horizontal.getValue() / 10.0);
            switch (this.mode.getValue()) {
                case Simple:
                case Packet: {
                    this.doBaseMovement(motion[0], motion[1]);
                    break;
                }

                case Strict: {
                    // doesn't work, i'll have to fix
                    this.doBaseMovement(motion[0], motion[1]);

                    if (!mc.gameSettings.keyBindJump.isKeyDown() && !mc.gameSettings.keyBindSneak.isKeyDown()) {
                        mc.player.motionY = -1.01E-4;
                    }
                    break;
                }
            }
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof SPacketPlayerPosLook && this.accept.getValue() && mc.player.isElytraFlying()) {
            SPacketPlayerPosLook packet = event.getPacket();
            mc.player.connection.sendPacket(new CPacketConfirmTeleport(packet.getTeleportId()));
            mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(packet.getX(), packet.getY(), packet.getZ(), packet.getYaw(), packet.getPitch(), mc.player.onGround));
        }
    }

    private void doBaseMovement(double x, double z) {
        mc.player.motionX = x;
        mc.player.motionZ = z;

        if (mc.gameSettings.keyBindJump.isKeyDown()) {
            if (this.mode.getValue() == Mode.Strict) {
                Inferno.rotationManager.setRotations(mc.player.rotationYaw, -35.0f);
            }

            mc.player.motionY = this.veritcal.getValue() / 10.0;
        } else if (mc.gameSettings.keyBindSneak.isKeyDown()) {
            if (this.mode.getValue() == Mode.Strict) {
                Inferno.rotationManager.setRotations(mc.player.rotationYaw, 42.0f);
            }

            mc.player.motionY = -(this.veritcal.getValue() / 10.0);
        }

        Vec3d vec = mc.player.getPositionVector().add(x, mc.player.motionY, z);

        mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(vec.x, vec.y, vec.z, mc.player.rotationYaw, Inferno.rotationManager.getPitch(true), false));

        if (this.mode.getValue() == Mode.Packet) {
            mc.player.setPosition(vec.x, vec.y, vec.z);
            mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(vec.x, vec.y - 1337.0, vec.z, mc.player.rotationYaw, mc.player.rotationPitch, false));
        }
    }

    public enum Mode {
        Simple, Packet, Strict
    }
}
