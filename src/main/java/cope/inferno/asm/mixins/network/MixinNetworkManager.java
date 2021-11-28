package cope.inferno.asm.mixins.network;

import cope.inferno.impl.event.network.PacketEvent;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(NetworkManager.class)
public class MixinNetworkManager {
    @Inject(method = "channelRead0", at = @At("HEAD"), cancellable = true)
    public void channelRead0(ChannelHandlerContext p_channelRead0_1_, Packet<?> p_channelRead0_2_, CallbackInfo info) {
        PacketEvent.Receive event = new PacketEvent.Receive(p_channelRead0_2_);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            info.cancel();
        }
    }

    @Inject(method = "sendPacket", at = @At("HEAD"), cancellable = true)
    public void sendPacket(Packet<?> packetIn, CallbackInfo info) {
        PacketEvent.Send event = new PacketEvent.Send(packetIn);
        MinecraftForge.EVENT_BUS.post(event);
        if (event.isCanceled()) {
            info.cancel();
        }
    }
}
