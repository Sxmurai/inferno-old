package cope.inferno.impl.features.module.modules.combat;

import cope.inferno.Inferno;
import cope.inferno.impl.event.entity.EntityRemoveEvent;
import cope.inferno.impl.event.network.PacketEvent;
import cope.inferno.impl.features.module.Module;
import cope.inferno.impl.manager.InventoryManager;
import cope.inferno.impl.settings.Setting;
import cope.inferno.util.combat.CrystalUtil;
import cope.inferno.util.combat.DamageUtil;
import cope.inferno.util.entity.EntityUtil;
import cope.inferno.util.entity.InventoryUtil;
import cope.inferno.util.entity.RotationUtil;
import cope.inferno.util.render.ColorUtil;
import cope.inferno.util.render.RenderUtil;
import cope.inferno.util.timing.Timer;
import cope.inferno.util.world.BlockUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.server.SPacketDestroyEntities;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.network.play.server.SPacketSpawnObject;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Module.Define(name = "AutoCrystal", category = Module.Category.Combat)
@Module.Info(description = "Automatically breaks and destroys end crystals")
public class AutoCrystal extends Module {
    public static AutoCrystal INSTANCE;

    // place settings
    public final Setting<Boolean> place = new Setting<>("Place", true);
    public final Setting<Double> placeRange = new Setting<>("PlaceRange", 5.0, 1.0, 6.0);
    public final Setting<Double> placeTrace = new Setting<>("PlaceTrace", 3.0, 1.0, 6.0);
    public final Setting<Float> placeSpeed = new Setting<>("PlaceSpeed", 18.0f, 1.0f, 20.0f);
    public final Setting<CrystalUtil.Direction> direction = new Setting<>("Direction", CrystalUtil.Direction.Normal);
    public final Setting<Float> placeDamage = new Setting<>("PlaceDamage", 4.0f, 1.0f, 20.0f);
    public final Setting<Placement> placement = new Setting<>("Placement", Placement.Native);
    public final Setting<Float> faceplace = new Setting<>("Faceplace", 14.0f, 1.0f, 20.0f);
    public final Setting<Float> faceplaceDamage = new Setting<>("FaceplaceDamage", 2.0f, 1.0f, 4.0f);
    public final Setting<Boolean> prediction = new Setting<>("Prediction", true);
    public final Setting<Float> predictDelay = new Setting<>("PredictDelay", 25.0f, 0.0f, 500.0f);

    // explode settings
    public final Setting<Boolean> explode = new Setting<>("Explode", true);
    public final Setting<Double> explodeRange = new Setting<>("ExplodeRange", 5.0, 1.0, 6.0);
    public final Setting<Double> explodeTrace = new Setting<>("ExplodeTrace", 3.0, 1.0, 6.0);
    public final Setting<Float> explodeSpeed = new Setting<>("ExplodeSpeed", 17.0f, 1.0f, 20.0f);
    public final Setting<Float> explodeDamage = new Setting<>("ExplodeDamage", 2.0f, 1.0f, 20.0f);
    public final Setting<Integer> ticksExisted = new Setting<>("TicksExisted", 1, 0, 10);
    public final Setting<Boolean> inhibit = new Setting<>("Inhibit", true);

    // miscellaneous settings
    public final Setting<Float> oppRange = new Setting<>("TargetRange", 10.0f, 2.0f, 15.0f);
    public final Setting<InventoryManager.Swap> swap = new Setting<>("Swap", InventoryManager.Swap.Legit);
    public final Setting<Timing> timing = new Setting<>("Timing", Timing.Sequential);
    public final Setting<Raytrace> raytrace = new Setting<>("Raytrace", Raytrace.Normal);
    public final Setting<Boolean> ignoreTerrain = new Setting<>("IgnoreTerrain", false);
    public final Setting<Boolean> swing = new Setting<>("Swing", true);
    public final Setting<Rotate> rotate = new Setting<>("Rotate", Rotate.Normal);
    public final Setting<Float> threshold = new Setting<>("Threshold", 55.0f, 10.0f, 360.0f, () -> this.rotate.getValue() == Rotate.Limit);
    public final Setting<Sync> sync = new Setting<>("Sync", Sync.Sound);
    public final Setting<Float> maxLocal = new Setting<>("MaxLocal", 8.0f, 1.0f, 20.0f);
    public final Setting<Float> localBias = new Setting<>("LocalBias", 0.5f, 0.0f, 10.0f);

