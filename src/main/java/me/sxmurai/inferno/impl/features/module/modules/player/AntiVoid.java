package me.sxmurai.inferno.impl.features.module.modules.player;

import me.sxmurai.inferno.impl.features.module.Module;
import me.sxmurai.inferno.impl.settings.Setting;
import net.minecraft.network.play.client.CPacketPlayer;

@Module.Define(name = "AntiVoid", category = Module.Category.Player)
@Module.Info(description = "Stops you from falling into the void")
public class AntiVoid extends Module {
    public final Setting<Mode> mode = new Setting<>("Mode", Mode.Suspend);

    @Override
    public void onUpdate() {
        if (mc.player.posY <= 0.0) {
            switch (this.mode.getValue()) {
                case Suspend: {
                    mc.player.motionY = 0.0;
                    break;
                }

                case Glide: {
                    mc.player.motionY /= 16.0;
                    break;
                }

                case Teleport: {
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 4.0, mc.player.posZ, true));
                    mc.player.setPosition(mc.player.posX, mc.player.posY + 4.0, mc.player.posZ);
                    break;
                }
            }
        }
    }

    public enum Mode {
        Suspend, Glide, Teleport
    }
}
