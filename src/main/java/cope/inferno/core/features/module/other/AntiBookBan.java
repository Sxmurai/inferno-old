package cope.inferno.core.features.module.other;

import com.mojang.realmsclient.gui.ChatFormatting;
import cope.inferno.core.features.module.Category;
import cope.inferno.core.features.module.Module;
import cope.inferno.core.setting.Setting;
import cope.inferno.util.entity.inventory.InventoryUtil;
import cope.inferno.util.internal.ChatUtil;
import net.minecraft.inventory.ClickType;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemWrittenBook;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.NonNullList;

public class AntiBookBan extends Module {
    public static AntiBookBan INSTANCE;

    public AntiBookBan() {
        super("AntiBookBan", Category.OTHER, "Tries to stop you from getting book banned");
        INSTANCE = this;
    }

    public static final Setting<Boolean> packet = new Setting<>("Packet", true);

    public static final Setting<Boolean> drop = new Setting<>("Drop", true);
    public static final Setting<Integer> threshold = new Setting<>(drop, "Threshold", 27, 1, 27);

    @Override
    public void onUpdate() {
        if (drop.getValue()) {
            InventoryUtil.getSlots(0, 36).forEach((slot, stack) -> {
                if (!(stack.getItem() instanceof ItemShulkerBox)) {
                    return;
                }

                NBTTagCompound nbt = stack.getTagCompound();
                if (nbt != null && nbt.hasKey("BlockEntityTag")) {
                    nbt = nbt.getCompoundTag("BlockEntityTag");
                    if (nbt.hasKey("Items")) {
                        NonNullList<ItemStack> slots = NonNullList.withSize(27, ItemStack.EMPTY);
                        ItemStackHelper.loadAllItems(nbt, slots);

                        int books = 0;
                        for (ItemStack itemStack : slots) {
                            if (itemStack.getItem() instanceof ItemWrittenBook) {
                                ++books;

                                // if we pass the threshold, we can just stop there and say fuck that
                                if (books >= threshold.getValue()) {
                                    mc.playerController.windowClick(0, InventoryUtil.toClickableSlot(slot), 0, ClickType.THROW, mc.player);
                                    ChatUtil.sendPrefixed("Found a possible book ban shulker! Found " + ChatFormatting.RESET + books + ChatFormatting.RESET + " books inside!");

                                    break;
                                }
                            }
                        }
                    }
                }
            });
        }
    }
}