    // local variables
    private final Timer placeTimer = new Timer();
    private final Timer explodeTimer = new Timer();
    private final Timer predictTimer = new Timer();

    private EnumHand hand = EnumHand.MAIN_HAND;
    private int oldSlot = -1;

    private RotationUtil.Rotation nextRotation = new RotationUtil.Rotation();

    public EntityPlayer target = null;
    private BlockPos placePos = null;
    private EntityEnderCrystal crystal = null;

    public AutoCrystal() {
        INSTANCE = this;
    }

    @Override
    protected void onDeactivated() {
        if (fullNullCheck() && this.oldSlot != -1) {
            Inferno.inventoryManager.swap(this.oldSlot, this.swap.getValue());
        }

        this.oldSlot = -1;
        this.hand = EnumHand.MAIN_HAND;
        this.placeTimer.reset();
        this.explodeTimer.reset();
        this.nextRotation = null;
        this.target = null;
        this.placePos = null;
        this.crystal = null;
    }

    @Override
    public String getDisplayInfo() {
        return this.target == null ? null : this.target.getName();
    }

    @Override
    public void onRenderWorld() {
        if (this.placePos != null) {
            RenderUtil.drawEsp(new AxisAlignedBB(this.placePos).offset(RenderUtil.getScreen()), true, true, 1.5f, ColorUtil.getColor(255, 0, 0, 77));
        }
    }

    @SubscribeEvent
    public void onEntityRemove(EntityRemoveEvent event) {
        if (event.getEntity() instanceof EntityEnderCrystal && event.getEntity().equals(this.crystal)) {
            this.crystal = null;
        }
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if (event.getPacket() instanceof CPacketUseEntity) {
            CPacketUseEntity packet = event.getPacket();
            Entity entity = packet.getEntityFromWorld(mc.world);

            if (packet.getAction() == CPacketUseEntity.Action.ATTACK && entity == null) {
                this.crystal = null;
                event.setCanceled(true);
                return;
            }

            if (entity instanceof EntityEnderCrystal && this.sync.getValue() == Sync.Instant) {
                mc.world.removeEntity(entity);
            }
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof SPacketDestroyEntities) {
            if (!this.inhibit.getValue()) {
                return;
            }

            for (int id : ((SPacketDestroyEntities) event.getPacket()).getEntityIDs()) {
                Entity entity = mc.world.getEntityByID(id);
                if (entity == null || !(entity instanceof EntityEnderCrystal)) {
                    continue;
                }

                if (entity.equals(this.crystal)) {
                    this.crystal = null;
                    if (this.timing.getValue() == Timing.Sequential) {
                        this.explodeTimer.reset();
                    }
                }

                entity.setDead();
                mc.world.removeEntity(entity);
            }
        } else if (event.getPacket() instanceof SPacketSoundEffect) {
            if (this.sync.getValue() != Sync.Sound) {
                return;
            }

            SPacketSoundEffect packet = event.getPacket();
            if (packet.getSound() == SoundEvents.ENTITY_GENERIC_EXPLODE) {
                for (Entity entity : mc.world.loadedEntityList) {
                    if (entity == null || !(entity instanceof EntityEnderCrystal)) {
                        continue;
                    }

                    if (entity.getDistance(packet.getX(), packet.getY(), packet.getZ()) <= 6.0) {
                        entity.setDead();
                        if (entity.equals(this.crystal)) {
                            this.crystal = null;
                            if (this.timing.getValue() == Timing.Sequential) {
                                this.explodeTimer.reset();
                            }
                        }
                    }
                }
            }
        } else if (event.getPacket() instanceof SPacketSpawnObject) {
            if (!this.prediction.getValue()) {
                return;
            }

            // scuffed shit
            SPacketSpawnObject packet = event.getPacket();
            if (this.explode.getValue() && this.target != null && packet.getType() == 51) {
                Entity entity = mc.world.getEntityByID(packet.getEntityID());
                if (entity == null || !(entity instanceof EntityEnderCrystal)) {
                    return;
                }

                if (!this.predictTimer.passedMs(this.predictDelay.getValue().longValue())) {
                    return;
                }

                this.predictTimer.reset();

                BlockPos pos = new BlockPos(packet.getX(), packet.getY(), packet.getZ());
                double dist = mc.player.getDistance(pos.getX(), pos.getY(), pos.getZ());
                if (!BlockUtil.canSeePos(pos, 0.5) && dist > this.explodeTrace.getValue() || dist > this.explodeTrace.getValue()) {
                    return;
                }

                float selfDamage = DamageUtil.calculateDamage(new Vec3d(pos).add(0.5, 1.0, 0.5), mc.player, this.ignoreTerrain.getValue());
                if (selfDamage + this.localBias.getValue() >= EntityUtil.getHealth(mc.player) || selfDamage >= this.maxLocal.getValue()) {
                    return;
                }

                float targetDamage = DamageUtil.calculateDamage(new Vec3d(pos).add(0.5, 1.0, 0.5), this.target, this.ignoreTerrain.getValue());
                if (selfDamage > targetDamage || targetDamage < this.explodeDamage.getValue()) {
                    return;
                }

                this.crystal = (EntityEnderCrystal) entity;
            }
        }
    }

