package cope.inferno.impl.features.module.modules.miscellaneous;

import cope.inferno.impl.features.Wrapper;
import cope.inferno.impl.features.module.Module;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.world.GameType;

@Module.Define(name = "FakePlayer")
@Module.Info(description = "Spawns in a fake player")
public class FakePlayer extends Module {
    private EntityOtherPlayerMP fake = null;

    @Override
    protected void onActivated() {
        if (!fullNullCheck()) {
            this.toggle();
            return;
        }

        this.fake = new EntityOtherPlayerMP(Wrapper.mc.world, Wrapper.mc.player.getGameProfile());
        this.fake.copyLocationAndAnglesFrom(Wrapper.mc.player);
        this.fake.inventory.copyInventory(Wrapper.mc.player.inventory);
        this.fake.setHealth(20.0f);
        this.fake.setGameType(GameType.SURVIVAL);

        Wrapper.mc.world.spawnEntity(this.fake);
    }

    @Override
    protected void onDeactivated() {
        if (fullNullCheck()) {
            Wrapper.mc.world.removeEntity(this.fake);
            Wrapper.mc.world.removeEntityDangerously(this.fake);
        }

        this.fake = null;
    }
}
