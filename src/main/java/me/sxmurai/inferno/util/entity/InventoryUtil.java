package me.sxmurai.inferno.util.entity;

import me.sxmurai.inferno.util.timing.TickTimer;
import me.sxmurai.inferno.util.Util;
import net.minecraft.block.Block;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraft.util.EnumHand;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class InventoryUtil implements Util {
    public static final int OFFHAND_SLOT = 45;

    public static int getHotbarBlockSlot(Block block, boolean offhand) {
        Item off = getHeld(EnumHand.OFF_HAND).getItem();
        if (offhand && off instanceof ItemBlock && ((ItemBlock) off).getBlock() == block) {
            return OFFHAND_SLOT;
        }

        for (int i = 0; i < 9; ++i) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.getItem() instanceof ItemBlock && ((ItemBlock) stack.getItem()).getBlock() == block) {
                return i;
            }
        }

        return -1;
    }

    public static int getHotbarItemSlot(Item item, boolean offhand) {
        if (offhand && getHeld(EnumHand.OFF_HAND).getItem() == item) {
            return OFFHAND_SLOT;
        }

        for (int i = 0; i < 9; ++i) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (!stack.isEmpty && stack.getItem() == item) {
                return i;
            }
        }

        return -1;
    }

    public static int getHotbarItemSlot(Class<? extends Item> clazz, boolean offhand) {
        if (offhand && clazz.isInstance(getHeld(EnumHand.OFF_HAND).getItem())) {
            return OFFHAND_SLOT;
        }

        for (int i = 0; i < 9; ++i) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (!stack.isEmpty && clazz.isInstance(stack.getItem())) {
                return i;
            }
        }

        return -1;
    }

    public static boolean isHolding(Item item, boolean offhand) {
        return getHeld(EnumHand.MAIN_HAND).getItem() == item || (offhand && getHeld(EnumHand.OFF_HAND).getItem() == item);
    }

    public static boolean isHolding(Class<? extends Item> clazz, boolean offhand) {
        return clazz.isInstance(getHeld(EnumHand.MAIN_HAND).getItem()) || (offhand && clazz.isInstance(getHeld(EnumHand.OFF_HAND).getItem()));
    }

    public static ItemStack getHeld(EnumHand hand) {
        return mc.player.getHeldItem(hand);
    }

    public static void switchTo(int slot, boolean silent) {
        mc.player.connection.sendPacket(new CPacketHeldItemChange(slot));

        if (!silent) {
            mc.player.inventory.currentItem = slot;
        }

        mc.playerController.updateController();
    }

    public static class Task {
        private final int slot;
        private final boolean update;
        private final boolean shiftClick;

        public Task(int slot, boolean update, boolean shiftClick) {
            this.slot = slot;
            this.update = update;
            this.shiftClick = shiftClick;
        }

        public void run() {
            mc.playerController.windowClick(0, this.slot, 0, this.shiftClick ? ClickType.QUICK_MOVE : ClickType.PICKUP, mc.player);
            if (this.update) {
                mc.playerController.updateController();
            }
        }
    }

    public static class TaskHandler {
        private final Queue<Task> tasks = new ConcurrentLinkedQueue<>();
        private final TickTimer timer = new TickTimer();

        public void run(int ticks, int actions) {
            if (this.timer.passed(ticks)) {
                this.timer.reset();

                for (int i = 0; i < actions; ++i) {
                    Task task = this.tasks.poll();
                    if (task == null) {
                        break;
                    }

                    task.run();
                }
            }
        }

        public void add(Task task) {
            this.tasks.add(task);
        }
    }
}
