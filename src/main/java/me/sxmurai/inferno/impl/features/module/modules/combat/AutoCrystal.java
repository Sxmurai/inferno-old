package me.sxmurai.inferno.impl.features.module.modules.combat;

import me.sxmurai.inferno.Inferno;
import me.sxmurai.inferno.util.entity.DamageUtil;
import me.sxmurai.inferno.util.entity.EntityUtil;
import me.sxmurai.inferno.util.entity.InventoryUtil;
import me.sxmurai.inferno.util.entity.RotationUtil;
import me.sxmurai.inferno.util.render.ColorUtil;
import me.sxmurai.inferno.util.render.RenderUtil;
import me.sxmurai.inferno.util.timing.TickTimer;
import me.sxmurai.inferno.util.world.BlockUtil;
import me.sxmurai.inferno.impl.event.entity.EntityRemoveEvent;
import me.sxmurai.inferno.impl.event.network.PacketEvent;
import me.sxmurai.inferno.impl.features.module.Module;
import me.sxmurai.inferno.impl.option.Option;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.server.SPacketDestroyEntities;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.network.play.server.SPacketSpawnObject;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

// this is gonna be fucking hell nightmare nightmare nightmare
@Module.Define(name = "AutoCrystal", category = Module.Category.Combat)
@Module.Info(description = "Automatically breaks and destroys end crystals")
public class AutoCrystal extends Module {
    public final Option<Boolean> place = new Option<>("Place", true);
    public final Option<Float> placeRange = new Option<>("PlaceRange", 4.5f, 1.0f, 6.0f);
    public final Option<Float> placeTrace = new Option<>("PlaceTrace", 3.0f, 1.0f, 6.0f);
    public final Option<Integer> placeDelay = new Option<>("PlaceDelay", 3, 0, 20);
    public final Option<Float> placeMin = new Option<>("PlaceMin", 4.0f, 1.0f, 36.0f);
    public final Option<Placements> placements = new Option<>("Placements", Placements.Native);
    public final Option<Direction> direction = new Option<>("Direction", Direction.Up);
    public final Option<Float> faceplace = new Option<>("Faceplace", 16.0f, 1.0f, 36.0f);
    public final Option<Float> faceplaceDamage = new Option<>("FaceplaceDamage", 2.0f, 1.0f, 6.0f);

    public final Option<Boolean> destroy = new Option<>("Destroy", true);
    public final Option<Float> destroyRange = new Option<>("DestroyRange", 4.5f, 1.0f, 6.0f);
    public final Option<Float> destroyTrace = new Option<>("DestroyTrace", 3.0f, 1.0f, 6.0f);
    public final Option<Integer> destroyDelay = new Option<>("DestroyDelay", 2, 0, 20);
    public final Option<Float> destroyMin = new Option<>("DestroyMin", 4.0f, 1.0f, 36.0f);
    public final Option<Boolean> inhibit = new Option<>("Inhibit", true);
    public final Option<Integer> ticksExisted = new Option<>("TicksExisted", 2, 0, 20);
    public final Option<Boolean> destroyPacket = new Option<>("DestroyPacket", true);

    public final Option<Boolean> await = new Option<>("Await", true);
    public final Option<InventoryUtil.Swap> swap = new Option<>("Swap", InventoryUtil.Swap.Legit);
    public final Option<Integer> swapDelay = new Option<>("SwapDelay", 2, 0, 12, () -> this.swap.getValue() != InventoryUtil.Swap.None);
    public final Option<Raytrace> raytrace = new Option<>("Raytrace", Raytrace.Base);
    public final Option<Boolean> sync = new Option<>("Sync", true);
    public final Option<Boolean> safe = new Option<>("Safe", true);
    public final Option<Float> maxLocal = new Option<>("MaxLocal", 12.0f, 1.0f, 36.0f);
    public final Option<Boolean> swing = new Option<>("Swing", true);
    public final Option<Boolean> rotate = new Option<>("Rotate", true);
    public final Option<YawStep> yawStep = new Option<>("YawStep", YawStep.Semi);
    public final Option<Float> yawStepThreshold = new Option<>("YawStepThreshold", 20.0f, 1.0f, 100.0f);
    public final Option<Integer> yawSteps = new Option<>("YawSteps", 6, 1, 16);
    public final Option<Integer> yawStepTicks = new Option<>("YawStepTicks", 2, 1, 10);
    public final Option<Targeting> targeting = new Option<>("Targeting", Targeting.Damage);
    public final Option<Float> targetRange = new Option<>("TargetRange", 10.0f, 1.0f, 16.0f);

