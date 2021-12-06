package cope.inferno.impl.features.module.modules.combat;

import cope.inferno.Inferno;
import cope.inferno.impl.features.module.Module;
import cope.inferno.impl.settings.Setting;
import cope.inferno.util.entity.InventoryUtil;
import net.minecraft.init.Items;

@Module.Define(name = "AutoBowRelease", category = Module.Category.Combat)
@Module.Info(description = "Automatically releases your bow for you")
public class AutoBowRelease extends Module {
    public final Setting<Integer> amount = new Setting<>("Amount", 4, 1, 30);

    @Override
    public String getDisplayInfo() {
        return String.valueOf(this.amount.getValue());
    }

    @Override
    public void onUpdate() {
        if (InventoryUtil.isHolding(Items.BOW, true) && mc.player.getItemInUseMaxCount() >= this.amount.getValue()) {
            Inferno.interactionManager.releaseItem();
        }
    }
}
