package cope.inferno.core.features.module.combat;

import cope.inferno.core.features.module.Category;
import cope.inferno.core.features.module.Module;
import cope.inferno.core.setting.Setting;
import cope.inferno.util.network.NetworkUtil;
import net.minecraft.item.ItemBow;
import net.minecraft.network.play.client.CPacketPlayerDigging;

public class BowRelease extends Module {
    public BowRelease() {
        super("BowRelease", Category.COMBAT, "Automatically releases your bow");
    }

    public static final Setting<Integer> ticks = new Setting<>("Ticks", 3, 3, 20);

    @Override
    public void onUpdate() {
        if (mc.player.isHandActive() &&
                mc.player.getActiveItemStack().getItem() instanceof ItemBow &&
                mc.player.getItemInUseMaxCount() >= ticks.getValue()) {

            mc.player.stopActiveHand();
            NetworkUtil.send(new CPacketPlayerDigging(CPacketPlayerDigging.Action.RELEASE_USE_ITEM, mc.player.getPosition(), mc.player.getHorizontalFacing()));
        }
    }
}
