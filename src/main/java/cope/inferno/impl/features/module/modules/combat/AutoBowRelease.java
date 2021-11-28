package cope.inferno.impl.features.module.modules.combat;

import cope.inferno.Inferno;
import cope.inferno.impl.features.module.Module;
import cope.inferno.impl.settings.Setting;
import cope.inferno.util.entity.InventoryUtil;
import cope.inferno.util.timing.TickTimer;
import net.minecraft.init.Items;

@Module.Define(name = "AutoBowRelease", category = Module.Category.Combat)
@Module.Info(description = "Automatically releases your bow for you")
public class AutoBowRelease extends Module {
    public final Setting<Boolean> offhand = new Setting<>("Offhand", true);
    public final Setting<Integer> amount = new Setting<>("Amount", 4, 1, 30);
    public final Setting<Integer> delay = new Setting<>("Delay", 1, 0, 10);

    private final TickTimer timer = new TickTimer();

    @Override
    public String getDisplayInfo() {
        return String.valueOf(this.amount.getValue());
    }

    @Override
    public void onTick() {
        if (this.timer.passed(this.delay.getValue())) {
            this.timer.reset();

            if (InventoryUtil.isHolding(Items.BOW, this.offhand.getValue()) && mc.player.getItemInUseMaxCount() >= this.amount.getValue()) {
                Inferno.interactionManager.releaseItem();
            }
        }
    }
}