package cope.inferno.asm.impl.entity.player;

import com.mojang.authlib.GameProfile;
import cope.inferno.core.events.EntityVelocityEvent;
import cope.inferno.core.events.MoveEvent;
import cope.inferno.core.events.UpdateWalkingPlayerEvent;
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

    @Shadow
    public abstract boolean isCurrentViewEntity();

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
        UpdateWalkingPlayerEvent event = new UpdateWalkingPlayerEvent(posX, posY, posZ, onGround, rotationYaw, rotationPitch);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            info.cancel();
            modifiedOnUpdateWalkingPlayer(event.getX(), event.getY(), event.getZ(), event.isOnGround(), event.getYaw(), event.getPitch());
        }
    }

    @Inject(method = "onUpdateWalkingPlayer", at = @At("RETURN"))
    public void onUpdateWalkingPlayerPost(CallbackInfo info) {
        MinecraftForge.EVENT_BUS.post(new UpdateWalkingPlayerEvent());
    }

    @Inject(method = "pushOutOfBlocks", at = @At("HEAD"), cancellable = true)
    public void pushOutOfBlocks(double x, double y, double z, CallbackInfoReturnable<Boolean> info) {
        EntityVelocityEvent event = new EntityVelocityEvent(EntityVelocityEvent.Material.BLOCK);
        MinecraftForge.EVENT_BUS.post(event);

        if (event.isCanceled()) {
            info.setReturnValue(false);
        }
    }

    /**
     * Taken from the minecraft code.
     * Spoofs position and rotation states sent to the server
     * @param x The x value
     * @param y The y value
     * @param z The z value
     * @param grounded If we are on the ground or not
     * @param yaw The yaw rotation value
     * @param pitch The pitch rotation value
     */
    private void modifiedOnUpdateWalkingPlayer(double x, double y, double z, boolean grounded, float yaw, float pitch) {
        if (isSprinting() != serverSprintState) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, isSprinting() ? CPacketEntityAction.Action.START_SPRINTING : CPacketEntityAction.Action.STOP_SPRINTING));
            serverSprintState = isSprinting();
        }

        if (isSneaking() != serverSneakState) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, isSneaking() ? CPacketEntityAction.Action.START_SNEAKING : CPacketEntityAction.Action.STOP_SNEAKING));
            serverSneakState = isSneaking();
        }

        if (isCurrentViewEntity()) {
            ++positionUpdateTicks;

            boolean moved = Math.pow(x - lastReportedPosX, 2) + Math.pow(y - lastReportedPosY, 2) + Math.pow(z - lastReportedPosZ, 2) > 9.0E-4D || positionUpdateTicks >= 20;
            boolean rotated = yaw - lastReportedYaw != 0.0f || pitch - lastReportedPitch != 0.0f;

            if (isRiding()) {
                mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(motionX, -999.0, motionZ, yaw, pitch, grounded));
                moved = false;
            }

            if (moved && rotated) {
                mc.player.connection.sendPacket(new CPacketPlayer.PositionRotation(x, y, z, yaw, pitch, grounded));
            } else if (moved) {
                mc.player.connection.sendPacket(new CPacketPlayer.Position(x, y, z, grounded));
            } else if (rotated) {
                mc.player.connection.sendPacket(new CPacketPlayer.Rotation(yaw, pitch, grounded));
            } else if (prevOnGround != grounded) {
                mc.player.connection.sendPacket(new CPacketPlayer(grounded));
            }

            if (moved) {
                lastReportedPosX = x;
                lastReportedPosY = y;
                lastReportedPosZ = z;
                positionUpdateTicks = 0;
            }

            if (rotated) {
                lastReportedYaw = yaw;
                lastReportedPitch = pitch;
            }

            prevOnGround = grounded;
            autoJumpEnabled = mc.gameSettings.autoJump;
        }
    }
}
