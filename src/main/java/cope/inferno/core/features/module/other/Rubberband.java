package cope.inferno.core.features.module.other;

import cope.inferno.core.features.module.Category;
import cope.inferno.core.features.module.Module;
import cope.inferno.util.network.NetworkUtil;
import net.minecraft.network.play.client.CPacketPlayer;

public class Rubberband extends Module {
    public Rubberband() {
        super("Rubberband", Category.OTHER, "Creates an artificial rubberband");
    }

    @Override
    protected void onEnable() {
        if (nullCheck()) {
            NetworkUtil.send(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.0, mc.player.posZ, false));
        }

        toggle();
    }
}
