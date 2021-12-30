package cope.inferno.core.features.module.other;

import cope.inferno.core.features.module.Category;
import cope.inferno.core.features.module.Module;
import cope.inferno.core.setting.Setting;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.world.GameType;

public class FakePlayer extends Module {
    public FakePlayer() {
        super("FakePlayer", Category.OTHER, "Spawns in a fake player");
    }

    public static final Setting<Mode> mode = new Setting<>("Mode", Mode.STATIC);

    private EntityOtherPlayerMP fakePlayer = null;

    @Override
    protected void onEnable() {
        if (!nullCheck()) {
            toggle();
            return;
        }

        spawn();
    }

    @Override
    protected void onDisable() {
        if (nullCheck()) {
            despawn();
        }
    }

    private void spawn() {
        despawn();

        fakePlayer = new EntityOtherPlayerMP(mc.world, mc.player.getGameProfile());
        fakePlayer.copyLocationAndAnglesFrom(mc.player);
        fakePlayer.inventory.copyInventory(mc.player.inventory);

        fakePlayer.setHealth(20.0f);
        fakePlayer.setGameType(GameType.SURVIVAL);

        mc.world.spawnEntity(fakePlayer);
    }

    public void despawn() {
        if (fakePlayer != null) {
            mc.world.removeEntity(fakePlayer);
            mc.world.removeEntityDangerously(fakePlayer);

            fakePlayer = null;
        }
    }

    public enum Mode {
        /**
         * Normal, default fake player
         */
        STATIC,

        /**
         * "Dynamic," it can take damage and pop totems for testing autocrystal and shiz
         * This is still a @todo
         */
        DYNAMIC
    }
}
