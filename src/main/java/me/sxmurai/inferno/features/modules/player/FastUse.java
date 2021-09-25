package me.sxmurai.inferno.features.modules.player;

import me.sxmurai.inferno.events.mc.UpdateEvent;
import me.sxmurai.inferno.features.settings.Setting;
import me.sxmurai.inferno.managers.modules.Module;
import me.sxmurai.inferno.utils.InventoryUtils;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Module.Define(name = "FastUse", description = "Lets you use things fast", category = Module.Category.PLAYER)
public class FastUse extends Module {
    public final Setting<Integer> delay = this.register(new Setting<>("Delay", 0, 0, 20));
    public final Setting<Integer> speed = this.register(new Setting<>("Speed", 0, 0, 4));
    public final Setting<Boolean> offhand = this.register(new Setting<>("Offhand", true));
    public final Setting<Boolean> xp = this.register(new Setting<>("XP", false));
    public final Setting<Boolean> fireworks = this.register(new Setting<>("Fireworks", false));
    public final Setting<Boolean> crystals = this.register(new Setting<>("Crystals", false));
    public final Setting<Boolean> blocks = this.register(new Setting<>("Blocks", false));

    private int ticks = 0;

    @SubscribeEvent
    public void onUpdate(UpdateEvent event) {
        if (this.delay.getValue() != 0) {
            ++this.ticks;
            if (this.ticks <= this.delay.getValue()) {
                return;
            }

            this.ticks = 0;
        }

        if (this.xp.getValue() && InventoryUtils.isHolding(Items.EXPERIENCE_BOTTLE, this.offhand.getValue())) {
            mc.rightClickDelayTimer = this.speed.getValue();
        }

        if (this.fireworks.getValue() && InventoryUtils.isHolding(Items.FIREWORKS, this.offhand.getValue())) {
            mc.rightClickDelayTimer = this.speed.getValue();
        }

        if (this.crystals.getValue() && InventoryUtils.isHolding(Items.END_CRYSTAL, this.offhand.getValue())) {
            mc.rightClickDelayTimer = this.speed.getValue();
        }

        if (this.blocks.getValue() && InventoryUtils.isHolding(ItemBlock.class, this.offhand.getValue())) {
            mc.rightClickDelayTimer = this.speed.getValue();
        }
    }
}
