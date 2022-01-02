package cope.inferno.util.entity.inventory;

import cope.inferno.util.internal.Wrapper;
import net.minecraft.block.Block;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Predicate;

public class InventoryUtil implements Wrapper {
    /**
     * The offhand slot id
     */
    public static int OFFHAND_SLOT = 45;

    /**
     * Converts client slot id to packet useable slot id
     * @param slot The slot number
     * @return The transformed slot
     */
    public static int toClickableSlot(int slot) {
        return slot < 9 ? slot + 36 : slot;
    }

    /**
     * Checks if we can combine two stacks
     * @param in The input stack
     * @param out Another input stack
     * @return true if they can combine, false if they cannot
     */
    public static boolean canStacksCombine(ItemStack in, ItemStack out) {
        if (!in.getDisplayName().equals(out.getDisplayName())) {
            return false;
        }

        if (isBlockItemStack(in) && isBlockItemStack(out)) {
            return ((ItemBlock) in.getItem()).getBlock().equals(((ItemBlock) out.getItem()).getBlock());
        } else {
            return in.getItem().equals(out.getItem());
        }
    }

    public static boolean isHolding(Block block, boolean offhand) {
        if (offhand) {
            ItemStack stack = mc.player.getHeldItemOffhand();
            if (isBlockItemStack(stack) && ((ItemBlock) stack.getItem()).getBlock().equals(block)) {
                return true;
            }
        }

        ItemStack stack = mc.player.getHeldItemMainhand();
        return isBlockItemStack(stack) && ((ItemBlock) stack.getItem()).getBlock().equals(block);
    }

    public static boolean isHolding(Item item, boolean offhand) {
        if (offhand && mc.player.getHeldItemOffhand().getItem().equals(item)) {
            return true;
        }

        return mc.player.getHeldItemMainhand().getItem().equals(item);
    }

    /**
     * Checks if the local player is holding an item
     * @param clazz The class to look for instances of in your hands
     * @param offhand If to check the offhand slot
     * @return true if either checks passed, false if neither passed
     */
    public static boolean isHolding(Class<? extends Item> clazz, boolean offhand) {
        if (offhand && clazz.isInstance(mc.player.getHeldItemOffhand().getItem())) {
            return true;
        }

        return clazz.isInstance(mc.player.getHeldItemMainhand().getItem());
    }

    public static int getHotbarBlock(Block block, Predicate<Item> filter, boolean offhand) {
        if (offhand) {
            ItemStack stack = mc.player.getHeldItemOffhand();
            if (isBlockItemStack(stack) && ((ItemBlock) stack.getItem()).getBlock().equals(block)) {
                return OFFHAND_SLOT;
            }
        }

        for (Map.Entry<Integer, ItemStack> entry : getSlots(0, 9).entrySet()) {
            ItemStack stack = entry.getValue();
            if (isBlockItemStack(stack) && ((ItemBlock) stack.getItem()).getBlock().equals(block)) {
                return entry.getKey();
            }
        }

        return -1;
    }

    /**
     * Gets an item slot for the item
     * @param item The item to look for
     * @param offhand If to check the offhand slot
     * @return -1 if none found, 45 if its in your offhand, or any int 1-9 for the slot id
     */
    public static int getHotbarItem(Item item, Predicate<Item> filter, boolean offhand) {
        if (offhand && mc.player.getHeldItemOffhand().getItem().equals(item)) {
            return OFFHAND_SLOT;
        }

        for (Map.Entry<Integer, ItemStack> entry : getSlots(0, 9).entrySet()) {
            ItemStack stack = entry.getValue();
            if (!stack.isEmpty() &&
                    stack.getItem().equals(item) &&
                    (filter == null || filter.test(stack.getItem()))) {

                return entry.getKey();
            }
        }

        return -1;
    }

    /**
     * Gets an item slot for the item class
     * @param clazz The class to look for instances of
     * @param offhand If to check the offhand slot
     * @return -1 if none found, 45 if its in your offhand, or any int 1-9 for the slot id
     */
    public static int getHotbarItem(Class<? extends Item> clazz, Predicate<Item> filter, boolean offhand) {
        if (offhand && clazz.isInstance(mc.player.getHeldItemOffhand().getItem())) {
            return OFFHAND_SLOT;
        }

        for (Map.Entry<Integer, ItemStack> entry : getSlots(0, 9).entrySet()) {
            ItemStack stack = entry.getValue();
            if (!stack.isEmpty() &&
                    clazz.isInstance(stack.getItem()) &&
                    (filter == null || filter.test(stack.getItem()))) {

                return entry.getKey();
            }
        }

        return -1;
    }

    /**
     * Gets an item slot for an item in the local players inventory
     * @param item The item to look for
     * @param filter The filter to... filter..
     * @param hotbar If to search the hotbar
     * @return -1 if none found, or the item slot of this item
     */
    public static int getInventoryItem(Item item, Predicate<Item> filter, boolean hotbar) {
        for (Map.Entry<Integer, ItemStack> entry : getSlots(hotbar ? 0 : 9, 36).entrySet()) {
            ItemStack stack = entry.getValue();

            if (!stack.isEmpty() &&
                    stack.getItem().equals(item) &&
                    (filter == null || filter.test(stack.getItem()))) {

                return entry.getKey();
            }
        }

        return -1;
    }

    /**
     * Checks if the item stack is for a block item
     * @param stack The stack to check
     * @return true if the item is a block, false if it is not
     */
    public static boolean isBlockItemStack(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ItemBlock;
    }

    /**
     * Gets all slots between two indexes inclusive
     * @param start The start index
     * @param end The ending index
     * @return A set of Map.Entry's containing the slot number and the ItemStack object
     */
    public static Map<Integer, ItemStack> getSlots(int start, int end) {
        Map<Integer, ItemStack> slots = new ConcurrentHashMap<>();

        for (int i = start; i < end; ++i) {
            slots.put(i, mc.player.inventory.getStackInSlot(i));
        }

        return slots;
    }
}
