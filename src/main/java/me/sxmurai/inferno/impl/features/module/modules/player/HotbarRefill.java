package me.sxmurai.inferno.impl.features.module.modules.player;

import me.sxmurai.inferno.util.entity.InventoryUtil;
import me.sxmurai.inferno.util.timing.TickTimer;
import me.sxmurai.inferno.impl.features.module.Module;
import me.sxmurai.inferno.impl.option.Option;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Module.Define(name = "HotbarRefill", category = Module.Category.Player)
@Module.Info(description = "Refills slots in your hotbar")
public class HotbarRefill extends Module {
    public final Option<Integer> delay = new Option<>("Delay", 2, 0, 16);
    public final Option<Integer> actions = new Option<>("Actions", 3, 1, 10);
    public final Option<Integer> threshold = new Option<>("Threshold", 45, 0, 63);
    public final Option<Boolean> shiftClick = new Option<>("ShiftClick", false);
    public final Option<Boolean> update = new Option<>("Update", true);

    private final Map<Integer, ItemStack> hotbar = new HashMap<>();
    private final Queue<InventoryUtil.Task> tasks = new ConcurrentLinkedQueue<>();
    private final TickTimer timer = new TickTimer();

    @Override
    protected void onDeactivated() {
        this.hotbar.clear();
    }

    @Override
    public void onTick() {
        if (this.hotbar.isEmpty()) {
            this.recordHotbar();
        }

        for (int i = 0; i < 9; ++i) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.getItem() != this.hotbar.get(i).getItem() || stack.stackSize < this.threshold.getValue() || stack.getMaxStackSize() > stack.stackSize) {
                this.refill(i);
            }
        }

        if (!this.tasks.isEmpty() && this.timer.passed(this.delay.getValue())) {
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

    private void refill(int slot) {
        ItemStack hotbarStack = mc.player.inventory.getStackInSlot(slot);

        int id = -1;
        for (int i = 9; i < 36; ++i) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.isEmpty || stack.getItem() != hotbarStack.getItem()) {
                continue;
            }

            if (!stack.getDisplayName().equals(hotbarStack.getDisplayName())) {
                continue;
            }

            if (hotbarStack.getItem() instanceof ItemBlock) {
                if (((ItemBlock) stack.getItem()).getBlock() != ((ItemBlock) hotbarStack.getItem()).getBlock()) {
                    continue;
                }
            }

            id = i;
            break;
        }

        if (id == -1) {
            return;
        }

        this.tasks.add(new InventoryUtil.Task(id, this.update.getValue(), this.shiftClick.getValue()));
        if (!this.shiftClick.getValue()) {
            int actualSlot = slot < 9 ? slot + 36 : slot;
            this.tasks.add(new InventoryUtil.Task(actualSlot, this.update.getValue(), false));
            if (mc.player.inventoryContainer.getSlot(actualSlot).getHasStack()) {
                this.tasks.add(new InventoryUtil.Task(id, this.update.getValue(), this.shiftClick.getValue())); // put back
            }
        }
    }

    private void recordHotbar() {
        this.hotbar.clear();
        for (int i = 0; i < 9; ++i) {
            this.hotbar.put(i, mc.player.inventory.getStackInSlot(i));
        }
    }
}
