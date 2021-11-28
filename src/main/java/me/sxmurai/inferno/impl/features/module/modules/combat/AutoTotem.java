package me.sxmurai.inferno.impl.features.module.modules.combat;

import me.sxmurai.inferno.Inferno;
import me.sxmurai.inferno.impl.features.module.Module;
import me.sxmurai.inferno.impl.manager.InventoryManager;
import me.sxmurai.inferno.impl.settings.Setting;
import me.sxmurai.inferno.impl.ui.InfernoGUI;
import me.sxmurai.inferno.util.entity.EntityUtil;
import me.sxmurai.inferno.util.entity.InventoryUtil;
import me.sxmurai.inferno.util.timing.TickTimer;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.CPacketPlayerDigging;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Module.Define(name = "AutoTotem", category = Module.Category.Combat)
@Module.Info(description = "Automatically places a totem or another item in your offhand")
public class AutoTotem extends Module {
    public final Setting<Mode> mode = new Setting<>("Mode", Mode.Crystal);
    public final Setting<Float> health = new Setting<>("Health", 16.0f, 1.0f, 20.0f);
    public final Setting<Gap> gapple = new Setting<>("GApple", Gap.Sword);
    public final Setting<Integer> delay = new Setting<>("Delay", 1, 0, 20);
    public final Setting<Boolean> guis = new Setting<>("Guis", false);

    private final Queue<InventoryUtil.Task> tasks = new ConcurrentLinkedQueue<>();
    private final TickTimer timer = new TickTimer();

    @Override
    public String getDisplayInfo() {
        Item item = mc.player.getHeldItemOffhand().getItem();

        // hardcoded shit, cope harder
        if (item == Items.TOTEM_OF_UNDYING) {
            return "Totem";
        } else if (item == Items.GOLDEN_APPLE) {
            return "GApple";
        } else if (item == Items.END_CRYSTAL) {
            return "Crystal";
        } else if (item == Items.EXPERIENCE_BOTTLE) {
            return "EXP";
        }

        return null;
    }

    @Override
    public void onTick() {
        if (!this.guis.getValue() && !(mc.currentScreen == null || mc.currentScreen instanceof GuiChat || mc.currentScreen instanceof InfernoGUI)) {
            return;
        }

        if (!this.tasks.isEmpty() && this.timer.passed(this.delay.getValue())) {
            InventoryUtil.Task task = this.tasks.poll();
            if (task != null) {
                this.timer.reset();
                task.run();
                return;
            }
        }

        if (EntityUtil.getHealth(mc.player) <= this.health.getValue() || this.willDieIfFall()) {
            this.setInOffhand(Items.TOTEM_OF_UNDYING);
            return;
        }

        if (mc.gameSettings.keyBindUseItem.isKeyDown() && this.gapple.getValue() != Gap.Off) {
            if (this.gapple.getValue() == Gap.Sword && !InventoryUtil.isHolding(ItemSword.class, false)) {
                return;
            }

            this.setInOffhand(Items.GOLDEN_APPLE);
            return;
        }

        this.setInOffhand(this.mode.getValue().item);
    }

    // https://gaming.stackexchange.com/questions/10218/how-is-fall-damage-calculated-in-minecraft
    // i could add things like feather falling checks or absorption, but im lazy. so cope.
    private boolean willDieIfFall() {
        return (mc.player.fallDistance - 3.0f) / 2.0f >= EntityUtil.getHealth(mc.player);
    }

    private void setInOffhand(Item item) {
        if (mc.player.getHeldItemOffhand().getItem() == item) {
            return;
        }

        int slot = InventoryUtil.getInventoryItemSlot(item, true);
        if (slot == -1) {
            return; // -1 = item wasnt found
        }

        // there's no need to send window click packets if its already in our hotbar
        // maybe a delay added here would be beneficial?
        if (slot <= 9) {
            int oldSlot = mc.player.inventory.currentItem;
            Inferno.inventoryManager.swap(slot, InventoryManager.Swap.Legit);
            mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.SWAP_HELD_ITEMS, mc.player.getPosition(), mc.player.getHorizontalFacing()));
            Inferno.inventoryManager.swap(oldSlot, InventoryManager.Swap.Legit);
            return;
        }

        this.tasks.add(new InventoryUtil.Task(slot, true, false));
        this.tasks.add(new InventoryUtil.Task(InventoryUtil.OFFHAND_SLOT, true, false));
        if (!mc.player.getHeldItemOffhand().isEmpty()) {
            this.tasks.add(new InventoryUtil.Task(slot, true, false));
        }
    }

    public enum Mode {
        Totem(Items.TOTEM_OF_UNDYING),
        GApple(Items.GOLDEN_APPLE),
        Crystal(Items.END_CRYSTAL),
        EXP(Items.EXPERIENCE_BOTTLE);

        private final Item item;
        Mode(Item item) {
            this.item = item;
        }
    }

    public enum Gap {
        Off, Sword, Always
    }
}
