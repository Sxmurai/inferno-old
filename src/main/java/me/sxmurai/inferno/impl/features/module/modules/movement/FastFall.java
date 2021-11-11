package me.sxmurai.inferno.impl.features.module.modules.movement;

import me.sxmurai.inferno.util.world.BlockUtil;
import me.sxmurai.inferno.util.timing.TickTimer;
import me.sxmurai.inferno.impl.features.module.Module;
import me.sxmurai.inferno.impl.option.Option;
import net.minecraft.network.play.client.CPacketEntityAction;

@Module.Define(name = "FastFall", category = Module.Category.Movement)
@Module.Info(description = "Makes you fall faster")
public class FastFall extends Module {
    public final Option<Mode> mode = new Option<>("Mode", Mode.Motion);
    public final Option<Double> speed = new Option<>("Speed", 1.0, 0.1, 5.0);
    public final Option<Double> height = new Option<>("Height", 2.0, 1.0, 10.0);
    public final Option<Boolean> liquids = new Option<>("Liquids", false);

    private final TickTimer timer = new TickTimer();
    private boolean sneaking = false;

    @Override
    public void onTick() {
        if (BlockUtil.isInLiquid() && !this.liquids.getValue()) {
            return;
        }

        if (mc.player.onGround) {
            switch (this.mode.getValue()) {
                case Motion: {
                    mc.player.motionY -= this.speed.getValue();
                    break;
                }

                case Strict:
                case TickShift: {
                    for (double y = 0.0; y < this.height.getValue() + 0.5; y += 0.1) {
                        if (!mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(0.0, -y, 0.0)).isEmpty()) {
                            mc.player.motionY -= (mc.player.ticksExisted % 12 == 6 ? 0.152666 : this.speed.getValue());

                            // this works, but only like 20% of the time. the other 80% it will desync the FUCK out of you.
                            if (this.mode.getValue() == Mode.TickShift) {
                                if (this.timer.passed(this.speed.getValue().floatValue())) {
                                    this.timer.reset();
                                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, this.sneaking ? CPacketEntityAction.Action.STOP_SNEAKING : CPacketEntityAction.Action.START_SNEAKING));
                                    this.sneaking = !this.sneaking;
                                }
                            }
                        } else {
                            if (this.sneaking) {
                                this.sneaking = false;
                                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SNEAKING));
                            }
                        }
                    }
                    break;
                }
            }
        }
    }

    public enum Mode {
        Motion, Strict, TickShift
    }
}
