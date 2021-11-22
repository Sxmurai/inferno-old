package me.sxmurai.inferno.impl.features.module.modules.movement;

import me.sxmurai.inferno.impl.event.network.PacketEvent;
import me.sxmurai.inferno.impl.event.world.AddBoxToListEvent;
import me.sxmurai.inferno.impl.features.module.Module;
import me.sxmurai.inferno.impl.settings.Setting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Module.Define(name = "Jesus", category = Module.Category.Movement)
@Module.Info(description = "Walks on liquids like Jesus Christ himself")
public class Jesus extends Module {
    private static final AxisAlignedBB LIQUID_FULL_BLOCK_AABB = new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.99, 1.0);

    public final Setting<Mode> mode = new Setting<>("Mode", Mode.Solid);
    public final Setting<Boolean> flowing = new Setting<>("Flowing", true);
    public final Setting<Boolean> lava = new Setting<>("Lava", true);
    public final Setting<Boolean> dip = new Setting<>("Dip", true);

    private double yOffset = 0.0;
    private int floatUpTimer = 0;

    @Override
    public void onUpdate() {
        if (!mc.gameSettings.keyBindSneak.isKeyDown()) {
            if (this.mode.getValue() == Mode.Solid || this.mode.getValue() == Mode.NCPStrict) {
                if (this.isInLiquid()) {
                    mc.player.motionY = 0.11;
                    this.floatUpTimer = 0;
                    return;
                }

                if (this.floatUpTimer == 0) {
                    mc.player.motionY = 0.3;
                } else if (this.floatUpTimer == 1) {
                    mc.player.motionY = 0.0;
                }

                ++this.floatUpTimer;
            } else {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), true);
            }
        }
    }

    @SubscribeEvent
    public void onAddBoxToList(AddBoxToListEvent event) {
        if (this.mode.getValue() == Mode.Solid || this.mode.getValue() == Mode.NCPStrict) {
            if (event.getEntity() == mc.player && this.isValidBlock(event.getBlock()) && !this.isInLiquid() && this.isAboveLiquid() && (this.dip.getValue() && !mc.player.isBurning()) && mc.player.fallDistance < 3.0f && !mc.player.isSneaking() && !mc.gameSettings.keyBindJump.isKeyDown()) {
                AxisAlignedBB box = Jesus.LIQUID_FULL_BLOCK_AABB.offset(event.getPos());
                if (event.getBox().intersects(box)) {
                    event.getList().add(box);
                    event.setCanceled(true);
                }
            }
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if (event.getPacket() instanceof CPacketPlayer && !this.isInLiquid() && this.isAboveLiquid()) {
            CPacketPlayer packet = event.getPacket();
            if (packet.moving && this.floatUpTimer != 0) {
                if (this.mode.getValue() == Mode.NCPStrict) {
                    this.yOffset += (mc.player.ticksExisted % 4 == 2 ? 0.12 : 0.08);
                    if (this.yOffset > 0.3) {
                        this.yOffset = 0.1;
                    }

                    packet.y -= this.yOffset;
                } else {
                    packet.y -= (mc.player.ticksExisted % 2 == 0 ? 0.05 : 0.0);
                }
            }
        }
    }

    private boolean isAboveLiquid() {
        for (double y = 0.0; y < 1.0; y += 0.1) {
            BlockPos position = new BlockPos(mc.player.posX, mc.player.posY - y, mc.player.posZ);
            if (mc.world.getBlockState(position).getBlock() instanceof BlockLiquid) {
                return true;
            }
        }

        return false;
    }

    private boolean isInLiquid() {
        return mc.player.isInWater() || (this.lava.getValue() && mc.player.isInLava());
    }

    private boolean isValidBlock(Block block) {
        if (!(block instanceof BlockLiquid)) {
            return false;
        }

        if (!this.flowing.getValue() && (block == Blocks.FLOWING_WATER || block == Blocks.FLOWING_LAVA)) {
            return false;
        }

        return this.lava.getValue() || (block != Blocks.LAVA && block != Blocks.FLOWING_LAVA);
    }

    public enum Mode {
        Solid, NCPStrict, Dolphin
    }
}
