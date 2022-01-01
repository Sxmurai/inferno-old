package cope.inferno.core.features.module.combat;

import cope.inferno.core.features.module.Category;
import cope.inferno.core.features.module.Module;
import cope.inferno.core.setting.Setting;
import cope.inferno.util.entity.EntityUtil;
import cope.inferno.util.entity.inventory.InventoryUtil;
import cope.inferno.util.entity.inventory.task.Task;
import cope.inferno.util.internal.timing.Format;
import cope.inferno.util.internal.timing.Timer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemSword;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class AutoTotem extends Module {
    public AutoTotem() {
        super("AutoTotem", Category.COMBAT, "Automatically places a totem in your offhand");
    }

    public static final Setting<Mode> mode = new Setting<>("Mode", Mode.TOTEM);
    public static final Setting<Float> health = new Setting<>("Health", 16.0f, 1.0f, 20.0f);
    public static final Setting<Gapple> gapple = new Setting<>("Gapple", Gapple.SWORD);
    public static final Setting<Boolean> hotbar = new Setting<>("Hotbar", true);
    public static final Setting<Integer> delay = new Setting<>("Delay", 1, 0, 10);

    private final Queue<Task> tasks = new ConcurrentLinkedQueue<>();
    private final Timer timer = new Timer();

    @Override
    protected void onDisable() {
        tasks.clear();
    }

    @Override
    public void onUpdate() {
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

            if (health.getValue() >= EntityUtil.getHealth(mc.player) || willDieWithFallDamage()) {
                doMagic(Items.TOTEM_OF_UNDYING);
                return;
            }

            if (!gapple.getValue().equals(Gapple.NEVER) && mc.gameSettings.keyBindUseItem.isKeyDown()) {
                if (gapple.getValue().equals(Gapple.ALWAYS) || (gapple.getValue().equals(Gapple.SWORD) && InventoryUtil.isHolding(ItemSword.class, false))) {
                    doMagic(Items.GOLDEN_APPLE);
                    return;
                }
            }

            doMagic(mode.getValue().item);
        }
    }

    private void doMagic(Item item) {
        if (mc.player.getHeldItemOffhand().getItem().equals(item) ||
                (mc.player.getHeldItemMainhand().getItem().equals(Items.GOLDEN_APPLE) && item.equals(Items.GOLDEN_APPLE))) {

            return;
        }

        int slot = InventoryUtil.getInventoryItem(item, null, hotbar.getValue());
        if (slot == -1) {
            return;
        }

        tasks.add(new Task(slot, hotbar.getValue()));
        tasks.add(new Task(InventoryUtil.OFFHAND_SLOT, false));
        if (!mc.player.getHeldItemOffhand().isEmpty()) {
            tasks.add(new Task(slot, hotbar.getValue()));
        }
    }

    private boolean willDieWithFallDamage() {
        return ((mc.player.fallDistance - 3.0f) / 2.0f) + 3.5f >= EntityUtil.getHealth(mc.player);
    }

    public enum Mode {
        TOTEM(Items.TOTEM_OF_UNDYING),
        GAPPLE(Items.GOLDEN_APPLE),
        CRYSTAL(Items.END_CRYSTAL);

        private final Item item;

        Mode(Item item) {
            this.item = item;
        }
    }

    public enum Gapple {
        /**
         * Never allow a right click gap
         */
        NEVER,

        /**
         * Only allow offhand gap swap when a sword is welded in the main hand
         */
        SWORD,

        /**
         * Always offhand gap if the use key is down
         */
        ALWAYS
    }
}
