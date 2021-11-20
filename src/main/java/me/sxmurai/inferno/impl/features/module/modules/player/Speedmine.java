package me.sxmurai.inferno.impl.features.module.modules.player;

import me.sxmurai.inferno.impl.event.world.DamageBlockEvent;
import me.sxmurai.inferno.impl.event.world.DestroyBlockEvent;
import me.sxmurai.inferno.impl.features.module.Module;
import me.sxmurai.inferno.impl.settings.Setting;
import me.sxmurai.inferno.util.entity.InventoryUtil;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Module.Define(name = "Speedmine", category = Module.Category.Player)
@Module.Info(description = "Mines blocks without you doing shit")
public class Speedmine extends Module {
    public final Setting<Mode> mode = new Setting<>("Mode", Mode.Packet);
    public final Setting<Boolean> reset = new Setting<>("Reset", true);
    public final Setting<Double> distance = new Setting<>("Distance", 6.0, 1.0, 15.0);
    public final Setting<Boolean> doubleBreak = new Setting<>("Double", false);
    public final Setting<InventoryUtil.Swap> swap = new Setting<>("Swap", InventoryUtil.Swap.None);
    public final Setting<Render> render = new Setting<>("Render", Render.Filled);

    private BlockPos pos;
    private int oldSlot = -1;

    @Override
    protected void onDeactivated() {
        if (fullNullCheck()) {
            this.swapBack();
        }
    }

    @Override
    public void onUpdate() {
        if (this.pos != null) {
            if (mc.player.getDistance(this.pos.getX(), this.pos.getY(), this.pos.getZ()) > this.distance.getValue()) {
                this.pos = null;
                this.swapBack();
            }
        }
    }

    @SubscribeEvent
    public void onDamageBlock(DamageBlockEvent event) {
        if (event.getPos() != null) {
            this.pos = event.getPos();
            mc.playerController.isHittingBlock = this.reset.getValue();

            if (this.swap.getValue() != InventoryUtil.Swap.None) {
                int slot = InventoryUtil.getHotbarItemSlot(ItemPickaxe.class, false);
                if (slot != -1) {
                    this.oldSlot = mc.player.inventory.currentItem;
                    InventoryUtil.swap(slot, this.swap.getValue());
                }
            }

            mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
            switch (this.mode.getValue()) {
                case Packet: {
                    mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, event.getPos(), event.getFacing()));
                    mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, event.getPos(), event.getFacing()));
                    break;
                }

                case Instant: {
                    mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, event.getPos(), event.getFacing()));
                    mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, event.getPos(), event.getFacing()));
                    mc.world.setBlockToAir(this.pos);
                    break;
                }

                case Damage: {
                    mc.playerController.curBlockDamageMP = 1.0f;
                    break;
                }
            }

            if (this.doubleBreak.getValue()) {
                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, event.getPos(), event.getFacing()));
                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, event.getPos(), event.getFacing()));
                mc.world.setBlockToAir(this.pos);
            }
        }
    }

    @SubscribeEvent
    public void onDestroyBlock(DestroyBlockEvent event) {
        if (event.getPos() == this.pos) {
            this.pos = null;
            this.swapBack();
        }
    }

    private void swapBack() {
        if (this.oldSlot != -1) {
            InventoryUtil.swap(this.oldSlot, this.swap.getValue());
            this.oldSlot = -1;
        }
    }

    public enum Mode {
        Packet, Instant, Damage
    }

    public enum Render {
        None, Filled, Outline, Both
    }
}