    private final TickTimer placeTimer = new TickTimer();
    private final TickTimer destroyTimer = new TickTimer();
    private final TickTimer swapTimer = new TickTimer();
    private final TickTimer yawStepTimer = new TickTimer();

    private int oldSlot = -1;
    private EnumHand hand = EnumHand.MAIN_HAND;

    private EntityPlayer target = null;
    private BlockPos placePos = null;
    private EntityEnderCrystal crystal = null;
    private float damage = 0.0f;

    private final RotationHandler rotationHandler = new RotationHandler();

    @Override
    protected void onDeactivated() {
        if (fullNullCheck() && this.oldSlot != -1) {
            InventoryUtil.swap(this.oldSlot, this.swap.getValue());
        }

        this.oldSlot = -1;
        this.hand = EnumHand.MAIN_HAND;

        this.target = null;
        this.placePos = null;
        this.crystal = null;
        this.damage = 0.0f;
        this.rotationHandler.reset();
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof SPacketSoundEffect) {
            if (this.sync.getValue()) {
                SPacketSoundEffect packet = (SPacketSoundEffect) event.getPacket();
                if (packet.getSound() == SoundEvents.ENTITY_GENERIC_EXPLODE) {
                    for (Entity entity : mc.world.loadedEntityList) {
                        if (entity.isDead || !(entity instanceof EntityEnderCrystal)) {
                            continue;
                        }

                        if (entity.getDistance(packet.getX(), packet.getY(), packet.getZ()) >= 6.0f) {
                            entity.setDead();
                            if (this.crystal != null && this.crystal.equals(entity)) {
                                this.crystal = null;
                            }
                        }
                    }
                }
            }
        } else if (event.getPacket() instanceof SPacketExplosion) {
            if (this.inhibit.getValue()) {
                SPacketExplosion packet = (SPacketExplosion) event.getPacket();
                for (int i = 0; i < mc.world.loadedEntityList.size(); ++i) {
                    Entity entity = mc.world.loadedEntityList.get(i);
                    if (entity.isDead || !(entity instanceof EntityEnderCrystal)) {
                        continue;
                    }

                    if (entity.getDistance(packet.getX(), packet.getY(), packet.getZ()) >= packet.getStrength()) {
                        entity.setDead();
                        if (this.crystal != null && this.crystal.equals(entity)) {
                            this.crystal = null;
                        }
                    }
                }
            }
        } else if (event.getPacket() instanceof SPacketSpawnObject) {
            SPacketSpawnObject packet = (SPacketSpawnObject) event.getPacket();
            if (packet.getType() == 51 && mc.world.getEntityByID(packet.getEntityID()) instanceof EntityEnderCrystal) {
                BlockPos pos = new BlockPos(packet.getX(), packet.getY(), packet.getZ());
                if (this.placePos != null && this.placePos.equals(pos.down())) {
                    this.placePos = null;
                    this.placeTimer.reset();
                }
            }
        } else if (event.getPacket() instanceof SPacketDestroyEntities) {
            if (this.sync.getValue()) {
                SPacketDestroyEntities packet = (SPacketDestroyEntities) event.getPacket();
                for (int id : packet.getEntityIDs()) {
                    Entity entity = mc.world.getEntityByID(id);
                    if (entity == null || entity.isDead || !(entity instanceof EntityEnderCrystal)) {
                        continue;
                    }

                    mc.world.removeEntity(entity);
                    if (this.crystal != null && this.crystal.equals(entity)) {
                        this.crystal = null;
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void onEntityRemove(EntityRemoveEvent event) {
        if (this.crystal.equals(event.getEntity())) {
            this.crystal = null;
        }
    }

    @Override
    public void onRenderWorld() {
        if (this.placePos != null) {
            int c = ColorUtil.getColor(255, 255, 255, 80);
            if (this.crystal != null) {
                if (this.placePos.equals(this.crystal.getPosition().down())) {
                    c = ColorUtil.getColor(0, 255, 0, 80);
                }
            }

            RenderUtil.drawFilledBox(RenderUtil.toScreen(new AxisAlignedBB(this.placePos)), c);
        }
    }

    @Override
    public void onTick() {
        if (!this.check()) {
            return;
        }

        if (this.place.getValue()) {
            this.doPlace();
        }

        if (this.destroy.getValue()) {
            this.doDestroy();
        }

        this.updateRotations();
    }

    private void doPlace() {
        if (this.placeTimer.passed(this.placeDelay.getValue())) {
            Crystal crystal = this.getPlacePosition();
            if (crystal == null) {
                return;
            }

            this.placePos = crystal.getPos();
            this.damage = crystal.getDamage();

            if (!InventoryUtil.isHolding(Items.END_CRYSTAL, true)) {
                return;
            }

            if (this.placePos != null && this.hand != null) {
                this.updateRotations();
                DamageUtil.place(this.placePos, this.hand, this.direction.getValue().facing, this.swing.getValue(), this.raytrace.getValue().offset);
                this.placeTimer.reset();
            }
        }
    }

    private void doDestroy() {
        if (this.destroyTimer.passed(this.destroyDelay.getValue())) {
            if (this.placePos != null) {
                List<EntityEnderCrystal> atPos = mc.world.getEntitiesWithinAABB(EntityEnderCrystal.class, new AxisAlignedBB(this.placePos.up()));
                if (!atPos.isEmpty()) {
                    this.crystal = atPos.get(0);
                }
            }

            if (this.crystal == null) {
                List<EntityEnderCrystal> crystals = mc.world.getEntities(EntityEnderCrystal.class, (c) -> !c.isDead && c.ticksExisted >= this.ticksExisted.getValue());
                for (EntityEnderCrystal crystal : crystals) {
                    if (crystal == null) {
                        continue;
                    }

                    double dist = mc.player.getDistance(crystal);
                    if (dist > this.destroyRange.getValue() || !mc.player.canEntityBeSeen(crystal) && dist > this.destroyTrace.getValue()) {
                        continue;
                    }

                    // @todo calculations
                    this.crystal = crystal;
                }
            }

            if (this.crystal != null) {
                this.placePos = null;
                this.updateRotations();

                if (this.destroyPacket.getValue()) {
                    mc.player.connection.sendPacket(new CPacketUseEntity(this.crystal));
                } else {
                    mc.playerController.attackEntity(mc.player, this.crystal);
                }

                this.destroyTimer.reset();
            }
        }
    }

    private Crystal getPlacePosition() {
        List<BlockPos> positions = DamageUtil.getPositions(this.placeRange.getValue().intValue(), this.placements.getValue() == Placements.Protocol);
        if (!positions.isEmpty()) {
            BlockPos pos = null;
            float damage = 0.0f;
            EntityPlayer t = this.target;

            for (BlockPos place : positions) {
                if (!BlockUtil.canSeePos(place, this.raytrace.getValue().offset) && mc.player.getDistance(place.x, place.y, place.z) > this.placeTrace.getValue()) {
                    continue;
                }

                float selfDamage = DamageUtil.calculateDamage(new Vec3d(place.x + 0.5, place.y + 1.0, place.z + 0.5), mc.player);
                if ((this.safe.getValue() && selfDamage + 0.5f >= EntityUtil.getHealth(mc.player)) || selfDamage + 0.5f > this.maxLocal.getValue()) {
                    continue;
                }

                float targetDamage = 0.0f;
                if (t != null) {
                    targetDamage = DamageUtil.calculateDamage(new Vec3d(place.x + 0.5, place.y + 1.0, place.z + 0.5), t);
                    if (selfDamage > targetDamage || targetDamage < this.placeMin.getValue()) {
                        continue;
                    }
                }

                if (this.targeting.getValue() == Targeting.Damage) {
                    for (EntityPlayer player : mc.world.playerEntities) {
                        if (player == mc.player || mc.player.getDistance(player) > this.targetRange.getValue()) {
                            continue;
                        }

                        if (t == null) {
                            t = player;
                            continue;
                        }

                        float playerDamage = DamageUtil.calculateDamage(new Vec3d(place.x + 0.5, place.y + 1.0, place.z + 0.5), player);
                        if (playerDamage > targetDamage) {
                            t = player;
                            targetDamage = playerDamage;
                        }
                    }
                }

                if (targetDamage > damage) {
                    damage = targetDamage;
                    pos = place;
                }
            }

            if (pos == null && t != null && EntityUtil.getHealth(t) <= this.faceplace.getValue()) {
                BlockPos base = new BlockPos(t.posX, t.posY, t.posZ);
                for (EnumFacing facing : EnumFacing.values()) {
                    if (facing == EnumFacing.UP || facing == EnumFacing.DOWN) {
                        continue;
                    }

                    BlockPos neighbor = base.offset(facing);
                    if (!DamageUtil.canPlaceCrystal(neighbor, this.placements.getValue() == Placements.Protocol)) {
                        continue;
                    }

                    float d = DamageUtil.calculateDamage(new Vec3d(neighbor.x + 0.5, neighbor.y + 1.0, neighbor.z + 0.5), t);
                    if (d <= this.faceplaceDamage.getValue()) {
                        continue;
                    }

                    if (d > damage) {
                        damage = d;
                        pos = neighbor;
                    }
                }
            }

            if (pos != null) {
                return new Crystal(pos, damage);
            }
        }

        return null;
    }

    private boolean check() {
        if (!this.swapTimer.passed(this.swapDelay.getValue())) {
            return false;
        }

        if (!InventoryUtil.isHolding(Items.END_CRYSTAL, true)) {
            if (this.swap.getValue() != InventoryUtil.Swap.None && this.placePos != null) {
                int slot = InventoryUtil.getHotbarItemSlot(Items.END_CRYSTAL, true);
                if (slot == -1) {
                    return false;
                }

                this.hand = slot == InventoryUtil.OFFHAND_SLOT ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND;
                if (this.hand == EnumHand.MAIN_HAND) {
                    this.oldSlot = mc.player.inventory.currentItem;
                    InventoryUtil.swap(slot, this.swap.getValue());
                }

                if (this.hand == null) {
                    return false;
                }

                mc.player.setActiveHand(this.hand);

                this.swapTimer.reset();
                if (this.await.getValue()) {
                    return false;
                }
            }
        } else {
            if (InventoryUtil.getHeld(EnumHand.MAIN_HAND).getItem() == Items.END_CRYSTAL) {
                this.hand = EnumHand.MAIN_HAND;
            }

            if (InventoryUtil.getHeld(EnumHand.OFF_HAND).getItem() == Items.END_CRYSTAL) {
                this.hand = EnumHand.OFF_HAND;
            }
        }

        if (this.rotate.getValue()) {
            this.updateRotations();
        }

        return true;
    }

    private void updateRotations() {
        if (this.rotate.getValue()) {
            if (this.placePos != null) {
                RotationUtil.Rotation rotation = RotationUtil.calcRotations(new Vec3d(this.placePos.x + 0.5, this.placePos.y + 0.5, this.placePos.z + 0.5));
                this.rotationHandler.rotate(rotation.getYaw(), rotation.getPitch(), false);
            } else {
                if (this.crystal != null) {
                    RotationUtil.Rotation rotation = RotationUtil.calcRotations(this.crystal.getPositionEyes(mc.getRenderPartialTicks()));
                    this.rotationHandler.rotate(rotation.getYaw(), rotation.getPitch(), true);
                } else {
                    this.rotationHandler.reset();
                }
            }
        }
    }

    public enum Placements {
        Native, Protocol
    }

    public enum Direction {
        Up(EnumFacing.UP), Down(EnumFacing.DOWN);

        private final EnumFacing facing;
        Direction(EnumFacing facing) {
            this.facing = facing;
        }
    }

    public enum YawStep {
        None, Semi, Full
    }

    public enum Targeting {
        Closest, Damage
    }

    public enum Raytrace {
        None(-1.0), Base(0.5), Normal(1.5);

        private final double offset;
        Raytrace(double offset) {
            this.offset = offset;
        }
    }

    public static class Crystal {
        private final BlockPos pos;
        private final float damage;

        public Crystal(BlockPos pos, float damage) {
            this.pos = pos;
            this.damage = damage;
        }

        public BlockPos getPos() {
            return pos;
        }

        public float getDamage() {
            return damage;
        }
    }

    public class RotationHandler {
        private final RotationUtil.Rotation rotation = new RotationUtil.Rotation();

        private final Queue<RotationUtil.Rotation> rotations = new ConcurrentLinkedQueue<>();
        private final TickTimer timer = new TickTimer();

        public void rotate(float yaw, float pitch, boolean breaking) {
            if (AutoCrystal.this.yawStep.getValue() == YawStep.None) {
                this.rotation.setYaw(yaw);
                this.rotation.setPitch(pitch);
                Inferno.rotationManager.setRotations(yaw, pitch);
            } else {
                if (AutoCrystal.this.yawStep.getValue() == YawStep.Semi && !breaking) {
                    this.rotation.setYaw(yaw);
                    this.rotation.setPitch(pitch);
                    Inferno.rotationManager.setRotations(yaw, pitch);
                    return;
                }

                this.rotate(yaw, pitch);
            }
        }

        public void rotate(float yaw, float pitch) {
            if (AutoCrystal.this.yawStep.getValue() != YawStep.None) {
                if (this.getYaw() == -1.0f && this.getPitch() == -1.0f) {
                    this.rotation.setYaw(yaw);
                    this.rotation.setPitch(pitch);
                    Inferno.rotationManager.setRotations(yaw, pitch);
                    return;
                }

                if (this.rotations.isEmpty()) {
                    double yawDiff = 0.0, pitchDiff = 0.0;

                    if (Math.abs(yaw - this.rotation.getYaw()) >= AutoCrystal.this.yawStepThreshold.getValue()) {
                        yawDiff = Math.abs(yaw - this.rotation.getYaw()) / AutoCrystal.this.yawSteps.getValue();
                    }

                    if (Math.abs(pitch - this.rotation.getPitch()) >= AutoCrystal.this.yawStepThreshold.getValue()) {
                        pitchDiff = Math.abs(pitch - this.rotation.getPitch()) / AutoCrystal.this.yawSteps.getValue();
                    }

                    if (yawDiff != 0.0 || pitchDiff != 0.0) {
                        this.timer.reset();

                        float y = yawDiff == 0.0 ? yaw : this.rotation.getYaw();
                        float p = pitchDiff == 0.0 ? pitch : this.rotation.getPitch();
                        for (int step = 0; step < AutoCrystal.this.yawSteps.getValue(); ++step) {
                            y = this.rotation.getYaw() > yaw ? y - (float) yawDiff : y + (float) yawDiff;
                            p = this.rotation.getPitch() > pitch ? p - (float) pitchDiff : p + (float) pitchDiff;

                            this.rotations.add(new RotationUtil.Rotation(y, p));
                        }
                    }
                } else {
                    if (this.timer.passed(AutoCrystal.this.yawStepTicks.getValue())) {
                        this.timer.reset();

                        int packets = AutoCrystal.this.yawSteps.getValue() / AutoCrystal.this.yawStepTicks.getValue();
                        for (int i = 0; i < packets; ++i) {
                            RotationUtil.Rotation r = this.rotations.poll();
                            if (r == null) {
                                break;
                            }

                            this.rotation.setYaw(r.getYaw());
                            this.rotation.setPitch(r.getPitch());
                            Inferno.rotationManager.setRotations(r.getYaw(), r.getPitch());
                        }
                    }
                }
            } else {
                this.rotation.setYaw(yaw);
                this.rotation.setPitch(pitch);
                Inferno.rotationManager.setRotations(yaw, pitch);
            }
        }

        public void reset() {
            this.rotation.setYaw(-1.0f);
            this.rotation.setPitch(-1.0f);
            this.timer.reset();
        }

        public float getYaw() {
            return this.rotation.getYaw();
        }

        public float getPitch() {
            return this.rotation.getPitch();
        }
    }
}
