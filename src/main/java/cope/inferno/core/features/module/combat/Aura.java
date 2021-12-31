package cope.inferno.core.features.module.combat;

import cope.inferno.asm.duck.IEntityPlayer;
import cope.inferno.core.features.module.Category;
import cope.inferno.core.features.module.Module;
import cope.inferno.core.manager.managers.relationships.impl.Status;
import cope.inferno.core.setting.Setting;
import cope.inferno.util.entity.EntityUtil;
import cope.inferno.util.entity.inventory.InventoryUtil;
import cope.inferno.util.entity.inventory.Swap;
import cope.inferno.util.entity.player.LocalPlayerUtil;
import cope.inferno.util.entity.player.rotation.Rotation;
import cope.inferno.util.entity.player.rotation.RotationUtil;
import cope.inferno.util.internal.MathUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemSword;

import java.util.Comparator;
import java.util.List;

public class Aura extends Module {
    public Aura() {
        super("Aura", Category.COMBAT, "Attacks entities around you");
    }

    public static final Setting<Mode> mode = new Setting<>("Mode", Mode.SINGLE);

    public static final Setting<Double> range = new Setting<>("Range", 4.2, 1.0, 6.0);
    public static final Setting<Double> walls = new Setting<>("Walls", 3.5, 1.0, 6.0);

    public static final Setting<Timing> timing = new Setting<>("Timing", Timing.SEQUENTIAL);
    public static final Setting<Boolean> stopSprint = new Setting<>("StopSprint", true);
    public static final Setting<Weapon> weapon = new Setting<>("Weapon", Weapon.REQUIRE);

    public static final Setting<Boolean> rotate = new Setting<>("Rotate", true);
    public static final Setting<Boolean> swing = new Setting<>("Swing", true);

    public static final Setting<Boolean> invisible = new Setting<>("Invisible", true);
    public static final Setting<Boolean> players = new Setting<>("Players", true);
    public static final Setting<Boolean> friends = new Setting<>(players, "Friends", false);
    public static final Setting<Boolean> passive = new Setting<>("Passive", false);
    public static final Setting<Boolean> hostile = new Setting<>("Hostile", true);

    private EntityLivingBase target = null;
    private int oldSlot = -1;

    @Override
    protected void onDisable() {
        if (oldSlot != -1) {
            getInferno().getInventoryManager().swap(oldSlot, Swap.CLIENT);
            oldSlot = -1;
        }

        target = null;
    }

    @Override
    public void onTick() {
        if (target == null || (mode.getValue().equals(Mode.SINGLE) && target.isDead || !isValid(target))) {
            target = null;

            List<EntityLivingBase> entities = mc.world.getEntities(EntityLivingBase.class, this::isValid);

            if (entities.isEmpty()) {
                return;
            }

            entities.sort(Comparator.comparingDouble((e) -> -mc.player.getDistance(e)));
            target = entities.get(0);
        }

        if (target == null && oldSlot != -1) {
            getInferno().getInventoryManager().swap(oldSlot, Swap.CLIENT);
            oldSlot = -1;
            return;
        }

        if (!weapon.getValue().equals(Weapon.NONE)) {
            int slot = InventoryUtil.getHotbarItem(ItemSword.class, null, false);
            if (slot == -1) {
                return;
            }

            if (!InventoryUtil.isHolding(ItemSword.class, false)) {
                if (weapon.getValue().equals(Weapon.REQUIRE)) {
                    return;
                }

                oldSlot = mc.player.inventory.currentItem;
                getInferno().getInventoryManager().swap(slot, Swap.CLIENT);
            }
        }

        if (rotate.getValue()) {
            Rotation rotation = RotationUtil.calcRotations(mc.player.getPositionEyes(mc.getRenderPartialTicks()), target.getPositionEyes(mc.getRenderPartialTicks()));

            getInferno().getRotationManager().setRotations(
                    rotation.getYaw() + (float) MathUtil.random(-0.4, 1.2),
                    rotation.getPitch() + (float) MathUtil.random(-0.4, 1.2));
        }

        if (canAttack(target)) {
            if (stopSprint.getValue()) {
                LocalPlayerUtil.sprint(false);
            }

            getInferno().getInteractionManager().attack(target, false, swing.getValue());
        }
    }

    private boolean canAttack(EntityLivingBase entity) {
        boolean fullyCharged = mc.player.getCooledAttackStrength(timing.getValue().equals(Timing.VANILLA) ? 1.0f : 0.0f) == 1.0f;
        return timing.getValue().equals(Timing.VANILLA) ? fullyCharged : fullyCharged && entity.hurtTime <= 0.0f;
    }

    private boolean isValid(Entity entity) {
        if (entity == null || !(entity instanceof EntityLivingBase) || entity.isDead || entity == mc.player) {
            return false;
        }

        double distance = mc.player.getDistance(entity);
        if (!mc.player.canEntityBeSeen(entity) && distance > walls.getValue() || distance > range.getValue()) {
            return false;
        }

        if (!invisible.getValue() && EntityUtil.isInvisible(entity)) {
            return false;
        }

        if (entity instanceof EntityPlayer) {
            if (!players.getValue()) {
                return false;
            }

            if (!friends.getValue() && ((IEntityPlayer) entity).isRelationship(Status.FRIEND)) {
                return false;
            }
        }

        if (!hostile.getValue() && EntityUtil.isHostile(entity)) {
            return false;
        }

        if (!passive.getValue() && EntityUtil.isPassive(entity)) {
            return false;
        }

        return true;
    }

    public enum Mode {
        /**
         * Focus on only one entity until it is invalid
         */
        SINGLE,

        /**
         * Switch targets depending on their distance
         */
        SWITCH
    }

    public enum Timing {
        /**
         * Uses the default vanilla cooldown delay
         */
        VANILLA,

        /**
         * Does the same as VANILLA, but waits for the hurt time of the entity to be 0
         */
        SEQUENTIAL
    }

    public enum Weapon {
        /**
         * Attacks with anything in your main hand
         */
        NONE,

        /**
         * Swaps to a sword if one is not present in your main hand
         */
        SWITCH,

        /**
         * Require the user to weld a sword to attack
         */
        REQUIRE
    }
}
