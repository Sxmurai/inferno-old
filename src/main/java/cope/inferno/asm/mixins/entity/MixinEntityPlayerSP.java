package cope.inferno.asm.mixins.entity;

import com.mojang.authlib.GameProfile;
import cope.inferno.impl.event.entity.MoveEvent;
import cope.inferno.impl.event.entity.PushEvent;
import cope.inferno.impl.event.entity.UpdateWalkingPlayerEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.MoverType;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityPlayerSP.class)
public abstract class MixinEntityPlayerSP extends AbstractClientPlayer {
    @Shadow public Minecraft mc;

    @Shadow public double lastReportedPosX;
    @Shadow public double lastReportedPosY;
    @Shadow public double lastReportedPosZ;
    @Shadow public float lastReportedYaw;
    @Shadow public float lastReportedPitch;
    @Shadow public boolean serverSprintState;
    @Shadow public boolean serverSneakState;
    @Shadow public boolean prevOnGround;
    @Shadow public int positionUpdateTicks;
    @Shadow public boolean autoJumpEnabled;

    public MixinEntityPlayerSP(World worldIn, GameProfile playerProfile) {
        super(worldIn, playerProfile);
    }

    @Shadow public abstract boolean isCurrentViewEntity();

    @Redirect(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/AbstractClientPlayer;move(Lnet/minecraft/entity/MoverType;DDD)V"))
    public void move(AbstractClientPlayer player, MoverType moverType, double x, double y, double z) {
        MoveEvent event = new MoveEvent(moverType, x, y, z);
        MinecraftForge.EVENT_BUS.post(event);

        if (!event.isCanceled() || event.stillMove()) {
            super.move(moverType, event.getX(), event.getY(), event.getZ());
        }
    }

    @Inject(method = "onUpdateWalkingPlayer", at = @At("HEAD"), cancellable = true)
    public void onUpdateWalkingPlayerPre(CallbackInfo info) {
        UpdateWalkingPlayerEvent event = new UpdateWalkingPlayerEvent(UpdateWalkingPlayerEvent.Era.PRE, this.rotationYaw, this.rotationPitch);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            this.handlePositioning(event.getYaw(), event.getPitch());
            info.cancel();
        }
    }

    @Inject(method = "onUpdateWalkingPlayer", at = @At("RETURN"))
    public void onUpdateWalkingPlayerPost(CallbackInfo info) {
        MinecraftForge.EVENT_BUS.post(new UpdateWalkingPlayerEvent(UpdateWalkingPlayerEvent.Era.POST));
    }

    @Inject(method = "pushOutOfBlocks", at = @At("HEAD"), cancellable = true)
    public void onPushOutOfBlocks(double x, double y, double z, CallbackInfoReturnable<Boolean> info) {
        PushEvent event = new PushEvent(PushEvent.Type.BLOCKS, this);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            info.setReturnValue(false);
        }
    }

    // this is taken exactly from the minecraft code, just the variables are renamed to more human-readable names.
    // this is because we cancel onUpdateWalkingPlayer in our RotationManager, so we want to make sure to sync our states with the server.
    private void handlePositioning(float yaw, float pitch) {
        if (this.isSprinting() != this.serverSprintState) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, this.isSprinting() ? CPacketEntityAction.Action.START_SPRINTING : CPacketEntityAction.Action.STOP_SPRINTING));
            this.serverSprintState = this.isSprinting();
        }

        if (this.isSneaking() != this.serverSneakState) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, this.isSneaking() ? CPacketEntityAction.Action.START_SNEAKING : CPacketEntityAction.Action.STOP_SNEAKING));
            this.serverSneakState = this.isSneaking();
        }

        if (this.isCurrentViewEntity()) {
            ++this.positionUpdateTicks;

            double minY = this.getEntityBoundingBox().minY;

            boolean moved = Math.pow(this.posX - this.lastReportedPosX, 2) + Math.pow(minY - this.lastReportedPosY, 2) + Math.pow(this.posZ - this.lastReportedPosZ, 2) > 9.0E-4D || this.positionUpdateTicks >= 20;
            boolean rotated = yaw - this.lastReportedYaw != 0.0f || pitch - this.lastReportedPitch != 0.0f;

            if (this.isRiding()) {
                mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(this.motionX, -999.0, this.motionZ, yaw, pitch, this.onGround));
                moved = false;
            }

            if (moved && rotated) {
                mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(this.posX, minY, this.posZ, yaw, pitch, this.onGround));
            } else if (moved) {
                mc.player.connection.sendPacket(new CPacketPlayer.Position(this.posX, minY, this.posZ, this.onGround));
            } else if (rotated) {
                mc.player.connection.sendPacket(new CPacketPlayer.Rotation(yaw, pitch, this.onGround));
            } else if (this.prevOnGround != this.onGround) {
                mc.player.connection.sendPacket(new CPacketPlayer(this.onGround));
            }

            if (moved) {
                this.lastReportedPosX = this.posX;
                this.lastReportedPosY = minY;
                this.lastReportedPosZ = this.posZ;
                this.positionUpdateTicks = 0;
            }

            if (rotated) {
                this.lastReportedYaw = yaw;
                this.lastReportedPitch = pitch;
            }

            this.prevOnGround = this.onGround;
            this.autoJumpEnabled = mc.gameSettings.autoJump;
        }
    }
}
