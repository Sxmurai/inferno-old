package cope.inferno.core.features.module.player;

import cope.inferno.core.features.module.Category;
import cope.inferno.core.features.module.Module;
import cope.inferno.core.setting.Setting;
import cope.inferno.util.entity.inventory.InventoryUtil;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;

public class FastPlace extends Module {
    public FastPlace() {
        super("FastPlace", Category.PLAYER, "Places things faster");
    }

    public static final Setting<Integer> speed = new Setting<>("Speed", 4, 0, 4);

    public static final Setting<Boolean> xp = new Setting<>("XP", true);
    public static final Setting<Boolean> blocks = new Setting<>("Blocks", false);
    public static final Setting<Boolean> crystals = new Setting<>("Crystals", false);
    public static final Setting<Boolean> fireworks = new Setting<>("Fireworks", false);

    @Override
    public void onUpdate() {
        // big ass if statement, ew
        if ((xp.getValue() && InventoryUtil.isHolding(Items.EXPERIENCE_BOTTLE, true) )||
                (blocks.getValue() && InventoryUtil.isHolding(ItemBlock.class, true)) ||
                (crystals.getValue() && InventoryUtil.isHolding(Items.END_CRYSTAL, true)) ||
                (fireworks.getValue() && InventoryUtil.isHolding(Items.FIREWORKS, true))) {

            mc.rightClickDelayTimer = 4 - speed.getValue();
        }
    }
}
