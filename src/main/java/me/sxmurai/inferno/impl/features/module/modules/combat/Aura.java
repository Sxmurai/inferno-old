package me.sxmurai.inferno.impl.features.module.modules.combat;

import me.sxmurai.inferno.Inferno;
import me.sxmurai.inferno.util.entity.DamageUtil;
import me.sxmurai.inferno.util.entity.EntityUtil;
import me.sxmurai.inferno.util.entity.InventoryUtil;
import me.sxmurai.inferno.util.timing.TickTimer;
import me.sxmurai.inferno.impl.features.module.Module;
import me.sxmurai.inferno.impl.option.Option;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;

import java.util.Comparator;
import java.util.List;

@Module.Define(name = "Aura", category = Module.Category.Combat)
@Module.Info(description = "Attacks entities around you")
public class Aura extends Module {
    public final Option<Priority> priority = new Option<>("Priority", Priority.Closest);
    public final Option<Timing> timing = new Option<>("Timing", Timing.Vanilla);
    public final Option<Weapon> weapon = new Option<>("Weapon", Weapon.Require);
    public final Option<Boolean> fov = new Option<>("FOV", false);
    public final Option<Double> range = new Option<>("Range", 4.5, 1.0, 6.0);
    public final Option<Double> raytrace = new Option<>("Raytrace", 2.0, 1.0, 6.0);
    public final Option<Boolean> rotate = new Option<>("Rotate", true);
    public final Option<Boolean> swing = new Option<>("Swing", true);
    public final Option<Boolean> teleport = new Option<>("Teleport", false);
    public final Option<Sprint> sprint = new Option<>("Sprint", Sprint.Start);

    public final Option<Boolean> players = new Option<>("Players", true);
    public final Option<Boolean> invisible = new Option<>("Invisible", true);
    public final Option<Boolean> mobs = new Option<>("Mobs", true);
    public final Option<Boolean> passive = new Option<>("Passive", true);

    private final TickTimer timer = new TickTimer();
    private EntityLivingBase target = null;
    private int oldSlot = -1;

    @Override
    public void onUpdate() {
        if (this.isInvalid(this.target)) {
            this.target = null;

            List<EntityLivingBase> entities = mc.world.getEntities(EntityLivingBase.class, (e) -> !e.isDead && !this.isInvalid(e));
            if (!entities.isEmpty()) {
                entities.sort(Comparator.comparingDouble((e) -> this.priority.getValue() == Priority.Closest ? mc.player.getDistance(e) : DamageUtil.getAttackDamage(e)));
                this.target = entities.get(0);
            }

            if (this.target == null && this.oldSlot != -1) {
                InventoryUtil.switchTo(this.oldSlot, false);
                this.oldSlot = -1;
            }
        } else {
            if (this.weapon.getValue() != Weapon.None && !InventoryUtil.isHolding(ItemSword.class, false)) {
                int slot = InventoryUtil.getHotbarItemSlot(ItemSword.class, false);
                if (slot == -1 && this.weapon.getValue() == Weapon.Require) {
                    return;
                }

                if (slot != -1) {
                    InventoryUtil.switchTo(slot, false);
                }
            }

            if (this.rotate.getValue()) {
                Inferno.rotationManager.look(this.target);
            }

            if (!this.canAttack()) {
                return;
            }

            if (this.timing.getValue() == Timing.Sequential) {
                this.timer.reset();
            }

            BlockPos current = new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ);
            if (this.teleport.getValue()) {
                mc.player.connection.sendPacket(new CPacketPlayer.Position(this.target.posX, this.target.posY, this.target.posZ, mc.player.onGround));
            }

            if (this.sprint.getValue() != Sprint.None) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, this.sprint.getValue().action));
            }

            mc.playerController.attackEntity(mc.player, this.target);

            if (swing.getValue()) {
                mc.player.swingArm(EnumHand.MAIN_HAND);
            }

            if (this.sprint.getValue() == Sprint.Start) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
            }

            if (this.teleport.getValue()) {
                mc.player.connection.sendPacket(new CPacketPlayer.Position(current.getX(), current.getY(), current.getZ(), mc.player.onGround));
            }
        }
    }

    public boolean canAttack() {
        return !this.isInvalid(this.target) && (this.timing.getValue() == Timing.Vanilla ? mc.player.getCooledAttackStrength(mc.getRenderPartialTicks()) == 1.0f : this.timer.passed(15));
    }

    public boolean isInvalid(EntityLivingBase target) {
        if (target == null || target == mc.player || target.isDead || (!EntityUtil.isInFrustum(target) && this.fov.getValue())) {
            return true;
        }

        if (this.invisible.getValue() && EntityUtil.isInvisible(target) || !this.players.getValue() && EntityUtil.isPlayer(target) || !this.mobs.getValue() && EntityUtil.isHostile(target) || !this.passive.getValue() && EntityUtil.isPassive(target)) {
            return true;
        }

        double dist = mc.player.getDistance(target);
        return mc.player.canEntityBeSeen(target) ? dist >= this.range.getValue() : dist >= this.raytrace.getValue();
    }

    public enum Priority {
        Closest, Damage
    }

    public enum Timing {
        Vanilla, Sequential
    }

    public enum Weapon {
        None, Switch, Require
    }

    public enum Sprint {
        None(null),
        Stop(CPacketEntityAction.Action.STOP_SPRINTING),
        Start(CPacketEntityAction.Action.START_SPRINTING);

        private final CPacketEntityAction.Action action;
        Sprint(CPacketEntityAction.Action action) {
            this.action = action;
        }
    }
}
