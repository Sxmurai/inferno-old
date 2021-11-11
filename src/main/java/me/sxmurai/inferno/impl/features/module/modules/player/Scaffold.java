package me.sxmurai.inferno.impl.features.module.modules.player;

import me.sxmurai.inferno.util.world.BlockUtil;
import me.sxmurai.inferno.util.entity.InventoryUtil;
import me.sxmurai.inferno.util.timing.Timer;
import me.sxmurai.inferno.impl.features.module.Module;
import me.sxmurai.inferno.impl.option.Option;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

@Module.Define(name = "Scaffold", category = Module.Category.Player)
@Module.Info(description = "Places blocks under your feet")
public class Scaffold extends Module {
    private static final BlockPos[] DIRECTION_OFFSETS = new BlockPos[] {
            new BlockPos(0, 0, 0),
            new BlockPos(-1, 0, 0),
            new BlockPos(1, 0, 0),
            new BlockPos(0, 0, -1),
            new BlockPos(0, 0, 1)
    };

    public final Option<Boolean> tower = new Option<>("Tower", true);
    public final Option<Boolean> offhand = new Option<>("Offhand", true);
    public final Option<Switch> switchTo = new Option<>("Switch", Switch.Legit);
    public final Option<Place> place = new Option<>("Place", Place.Vanilla);
    public final Option<Boolean> rotate = new Option<>("Rotate", true);
    public final Option<Boolean> swing = new Option<>("Swing", true);
    public final Option<Boolean> sneak = new Option<>("Sneak", false);

    private final Timer towerTimer = new Timer();

    @Override
    public void onUpdate() {
        BlockPos origin = new BlockPos(mc.player.posX, mc.player.posY - 1.0, mc.player.posZ);
        if (mc.world.isAirBlock(origin)) {
            BlockPos next = this.getNextPlacePos(origin);
            if (next == null) {
                return;
            }

            int slot = InventoryUtil.getHotbarItemSlot(ItemBlock.class, this.offhand.getValue());
            if (slot == -1) {
                return;
            }

            int oldSlot = -1;
            EnumHand hand = slot == InventoryUtil.OFFHAND_SLOT ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND;
            if (hand == EnumHand.MAIN_HAND) {
                oldSlot = mc.player.inventory.currentItem;
                InventoryUtil.switchTo(slot, this.switchTo.getValue() == Switch.Silent);
            }

            mc.player.setActiveHand(hand);

            BlockUtil.place(next, hand, this.place.getValue() == Place.Packet, this.sneak.getValue(), this.swing.getValue(), this.rotate.getValue());

            if (this.tower.getValue() && next.equals(origin) && !mc.world.isAirBlock(next) && mc.gameSettings.keyBindJump.isKeyDown()) {
                mc.player.motionX *= 0.3;
                mc.player.motionZ *= 0.3;
                mc.player.jump();

                if (this.towerTimer.passedMs(1200L)) {
                    this.towerTimer.reset();
                    mc.player.motionY = -0.28;
                }
            }

            if (oldSlot != -1) {
                InventoryUtil.switchTo(oldSlot, this.switchTo.getValue() == Switch.Silent);
            }
        }
    }

    private BlockPos getNextPlacePos(BlockPos origin) {
        if (!mc.world.isAirBlock(origin)) {
            return null;
        }

        for (BlockPos offset : Scaffold.DIRECTION_OFFSETS) {
            for (EnumFacing direction : EnumFacing.values()) {
                if (direction == EnumFacing.UP) {
                    continue;
                }

                if (direction == EnumFacing.DOWN) {
                    return origin;
                } else {
                    return origin.add(offset);
                }
            }
        }

        return null;
    }

    public enum Switch {
        Legit, Silent
    }

    public enum Place {
        Vanilla, Packet
    }
}
