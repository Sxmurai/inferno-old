package cope.inferno.impl.features.module.modules.movement;

import cope.inferno.Inferno;
import cope.inferno.impl.features.Wrapper;
import cope.inferno.impl.features.module.Module;
import cope.inferno.impl.settings.EnumConverter;
import cope.inferno.impl.settings.Setting;
import cope.inferno.util.world.BlockUtil;

@Module.Define(name = "FastFall", category = Module.Category.Movement)
@Module.Info(description = "Makes you fall faster")
public class FastFall extends Module {
    public final Setting<Mode> mode = new Setting<>("Mode", Mode.Motion);
    public final Setting<Double> speed = new Setting<>("Speed", 1.0, 0.1, 5.0);
    public final Setting<Double> height = new Setting<>("Height", 2.0, 1.0, 10.0);
    public final Setting<Boolean> liquids = new Setting<>("Liquids", false);

    private boolean resetTimer = false;

    @Override
    public String getDisplayInfo() {
        return EnumConverter.getActualName(this.mode.getName());
    }

    @Override
    protected void onDeactivated() {
        if (this.resetTimer) {
            Inferno.tickManager.reset();
        }
    }

    @Override
    public void onTick() {
        if (Wrapper.mc.player.onGround) {
            if (BlockUtil.isInLiquid() && !this.liquids.getValue()) {
                return;
            }

            if (this.mode.getValue() == Mode.Motion) {
                Wrapper.mc.player.motionY = -this.speed.getValue();
            } else {
                for (double y = 0.0; y < this.height.getValue() + 0.5; y += 0.1) {
                    if (!Wrapper.mc.world.getCollisionBoxes(Wrapper.mc.player, Wrapper.mc.player.getEntityBoundingBox().offset(0.0, -y, 0.0)).isEmpty()) {
                        this.resetTimer = true;
                        Inferno.tickManager.setTickLength(2.5f);
                        Wrapper.mc.player.motionY = -this.speed.getValue() / 1.25;
                    } else {
                        if (this.resetTimer) {
                            this.resetTimer = false;
                            Inferno.tickManager.reset();
                        }
                    }
                }
            }
        }

        if (Wrapper.mc.player.motionY < 0.0 && Wrapper.mc.player.onGround && this.resetTimer) {
            this.resetTimer = false;
            Inferno.tickManager.reset();
        }
    }

    public enum Mode {
        Motion, Strict
    }
}
