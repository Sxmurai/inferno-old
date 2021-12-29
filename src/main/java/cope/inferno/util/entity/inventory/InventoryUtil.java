package cope.inferno.util.entity.inventory;

import cope.inferno.util.internal.Wrapper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
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

        for (Map.Entry<Integer, ItemStack> entry : getSlots(0, 9)) {
            ItemStack stack = entry.getValue();
            if (!stack.isEmpty() &&
                    clazz.isInstance(stack.getItem()) &&
                    (filter != null && filter.test(stack.getItem()))) {

                return entry.getKey();
            }
        }

        return -1;
    }

    /**
     * Gets all slots between two indexes inclusive
     * @param start The start index
     * @param end The ending index
     * @return A set of Map.Entry's containing the slot number and the ItemStack object
     */
    public static Set<Map.Entry<Integer, ItemStack>> getSlots(int start, int end) {
        Map<Integer, ItemStack> slots = new HashMap<>();

        for (int i = start; i < end; ++i) {
            slots.put(i, mc.player.inventory.getStackInSlot(i));
        }

        return slots.entrySet();
    }
}
