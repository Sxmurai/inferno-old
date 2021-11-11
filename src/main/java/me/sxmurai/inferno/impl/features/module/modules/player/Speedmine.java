package me.sxmurai.inferno.impl.features.module.modules.player;

import me.sxmurai.inferno.impl.event.world.DamageBlockEvent;
import me.sxmurai.inferno.impl.event.world.DestroyBlockEvent;
import me.sxmurai.inferno.util.render.ColorUtil;
import me.sxmurai.inferno.util.entity.InventoryUtil;
import me.sxmurai.inferno.util.render.RenderUtil;
import me.sxmurai.inferno.util.timing.Timer;
import me.sxmurai.inferno.impl.features.module.Module;
import me.sxmurai.inferno.impl.option.Option;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Module.Define(name = "Speedmine", category = Module.Category.Player)
@Module.Info(description = "Mines things faster")
public class Speedmine extends Module {
    public final Option<Mode> mode = new Option<>("Mode", Mode.Packet);
    public final Option<Float> damage = new Option<>("Damage", 1.0f, 0.1f, 1.0f, () -> mode.getValue() == Mode.Damage);
    public final Option<Boolean> reset = new Option<>("Reset", false);
    public final Option<Boolean> doublePacket = new Option<>("Double", false);
    public final Option<Float> range = new Option<>("Range", 5.0f, 1.0f, 10.0f);
    public final Option<Switch> switchTo = new Option<>("Switch", Switch.None);
    public final Option<Boolean> render = new Option<>("Render", true);
    public final Option<Boolean> filled = new Option<>("Filled", true, render::getValue);
    public final Option<Boolean> outlined = new Option<>("Outlined", true, render::getValue);
    public final Option<Float> lineWidth = new Option<>("Width", 1.0f, 0.1f, 5.0f, () -> render.getValue() && outlined.getValue());

    private final Timer timer = new Timer();
    private BlockPos current = null;
    private int oldSlot = -1;

    @Override
    protected void onDeactivated() {
        this.current = null;
        this.switchBack();
    }

    @Override
    public void onRenderWorld() {
        if (this.current != null && this.render.getValue()) {
            boolean passed = this.timer.passedMs(2000L);
            int red = passed ? 0 : 255;
            int green = passed ? 255 : 0;

            RenderUtil.drawEsp(RenderUtil.toScreen(new AxisAlignedBB(this.current)), this.filled.getValue(), this.outlined.getValue(), this.lineWidth.getValue(), ColorUtil.getColor(red, green, 0, 80));
        }
    }

    @Override
    public void onUpdate() {
        if (this.current != null) {
            if (mc.world.isAirBlock(this.current) || mc.player.getDistance(this.current.x, this.current.y, this.current.z) > this.range.getValue()) {
                this.current = null;
                this.switchBack();
            }
        }
    }

    @SubscribeEvent
    public void onDamageBlock(DamageBlockEvent event) {
        this.current = event.getPos();
        mc.playerController.isHittingBlock = this.reset.getValue();

        if (!InventoryUtil.isHolding(ItemPickaxe.class, false) && this.switchTo.getValue() != Switch.None) {
            int slot = InventoryUtil.getHotbarItemSlot(ItemPickaxe.class, false);
            if (slot != -1) {
                this.oldSlot = mc.player.inventory.currentItem;
                InventoryUtil.switchTo(slot, this.switchTo.getValue() == Switch.Silent);
            }
        }

        this.timer.reset();

        switch (this.mode.getValue()) {
            case Packet: {
                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, this.current, event.getFacing()));
                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, this.current, event.getFacing()));
                break;
            }

            case Instant: {
                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, this.current, event.getFacing()));
                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, this.current, event.getFacing()));
                mc.playerController.onPlayerDestroyBlock(this.current);
                mc.world.setBlockToAir(this.current);
                break;
            }

            case Damage: {
                mc.playerController.curBlockDamageMP = this.damage.getValue();
                break;
            }
        }

        if (this.current != null && this.doublePacket.getValue()) {
            mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, this.current, event.getFacing()));
            mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, this.current, event.getFacing()));
            mc.playerController.onPlayerDestroyBlock(this.current);
            mc.world.setBlockToAir(this.current);
        }
    }

    @SubscribeEvent
    public void onDestroyBlock(DestroyBlockEvent event) {
        if (event.getPos().equals(this.current)) {
            this.current = null;
            this.switchBack();
        }
    }

    private void switchBack() {
        if (this.oldSlot != -1) {
            InventoryUtil.switchTo(this.oldSlot, this.switchTo.getValue() == Switch.Silent);
        }

        this.oldSlot = -1;
    }

    public enum Mode {
        Packet, Instant, Damage
    }

    public enum Switch {
        None, Legit, Silent
    }
}
