package cope.inferno.impl.manager;

import com.mojang.realmsclient.gui.ChatFormatting;
import cope.inferno.impl.features.Wrapper;
import cope.inferno.impl.features.module.modules.client.Notifier;
import io.netty.util.internal.ConcurrentSet;
import cope.inferno.Inferno;
import cope.inferno.impl.event.entity.DeathEvent;
import cope.inferno.impl.event.entity.TotemPopEvent;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class TotemPopManager implements Wrapper {
    private final Map<EntityPlayer, Integer> pops = new ConcurrentHashMap<>();
    private final Set<EntityPlayer> toAnnounce = new ConcurrentSet<>();

    @SubscribeEvent
    public void onUpdate(LivingEvent.LivingUpdateEvent event) {
        if (event.getEntityLiving() == mc.player && fullNullCheck() && !this.toAnnounce.isEmpty() && Notifier.INSTANCE.isOn() && Notifier.totems.getValue()) {
            for (EntityPlayer player : this.toAnnounce) {
                Integer totems = this.pops.get(player);
                if (totems == null) {
                    this.toAnnounce.remove(player);
                    continue;
                }

                Inferno.notificationManager.notify(
                        player.entityId,
                        player.getName() + " has popped " + ChatFormatting.GREEN + totems + ChatFormatting.RESET + " totems."
                );

                this.toAnnounce.remove(player);
            }
        }
    }

    @SubscribeEvent
    public void onDeath(DeathEvent event) {
        Integer pops = this.pops.get(event.getPlayer());
        if (pops == null) {
            return;
        }

        Inferno.notificationManager.notify(
                event.getPlayer().entityId,
                event.getPlayer().getName() + " died after popping " + ChatFormatting.GREEN + pops + ChatFormatting.RESET + " totems!"
        );

        this.pops.remove(event.getPlayer());
        this.toAnnounce.remove(event.getPlayer());
    }

    @SubscribeEvent
    public void onTotemPop(TotemPopEvent event) {
        if (event.getPlayer() != mc.player) {
            this.pops.merge(event.getPlayer(), 1, Integer::sum);
            this.toAnnounce.add(event.getPlayer());
        }
    }

    @SubscribeEvent
    public void onClientDisconnect(FMLNetworkEvent.ClientDisconnectionFromServerEvent event) {
        this.pops.clear();
    }

    public int getPops(EntityPlayer player) {
        return this.pops.getOrDefault(player, 0);
    }
}
