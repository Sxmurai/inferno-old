package me.sxmurai.inferno.impl.features.module.modules.combat;

import me.sxmurai.inferno.api.entity.EntityUtil;
import me.sxmurai.inferno.api.entity.InventoryUtil;
import me.sxmurai.inferno.api.timing.TickTimer;
import me.sxmurai.inferno.impl.features.module.Module;
import me.sxmurai.inferno.impl.option.Option;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.EnumHand;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Module.Define(name = "Offhand", category = Module.Category.Combat)
@Module.Info(description = "Puts things in your offhand")
public class Offhand extends Module {
    public final Option<Mode> mode = new Option<>("Mode", Mode.Crystal);
    public final Option<Float> health = new Option<>("Health", 16.0f, 1.0f, 20.0f);
    public final Option<Float> fallDistance = new Option<>("FallDistance", 10.0f, 3.0f, 256.0f);
    public final Option<Gap> offhandGap = new Option<>("OffhandGap", Gap.Always);
    public final Option<Integer> delay = new Option<>("Delay", 2, 0, 10);
    public final Option<Integer> actions = new Option<>("Actions", 3, 1, 10);
    public final Option<Boolean> update = new Option<>("Update", true);

    private final Queue<InventoryUtil.Task> tasks = new ConcurrentLinkedQueue<>();
    private final TickTimer timer = new TickTimer();
    private Item current = Items.TOTEM_OF_UNDYING;

    @Override
    public void onUpdate() {
        if (this.timer.passed(this.delay.getValue()) && !this.tasks.isEmpty()) {
            this.timer.reset();

            for (int i = 0; i < this.actions.getValue(); ++i) {
                InventoryUtil.Task task = this.tasks.poll();
                if (task == null) {
                    break;
                }

                task.run();
            }
        }

        Item curr = this.mode.getValue().getItem();
        if (EntityUtil.getHealth(mc.player) <= this.health.getValue() || mc.player.fallDistance >= this.fallDistance.getValue()) {
            curr = Items.TOTEM_OF_UNDYING;
            this.swap(curr);
            return;
        }

        if (this.offhandGap.getValue() != Gap.Never) {
            if ((this.offhandGap.getValue() == Gap.Always || this.offhandGap.getValue() == Gap.Sword) && InventoryUtil.isHolding(ItemSword.class, false)) {
                curr = Items.GOLDEN_APPLE;
            }

            if (this.offhandGap.getValue() == Gap.Always && mc.gameSettings.keyBindUseItem.isKeyDown()) {
                curr = Items.GOLDEN_APPLE;
            }
        }

        this.swap(curr);
    }

    private void swap(Item item) {
        ItemStack offhand = InventoryUtil.getHeld(EnumHand.OFF_HAND);
        if (offhand.getItem() == item || InventoryUtil.getHeld(EnumHand.MAIN_HAND).getItem() == item) {
            return;
        }

        this.current = item;

        int slot = -1;
        for (int i = 0; i < 36; ++i) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.getItem() == this.current) {
                slot = i < 9 ? i + 36 : i;
                break;
            }
        }

        if (slot == -1) {
            return;
        }

        this.tasks.add(new InventoryUtil.Task(slot, this.update.getValue(), false));
        this.tasks.add(new InventoryUtil.Task(InventoryUtil.OFFHAND_SLOT, this.update.getValue(), false));

        if (!offhand.isEmpty) {
            this.tasks.add(new InventoryUtil.Task(slot, this.update.getValue(), false)); // put the item back
        }
    }

    public enum Mode {
        Totem(Items.TOTEM_OF_UNDYING),
        GApple(Items.GOLDEN_APPLE),
        Crystal(Items.END_CRYSTAL),
        Exp(Items.EXPERIENCE_BOTTLE);

        private final Item item;
        Mode(Item item) {
            this.item = item;
        }

        public Item getItem() {
            return item;
        }
    }

    public enum Gap {
        Never, Sword, Always
    }
}
