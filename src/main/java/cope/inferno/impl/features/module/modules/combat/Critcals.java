package cope.inferno.impl.features.module.modules.combat;

import cope.inferno.impl.event.network.PacketEvent;
import cope.inferno.impl.features.module.Module;
import cope.inferno.impl.settings.EnumConverter;
import cope.inferno.impl.settings.Setting;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Module.Define(name = "Criticals", category = Module.Category.Combat)
@Module.Info(description = "Scores some critical hits for you")
public class Critcals extends Module {
    public final Setting<Mode> mode = new Setting<>("Mode", Mode.Packet);

    @Override
    public String getDisplayInfo() {
        return EnumConverter.getActualName(this.mode.getValue());
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if (event.getPacket() instanceof CPacketUseEntity) {
            CPacketUseEntity packet = event.getPacket();
            if (packet.getAction() != CPacketUseEntity.Action.ATTACK || !(packet.getEntityFromWorld(mc.world) instanceof EntityLivingBase) || !mc.player.onGround) {
                return;
            }

            mc.player.onGround = false;
            switch (this.mode.getValue()) {
                case Packet: {
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.3, mc.player.posZ, false));
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, false));
                    break;
                }

                case NCPStrict: {
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.062602401692772, mc.player.posZ, false));
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.0726023996066094, mc.player.posZ, false));
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, false));
                    break;
                }

                case Jump: {
                    mc.player.jump();
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY - 0.055, mc.player.posZ, false));
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, false));
                    break;
                }

                case MiniJump: {
                    mc.player.motionY = 0.2;
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY - 0.02, mc.player.posZ, false));
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY, mc.player.posZ, false));
                    break;
                }
            }
        }
    }

    public enum Mode {
        Packet, NCPStrict, Jump, MiniJump
    }
}
