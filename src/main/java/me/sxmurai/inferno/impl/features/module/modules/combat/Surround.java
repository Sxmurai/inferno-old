package me.sxmurai.inferno.impl.features.module.modules.combat;

import me.sxmurai.inferno.Inferno;
import me.sxmurai.inferno.impl.event.entity.JumpEvent;
import me.sxmurai.inferno.impl.features.module.Module;
import me.sxmurai.inferno.impl.manager.InteractionManager;
import me.sxmurai.inferno.impl.settings.Setting;
import me.sxmurai.inferno.util.entity.InventoryUtil;
import me.sxmurai.inferno.util.entity.MovementUtil;
import me.sxmurai.inferno.util.timing.TickTimer;
import me.sxmurai.inferno.util.world.BlockUtil;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.ArrayList;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

@Module.Define(name = "Surround", category = Module.Category.Combat)
@Module.Info(description = "Automatically surrounds you in obsidian")
public class Surround extends Module {
    public final Setting<Pattern> pattern = new Setting<>("Pattern", Pattern.Normal);
    public final Setting<MovementUtil.Center> center = new Setting<>("Center", MovementUtil.Center.Teleport);
    public final Setting<InventoryUtil.Swap> swap = new Setting<>("Swap", InventoryUtil.Swap.Legit);
    public final Setting<Disable> disable = new Setting<>("Disable", Disable.Finished);
    public final Setting<InteractionManager.Placement> place = new Setting<>("Place", InteractionManager.Placement.Legit);
    public final Setting<Boolean> rotate = new Setting<>("Rotate", true);
    public final Setting<Boolean> swing = new Setting<>("Swing", true);
    public final Setting<Boolean> sneak = new Setting<>("Sneak", false);
    public final Setting<Integer> blocks = new Setting<>("Blocks", 2, 1, 9);
    public final Setting<Integer> delay = new Setting<>("Delay", 1, 0, 10);

    private final Queue<BlockPos> queue = new ConcurrentLinkedQueue<>();
    private final TickTimer timer = new TickTimer();

    private int oldSlot = -1;
    private EnumHand hand = null;

    @Override
    protected void onDeactivated() {
        if (fullNullCheck() && this.oldSlot != -1) {
            InventoryUtil.swap(this.oldSlot, this.swap.getValue());
            this.oldSlot = -1;
            this.hand = null;
        }
    }

    @SubscribeEvent
    public void onJump(JumpEvent event) {
        if (event.getPlayer() == mc.player && this.disable.getValue() == Disable.Jump) {
            this.toggle();
        }
    }

    @Override
    public void onTick() {
        if (this.queue.isEmpty()) {
            ArrayList<BlockPos> offsets = new ArrayList<>();
            for (BlockPos offset : this.pattern.getValue().offsets) {
                BlockPos real = new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ).add(offset);
                if (BlockUtil.isClickable(real)) {
                    offsets.add(real);
                }
            }

            offsets.forEach((pos) -> {
                if (!this.queue.contains(pos)) {
                    this.queue.add(pos);
                }
            });

            if (offsets.isEmpty()) {
                if (this.disable.getValue() == Disable.Finished) {
                    this.toggle();
                    return;
                }

                if (this.oldSlot != -1) {
                    InventoryUtil.swap(this.oldSlot, this.swap.getValue());
                    this.oldSlot = -1;
                }
            }
        } else {
            if (this.swap.getValue() != InventoryUtil.Swap.None) {
                int slot = InventoryUtil.getHotbarBlockSlot(Blocks.OBSIDIAN, true);
                if (slot == -1) {
                    this.toggle();
                    return;
                }

                this.hand = slot == 45 ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND;
                if (this.hand == EnumHand.MAIN_HAND) {
                    this.oldSlot = mc.player.inventory.currentItem;
                    InventoryUtil.swap(slot, this.swap.getValue());
                }
            } else {
                if (mc.player.getHeldItemMainhand().getItem() instanceof ItemBlock && ((ItemBlock) mc.player.getHeldItemMainhand().getItem()).getBlock() == Blocks.OBSIDIAN) {
                    this.hand = EnumHand.MAIN_HAND;
                }

                if (mc.player.getHeldItemOffhand().getItem() instanceof ItemBlock && ((ItemBlock) mc.player.getHeldItemOffhand().getItem()).getBlock() == Blocks.OBSIDIAN) {
                    this.hand = EnumHand.OFF_HAND;
                }

                if (this.hand == null) {
                    this.toggle();
                    return;
                }
            }

            if (this.timer.passed(this.delay.getValue())) {
                this.timer.reset();

                MovementUtil.center(this.center.getValue(), 0.2);

                for (int i = 0; i < this.blocks.getValue(); ++i) {
                    BlockPos pos = this.queue.poll();
                    if (pos == null) {
                        break;
                    }

                    Inferno.interactionManager.place(pos, this.place.getValue(), this.hand, this.rotate.getValue(), this.swing.getValue(), this.sneak.getValue());
                }
            }
        }
    }

    public enum Pattern {
        Normal(
                new BlockPos(0, -1, 0),
                new BlockPos(-1, 0, 0),
                new BlockPos(1, 0, 0),
                new BlockPos(0, 0, 1),
                new BlockPos(0, 0, -1)
        ),
        AntiCity(
                new BlockPos(0, -1, 0),
                new BlockPos(-2, 0, 0),
                new BlockPos(-1, 0, 0),
                new BlockPos(1, 0, 0),
                new BlockPos(2, 0, 0),
                new BlockPos(0, 0, 1),
                new BlockPos(0, 0, 2),
                new BlockPos(0, 0, -1),
                new BlockPos(0, 0, -2)
        ),
        SelfTrap(
                new BlockPos(0, -1, 0),
                new BlockPos(-1, 0, 0),
                new BlockPos(1, 0, 0),
                new BlockPos(0, 0, 1),
                new BlockPos(0, 0, -1),
                new BlockPos(-1, 1, 0),
                new BlockPos(1, 1, 0),
                new BlockPos(0, 1, 1),
                new BlockPos(0, 1, -1)
        );

        private final BlockPos[] offsets;
        Pattern(BlockPos... offsets) {
            this.offsets = offsets;
        }
    }

    public enum Disable {
        Manual, Finished, Jump
    }
}
