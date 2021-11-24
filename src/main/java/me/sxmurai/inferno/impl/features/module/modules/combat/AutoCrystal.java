package me.sxmurai.inferno.impl.features.module.modules.combat;

import me.sxmurai.inferno.Inferno;
import me.sxmurai.inferno.impl.event.entity.EntityRemoveEvent;
import me.sxmurai.inferno.impl.event.network.PacketEvent;
import me.sxmurai.inferno.impl.features.module.Module;
import me.sxmurai.inferno.impl.manager.InventoryManager;
import me.sxmurai.inferno.impl.settings.Setting;
import me.sxmurai.inferno.util.entity.DamageUtil;
import me.sxmurai.inferno.util.entity.EntityUtil;
import me.sxmurai.inferno.util.entity.InventoryUtil;
import me.sxmurai.inferno.util.entity.RotationUtil;
import me.sxmurai.inferno.util.render.ColorUtil;
import me.sxmurai.inferno.util.render.RenderUtil;
import me.sxmurai.inferno.util.timing.TickTimer;
import me.sxmurai.inferno.util.world.BlockUtil;
import me.sxmurai.inferno.util.world.CrystalUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
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

@Module.Define(name = "AutoCrystal", category = Module.Category.Combat)
@Module.Info(description = "Automatically breaks and destroys end crystals")
public class AutoCrystal extends Module {
    public final Setting<Boolean> place = new Setting<>("Place", true);
    public final Setting<Float> placeRange = new Setting<>("PlaceRange", 4.5f, 1.0f, 6.0f);
    public final Setting<Float> placeTrace = new Setting<>("PlaceTrace", 3.0f, 1.0f, 6.0f);
    public final Setting<Integer> placeDelay = new Setting<>("PlaceDelay", 3, 0, 20);
    public final Setting<Float> placeMin = new Setting<>("PlaceMin", 4.0f, 1.0f, 36.0f);
    public final Setting<Placements> placements = new Setting<>("Placements", Placements.Native);
    public final Setting<CrystalUtil.Placement> direction = new Setting<>("Direction", CrystalUtil.Placement.Normal);
    public final Setting<Float> faceplace = new Setting<>("Faceplace", 16.0f, 1.0f, 36.0f);
    public final Setting<Float> faceplaceDamage = new Setting<>("FaceplaceDamage", 2.0f, 1.0f, 6.0f);

    public final Setting<Boolean> destroy = new Setting<>("Destroy", true);
    public final Setting<Float> destroyRange = new Setting<>("DestroyRange", 4.5f, 1.0f, 6.0f);
    public final Setting<Float> destroyTrace = new Setting<>("DestroyTrace", 3.0f, 1.0f, 6.0f);
    public final Setting<Integer> destroyDelay = new Setting<>("DestroyDelay", 2, 0, 20);
    public final Setting<Float> destroyMin = new Setting<>("DestroyMin", 4.0f, 1.0f, 36.0f);
    public final Setting<Boolean> inhibit = new Setting<>("Inhibit", true);
    public final Setting<Integer> ticksExisted = new Setting<>("TicksExisted", 2, 0, 20);
    public final Setting<Boolean> destroyPacket = new Setting<>("DestroyPacket", true);

    public final Setting<Boolean> await = new Setting<>("Await", true);
    public final Setting<InventoryManager.Swap> swap = new Setting<>("Swap", InventoryManager.Swap.Legit);
    public final Setting<Integer> swapDelay = new Setting<>("SwapDelay", 2, 0, 12, () -> this.swap.getValue() != InventoryManager.Swap.None);
    public final Setting<Raytrace> raytrace = new Setting<>("Raytrace", Raytrace.Base);
    public final Setting<Boolean> sync = new Setting<>("Sync", true);
    public final Setting<Boolean> safe = new Setting<>("Safe", true);
    public final Setting<Float> maxLocal = new Setting<>("MaxLocal", 12.0f, 1.0f, 36.0f);
    public final Setting<Boolean> swing = new Setting<>("Swing", true);
    public final Setting<Boolean> rotate = new Setting<>("Rotate", true);
    public final Setting<YawStep> yawStep = new Setting<>("YawStep", YawStep.Semi);
    public final Setting<Float> yawStepThreshold = new Setting<>("YawStepThreshold", 20.0f, 1.0f, 100.0f);
    public final Setting<Integer> yawSteps = new Setting<>("YawSteps", 6, 1, 16);
    public final Setting<Integer> yawStepTicks = new Setting<>("YawStepTicks", 2, 1, 10);
    public final Setting<Targeting> targeting = new Setting<>("Targeting", Targeting.Damage);
    public final Setting<Float> targetRange = new Setting<>("TargetRange", 10.0f, 1.0f, 16.0f);

