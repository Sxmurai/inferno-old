package cope.inferno.impl.features.module.modules.player;

import cope.inferno.Inferno;
import cope.inferno.impl.event.world.DamageBlockEvent;
import cope.inferno.impl.event.world.DestroyBlockEvent;
import cope.inferno.impl.features.module.Module;
import cope.inferno.impl.manager.InventoryManager;
import cope.inferno.impl.settings.EnumConverter;
import cope.inferno.impl.settings.Setting;
import cope.inferno.util.entity.InventoryUtil;
import cope.inferno.util.render.ColorUtil;
import cope.inferno.util.render.RenderUtil;
import cope.inferno.util.timing.Timer;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.network.play.client.CPacketAnimation;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Module.Define(name = "Speedmine", category = Module.Category.Player)
@Module.Info(description = "Mines blocks without you doing shit")
public class Speedmine extends Module {
    public final Setting<Mode> mode = new Setting<>("Mode", Mode.Packet);
    public final Setting<Boolean> reset = new Setting<>("Reset", true);
    public final Setting<Double> distance = new Setting<>("Distance", 6.0, 1.0, 15.0);
    public final Setting<Boolean> doubleBreak = new Setting<>("Double", false);
    public final Setting<InventoryManager.Swap> swap = new Setting<>("Swap", InventoryManager.Swap.None);
    public final Setting<Render> render = new Setting<>("Render", Render.Filled);

    private final Timer timer = new Timer();
    private BlockPos pos;
    private int oldSlot = -1;

    @Override
    public String getDisplayInfo() {
        return EnumConverter.getActualName(this.mode.getValue());
    }

    @Override
    protected void onDeactivated() {
        if (fullNullCheck()) {
            this.swapBack();
        }
    }

    @Override
    public void onRenderWorld() {
        if (this.pos != null && this.render.getValue() != Render.None) {
            boolean passed = this.timer.passedMs(2000L);
            int r = passed ? 0 : 255;
            int g = passed ? 255 : 0;

            RenderUtil.drawEsp(new AxisAlignedBB(this.pos).offset(RenderUtil.getScreen()), this.render.getValue() == Render.Filled || this.render.getValue() == Render.Both, this.render.getValue() == Render.Outline || this.render.getValue() == Render.Both, 1.5f, ColorUtil.getColor(r, g, 0, 80));
        }
    }

    @Override
    public void onUpdate() {
        if (this.pos != null) {
            if (mc.world.isAirBlock(pos) || mc.player.getDistance(this.pos.getX(), this.pos.getY(), this.pos.getZ()) > this.distance.getValue()) {
                this.pos = null;
                this.swapBack();
            }
        }
    }

    @SubscribeEvent
    public void onDamageBlock(DamageBlockEvent event) {
        if (event.getPos() != null) {
            this.pos = event.getPos();
            if (mc.world.getBlockState(this.pos).getBlock().blockHardness == -1.0f) {
                this.pos = null;
                return;
            }

            mc.playerController.isHittingBlock = this.reset.getValue();

            if (this.swap.getValue() != InventoryManager.Swap.None) {
                int slot = InventoryUtil.getHotbarItemSlot(ItemPickaxe.class, false);
                if (slot != -1) {
                    this.oldSlot = mc.player.inventory.currentItem;
                    Inferno.inventoryManager.swap(slot, this.swap.getValue());
                }
            }

            this.timer.reset();
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

            mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));

            if (this.doubleBreak.getValue()) {
                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.START_DESTROY_BLOCK, event.getPos(), event.getFacing()));
                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.STOP_DESTROY_BLOCK, event.getPos(), event.getFacing()));
                mc.world.setBlockToAir(this.pos);
                mc.player.connection.sendPacket(new CPacketAnimation(EnumHand.MAIN_HAND));
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
            Inferno.inventoryManager.swap(this.oldSlot, this.swap.getValue());
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
