package cope.inferno.core.features.module.movement;

import cope.inferno.core.events.AddBoxToListEvent;
import cope.inferno.core.events.UpdateWalkingPlayerEvent;
import cope.inferno.core.features.module.Category;
import cope.inferno.core.features.module.Module;
import cope.inferno.core.setting.Setting;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Jesus extends Module {
    private static final AxisAlignedBB LIQUID_FULL_BLOCK_AABB = new AxisAlignedBB(0.0, 0.0, 0.0, 1.0, 0.921, 1.0);

    public Jesus() {
        super("Jesus", Category.MOVEMENT, "Lets you walk on water");
    }

    public static final Setting<Mode> mode = new Setting<>("Mode", Mode.SOLID);

    // liquid options
    public static final Setting<Boolean> lava = new Setting<>("Lava", true);
    public static final Setting<Boolean> flowing = new Setting<>("Flowing", false);

    // other shit
    public static final Setting<Float> dipDistance = new Setting<>("DipDistance", 3.0f, 1.0f, 10.0f);

    private double ncpStrictY = 0.0;
    private int floatUpTimer = 0;

    @Override
    protected void onDisable() {
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), false);

        ncpStrictY = 0.0;
        floatUpTimer = 0;
    }

    @Override
    public void onUpdate() {
        if (mode.getValue().equals(Mode.DOLPHIN)) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), isInLiquid());
        } else if (!mc.gameSettings.keyBindSneak.isKeyDown()) {
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
        }
    }

    @SubscribeEvent
    public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent event) {
        if (event.getEra().equals(UpdateWalkingPlayerEvent.Era.PRE) && isAboveLiquid() && !isInLiquid() && !mc.gameSettings.keyBindSneak.isKeyDown()) {
            if (floatUpTimer != 0) {
                event.setCanceled(true);
                event.setOnGround(false);

                if (mode.getValue().equals(Mode.NCPSTRICT)) {
                    if (ncpStrictY >= 0.5f) {
                        ncpStrictY = 0.0f;
                    }

                    ncpStrictY += 0.1 + (Math.random() / 10.0);

                    event.setY(event.getY() - ncpStrictY);
                } else if (mode.getValue().equals(Mode.SOLID)) {
                    if (mc.player.ticksExisted % 2 == 0) {
                        event.setY(event.getY() - 0.2);
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onAddBoxToList(AddBoxToListEvent event) {
        if (mode.getValue().equals(Mode.SOLID) || mode.getValue().equals(Mode.NCPSTRICT)) {
            if (event.getEntity() == mc.player &&
                    isValidBlock(event.getBlock()) &&
                    !isInLiquid() &&
                    isAboveLiquid() &&
                    !mc.player.isBurning() &&
                    mc.player.fallDistance < dipDistance.getValue() &&
                    !mc.player.isSneaking() &&
                    !mc.gameSettings.keyBindJump.isKeyDown()) {

                AxisAlignedBB box = Jesus.LIQUID_FULL_BLOCK_AABB.offset(event.getPos());
                if (event.getBox().intersects(box)) {
                    event.getList().add(box);
                    event.setCanceled(true);
                }
            }
        }
    }

    private boolean isInLiquid() {
        return mc.player.isInWater() || (lava.getValue() && mc.player.isInLava());
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


    private boolean isValidBlock(Block block) {
        if (!(block instanceof BlockLiquid)) {
            return false;
        }

        if (!flowing.getValue() && (block == Blocks.FLOWING_WATER || block == Blocks.FLOWING_LAVA)) {
            return false;
        }

        return lava.getValue() || (block != Blocks.LAVA && block != Blocks.FLOWING_LAVA);
    }

    public enum Mode {
        /**
         * Normal NCP jesus. Does not bypass NCP-Updated
         */
        SOLID,

        /**
         * Bypasses NCP-Updated
         */
        NCPSTRICT,

        /**
         * Holds jump in the water for you
         */
        DOLPHIN
    }
}