    @Override
    public void onTick() {
        if (!InventoryUtil.isHolding(Items.END_CRYSTAL, true) || this.hand == null && this.placePos != null && this.target != null) {
            int slot = InventoryUtil.getHotbarItemSlot(Items.END_CRYSTAL, true);
            if (slot == -1) {
                return;
            }

            this.hand = slot == 45 ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND;
            if (this.hand == EnumHand.MAIN_HAND) {
                Inferno.inventoryManager.swap(slot, this.swap.getValue());
            }

            mc.player.setActiveHand(this.hand);

            return;
        }

        this.calculatePlacePosition();
        if (this.place.getValue() && this.placeTimer.getPassedTimeMsFloat() / 50.0f >= 20.0f - this.placeSpeed.getValue()) {
            if (this.placePos == null || this.target == null) {
                return;
            }

            if (this.rotate.getValue() != Rotate.None) {
                RotationUtil.Rotation to = RotationUtil.calcRotations(new Vec3d(this.placePos).add(0.5, 0.5, 0.5));

                if (this.rotate.getValue() == Rotate.Limit) {
                    if (this.nextRotation != null) {
                        Inferno.rotationManager.setRotations(this.nextRotation.getYaw(), this.nextRotation.getPitch());
                        this.nextRotation = null;
                    } else {
                        RotationUtil.Rotation[] limit = this.getLimitRotations(to);
                        if (limit == null) {
                            Inferno.rotationManager.setRotations(to.getYaw(), to.getPitch());
                        } else {
                            this.nextRotation = limit[1];
                            Inferno.rotationManager.setRotations(limit[0].getYaw(), limit[1].getPitch());
                            return; // wait a tick
                        }
                    }
                } else {
                    Inferno.rotationManager.setRotations(to.getYaw(), to.getPitch());
                }
            }

            this.placeTimer.reset();
            CrystalUtil.place(this.placePos, this.hand, this.direction.getValue(), this.swing.getValue(), this.raytrace.getValue().boost);
        }

        this.calculateBestCrystal();
        if (this.explode.getValue() && this.explodeTimer.getPassedTimeMsFloat() / 50.0f >= 20.0f - this.explodeSpeed.getValue()) {
            if (this.crystal == null) {
                return;
            }

            if (this.rotate.getValue() != Rotate.None) {
                RotationUtil.Rotation to = RotationUtil.calcRotations(this.crystal.getPositionEyes(mc.getRenderPartialTicks()));

                if (this.rotate.getValue() == Rotate.AllLimit) {
                    if (this.nextRotation != null) {
                        Inferno.rotationManager.setRotations(this.nextRotation.getYaw(), this.nextRotation.getPitch());
                        this.nextRotation = null;
                    } else {
                        RotationUtil.Rotation[] limit = this.getLimitRotations(to);
                        if (limit == null) {
                            Inferno.rotationManager.setRotations(to.getYaw(), to.getPitch());
                        } else {
                            this.nextRotation = limit[1];
                            Inferno.rotationManager.setRotations(limit[0].getYaw(), limit[1].getPitch());
                            return; // wait the tick
                        }
                    }
                } else {
                    Inferno.rotationManager.setRotations(to.getYaw(), to.getPitch());
                }
            }

            this.explodeTimer.reset();
            CrystalUtil.destroy(this.crystal, EnumHand.MAIN_HAND, this.swing.getValue());
        }
    }

