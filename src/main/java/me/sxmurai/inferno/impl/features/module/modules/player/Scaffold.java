package me.sxmurai.inferno.impl.features.module.modules.player;

import me.sxmurai.inferno.Inferno;
import me.sxmurai.inferno.impl.features.module.Module;
import me.sxmurai.inferno.impl.manager.InteractionManager;
import me.sxmurai.inferno.impl.manager.InventoryManager;
import me.sxmurai.inferno.impl.settings.Setting;
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
    public final Setting<Boolean> tower = new Setting<>("Tower", true);
    public final Setting<InventoryManager.Swap> swap = new Setting<>("Swap", InventoryManager.Swap.Legit);
    public final Setting<InteractionManager.Placement> place = new Setting<>("Place", InteractionManager.Placement.Legit);
    public final Setting<Boolean> rotate = new Setting<>("Rotate", true);
    public final Setting<Boolean> swing = new Setting<>("Swing", true);
    public final Setting<Boolean> sneak = new Setting<>("Sneak", false);

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
                if (this.swap.getValue() == InventoryManager.Swap.None) {
                    return;
                }

                int slot = InventoryUtil.getHotbarItemSlot(ItemBlock.class, true);
                if (slot == -1) {
                    return;
                }

                hand = slot == 45 ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND;
                if (hand == EnumHand.MAIN_HAND) {
                    oldSlot = mc.player.inventory.currentItem;
                    Inferno.inventoryManager.swap(slot, this.swap.getValue());
                }
            }

            if (hand == null) {
                return;
            }

            EnumFacing direction = BlockUtil.getFacing(below);
            if (direction == null) {
                return;
            }

            Inferno.interactionManager.place(below, this.place.getValue(), hand, this.rotate.getValue(), this.swing.getValue(), this.sneak.getValue());

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
                Inferno.inventoryManager.swap(oldSlot, this.swap.getValue());
            }
        }
    }
}
