package cope.inferno.impl.features.module.modules.player;

import cope.inferno.impl.features.module.Module;
import cope.inferno.impl.settings.Setting;
import cope.inferno.util.entity.InventoryUtil;
import cope.inferno.util.timing.TickTimer;
import net.minecraft.init.Items;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Module.Define(name = "HotbarRefill", category = Module.Category.Player)
@Module.Info(description = "Refills slots in your hotbar")
public class HotbarRefill extends Module {
    public final Setting<Integer> threshold = new Setting<>("Threshold", 50, 0, 63);
    public final Setting<Integer> delay = new Setting<>("Delay", 1, 0, 10);

    private final Map<Integer, ItemStack> hotbar = new HashMap<>();
    private final Queue<InventoryUtil.Task> tasks = new ConcurrentLinkedQueue<>();
    private final TickTimer timer = new TickTimer();

    @Override
    protected void onDeactivated() {
        hotbar.clear();
        tasks.clear();
    }

    @Override
    public void onTick() {
        if (hotbar.isEmpty()) {
            if (!InventoryUtil.getSlots(0, 9).values().stream().allMatch((stack) -> stack.getItem().equals(Items.AIR))) {
                saveHotbar();
            }

            return; // do not continue on
        }

        if (!tasks.isEmpty() && timer.passed(delay.getValue())) {
            timer.reset();

            InventoryUtil.Task task = tasks.poll();
            if (task != null) {
                task.run();
                return;
            }
        }

        for (Map.Entry<Integer, ItemStack> entry : InventoryUtil.getSlots(0, 9).entrySet()) {
            refill(entry.getKey(), entry.getValue());
        }
    }

    private void refill(int slot, ItemStack stack) {
        if (!stack.isEmpty()) {
            int threshold = stack.getMaxStackSize() == 64 ? this.threshold.getValue() : stack.getMaxStackSize() - 1;
            if (stack.stackSize > threshold) {
                return;
            }
        }

        int inventorySlot = -1;
        for (Map.Entry<Integer, ItemStack> entry : InventoryUtil.getSlots(9, 36).entrySet()) {
            ItemStack val = entry.getValue();
            if (!stack.getDisplayName().equals(val.getDisplayName())) {
                continue;
            }

            if (stack.getItem() instanceof ItemBlock) {
                if (!(val.getItem() instanceof ItemBlock)) {
                    continue;
                }

                if (((ItemBlock) stack.getItem()).block != ((ItemBlock) val.getItem()).block) {
                    continue;
                }
            }

            inventorySlot = entry.getKey();
        }

        if (inventorySlot != -1) {
            tasks.add(new InventoryUtil.Task(inventorySlot, false, false));
            tasks.add(new InventoryUtil.Task(slot + 36, false, false));
            if (stack.stackSize + mc.player.inventory.getStackInSlot(inventorySlot).stackSize > stack.getMaxStackSize()) {
                tasks.add(new InventoryUtil.Task(inventorySlot, false, false));
            }
        }
    }

    private void saveHotbar() {
        hotbar.clear();
        hotbar.putAll(InventoryUtil.getSlots(0, 9));
    }
}