    private void calculatePlacePosition() {
        List<BlockPos> placements = DamageUtil.getPositions(this.placeRange.getValue().intValue(), this.placement.getValue() == Placement.Protocol);
        if (placements == null || placements.isEmpty()) {
            return;
        }

        if (!this.isTargetValid(this.target)) {
            Optional<EntityPlayer> player = mc.world.getEntities(EntityPlayer.class, this::isTargetValid).stream().min(Comparator.comparingDouble((e) -> mc.player.getDistance(e)));
            player.ifPresent(entityPlayer -> this.target = entityPlayer);
        }

        BlockPos placement = null;
        float damage = 0.0f;

        for (BlockPos pos : placements) {
            if (!BlockUtil.canSeePos(pos, 0.5) && mc.player.getDistance(pos.getX(), pos.getY(), pos.getZ()) > this.placeTrace.getValue()) {
                continue;
            }

            Vec3d location = new Vec3d(pos).add(0.5, 1.0, 0.5);

            float localDamage = DamageUtil.calculateDamage(location, mc.player, this.ignoreTerrain.getValue());
            if (localDamage + this.localBias.getValue() >= EntityUtil.getHealth(mc.player) || localDamage >= this.maxLocal.getValue()) {
                continue;
            }

            float targetDamage = 0.0f;
            if (this.target != null) {
                targetDamage = DamageUtil.calculateDamage(location, this.target, this.ignoreTerrain.getValue());
                if (targetDamage < localDamage || targetDamage < this.placeDamage.getValue()) {
                    continue;
                }
            }

            for (EntityPlayer player : mc.world.playerEntities) {
                if (!this.isTargetValid(player)) {
                    continue;
                }

                float playerDamage = DamageUtil.calculateDamage(location, player, this.ignoreTerrain.getValue());
                if (targetDamage > playerDamage) {
                    targetDamage = playerDamage;
                    this.target = player;
                }
            }

            if (targetDamage - 0.5f > damage) {
                placement = pos;
                damage = targetDamage;
            }
        }

        if (placement == null && this.target != null && EntityUtil.getHealth(mc.player) <= this.faceplace.getValue()) {
            BlockPos base = new BlockPos(this.target.posX, this.target.posY, this.target.posZ);
            for (EnumFacing facing : EnumFacing.values()) {
                BlockPos surrounding = base.offset(facing);
                if (!DamageUtil.canPlaceCrystal(surrounding, this.placement.getValue() == Placement.Protocol)) {
                    continue;
                }

                // do we even need to calculate local damage? i dont think so

                float targetDamage = DamageUtil.calculateDamage(new Vec3d(surrounding).add(0.5, 1.0, 0.5), this.target, this.ignoreTerrain.getValue());
                if (targetDamage < this.faceplaceDamage.getValue()) {
                    continue;
                }

                if (targetDamage - 0.5f > damage) {
                    placement = surrounding;
                    damage = targetDamage;
                }
            }
        }

        if (!this.isTargetValid(this.target)) {
            this.target = null;
            this.placePos = null;
            return;
        }

        this.placePos = placement;
    }

