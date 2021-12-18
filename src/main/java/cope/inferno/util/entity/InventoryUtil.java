package cope.inferno.util.entity;

import cope.inferno.Inferno;
import cope.inferno.util.Util;
import net.minecraft.block.Block;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;

import java.util.HashMap;
import java.util.Map;

public class InventoryUtil implements Util {
    public static final int OFFHAND_SLOT = 45;

    public static int getItemCount(Item item) {
        return mc.player.inventory.mainInventory.stream()
                .filter((stack) -> stack.getItem().equals(item))
                .mapToInt((stack) -> stack.stackSize) // in case we have stacked totems
                .sum();
    }

    public static int getInventoryItemSlot(Item item, boolean hotbar) {
        for (int i = hotbar ? 0 : 9; i < 36; ++i) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (!stack.isEmpty() && stack.getItem() == item) {
                return i;
            }
        }

        return -1;
    }

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

    public static Map<Integer, ItemStack> getSlots(int from, int to) {
        Map<Integer, ItemStack> slots = new HashMap<>();
        for (int i = from; i < to; ++i) {
            slots.put(i, mc.player.inventory.getStackInSlot(i));
        }

        return slots;
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
            Inferno.inventoryManager.click(this.slot, this.shiftClick ? ClickType.QUICK_MOVE : ClickType.PICKUP);
            if (this.update) {
                mc.playerController.updateController();
            }
        }
    }
}
