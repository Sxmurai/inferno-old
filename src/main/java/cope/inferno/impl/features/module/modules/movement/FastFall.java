package cope.inferno.impl.features.module.modules.movement;

import cope.inferno.impl.features.module.Module;
import cope.inferno.impl.settings.Setting;
import net.minecraft.entity.MoverType;
import net.minecraft.network.play.client.CPacketPlayer;

@Module.Define(name = "FastFall", category = Module.Category.Movement)
@Module.Info(description = "Makes you fall faster")
public class FastFall extends Module {
    public final Setting<Mode> mode = new Setting<>("Mode", Mode.Normal);
    public final Setting<Double> speed = new Setting<>("Speed", 1.0, 0.5, 5.0);
    public final Setting<Double> height = new Setting<>("Height", 2.0, 1.0, 10.0);

    @Override
    public void onUpdate() {
        if (mc.player.onGround) {
            for (double y = 0.0; y <= height.getValue() + 0.5; y += 0.1) {
                if (!mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().offset(0.0, -y, 0.0)).isEmpty()) {
                    if (mode.getValue().equals(Mode.Normal)) {
                        mc.player.connection.sendPacket(new CPacketPlayer(false));
                        mc.player.motionY = -speed.getValue();
                    }
                }
            }
        }
    }

    public enum Mode {
        Normal, Strict
    }
}