    private boolean isTargetValid(EntityPlayer p) {
        if (p == null) {
            return false;
        }

        return p != mc.player && !p.isDead && mc.player.getDistance(p) <= this.oppRange.getValue() && !Inferno.friendManager.isFriend(p.getUniqueID());
    }

    private void calculateBestCrystal() {
        if (this.target == null) {
            this.crystal = null;
            return;
        }

        EntityEnderCrystal best = this.crystal;
        float damage = 0.0f;

        for (Entity entity : mc.world.loadedEntityList) {
            if (entity == null || !(entity instanceof EntityEnderCrystal) || entity.ticksExisted < this.ticksExisted.getValue()) {
                continue;
            }

            double distance = mc.player.getDistance(entity);
            if (!mc.player.canEntityBeSeen(entity) && distance > this.explodeTrace.getValue() || distance > this.explodeRange.getValue()) {
                continue;
            }

            Vec3d location = new Vec3d(entity.posX, entity.posY + 0.5, entity.posZ);

            float localDamage = DamageUtil.calculateDamage(location, mc.player, this.ignoreTerrain.getValue());
            if (localDamage + this.localBias.getValue() >= EntityUtil.getHealth(mc.player) || localDamage > this.maxLocal.getValue()) {
                continue;
            }

            float targetDamage = DamageUtil.calculateDamage(location, this.target, this.ignoreTerrain.getValue());
            if (localDamage > targetDamage || targetDamage < this.explodeDamage.getValue()) {
                continue;
            }

            if (targetDamage > damage) {
                best = (EntityEnderCrystal) entity;
                damage = targetDamage;
            }
        }

        this.crystal = best;
    }

    // warning: shit code ahead. if you cant handle it just scroll past it until i make it better
    // so linus says that he thinks that instead of implementing a whole yawstep going by half of the rotation and just delaying to the next tick should be fine
    // this is my implementation, dunno if its good cause i didnt skid it so
    private RotationUtil.Rotation[] getLimitRotations(RotationUtil.Rotation requested) {
        float yaw = Inferno.rotationManager.getYaw();
        float pitch = Inferno.rotationManager.getPitch();

        boolean rYaw = false, rPitch = false;

        float yawDiff = Math.abs(yaw - requested.getYaw());
        if (yawDiff >= this.threshold.getValue()) {
            yawDiff /= 2.0f;
            rYaw = true;
        }

        float pitchDiff = Math.abs(pitch - requested.getPitch());
        if (pitchDiff >= this.threshold.getValue()) {
            pitchDiff /= 2.0f;
            rPitch = true;
        }

        if (!rYaw && !rPitch) {
            return null;
        }

        float halfYaw = rYaw ? (requested.getYaw() > yaw ? yaw + yawDiff : yaw - yawDiff) : requested.getYaw();
        float halfPitch = rPitch ? (requested.getPitch() > pitch ? pitch - pitchDiff : pitch + pitchDiff) : requested.getPitch();

        // 0 = should rotate current
        // 1 = next tick rotation
        return new RotationUtil.Rotation[] { new RotationUtil.Rotation(halfYaw, halfPitch), requested };
    }

    public enum Placement {
        Native, Protocol
    }

    public enum Timing {
        Vanilla, Sequential
    }

    public enum Raytrace {
        None(0.0), Normal(0.5), Double(1.5);

        private final double boost;
        Raytrace(double boost) {
            this.boost = boost;
        }
    }

    public enum Rotate {
        None, Normal, Limit, AllLimit
    }

    public enum Sync {
        None, Sound, Instant
    }
}
