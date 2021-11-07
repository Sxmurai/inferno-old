package me.sxmurai.inferno.asm.mixins.entity;

import com.mojang.authlib.GameProfile;
import me.sxmurai.inferno.impl.event.entity.MoveEvent;
import me.sxmurai.inferno.impl.event.entity.PushEvent;
import me.sxmurai.inferno.impl.event.entity.UpdateWalkingPlayerEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.MoverType;
import net.minecraft.network.play.client.CPacketEntityAction;
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
public class MixinEntityPlayerSP extends AbstractClientPlayer {
    @Shadow public Minecraft mc;

    public MixinEntityPlayerSP(World worldIn, GameProfile playerProfile) {
        super(worldIn, playerProfile);
    }

    @Redirect(method = "move", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/entity/AbstractClientPlayer;move(Lnet/minecraft/entity/MoverType;DDD)V"))
    public void move(AbstractClientPlayer player, MoverType moverType, double x, double y, double z) {
        MoveEvent event = new MoveEvent(moverType, x, y, z);
        MinecraftForge.EVENT_BUS.post(event);
        if (!event.isCanceled()) {
            super.move(moverType, event.getX(), event.getY(), event.getZ());
        }
    }

    @Inject(method = "onUpdateWalkingPlayer", at = @At("HEAD"), cancellable = true)
    public void onUpdateWalkingPlayerPre(CallbackInfo info) {
        UpdateWalkingPlayerEvent event = new UpdateWalkingPlayerEvent(UpdateWalkingPlayerEvent.Era.PRE);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            info.cancel();
            handlePositioning();
        }
    }

    @Inject(method = "onUpdateWalkingPlayer", at = @At("RETURN"))
    public void onUpdateWalkingPlayerPost(CallbackInfo info) {
        MinecraftForge.EVENT_BUS.post(new UpdateWalkingPlayerEvent(UpdateWalkingPlayerEvent.Era.POST));
    }

    @Inject(method = "pushOutOfBlocks", at = @At("HEAD"), cancellable = true)
    public void onPushOutOfBlocks(double x, double y, double z, CallbackInfoReturnable<Boolean> info) {
        PushEvent event = new PushEvent(PushEvent.Type.BLOCKS, (Entity) (Object) this);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            info.setReturnValue(false);
        }
    }

    private void handlePositioning() {
        if (mc.player.isSprinting() != mc.player.serverSprintState) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, mc.player.isSprinting() ? CPacketEntityAction.Action.START_SPRINTING : CPacketEntityAction.Action.STOP_SPRINTING));
            mc.player.serverSprintState = mc.player.isSprinting();
        }

        if (mc.player.isSneaking() != mc.player.serverSneakState) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, mc.player.isSneaking() ? CPacketEntityAction.Action.START_SNEAKING : CPacketEntityAction.Action.STOP_SNEAKING));
            mc.player.serverSneakState = mc.player.isSneaking();
        }

        mc.player.lastReportedPosX = mc.player.posX;
        mc.player.lastReportedPosY = mc.player.posY;
        mc.player.lastReportedPosZ = mc.player.posZ;

        mc.player.prevOnGround = mc.player.onGround;

        mc.playerController.updateController();
    }
}
