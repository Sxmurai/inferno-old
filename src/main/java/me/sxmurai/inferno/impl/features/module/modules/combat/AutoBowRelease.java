package me.sxmurai.inferno.impl.features.module.modules.combat;

import me.sxmurai.inferno.impl.settings.Setting;
import me.sxmurai.inferno.util.entity.InventoryUtil;
import me.sxmurai.inferno.util.timing.TickTimer;
import me.sxmurai.inferno.impl.features.module.Module;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.CPacketPlayerDigging;

@Module.Define(name = "AutoBowRelease", category = Module.Category.Combat)
@Module.Info(description = "Automatically releases your bow for you")
public class AutoBowRelease extends Module {
    public final Setting<Boolean> offhand = new Setting<>("Offhand", true);
    public final Setting<Integer> amount = new Setting<>("Amount", 4, 1, 30);
    public final Setting<Integer> delay = new Setting<>("Delay", 1, 0, 10);

    private final TickTimer timer = new TickTimer();

    @Override
    public void onTick() {
        if (this.timer.passed(this.delay.getValue())) {
            this.timer.reset();

            if (InventoryUtil.isHolding(Items.BOW, this.offhand.getValue()) && mc.player.getItemInUseMaxCount() >= this.amount.getValue()) {
                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, mc.player.getPosition(), mc.player.getHorizontalFacing()));
                mc.player.stopActiveHand();
            }
        }
    }
}
