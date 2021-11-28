package cope.inferno.impl.features.module.modules.movement;

import cope.inferno.Inferno;
import cope.inferno.impl.event.entity.MoveEvent;
import cope.inferno.impl.event.entity.UpdateWalkingPlayerEvent;
import cope.inferno.impl.event.network.PacketEvent;
import cope.inferno.impl.features.Wrapper;
import cope.inferno.impl.features.module.Module;
import cope.inferno.impl.settings.EnumConverter;
import cope.inferno.impl.settings.Setting;
import cope.inferno.util.entity.MovementUtil;
import cope.inferno.util.timing.Timer;
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

    @Override
    public String getDisplayInfo() {
        return EnumConverter.getActualName(this.mode.getName());
    }

    @SubscribeEvent
    public void onMove(MoveEvent event) {
        if (Wrapper.mc.player.isElytraFlying()) {
            event.setX(Wrapper.mc.player.motionX);
            event.setY(Wrapper.mc.player.motionY);
            event.setZ(Wrapper.mc.player.motionZ);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent event) {
        if (this.autoStart.getValue() && !Wrapper.mc.player.isElytraFlying() && !Wrapper.mc.player.onGround) {
            if (this.timer.passedMs(1000L)) {
                this.timer.reset();
                Wrapper.mc.player.connection.sendPacket(new CPacketEntityAction(Wrapper.mc.player, CPacketEntityAction.Action.START_FALL_FLYING));
            }

            return;
        }

        if (Wrapper.mc.player.isElytraFlying()) {
            if (this.mode.getValue() != Mode.Packet) {
                Wrapper.mc.player.setVelocity(0.0, 0.0, 0.0);
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

                    if (!Wrapper.mc.gameSettings.keyBindJump.isKeyDown() && !Wrapper.mc.gameSettings.keyBindSneak.isKeyDown()) {
                        Wrapper.mc.player.motionY = -1.01E-4;
                    }
                    break;
                }
            }
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof SPacketPlayerPosLook && this.accept.getValue() && Wrapper.mc.player.isElytraFlying()) {
            SPacketPlayerPosLook packet = event.getPacket();
            Wrapper.mc.player.connection.sendPacket(new CPacketConfirmTeleport(packet.getTeleportId()));
            Wrapper.mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(packet.getX(), packet.getY(), packet.getZ(), packet.getYaw(), packet.getPitch(), Wrapper.mc.player.onGround));
        }
    }

    private void doBaseMovement(double x, double z) {
        Wrapper.mc.player.motionX = x;
        Wrapper.mc.player.motionZ = z;

        if (Wrapper.mc.gameSettings.keyBindJump.isKeyDown()) {
            if (this.mode.getValue() == Mode.Strict) {
                Inferno.rotationManager.setRotations(Wrapper.mc.player.rotationYaw, -35.0f);
            }

            Wrapper.mc.player.motionY = this.veritcal.getValue() / 10.0;
        } else if (Wrapper.mc.gameSettings.keyBindSneak.isKeyDown()) {
            if (this.mode.getValue() == Mode.Strict) {
                Inferno.rotationManager.setRotations(Wrapper.mc.player.rotationYaw, 42.0f);
            }

            Wrapper.mc.player.motionY = -(this.veritcal.getValue() / 10.0);
        }

        Vec3d vec = Wrapper.mc.player.getPositionVector().add(x, Wrapper.mc.player.motionY, z);

        Wrapper.mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(vec.x, vec.y, vec.z, Wrapper.mc.player.rotationYaw, Inferno.rotationManager.getPitch(true), false));

        if (this.mode.getValue() == Mode.Packet) {
            Wrapper.mc.player.setPosition(vec.x, vec.y, vec.z);
            Wrapper.mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(vec.x, vec.y - 1337.0, vec.z, Wrapper.mc.player.rotationYaw, Wrapper.mc.player.rotationPitch, false));
        }
    }

    public enum Mode {
        Simple, Packet, Strict
    }
}