    private final TickTimer placeTimer = new TickTimer();
    private final TickTimer destroyTimer = new TickTimer();
    private final TickTimer swapTimer = new TickTimer();

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
            Inferno.inventoryManager.swap(this.oldSlot, this.swap.getValue());
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
                SPacketSoundEffect packet = event.getPacket();
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
                SPacketExplosion packet = event.getPacket();
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
            SPacketSpawnObject packet = event.getPacket();
            if (packet.getType() == 51 && mc.world.getEntityByID(packet.getEntityID()) instanceof EntityEnderCrystal) {
                BlockPos pos = new BlockPos(packet.getX(), packet.getY(), packet.getZ());
                if (this.placePos != null && this.placePos.equals(pos.down())) {
                    this.placePos = null;
                    this.placeTimer.reset();
                }
            }
        } else if (event.getPacket() instanceof SPacketDestroyEntities) {
            if (this.sync.getValue()) {
                for (int id : ((SPacketDestroyEntities) event.getPacket()).getEntityIDs()) {
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
                CrystalUtil.place(this.placePos, this.hand, this.direction.getValue(), this.swing.getValue(), this.raytrace.getValue().offset);
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
                CrystalUtil.destroy(this.crystal, this.hand, this.swing.getValue(), this.destroyPacket.getValue());
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

                        if (Inferno.friendManager.isFriend(player.getUniqueID())) {
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
            if (this.swap.getValue() != InventoryManager.Swap.None && this.placePos != null) {
                int slot = InventoryUtil.getHotbarItemSlot(Items.END_CRYSTAL, true);
                if (slot == -1) {
                    return false;
                }

                this.hand = slot == InventoryUtil.OFFHAND_SLOT ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND;
                if (this.hand == EnumHand.MAIN_HAND) {
                    this.oldSlot = mc.player.inventory.currentItem;
                    Inferno.inventoryManager.swap(slot, this.swap.getValue());
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

    public enum YawStep {
        None, Semi, Full
    }

    public enum Targeting {
        Closest, Damage
    }

    public enum Raytrace {
        None(-1.0),
        Base(0.5),
        Normal(1.5),
        Double(2.5);

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
            if (yawStep.getValue() == YawStep.None) {
                this.rotation.setYaw(yaw);
                this.rotation.setPitch(pitch);
                Inferno.rotationManager.setRotations(yaw, pitch);
            } else {
                if (yawStep.getValue() == YawStep.Semi && !breaking) {
                    this.rotation.setYaw(yaw);
                    this.rotation.setPitch(pitch);
                    Inferno.rotationManager.setRotations(yaw, pitch);
                    return;
                }

                this.rotate(yaw, pitch);
            }
        }

        public void rotate(float yaw, float pitch) {
            if (yawStep.getValue() != YawStep.None) {
                if (this.getYaw() == -1.0f && this.getPitch() == -1.0f) {
                    this.rotation.setYaw(yaw);
                    this.rotation.setPitch(pitch);
                    Inferno.rotationManager.setRotations(yaw, pitch);
                    return;
                }

                if (this.rotations.isEmpty()) {
                    double yawDiff = 0.0, pitchDiff = 0.0;

                    if (Math.abs(yaw - this.rotation.getYaw()) >= yawStepThreshold.getValue()) {
                        yawDiff = Math.abs(yaw - this.rotation.getYaw()) / yawSteps.getValue();
                    }

                    if (Math.abs(pitch - this.rotation.getPitch()) >= yawStepThreshold.getValue()) {
                        pitchDiff = Math.abs(pitch - this.rotation.getPitch()) / yawSteps.getValue();
                    }

                    if (yawDiff != 0.0 || pitchDiff != 0.0) {
                        this.timer.reset();

                        float y = yawDiff == 0.0 ? yaw : this.rotation.getYaw();
                        float p = pitchDiff == 0.0 ? pitch : this.rotation.getPitch();
                        for (int step = 0; step < yawSteps.getValue(); ++step) {
                            y = this.rotation.getYaw() > yaw ? y - (float) yawDiff : y + (float) yawDiff;
                            p = this.rotation.getPitch() > pitch ? p - (float) pitchDiff : p + (float) pitchDiff;

                            this.rotations.add(new RotationUtil.Rotation(y, p));
                        }
                    }
                } else {
                    if (this.timer.passed(yawStepTicks.getValue())) {
                        this.timer.reset();

                        int packets = yawSteps.getValue() / yawStepTicks.getValue();
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
