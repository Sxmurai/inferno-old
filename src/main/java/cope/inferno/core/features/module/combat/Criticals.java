package cope.inferno.core.features.module.combat;

import cope.inferno.core.events.PacketEvent;
import cope.inferno.core.features.module.Category;
import cope.inferno.core.features.module.Module;
import cope.inferno.core.setting.Setting;
import cope.inferno.util.network.NetworkUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Criticals extends Module {
    public Criticals() {
        super("Criticals", Category.COMBAT, "Makes your attacks into critical hits");
    }

    public static final Setting<Mode> mode = new Setting<>("Mode", Mode.PACKET);
    public static final Setting<Boolean> entityCheck = new Setting<>("EntityCheck", true);

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if (event.getPacket() instanceof CPacketUseEntity) {
            CPacketUseEntity packet = event.getPacket();
            Entity entity = packet.getEntityFromWorld(mc.world);
            if (entity == null && entityCheck.getValue()) {
                event.setCanceled(true); // dont try to attack a null entity
                return;
            }

            if (packet.getAction().equals(CPacketUseEntity.Action.ATTACK) &&
                    mc.player.onGround &&
                    !mc.player.isOnLadder() &&
                    !mc.player.isInWater() &&
                    !mc.player.isInLava() &&
                    (entityCheck.getValue() && entity instanceof EntityLivingBase)) {

                for (double offset : mode.getValue().getPositions()) {
                    NetworkUtil.send(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + offset, mc.player.posZ, false));
                }

                NetworkUtil.send(new CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, false));
            }
        }
    }

    public enum Mode {
        /**
         * Normal packet criticals, changes server y by 0.2
         */
        PACKET(0.2),

        /**
         * Bypasses NCP-Updated critical check
         */
        STRICT(0.062602401692772, 0.0726023996066094),

        /**
         * Jumps upwards client-side to make a critical hit
         */
        JUMP(0.98),

        /**
         * Goes up 0.1 in client side velocity
         */
        MINIJUMP(0.2);

        private final double[] positions;

        Mode(double... positions) {
            this.positions = positions;
        }

        public double[] getPositions() {
            return positions;
        }
    }
}
