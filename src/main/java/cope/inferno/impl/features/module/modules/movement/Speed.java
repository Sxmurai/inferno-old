package cope.inferno.impl.features.module.modules.movement;

import cope.inferno.impl.event.entity.MoveEvent;
import cope.inferno.impl.event.entity.UpdateWalkingPlayerEvent;
import cope.inferno.impl.event.network.PacketEvent;
import cope.inferno.impl.features.Wrapper;
import cope.inferno.impl.features.module.Module;
import cope.inferno.impl.settings.EnumConverter;
import cope.inferno.impl.settings.Setting;
import cope.inferno.util.entity.MovementUtil;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.init.MobEffects;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Module.Define(name = "Speed", category = Module.Category.Movement)
@Module.Info(description = "vroom vroom skadadle skadoodle your dick is now a noodle")
public class Speed extends Module {
    public final Setting<Mode> mode = new Setting<>("Mode", Mode.Strafe);
    public final Setting<Float> timer = new Setting<>("Timer", 1.0f, 1.0f, 2.0f);

    public final Setting<Double> speed = new Setting<>("Speed", 1.2, 0.1, 5.0, () -> this.mode.getValue() == Mode.Vanilla || this.mode.getValue() == Mode.BHop);

    // strafe/strictstrafe
    private int stage = 2;
    private double moveSpeed = 0.0;
    private double lastDistance = 0.0;

    // yport/onground/bhop
    private boolean up = false;

    @Override
    public String getDisplayInfo() {
        return EnumConverter.getActualName(this.mode.getValue());
    }

    @Override
    public void onUpdate() {
        Wrapper.mc.timer.tickLength = 50.0f / this.timer.getValue();

        if (this.up) {
            switch (this.mode.getValue()) {
                case BHop:
                case YPort: {
                    Wrapper.mc.player.jump();
                    this.up = false;
                    break;
                }

                case OnGround: {
                    Wrapper.mc.player.motionY = 0.2;
                    this.up = false;
                    break;
                }
            }
        }
    }

    @SubscribeEvent
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent event) {
        if (event.getEra() == UpdateWalkingPlayerEvent.Era.PRE && (this.mode.getValue() == Mode.Strafe || this.mode.getValue() == Mode.StrictStrafe)) {
            EntityPlayerSP player = Wrapper.mc.player;
            this.lastDistance = Math.sqrt(Math.pow(player.prevPosX - player.posX, 2) + Math.pow(player.prevPosZ - player.posZ, 2));
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof SPacketPlayerPosLook) {
            this.stage = 4;
            this.moveSpeed = 0.0;
            this.lastDistance = 0.0;
        }
    }

    @SubscribeEvent
    public void onMove(MoveEvent event) {
        switch (this.mode.getValue()) {
            case Strafe:
            case StrictStrafe: {
                if (Wrapper.mc.player.onGround && MovementUtil.isMoving()) {
                    this.stage = 2; // jump up
                }

                switch (this.stage) {
                    case 0: {
                        this.moveSpeed = this.mode.getValue() == Mode.StrictStrafe ? 0.34219999999999994 : (1.27 * this.getNCPBaseSpeed());
                        this.lastDistance = 0.0;
                        ++this.stage;
                        break;
                    }

                    case 2: {
                        if (Wrapper.mc.player.onGround && MovementUtil.isMoving()) {
                            event.setY(Wrapper.mc.player.motionY = this.getJumpHeight());
                            this.moveSpeed *= 2.149;
                        }
                        break;
                    }

                    case 3: {
                        this.moveSpeed = this.lastDistance - 0.76 * (this.lastDistance - (this.mode.getValue() == Mode.StrictStrafe ? 0.2372 : this.getNCPBaseSpeed()));
                        break;
                    }

                    default: {
                        if (!Wrapper.mc.world.getCollisionBoxes(Wrapper.mc.player, Wrapper.mc.player.getEntityBoundingBox().offset(0.0, Wrapper.mc.player.motionY, 0.0)).isEmpty() || (Wrapper.mc.player.collidedVertically && this.stage > 0)) {
                            this.stage = 0;
                        }

                        this.moveSpeed = this.lastDistance - this.lastDistance / 159.0;
                        break;
                    }
                }

                this.moveSpeed = Math.max(this.moveSpeed, this.getNCPBaseSpeed());

                float forward = Wrapper.mc.player.movementInput.moveForward,
                        strafe = Wrapper.mc.player.movementInput.moveStrafe,
                        yaw = Wrapper.mc.player.rotationYaw;

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

                event.setX((forward * this.moveSpeed * -sin + strafe * this.moveSpeed * cos) * 0.99);
                event.setZ((forward * this.moveSpeed * cos - strafe * this.moveSpeed * -sin) * 0.99);
                this.stage++;

                break;
            }

            case OnGround: {
                event.setX(event.getX() * 1.590000033378601);
                event.setZ(event.getZ() * 1.590000033378601);

                if (this.up) {
                    event.setY(event.getY() + 0.3);
                }

                if (Wrapper.mc.player.onGround) {
                    this.up = !this.up;
                }
                break;
            }

            case YPort: {
                if (MovementUtil.isMoving()) {
                    this.up = Wrapper.mc.player.onGround;
                    double[] velocity = MovementUtil.getDirectionalSpeed(this.getNCPBaseSpeed() + (this.speed.getValue() / 10.0));
                    event.setX(velocity[0]);
                    if (!this.up) {
                        event.setY(-1.0);
                    }
                    event.setZ(velocity[1]);
                }
                break;
            }

            case Vanilla:
            case BHop: {
                if (Wrapper.mc.player.onGround && MovementUtil.isMoving()) {
                    this.up = true;
                }

                double[] velocity = MovementUtil.getDirectionalSpeed(this.speed.getValue() / 10.0);
                event.setX(velocity[0] * (up ? 1.167 : 1.0));
                event.setZ(velocity[1] * (up ? 1.167 : 1.0));
                break;
            }
        }
    }

    private double getNCPBaseSpeed() {
        double baseSpeed = 0.2873;
        if (Wrapper.mc.player.isPotionActive(MobEffects.SPEED)) {
            baseSpeed *= 1.0 + 0.2 * (Wrapper.mc.player.getActivePotionEffect(MobEffects.SPEED).getAmplifier() + 1);
        }

        return baseSpeed;
    }

    private double getJumpHeight() {
        double y = 0.3995;
        if (Wrapper.mc.player.isPotionActive(MobEffects.JUMP_BOOST)) {
            y += (Wrapper.mc.player.getActivePotionEffect(MobEffects.JUMP_BOOST).getAmplifier() + 1) * 0.1;
        }

        return y;
    }

    public enum Mode {
        Strafe, StrictStrafe, OnGround, YPort, Vanilla, BHop
    }
}
