package cope.inferno.core.features.module.player;

import cope.inferno.core.features.module.Category;
import cope.inferno.core.features.module.Module;
import cope.inferno.core.setting.Setting;
import cope.inferno.util.entity.inventory.InventoryUtil;
import cope.inferno.util.entity.inventory.task.Task;
import cope.inferno.util.internal.timing.Format;
import cope.inferno.util.internal.timing.Timer;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Replenish extends Module {
    public Replenish() {
        super("Replenish", Category.PLAYER, "Refills your hotbar with items from your inventory");
    }

    public static final Setting<Integer> threshold = new Setting<>("Threshold", 30, 0, 63);
    public static final Setting<Integer> delay = new Setting<>("Delay", 1, 0, 10);

    private final Map<Integer, ItemStack> hotbar = new HashMap<>();
    private final Queue<Task> tasks = new ConcurrentLinkedQueue<>();
    private final Timer timer = new Timer();

    @Override
    protected void onEnable() {
        if (nullCheck()) {
            save();
        }
    }

    @Override
    protected void onDisable() {
        hotbar.clear();
    }

    @Override
    public void onUpdate() {
        if (hotbar.isEmpty()) {
            save();
         }

        if (mc.currentScreen == null) {
            if (!tasks.isEmpty()) {
                if (!timer.passed(delay.getValue().longValue(), Format.TICKS)) {
                    return;
                }

                timer.reset();

                Task task = tasks.poll();
                if (task != null) {
                    task.runTask();
                    return;
                }
            }

            InventoryUtil.getSlots(0, 9).forEach((slot, stack) -> {
                int dynamicThreshold = stack.getMaxStackSize() != 64 ? stack.getMaxStackSize() - 1 : threshold.getValue();
                if (stack.getCount() <= dynamicThreshold) {
                    refill(slot, stack);
                }
            });
        }
    }

    private void refill(int slot, ItemStack stack) {
        int id = -1, count = 0;
        for (int i = 9; i < 36; ++i) {
            ItemStack itemStack = mc.player.inventory.getStackInSlot(i);
            if (InventoryUtil.canStacksCombine(itemStack, stack)) {
                id = i;
                count = itemStack.getCount();

                break;
            }
        }

        if (id == -1) {
            return;
        }

        tasks.add(new Task(id, true));
        tasks.add(new Task(slot, true));
        if (stack.getCount() + count > stack.getMaxStackSize()) {
            tasks.add(new Task(id, true));
        }
    }

    private void save() {
        hotbar.clear();
        hotbar.putAll(InventoryUtil.getSlots(0, 9));
    }
}
