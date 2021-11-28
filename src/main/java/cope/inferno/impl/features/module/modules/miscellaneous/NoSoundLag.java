package cope.inferno.impl.features.module.modules.miscellaneous;

import cope.inferno.impl.event.network.PacketEvent;
import cope.inferno.impl.features.module.Module;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.util.SoundEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Arrays;

@Module.Define(name = "NoSoundLag")
@Module.Info(description = "Stops you from lagging because of sounds")
public class NoSoundLag extends Module {
    private static final SoundEvent[] EVENTS = new SoundEvent[] {
            SoundEvents.ITEM_ARMOR_EQUIP_GENERIC,
            SoundEvents.ITEM_ARMOR_EQIIP_ELYTRA,
            SoundEvents.ITEM_ARMOR_EQUIP_CHAIN,
            SoundEvents.ITEM_ARMOR_EQUIP_DIAMOND,
            SoundEvents.ITEM_ARMOR_EQUIP_GOLD,
            SoundEvents.ITEM_ARMOR_EQUIP_IRON,
            SoundEvents.ITEM_ARMOR_EQUIP_LEATHER,
            SoundEvents.BLOCK_PISTON_EXTEND,
            SoundEvents.BLOCK_PISTON_CONTRACT,
            SoundEvents.ENTITY_PLAYER_LEVELUP
    };

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof SPacketSoundEffect) {
            SPacketSoundEffect packet = event.getPacket();
            if (Arrays.stream(EVENTS).anyMatch((sound) -> sound.equals(packet.getSound()))) {
                event.setCanceled(true);
            }
        }
    }
}
