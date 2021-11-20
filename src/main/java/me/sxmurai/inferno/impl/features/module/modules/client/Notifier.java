package me.sxmurai.inferno.impl.features.module.modules.client;

import com.mojang.realmsclient.gui.ChatFormatting;
import io.netty.util.internal.ConcurrentSet;
import me.sxmurai.inferno.Inferno;
import me.sxmurai.inferno.impl.event.entity.EntityRemoveEvent;
import me.sxmurai.inferno.impl.event.entity.EntitySpawnEvent;
import me.sxmurai.inferno.impl.event.inferno.ModuleToggledEvent;
import me.sxmurai.inferno.impl.event.network.PacketEvent;
import me.sxmurai.inferno.impl.features.command.Command;
import me.sxmurai.inferno.impl.features.module.Module;
import me.sxmurai.inferno.impl.settings.Setting;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.MobEffects;
import net.minecraft.init.SoundEvents;
import net.minecraft.network.play.server.SPacketSoundEffect;
import net.minecraftforge.event.entity.living.PotionEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Comparator;
import java.util.Set;

@Module.Define(name = "Notifier", category = Module.Category.Client)
@Module.Info(description = "Notifies you when things happen")
public class Notifier extends Module {
    public static Notifier INSTANCE;

    public final Setting<Boolean> modules = new Setting<>("Modules", true);
    public static final Setting<Boolean> totems = new Setting<>("Totems", false);
    public final Setting<Boolean> pearl = new Setting<>("Pearl", false);
    public final Setting<Boolean> chorus = new Setting<>("Chorus", false);
    public final Setting<Boolean> visualRange = new Setting<>("VisualRange", false);
    public final Setting<Boolean> friends = new Setting<>("Friends", false, this.visualRange::getValue);
    public final Setting<Boolean> strength = new Setting<>("Strength", false);

    private final Set<EntityPlayer> strengthCunts = new ConcurrentSet<>();

    public Notifier() {
        INSTANCE = this;
    }

    @Override
    protected void onDeactivated() {
        this.strengthCunts.clear();
    }

    @Override
    public void onUpdate() {
        if (!this.strengthCunts.isEmpty()) {
            if (!this.strength.getValue()) {
                this.strengthCunts.clear();
                return;
            }

            for (EntityPlayer player : this.strengthCunts) {
                if (!player.isPotionActive(MobEffects.STRENGTH)) {
                    Command.send(player.getName() + " no longer has strength!");
                    this.strengthCunts.remove(player);
                }
            }
        }
    }

    @SubscribeEvent
    public void onModuleToggled(ModuleToggledEvent event) {
        if (this.modules.getValue()) {
            Module module = event.getModule();
            Inferno.notificationManager.notify(
                    module.hashCode(),
                    module.getName() + " has been toggled "
                            + (module.isOn() ? (ChatFormatting.GREEN + "on") : (ChatFormatting.RED + "off")) + ChatFormatting.RESET + "."
            );
        }
    }

    @SubscribeEvent
    public void onEntitySpawnEvent(EntitySpawnEvent event) {
        if (event.getEntity() instanceof EntityPlayer) {
            if (this.visualRange.getValue()) {
                Inferno.notificationManager.notifyNoEdit(event.getEntity().getName() + " has entered your render range.");
            }
        } else if (event.getEntity() instanceof EntityEnderPearl) {
            // @todo: this for some reason also notifies when the pearl hits the ground. i dont know why it does this
            if (this.pearl.getValue()) {
                mc.world.playerEntities.stream()
                        // .filter((player) -> player != null)
                        .min(Comparator.comparingDouble((v) -> event.getEntity().getDistance(v)))
                        .ifPresent(player -> Inferno.notificationManager.notifyNoEdit(player.getName() + " has thrown a pearl!"));
            }
        }
    }

    @SubscribeEvent
    public void onEntityRemove(EntityRemoveEvent event) {
        if (event.getEntity() instanceof EntityPlayer && this.visualRange.getValue()) {
            Inferno.notificationManager.notifyNoEdit(event.getEntity().getName() + " has left your render range.");
        }
    }

    @SubscribeEvent
    public void onPacketReceive(PacketEvent.Receive event) {
        if (event.getPacket() instanceof SPacketSoundEffect) {
            SPacketSoundEffect packet = (SPacketSoundEffect) event.getPacket();
            if (packet.getSound() == SoundEvents.ITEM_CHORUS_FRUIT_TELEPORT && this.chorus.getValue()) {
                double x = packet.getX();
                double y = packet.getY();
                double z = packet.getZ();
                Command.send("A player ate a chorus fruit at " + ChatFormatting.RED + "X: " + x + ", Y: " + y + ", Z: " + z + ChatFormatting.RESET);
            }
        }
    }

    @SubscribeEvent
    public void onPotionAdded(PotionEvent.PotionAddedEvent event) {
        if (event.getPotionEffect().getPotion() == MobEffects.STRENGTH && this.strength.getValue()) {
            if (event.getEntity() instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) event.getEntity();
                if (!this.strengthCunts.contains(player)) {
                    this.strengthCunts.add(player);
                    Command.send(player.getName() + " has strength!");
                }
            }
        }
    }
}
