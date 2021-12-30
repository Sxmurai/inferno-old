package cope.inferno.core.features.module.movement;

import cope.inferno.core.features.module.Category;
import cope.inferno.core.features.module.Module;
import cope.inferno.core.setting.Setting;
import cope.inferno.util.network.NetworkUtil;
import net.minecraft.network.play.client.CPacketPlayer;

public class NoFall extends Module {
    public NoFall() {
        super("NoFall", Category.MOVEMENT, "Negates fall damage");
    }

    public static final Setting<Mode> mode = new Setting<>("Mode", Mode.PACKET);

    @Override
    public void onUpdate() {
        if (mc.player.fallDistance >= 3.0f) {
            switch (mode.getValue()) {
                case PACKET: {
                    NetworkUtil.send(new CPacketPlayer(true));
                    break;
                }

                case RUBBERBAND: {
                    NetworkUtil.send(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.0, mc.player.posZ, false));
                    break;
                }
            }
        }
    }

    public enum Mode {
        /**
         * Normal onGround packet
         */
        PACKET,

        /**
         * Tries to MLG clutch
         */
        WATER,

        /**
         * Sends an invalid move packet
         */
        RUBBERBAND,

        /**
         * A NCP NoFall bypass
         */
        NCP
    }
}
