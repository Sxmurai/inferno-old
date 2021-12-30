package cope.inferno.core.features.module.combat;

import cope.inferno.core.features.module.Category;
import cope.inferno.core.features.module.Module;
import cope.inferno.core.setting.Setting;
import cope.inferno.util.entity.inventory.InventoryUtil;
import cope.inferno.util.entity.inventory.Swap;
import cope.inferno.util.entity.player.LocalPlayerUtil;
import cope.inferno.util.network.NetworkUtil;
import cope.inferno.util.world.block.Place;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

// fake jump is legit in every client imaginable, idek who to credit for them at this point
public class SelfFill extends Module {
    public SelfFill() {
        super("SelfFill", Category.COMBAT, "Fills your hole with a block");
    }

    public static final Setting<Type> type = new Setting<>("Type", Type.OBSIDIAN);
    public static final Setting<Swap> swap = new Setting<>("Swap", Swap.CLIENT);
    public static final Setting<Boolean> rotate = new Setting<>("Rotate", true);
    public static final Setting<Boolean> swing = new Setting<>("Swing", true);

    @Override
    protected void onEnable() {
        if (nullCheck()) {
            BlockPos pos = LocalPlayerUtil.getLocalPosition();
            if (!mc.world.isAirBlock(pos.add(0.0, 1.0, 0.0)) ||
                    !mc.world.isAirBlock(pos.add(0.0, 2.0, 0.0)) ||
                    intersects(pos)) {

                toggle();
                return;
            }

            int slot = InventoryUtil.getHotbarBlock(type.getValue().block, null, true);
            if (slot == -1) {
                toggle();
                return;
            }

            EnumHand hand = slot == InventoryUtil.OFFHAND_SLOT ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND;
            int oldSlot = -1;

            mc.player.setActiveHand(hand);

            if (!InventoryUtil.isHolding(type.getValue().block, true) &&
                    !swap.getValue().equals(Swap.NONE)) {

                oldSlot = mc.player.inventory.currentItem;
                getInferno().getInventoryManager().swap(slot, swap.getValue());
            }

            NetworkUtil.send(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.41999998688698, mc.player.posZ, true));
            NetworkUtil.send(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 0.7531999805211997, mc.player.posZ, true));
            NetworkUtil.send(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.00133597911214, mc.player.posZ, true));
            NetworkUtil.send(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 1.16610926093821, mc.player.posZ, true));

            getInferno().getInteractionManager().place(pos, Place.PACKET, rotate.getValue(), swing.getValue());

            NetworkUtil.send(new CPacketPlayer.Position(mc.player.posX, mc.player.posY + 3.0, mc.player.posZ, true));

            if (oldSlot != -1) {
                getInferno().getInventoryManager().swap(oldSlot, swap.getValue());
            }

            toggle();
        } else {
            toggle();
        }
    }

    private boolean intersects(BlockPos pos) {
        for (Entity entity : mc.world.loadedEntityList) {
            if (entity == mc.player || entity instanceof EntityItem) {
                continue;
            }

            if (new AxisAlignedBB(pos).intersects(entity.getEntityBoundingBox())) {
                return true;
            }
        }

        return false;
    }

    public enum Type {
        OBSIDIAN(Blocks.OBSIDIAN),
        ECHEST(Blocks.ENDER_CHEST),
        ENDROD(Blocks.END_ROD),
        CHEST(Blocks.CHEST);

        private final Block block;

        Type(Block block) {
            this.block = block;
        }
    }
}
