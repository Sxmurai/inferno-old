package cope.inferno.core.features.module.movement;

import cope.inferno.core.features.module.Category;
import cope.inferno.core.features.module.Module;
import cope.inferno.core.setting.Setting;

public class Sprint extends Module {
    public Sprint() {
        super("Sprint", Category.MOVEMENT, "Automatically makes you sprint");
    }

    public static final Setting<Mode> mode = new Setting<>("Mode", Mode.LEGIT);

    @Override
    protected void onDisable() {
        if (nullCheck()) {
            mc.player.setSprinting(false);
        }
    }

    @Override
    public void onUpdate() {
        if (mode.getValue().equals(Mode.LEGIT) && (!mc.gameSettings.keyBindForward.isKeyDown() || mc.player.isSneaking() || mc.player.isHandActive() || mc.player.getFoodStats().getFoodLevel() <= 6 || mc.player.collidedHorizontally)) {
            return;
        }

        if (!mc.player.isSprinting()) {
            mc.player.setSprinting(true);
        }
    }

    public enum Mode {
        /**
         * Makes sprinting vanilla. Stops you from sprinting upon collision, hunger is below 3 bars, you're sneaking/using an item etc
         */
        LEGIT,

        /**
         * No check sprint, always sprinting.
         */
        RAGE
    }
}
