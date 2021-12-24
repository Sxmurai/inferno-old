package cope.inferno.impl.features.module.modules.combat;

import cope.inferno.Inferno;
import cope.inferno.impl.event.entity.EntityRemoveEvent;
import cope.inferno.impl.features.module.Module;
import cope.inferno.impl.manager.InventoryManager;
import cope.inferno.impl.settings.Setting;
import cope.inferno.util.entity.EntityUtil;
import cope.inferno.util.entity.InventoryUtil;
import cope.inferno.util.entity.RotationUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@Module.Define(name = "Aura", category = Module.Category.Combat)
@Module.Info(description = "Attacks entities around you")
public class Aura extends Module {
    public static Aura INSTANCE;

    public Aura() {
        INSTANCE = this;
    }

    public static final Setting<Mode> mode = new Setting<>("Mode", Mode.Single);

    public static final Setting<Double> range = new Setting<>("Range", 4.5, 1.0, 6.0);
    public static final Setting<Double> walls = new Setting<>("Walls", 3.5, 1.0, 6.0);
    public static final Setting<Timing> timing = new Setting<>("Timing", Timing.Vanilla);
    public static final Setting<Weapon> weapon = new Setting<>("Weapon", Weapon.Require);
    public static final Setting<Boolean> rotate = new Setting<>("Rotate", true);
    public static final Setting<Boolean> swing = new Setting<>("Swing", true);
    public static final Setting<Boolean> stopSprint = new Setting<>("StopSprint", true);

    public static final Setting<Boolean> invisible = new Setting<>("Invisible", true);
    public static final Setting<Boolean> players = new Setting<>("Players", true);
    public static final Setting<Boolean> friends = new Setting<>("Friends", false, players::getValue);
    public static final Setting<Boolean> passive = new Setting<>("Passive", false);
    public static final Setting<Boolean> hostile = new Setting<>("Hostile", true);

    public EntityLivingBase target = null;
    private int oldSlot = -1;

    @Override
    protected void onDeactivated() {
        swapBack();
    }

    @SubscribeEvent
    public void onEntityRemove(EntityRemoveEvent event) {
        if (event.getEntity().equals(target)) {
            target = null;
        }
    }

    @Override
    public void onTick() {
        if (target == null || (mode.getValue().equals(Mode.Single) && target.isDead || !isValid(target))) {
            target = null;
            
            List<EntityLivingBase> possibleTargets = mc.world.getEntities(EntityLivingBase.class, this::isValid);
            if (possibleTargets.isEmpty()) {
                return;
            }

            possibleTargets.sort(Comparator.comparingDouble((entity) -> -mc.player.getDistance(entity)));
            target = possibleTargets.get(0);
        }

        if (target == null) {
            swapBack();
            return;
        }

        if (!weapon.getValue().equals(Weapon.None) && !InventoryUtil.isHolding(ItemSword.class, false)) {
            if (weapon.getValue().equals(Weapon.Require)) {
                return;
            }

            int slot = InventoryUtil.getHotbarItemSlot(ItemSword.class, false);
            if (weapon.getValue().equals(Weapon.Switch)) {
                oldSlot = mc.player.inventory.currentItem;
                Inferno.inventoryManager.swap(slot, InventoryManager.Swap.Legit);
            }
        }

        if (rotate.getValue()) {
            RotationUtil.Rotation rotation = RotationUtil.calcRotations(target.getPositionEyes(mc.getRenderPartialTicks()));
            Inferno.rotationManager.setRotations(rotation.getYaw() + getRandomRotationDiff(), rotation.getPitch() + getRandomRotationDiff());
        }

        if (canAttack(target)) {
            if (stopSprint.getValue() && (mc.player.isSprinting() || mc.player.serverSprintState)) {
                mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.STOP_SPRINTING));
                mc.player.serverSprintState = false;
            }

            Inferno.interactionManager.attack(target, false, false, swing.getValue());
        }
    }

    private void swapBack() {
        if (oldSlot != -1) {
            Inferno.inventoryManager.swap(oldSlot, InventoryManager.Swap.Legit);
            oldSlot = -1;
        }
    }

    private boolean isValid(Entity entity) {
        if (entity == null || entity == mc.player || entity.isDead) {
            return false;
        }

        double distance = mc.player.getDistance(entity);
        if (!mc.player.canEntityBeSeen(entity) && distance > walls.getValue() || distance > range.getValue()) {
            return false;
        }

        if (!invisible.getValue() && EntityUtil.isInvisible(entity)) {
            return false;
        }

        if (!hostile.getValue() && EntityUtil.isHostile(entity)) {
            return false;
        }

        if (!passive.getValue() && EntityUtil.isPassive(entity)) {
            return false;
        }

        if (EntityUtil.isPlayer(entity)) {
            if (!friends.getValue() && Inferno.friendManager.isFriend(entity.getUniqueID())) {
                return false;
            }

            return players.getValue();
        }

        return true;
    }

    private boolean canAttack(EntityLivingBase entity) {
        boolean fullyCharged = mc.player.getCooledAttackStrength(timing.getValue().equals(Timing.Vanilla) ? 1.0f : 0.0f) == 1.0f;
        return timing.getValue().equals(Timing.Vanilla) ? fullyCharged : fullyCharged && entity.hurtTime <= 0.0f;
    }

    private float getRandomRotationDiff() {
        return (float) ThreadLocalRandom.current().nextDouble(-0.4, 1.4);
    }

    public enum Mode {
        Single, Multi
    }

    public enum Timing {
        Vanilla, Sequential
    }

    public enum Weapon {
        None, Require, Switch
    }
}
