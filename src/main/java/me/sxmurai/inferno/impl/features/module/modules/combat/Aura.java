package me.sxmurai.inferno.impl.features.module.modules.combat;

import me.sxmurai.inferno.Inferno;
import me.sxmurai.inferno.impl.features.module.Module;
import me.sxmurai.inferno.impl.settings.Setting;
import me.sxmurai.inferno.util.entity.DamageUtil;
import me.sxmurai.inferno.util.entity.EntityUtil;
import me.sxmurai.inferno.util.entity.InventoryUtil;
import me.sxmurai.inferno.util.timing.TickTimer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.util.math.BlockPos;

import java.util.Comparator;
import java.util.List;

@Module.Define(name = "Aura", category = Module.Category.Combat)
@Module.Info(description = "Attacks entities around you")
public class Aura extends Module {
    public final Setting<Priority> priority = new Setting<>("Priority", Priority.Closest);
    public final Setting<Timing> timing = new Setting<>("Timing", Timing.Vanilla);
    public final Setting<Weapon> weapon = new Setting<>("Weapon", Weapon.Require);
    public final Setting<Boolean> fov = new Setting<>("FOV", false);
    public final Setting<Double> range = new Setting<>("Range", 4.5, 1.0, 6.0);
    public final Setting<Double> raytrace = new Setting<>("Raytrace", 2.0, 1.0, 6.0);
    public final Setting<Boolean> rotate = new Setting<>("Rotate", true);
    public final Setting<Boolean> swing = new Setting<>("Swing", true);
    public final Setting<Boolean> teleport = new Setting<>("Teleport", false);
    public final Setting<Sprint> sprint = new Setting<>("Sprint", Sprint.Start);

    public final Setting<Boolean> players = new Setting<>("Players", true);
    public final Setting<Boolean> invisible = new Setting<>("Invisible", true);
    public final Setting<Boolean> mobs = new Setting<>("Mobs", true);
    public final Setting<Boolean> passive = new Setting<>("Passive", true);

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
                InventoryUtil.swap(this.oldSlot, InventoryUtil.Swap.Legit);
                this.oldSlot = -1;
            }
        } else {
            if (!InventoryUtil.isHolding(ItemSword.class, false) && this.weapon.getValue() != Weapon.None) {
                if (this.weapon.getValue() == Weapon.Require) {
                    return;
                } else if (this.weapon.getValue() == Weapon.Switch) {
                    int slot = InventoryUtil.getHotbarItemSlot(ItemSword.class, false);
                    if (slot != -1) {
                        this.oldSlot = mc.player.inventory.currentItem;
                        InventoryUtil.swap(slot, InventoryUtil.Swap.Legit);
                    }
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

            Inferno.interactionManager.attack(this.target, false, this.rotate.getValue(), this.swing.getValue());

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

        if ((!this.invisible.getValue() && EntityUtil.isInvisible(target)) || (!this.players.getValue() && EntityUtil.isPlayer(target)) || (!this.passive.getValue() && EntityUtil.isPassive(target)) || (!this.mobs.getValue() && EntityUtil.isHostile(target))) {
            return true;
        }

        if (target instanceof EntityPlayer && Inferno.friendManager.isFriend(target.getUniqueID())) {
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
