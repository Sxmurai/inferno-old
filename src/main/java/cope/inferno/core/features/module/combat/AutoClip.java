package cope.inferno.core.features.module.combat;

import cope.inferno.asm.duck.IEntityPlayer;
import cope.inferno.core.events.DeathEvent;
import cope.inferno.core.features.module.Category;
import cope.inferno.core.features.module.Module;
import cope.inferno.core.manager.managers.relationships.impl.Status;
import cope.inferno.core.setting.Setting;
import cope.inferno.util.internal.ChatUtil;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ScreenShotHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AutoClip extends Module {
    public AutoClip() {
        super("AutoClip", Category.COMBAT, "Automatically clips people on 2b2tpvp (cringe)");
    }

    public static final Setting<Mode> mode = new Setting<>("Mode", Mode.ALL);
    public static final Setting<Boolean> self = new Setting<>("Self", false);

    @SubscribeEvent
    public void onDeath(DeathEvent event) {
        // @todo: check for Aura, AutoCrystal, AutoTNT, AutoMinecart targets
        EntityPlayer player = event.getPlayer();

        if (player.equals(mc.player) && !self.getValue()) {
            return;
        }

        if (mode.getValue().equals(Mode.ENEMIES) && !((IEntityPlayer) player).isRelationship(Status.ENEMY)) {
            return;
        }

        if (mc.player.getDistance(player) <= 50.0f) {
            ScreenShotHelper.saveScreenshot(mc.gameDir, mc.displayWidth, mc.displayHeight, mc.framebuffer);
            ChatUtil.sendPrefixed("Clipped " + player.getName() + "!");
        }
    }

    public enum Mode {
        /**
         * Clips everyone
         */
        ALL,

        /**
         * Only clips your enemies
         */
        ENEMIES,
    }
}
