package me.sxmurai.inferno.impl.features.module.modules.combat;

import me.sxmurai.inferno.Inferno;
import me.sxmurai.inferno.impl.features.module.Module;
import me.sxmurai.inferno.impl.manager.HoleManager;
import me.sxmurai.inferno.impl.manager.InteractionManager;
import me.sxmurai.inferno.impl.manager.InventoryManager;
import me.sxmurai.inferno.impl.settings.Setting;
import me.sxmurai.inferno.util.entity.InventoryUtil;
import me.sxmurai.inferno.util.timing.TickTimer;
import me.sxmurai.inferno.util.world.BlockUtil;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.stream.Collectors;

@Module.Define(name = "HoleFiller", category = Module.Category.Combat)
@Module.Info(description = "Fills in safe holes for those shitters")
public class HoleFiller extends Module {
    public final Setting<Mode> mode = new Setting<>("Mode", Mode.Normal);
    public final Setting<Type> type = new Setting<>("Type", Type.Obsidian);
    public final Setting<InventoryManager.Swap> swap = new Setting<>("Swap", InventoryManager.Swap.Legit);
    public final Setting<Double> range = new Setting<>("Range", 4.0, 1.0, 6.0);
    public final Setting<Integer> blocks = new Setting<>("Blocks", 1, 1, 5);
    public final Setting<Integer> delay = new Setting<>("Delay", 1, 0, 20);
    public final Setting<InteractionManager.Placement> place = new Setting<>("Place", InteractionManager.Placement.Legit);
    public final Setting<Boolean> swing = new Setting<>("Swing", true);
    public final Setting<Boolean> rotate = new Setting<>("Rotate", true);

    private final Queue<BlockPos> positions = new ConcurrentLinkedQueue<>();
    private final TickTimer timer = new TickTimer();

    private int oldSlot = -1;
    private EnumHand hand;

    @Override
    protected void onDeactivated() {
        if (fullNullCheck()) {
            this.swapBack();
        }
    }

    @Override
    public void onTick() {
        if (this.hand == null) {
            if (this.swap.getValue() != InventoryManager.Swap.None) {
                int slot = InventoryUtil.getHotbarBlockSlot(this.type.getValue().block, true);
                if (slot == -1) {
                    this.toggle();
                    return;
                }

                this.hand = slot == 45 ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND;
                if (this.hand == EnumHand.MAIN_HAND) {
                    this.oldSlot = mc.player.inventory.currentItem;
                    Inferno.inventoryManager.swap(slot, this.swap.getValue());
                }

                return; // wait a tick
            } else {
                return;
            }
        }

        if (this.positions.isEmpty()) {
            List<HoleManager.Hole> holes = new ArrayList<>(Inferno.holeManager.getHoles());
            double range = this.range.getValue();

            if (this.mode.getValue() == Mode.Smart) {
                List<EntityPlayer> targets = mc.world.playerEntities
                        .stream().filter((p) -> p.isDead || p == mc.player || mc.player.getDistance(p) > range)
                        .collect(Collectors.toList());

                if (targets.isEmpty()) {
                    return;
                }

                holes = holes.stream().filter((hole) -> {
                    BlockPos pos = hole.getPos();
                    return !BlockUtil.isClickable(pos) || BlockUtil.intersects(pos) || mc.player.getDistance(pos.getX(), pos.getY(), pos.getZ()) > range;
                }).collect(Collectors.toList());
            }

            if (holes.isEmpty()) {
                this.toggle();
                return;
            }

            holes.forEach((hole) -> {
                if (!this.positions.contains(hole.getPos())) {
                    this.positions.add(hole.getPos());
                }
            });
        } else {
            if (this.timer.passed(this.delay.getValue())) {
                this.timer.reset();

                for (int i = 0; i < this.blocks.getValue(); ++i) {
                    BlockPos pos = this.positions.poll();
                    if (pos == null) {
                        this.toggle();
                        return;
                    }

                    if (mc.player.getDistance(pos.getX(), pos.getY(), pos.getZ()) > this.range.getValue()) {
                        continue;
                    }

                    Inferno.interactionManager.place(pos, this.place.getValue(), this.hand, this.rotate.getValue(), this.swing.getValue(), false);
                }
            }
        }
    }

    private void swapBack() {
        if (this.oldSlot != -1) {
            Inferno.inventoryManager.swap(this.oldSlot, this.swap.getValue());
            this.oldSlot = -1;
        }

        this.hand = null;
    }

    public enum Mode {
        Normal, Smart
    }

    public enum Type {
        Obsidian(Blocks.OBSIDIAN), Web(Blocks.WEB);

        private final Block block;
        Type(Block block) {
            this.block = block;
        }
    }
}
