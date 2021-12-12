package cope.inferno.impl.features.module.modules.movement;

import cope.inferno.Inferno;
import cope.inferno.impl.event.entity.MoveEvent;
import cope.inferno.impl.event.entity.UpdateWalkingPlayerEvent;
import cope.inferno.impl.event.network.PacketEvent;
import cope.inferno.impl.features.module.Module;
import cope.inferno.impl.settings.Setting;
import cope.inferno.util.entity.MovementUtil;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Module.Define(name = "Speed", category = Module.Category.Movement)
@Module.Info(description = "Speeds you up")
public class Speed extends Module {
    public final Setting<Mode> mode = new Setting<>("Mode", Mode.Strafe);
    public final Setting<Boolean> boost = new Setting<>("Boost", false, () -> mode.getValue() == Mode.Strafe || mode.getValue() == Mode.StrictStrafe);

    private int strafeStage = 4;
    private double moveSpeed = 0.0;
    private double distanceTraveled = 0.0;
    private int timerTicks = 0;
    private boolean slow = false;

    private boolean goUp = false;

    @Override
    protected void onDeactivated() {
        strafeStage = 4;
        moveSpeed = 0.0;
        distanceTraveled = 0.0;
        timerTicks = 0;

        Inferno.tickManager.reset();

        goUp = false;
    }

    @Override
    public String getDisplayInfo() {
        return mode.getValue().name();
    }

    @Override
    public void onUpdate() {
        if (mode.getValue() == Mode.YPort && MovementUtil.isMoving()) {
            double[] motion = MovementUtil.getDirectionalSpeed(MovementUtil.getBaseNCPSpeed());
            mc.player.motionX = motion[0];
            mc.player.motionZ = motion[1];

            goUp = mc.player.onGround;
            if (!goUp) {
                mc.player.motionY = -1.0;
            } else {
                mc.player.jump();
                goUp = false;
            }
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent event) {
        if (event.getEra() == UpdateWalkingPlayerEvent.Era.PRE) {
            distanceTraveled = Math.sqrt(Math.pow(mc.player.prevPosX - mc.player.posX, 2) + Math.pow(mc.player.prevPosZ - mc.player.posZ, 2));
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof SPacketPlayerPosLook) {
            moveSpeed = MovementUtil.getBaseNCPSpeed();
            distanceTraveled = 0.0;
            strafeStage = 4;
        }
    }

    @SubscribeEvent
    public void onMove(MoveEvent event) {
        if (mode.getValue() == Mode.Strafe || mode.getValue() == Mode.StrictStrafe) {
            if (MovementUtil.isMoving()) {
                if (mc.player.onGround) {
                    strafeStage = 2; // jump upwards
                }

                if (round(mc.player.posY - (int) mc.player.posY) == round(1.38)) {
                    System.out.println(round(1.38));
                    mc.player.motionY -= 0.8;
                    event.setY(event.getY() - 0.09316090325960147);
                    mc.player.posY -= 0.09316090325960147;
                }

                if (boost.getValue()) {
                    ++timerTicks;
                    if (timerTicks > 3 && timerTicks < 20) {
                        Inferno.tickManager.setTicks(1.045f);
                        event.setX(mc.player.motionX * 1.019);
                        event.setZ(mc.player.motionZ * 1.019);
                    } else {
                        timerTicks = 0;
                        Inferno.tickManager.reset();
                    }
                }
            }

            if (strafeStage == 1) {
                moveSpeed = (1.38 * MovementUtil.getBaseNCPSpeed()) - 0.01;
                strafeStage = 2;
            } else if (strafeStage == 2) {
                if (MovementUtil.isMoving() && mc.player.onGround) {
                    event.setY(mc.player.motionY = MovementUtil.getJumpHeight());
                    moveSpeed *= slow ? 1.495 : 1.725;
                    slow = !slow;
                }

                strafeStage = 3;
            } else if (strafeStage == 3) {
                double diff = 0.66 * (distanceTraveled - MovementUtil.getBaseNCPSpeed());
                moveSpeed = distanceTraveled - diff;
                strafeStage = 4;
            } else if (strafeStage == 4) {
                if (!mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(0.0, mc.player.motionY, 0.0)).isEmpty() || (mc.player.collidedVertically && strafeStage > 1)) {
                    strafeStage = 1;
                    timerTicks = 0;
                }

                moveSpeed = distanceTraveled - distanceTraveled / 159.0;
            }

            if (mode.getValue() == Mode.StrictStrafe) {
                moveSpeed = Math.min(moveSpeed, 0.462);
            } else {
                moveSpeed = Math.max(MovementUtil.getBaseNCPSpeed(), moveSpeed);
            }

            float forward = mc.player.movementInput.moveForward,
                    strafe = mc.player.movementInput.moveStrafe,
                    yaw = mc.player.rotationYaw;

            if (forward == 0.0f && strafe == 0.0f) {
                event.setZ(0.0);
                event.setZ(0.0);
            } else if (forward != 0.0f && strafe != 0.0f) {
                forward *= Math.sin(0.7853981633974483);
                strafe *= Math.cos(0.7853981633974483);
            }

            double rad = Math.toRadians(yaw);
            double cos = Math.cos(rad);
            double sin = Math.sin(rad);

            event.setX(forward * moveSpeed * -sin + strafe * moveSpeed * cos);
            event.setZ(forward * moveSpeed * cos - strafe * moveSpeed * -sin);
        } else if (mode.getValue() == Mode.OnGround) {
            event.setX(event.getX() * 1.59000003337860);
            event.setZ(event.getZ() * 1.59000003337860);

            goUp = !goUp;
            if (goUp) {
                mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX + mc.player.motionX, mc.player.posY + 0.198, mc.player.posZ + mc.player.motionZ, false));
            }
        }
    }

    private double round(double value) {
        return new BigDecimal(value).setScale(3, RoundingMode.HALF_UP).doubleValue();
    }

    public enum Mode {
        Strafe, StrictStrafe, YPort, OnGround
    }
}
