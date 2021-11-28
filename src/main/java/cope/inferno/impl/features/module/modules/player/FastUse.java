package cope.inferno.impl.features.module.modules.player;

import cope.inferno.impl.features.module.Module;
import cope.inferno.impl.settings.Setting;
import cope.inferno.util.entity.InventoryUtil;
import cope.inferno.util.timing.TickTimer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;

@Module.Define(name = "FastUse", category = Module.Category.Player)
@Module.Info(description = "Uses items faster")
public class FastUse extends Module {
    public final Setting<Integer> speed = new Setting<>("Speed", 0, 0, 4);
    public final Setting<Integer> delay = new Setting<>("Delay", 0, 0, 10);
    public final Setting<Boolean> offhand = new Setting<>("Offhand", true);
    public final Setting<Boolean> everything = new Setting<>("Everything", false);

    public final Setting<Boolean> exp = new Setting<>("Exp", false, () -> !this.everything.getValue());
    public final Setting<Boolean> crystals = new Setting<>("Crystals", false, () -> !this.everything.getValue());
    public final Setting<Boolean> blocks = new Setting<>("Blocks", false, () -> !this.everything.getValue());
    public final Setting<Boolean> fireworks = new Setting<>("Fireworks", false, () -> !this.everything.getValue());

    private final TickTimer timer = new TickTimer();

    @Override
    public void onTick() {
        if (this.timer.passed(this.delay.getValue())) {
            this.timer.reset();

            if (this.everything.getValue()) {
                mc.rightClickDelayTimer = this.speed.getValue();
            } else {
                if (this.exp.getValue() && InventoryUtil.isHolding(Items.EXPERIENCE_BOTTLE, this.offhand.getValue())) {
                    mc.rightClickDelayTimer = this.speed.getValue();
                }

                if (this.crystals.getValue() && InventoryUtil.isHolding(Items.END_CRYSTAL, this.offhand.getValue())) {
                    mc.rightClickDelayTimer = this.speed.getValue();
                }

                if (this.blocks.getValue() && InventoryUtil.isHolding(ItemBlock.class, this.offhand.getValue())) {
                    mc.rightClickDelayTimer = this.speed.getValue();
                }

                if (this.fireworks.getValue() && InventoryUtil.isHolding(Items.FIREWORKS, this.offhand.getValue())) {
                    mc.rightClickDelayTimer = this.speed.getValue();
                }
            }
        }
    }
}
