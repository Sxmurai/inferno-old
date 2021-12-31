package cope.inferno.core.features.module.client;

import com.mojang.realmsclient.gui.ChatFormatting;
import cope.inferno.core.events.ModuleToggleEvent;
import cope.inferno.core.events.PacketEvent;
import cope.inferno.core.features.module.Category;
import cope.inferno.core.features.module.Module;
import cope.inferno.core.setting.Setting;
import cope.inferno.util.internal.ChatUtil;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraft.network.play.server.SPacketSpawnObject;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Comparator;

public class Notifier extends Module {
    public static Notifier INSTANCE;

    public Notifier() {
        super("Notifier", Category.CLIENT, "Notifies you upon actions that happen");
        toggle(); // force enabled until configurations loaded say otherwise

        INSTANCE = this;
    }

    public static final Setting<Boolean> modules = new Setting<>("Modules", true);

    public static final Setting<Boolean> totems = new Setting<>("Totems", true);
    public static final Setting<Boolean> self = new Setting<>(totems, "Self", false);

    // @todo
    public static final Setting<Boolean> visualRange = new Setting<>("VisualRange", false);
    public static final Setting<Boolean> friends = new Setting<>(visualRange, "Friends", true);

    public static final Setting<Boolean> strength = new Setting<>("Strength", false);
    public static final Setting<Boolean> weakness = new Setting<>("Weakness", false);

    public static final Setting<Boolean> pearls = new Setting<>("Pearls", false);
    public static final Setting<Boolean> chorus = new Setting<>("Chorus", false);

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof SPacketSpawnObject) {
            SPacketSpawnObject packet = event.getPacket();
            if (pearls.getValue() && packet.getType() == 65) {
                mc.world.playerEntities.stream()
                        .min(Comparator.comparingDouble((p) -> p.getDistance(packet.getX(), packet.getY(), packet.getZ())))
                        .ifPresent((player) -> ChatUtil.sendPrefixed(player.getName() + " threw an ender pearl!"));
            }
        } else if (event.getPacket() instanceof SPacketSoundEffect) {
            SPacketSoundEffect packet = event.getPacket();
            if (chorus.getValue() && packet.getSound().equals(SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT)) {
                ChatUtil.sendPrefixed("Player teleported to X: " +
                        (int) packet.getX() + ", Y: " +
                        (int) packet.getY() + ", Z: " +
                        (int) packet.getZ() + ".");
            }
        }
    }

    @SubscribeEvent
    public void onModuleToggle(ModuleToggleEvent event) {
        if (modules.getValue()) {
            ChatUtil.sendPersistent(event.getModule().hashCode(), event.getModule().getName() +
                    (event.getState() ?
                            ChatFormatting.GREEN + " enabled" :
                            ChatFormatting.RED + " disabled"
                    )
            );
        }
    }

    @SubscribeEvent
    public void onPotionAdd(PotionEvent.PotionAddedEvent event) {
        if (strength.getValue() && event.getPotionEffect().getPotion().equals(MobEffects.STRENGTH)) {
            ChatUtil.sendPrefixed(event.getEntity().getName() + " has strength!");
        }

        if (weakness.getValue() && event.getPotionEffect().getPotion().equals(MobEffects.WEAKNESS) && event.getEntity().equals(mc.player)) {
            ChatUtil.sendPrefixed("You have been given weakness!");
        }
    }

    @SubscribeEvent
    public void onPotionRemove(PotionEvent.PotionRemoveEvent event) {
        if (strength.getValue() && event.getPotion().equals(MobEffects.STRENGTH)) {
            ChatUtil.sendPrefixed(event.getEntity().getName() + " no longer has strength!");
        }

        if (weakness.getValue() && event.getPotion().equals(MobEffects.WEAKNESS) && event.getEntity().equals(mc.player)) {
            ChatUtil.sendPrefixed("You no longer have weakness!");
        }
    }
}
