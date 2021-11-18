package me.sxmurai.inferno.impl.features.module.modules.player;

import me.sxmurai.inferno.impl.features.module.Module;
import me.sxmurai.inferno.impl.option.Option;
import me.sxmurai.inferno.util.entity.InventoryUtil;
import me.sxmurai.inferno.util.timing.Timer;
import me.sxmurai.inferno.util.world.BlockUtil;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

@Module.Define(name = "Scaffold", category = Module.Category.Player)
@Module.Info(description = "Places blocks under your feet")
public class Scaffold extends Module {
    public final Option<Boolean> tower = new Option<>("Tower", true);
    public final Option<InventoryUtil.Swap> swap = new Option<>("Swap", InventoryUtil.Swap.Legit);
    public final Option<Place> place = new Option<>("Place", Place.Vanilla);
    public final Option<Boolean> rotate = new Option<>("Rotate", true);
    public final Option<Boolean> swing = new Option<>("Swing", true);
    public final Option<Boolean> sneak = new Option<>("Sneak", false);

    private final Timer towerTimer = new Timer();

    @Override
    public void onUpdate() {
        BlockPos below = new BlockPos(mc.player.posX, mc.player.posY - 1.0, mc.player.posZ);
        if (mc.world.isAirBlock(below)) {
            EnumHand hand;
            int oldSlot = -1;
            if (InventoryUtil.isHolding(ItemBlock.class, true)) {
                hand = InventoryUtil.getHeld(EnumHand.MAIN_HAND).getItem() instanceof ItemBlock ? EnumHand.MAIN_HAND : InventoryUtil.getHeld(EnumHand.OFF_HAND).getItem() instanceof ItemBlock ? EnumHand.OFF_HAND : null;
            } else {
                if (this.swap.getValue() == InventoryUtil.Swap.None) {
                    return;
                }

                int slot = InventoryUtil.getHotbarItemSlot(ItemBlock.class, true);
                if (slot == -1) {
                    return;
                }

                hand = slot == 45 ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND;
                if (hand == EnumHand.MAIN_HAND) {
                    oldSlot = mc.player.inventory.currentItem;
                    InventoryUtil.swap(slot, this.swap.getValue());
                }
            }

            if (hand == null) {
                return;
            }

            EnumFacing direction = BlockUtil.getFacing(below);
            if (direction == null) {
                return;
            }

            BlockUtil.place(below, hand, this.place.getValue() == Place.Packet, this.sneak.getValue(), this.swing.getValue(), this.rotate.getValue());

            if (!mc.world.isAirBlock(below) && mc.gameSettings.keyBindJump.isKeyDown() && direction == EnumFacing.DOWN && this.tower.getValue()) {
                mc.player.motionX *= 0.3;
                mc.player.motionZ *= 0.3;
                mc.player.jump();

                if (this.towerTimer.passedMs(1200L)) {
                    this.towerTimer.reset();
                    mc.player.motionY = -0.28;
                }
            }

            if (oldSlot != -1) {
                InventoryUtil.swap(oldSlot, this.swap.getValue());
            }
        }
    }

    public enum Place {
        Vanilla, Packet
    }
}
