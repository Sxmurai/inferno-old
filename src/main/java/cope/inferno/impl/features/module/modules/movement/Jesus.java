package cope.inferno.impl.features.module.modules.movement;

import cope.inferno.impl.event.network.PacketEvent;
import cope.inferno.impl.event.world.AddBoxToListEvent;
import cope.inferno.impl.features.Wrapper;
import cope.inferno.impl.features.module.Module;
import cope.inferno.impl.settings.EnumConverter;
import cope.inferno.impl.settings.Setting;
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
    public String getDisplayInfo() {
        return EnumConverter.getActualName(this.mode.getValue());
    }

    @Override
    public void onUpdate() {
        if (!Wrapper.mc.gameSettings.keyBindSneak.isKeyDown()) {
            if (this.mode.getValue() == Mode.Solid || this.mode.getValue() == Mode.NCPStrict) {
                if (this.isInLiquid()) {
                    Wrapper.mc.player.motionY = 0.11;
                    this.floatUpTimer = 0;
                    return;
                }

                if (this.floatUpTimer == 0) {
                    Wrapper.mc.player.motionY = 0.3;
                } else if (this.floatUpTimer == 1) {
                    Wrapper.mc.player.motionY = 0.0;
                }

                ++this.floatUpTimer;
            } else {
                KeyBinding.setKeyBindState(Wrapper.mc.gameSettings.keyBindJump.getKeyCode(), true);
            }
        }
    }

    @SubscribeEvent
    public void onAddBoxToList(AddBoxToListEvent event) {
        if (this.mode.getValue() == Mode.Solid || this.mode.getValue() == Mode.NCPStrict) {
            if (event.getEntity() == Wrapper.mc.player && this.isValidBlock(event.getBlock()) && !this.isInLiquid() && this.isAboveLiquid() && (this.dip.getValue() && !Wrapper.mc.player.isBurning()) && Wrapper.mc.player.fallDistance < 3.0f && !Wrapper.mc.player.isSneaking() && !Wrapper.mc.gameSettings.keyBindJump.isKeyDown()) {
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
                    this.yOffset += (Wrapper.mc.player.ticksExisted % 4 == 2 ? 0.12 : 0.08);
                    if (this.yOffset > 0.3) {
                        this.yOffset = 0.1;
                    }

                    packet.y -= this.yOffset;
                } else {
                    packet.y -= (Wrapper.mc.player.ticksExisted % 2 == 0 ? 0.05 : 0.0);
                }
            }
        }
    }

    private boolean isAboveLiquid() {
        for (double y = 0.0; y < 1.0; y += 0.1) {
            BlockPos position = new BlockPos(Wrapper.mc.player.posX, Wrapper.mc.player.posY - y, Wrapper.mc.player.posZ);
            if (Wrapper.mc.world.getBlockState(position).getBlock() instanceof BlockLiquid) {
                return true;
            }
        }

        return false;
    }

    private boolean isInLiquid() {
        return Wrapper.mc.player.isInWater() || (this.lava.getValue() && Wrapper.mc.player.isInLava());
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
