package me.sxmurai.inferno.impl.features.module.modules.combat;

import me.sxmurai.inferno.impl.features.module.Module;
import me.sxmurai.inferno.impl.settings.Setting;
import me.sxmurai.inferno.util.entity.InventoryUtil;
import me.sxmurai.inferno.util.timing.TickTimer;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Module.Define(name = "AutoArmor", category = Module.Category.Combat)
@Module.Info(description = "Automatically equips the best pieces of armor")
public class AutoArmor extends Module {
    public final Setting<Priority> priority = new Setting<>("Priority", Priority.Blast);
    public final Setting<Boolean> noBinding = new Setting<>("NoBinding", true);
    public final Setting<Integer> delay = new Setting<>("Delay", 2, 0, 10);
    public final Setting<Integer> actions = new Setting<>("Actions", 3, 1, 10);
    public final Setting<Boolean> update = new Setting<>("Update", true);

    private final TickTimer timer = new TickTimer();
    private final Queue<InventoryUtil.Task> tasks = new ConcurrentLinkedQueue<>();

    @Override
    public void onUpdate() {
        if (this.tasks.isEmpty()) {
            Map<Integer, Integer> armor = new HashMap<>(); // slot, value
            int[] bestSlots = new int[] { -1, -1, -1, -1 };

            for (int i = 0; i < 4; ++i) {
                Item item = mc.player.inventory.armorInventory.get(i).getItem();
                armor.put(i, item instanceof ItemArmor ? ((ItemArmor) item).damageReduceAmount : -1);
            }

            for (int i = 0; i < 36; ++i) {
                ItemStack stack = mc.player.inventory.getStackInSlot(i);
                if (stack.isEmpty() || !(stack.getItem() instanceof ItemArmor)) {
                    continue;
                }

                ItemArmor item = (ItemArmor) stack.getItem();
                if (!this.noBinding.getValue() && EnchantmentHelper.hasBindingCurse(stack)) {
                    continue;
                }

                int type = item.armorType.getIndex();
                if (armor.get(type) < item.damageReduceAmount) {
                    bestSlots[type] = i;
                    armor.put(type, item.damageReduceAmount);
                }
            }

            for (int i = 0; i < 4; ++i) {
                int best = bestSlots[i];
                if (best == -1) {
                    continue;
                }

                int slot = best < 9 ? best + 36 : best;

                this.tasks.add(new InventoryUtil.Task(slot, this.update.getValue(), false));
                this.tasks.add(new InventoryUtil.Task(8 - i, this.update.getValue(), false));
                if (!mc.player.inventory.armorInventory.get(i).isEmpty()) {
                    this.tasks.add(new InventoryUtil.Task(slot, this.update.getValue(), false)); // put it back
                }
            }
        } else {
            if (!this.timer.passed(this.delay.getValue())) {
                return;
            }

            this.timer.reset();
            for (int i = 0; i < this.actions.getValue(); ++i) {
                InventoryUtil.Task task = this.tasks.poll();
                if (task == null) {
                    break;
                }

                task.run();
            }
        }
    }

    public enum Priority {
        Prot, Blast
    }
}
