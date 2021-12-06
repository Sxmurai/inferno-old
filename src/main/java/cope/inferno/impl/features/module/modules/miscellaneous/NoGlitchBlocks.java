package cope.inferno.impl.features.module.modules.miscellaneous;

import cope.inferno.impl.event.network.PacketEvent;
import cope.inferno.impl.features.module.Module;
import cope.inferno.impl.settings.Setting;
import cope.inferno.util.timing.TickTimer;
import cope.inferno.util.world.BlockUtil;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Module.Define(name = "NoGlitchBlocks")
@Module.Info(description = "Stops you from dying due to glitch blocks")
public class NoGlitchBlocks extends Module {
    public final Setting<Boolean> place = new Setting<>("Place", true);
    public final Setting<Integer> range = new Setting<>("Range", 4, 2, 10);
    public final Setting<Integer> delay = new Setting<>("Delay", 2, 1, 50);

    private final TickTimer timer = new TickTimer();

    @Override
    public void onUpdate() {
        if (this.timer.passed(this.delay.getValue())) {
            this.timer.reset();
            for (BlockPos pos : BlockUtil.getSphere(mc.player.getPosition(), this.range.getValue(), this.range.getValue(), false, true, 0)) {
                Block block = mc.world.getBlockState(pos).getBlock();
                if (block == Blocks.AIR || block.blockHardness == -1.0f) {
                    continue;
                }

                mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, pos, EnumFacing.DOWN));
            }
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if (this.place.getValue() && event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock) {
            CPacketPlayerTryUseItemOnBlock packet = event.getPacket();
            mc.player.connection.sendPacket(new CPacketPlayerDigging(CPacketPlayerDigging.Action.ABORT_DESTROY_BLOCK, packet.getPos(), packet.getDirection()));
        }
    }
}
