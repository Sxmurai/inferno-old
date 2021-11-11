package me.sxmurai.inferno.impl.features.module.modules.player;

import me.sxmurai.inferno.util.entity.InventoryUtil;
import me.sxmurai.inferno.util.timing.TickTimer;
import me.sxmurai.inferno.impl.features.module.Module;
import me.sxmurai.inferno.impl.option.Option;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;

@Module.Define(name = "FastUse", category = Module.Category.Player)
@Module.Info(description = "Uses items faster")
public class FastUse extends Module {
    public final Option<Integer> speed = new Option<>("Speed", 0, 0, 4);
    public final Option<Integer> delay = new Option<>("Delay", 0, 0, 10);
    public final Option<Boolean> offhand = new Option<>("Offhand", true);
    public final Option<Boolean> everything = new Option<>("Everything", false);

    public final Option<Boolean> exp = new Option<>("Exp", false, () -> !this.everything.getValue());
    public final Option<Boolean> crystals = new Option<>("Crystals", false, () -> !this.everything.getValue());
    public final Option<Boolean> blocks = new Option<>("Blocks", false, () -> !this.everything.getValue());
    public final Option<Boolean> fireworks = new Option<>("Fireworks", false, () -> !this.everything.getValue());

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
