package cope.inferno.asm.impl.network;

import cope.inferno.core.events.DeathEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.SPacketEntityMetadata;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetHandlerPlayClient.class)
public class MixinNetHandlerPlayClient {
    @Shadow private Minecraft client;

    // this is apparently a better way to get entity deaths vs a packet event.
    // it also seems to work 10x better, so idk
    @Inject(method = "handleEntityMetadata", at = @At("RETURN"))
    private void handleEntityMetadata(SPacketEntityMetadata packetIn, CallbackInfo info) {
        if (client != null && client.world != null) {
            Entity entity = client.world.getEntityByID(packetIn.getEntityId());
            if (entity instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) entity;
                if (player.getHealth() <= 0.0f || player.isDead) {
                    MinecraftForge.EVENT_BUS.post(new DeathEvent(player));
                }
            }
        }
    }
}
