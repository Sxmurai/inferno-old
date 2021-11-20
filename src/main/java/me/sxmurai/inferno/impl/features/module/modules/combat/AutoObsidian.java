package me.sxmurai.inferno.impl.features.module.modules.combat;

import me.sxmurai.inferno.impl.event.entity.JumpEvent;
import me.sxmurai.inferno.impl.settings.Setting;
import me.sxmurai.inferno.util.world.BlockUtil;
import me.sxmurai.inferno.util.entity.InventoryUtil;
import me.sxmurai.inferno.util.timing.TickTimer;
import me.sxmurai.inferno.impl.features.module.Module;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

// @todo Centering of player. make it a good center too, not a shitty one kthx
@Module.Define(name = "AutoObsidian", category = Module.Category.Combat)
@Module.Info(description = "Automatically surrounds you in obsidian")
public class AutoObsidian extends Module {
    public final Setting<Integer> blocks = new Setting<>("Blocks", 1, 1, 5);
    public final Setting<Integer> delay = new Setting<>("Delay", 2, 0, 10);
    public final Setting<Boolean> bottom = new Setting<>("Bottom", true);
    public final Setting<Place> place = new Setting<>("Place", Place.Vanilla);
    public final Setting<Toggle> toggle = new Setting<>("Toggle", Toggle.Finished);
    public final Setting<Boolean> offhand = new Setting<>("Offhand", true);
    public final Setting<InventoryUtil.Swap> swap = new Setting<>("Swap", InventoryUtil.Swap.Legit);
    public final Setting<Boolean> rotate = new Setting<>("Rotate", true);
    public final Setting<Boolean> swing = new Setting<>("Swing", true);
    public final Setting<Boolean> sneak = new Setting<>("Sneak", false);

    // timing
    private int placed = 0;
    private final TickTimer timer = new TickTimer();

    // inventory handling
    private int oldSlot = -1;
    private EnumHand hand = null;

    private final Queue<BlockPos> positions = new ConcurrentLinkedQueue<>();

    @Override
    protected void onDeactivated() {
        if (fullNullCheck() && this.oldSlot != -1) {
            InventoryUtil.swap(this.oldSlot, this.swap.getValue());
        }

        this.oldSlot = -1;
        this.hand = null;

        this.placed = 0;
        this.timer.reset();

        this.positions.clear();
    }

    @Override
    public void onUpdate() {
        if (this.positions.isEmpty()) {
            if (this.oldSlot == -1 || this.hand != EnumHand.OFF_HAND) {
                int slot = InventoryUtil.getHotbarBlockSlot(Blocks.OBSIDIAN, this.offhand.getValue());
                if (slot == -1) {
                    this.toggle();
                    return;
                }

                this.hand = slot == 45 ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND;
                if (this.hand == EnumHand.MAIN_HAND) {
                    this.oldSlot = mc.player.inventory.currentItem;
                    InventoryUtil.swap(slot, this.swap.getValue());
                }

                mc.player.setActiveHand(this.hand);
            }

            BlockPos base = new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ);
            for (EnumFacing direction : EnumFacing.values()) {
                if (direction == EnumFacing.UP) {
                    continue;
                }

                if (direction == EnumFacing.DOWN && !this.bottom.getValue()) {
                    continue;
                }

                BlockPos neighbor = base.offset(direction);
                if (!mc.world.isAirBlock(neighbor)) {
                    continue;
                }

                this.positions.add(neighbor);
            }

            if (this.positions.isEmpty()) {
                if (this.toggle.getValue() == Toggle.Finished) {
                    this.toggle();
                } else if (this.toggle.getValue() == Toggle.Sneak) {
                    if (mc.player.isSneaking()) {
                        this.toggle();
                    }
                }
            }
        } else {
            if (this.placed >= this.blocks.getValue() && !this.timer.passed(this.delay.getValue())) {
                return;
            }

            this.placed = 0;
            this.timer.reset();

            for (int i = 0; i < this.blocks.getValue(); ++i) {
                BlockPos pos = this.positions.poll();
                if (pos == null) {
                    break;
                }

                BlockUtil.place(pos, this.hand, this.place.getValue() == Place.Packet, this.sneak.getValue(), this.swing.getValue(), this.rotate.getValue());
                ++this.placed;
            }
        }
    }

    @SubscribeEvent
    public void onJump(JumpEvent event) {
        if (this.positions.isEmpty() && event.getPlayer() == mc.player && this.toggle.getValue() == Toggle.Jump) {
            this.toggle();
        }
    }

    public enum Place {
        Vanilla, Packet
    }

    public enum Toggle {
        Manual, Jump, Sneak, Finished
    }
}
