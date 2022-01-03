package cope.inferno.core.manager.managers;

import com.mojang.realmsclient.gui.ChatFormatting;
import cope.inferno.core.events.DeathEvent;
import cope.inferno.core.events.TotemPopEvent;
import cope.inferno.core.features.module.client.Notifier;
import cope.inferno.util.internal.ChatUtil;
import cope.inferno.util.internal.Wrapper;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TotemPopManager implements Wrapper {
    private final Map<EntityPlayer, Integer> pops = new ConcurrentHashMap<>();

    @SubscribeEvent
    public void onTotemPop(TotemPopEvent event) {
        EntityPlayer player = event.getPlayer();
        if (player == null) {
            return;
        }

        pops.merge(player, 1, Integer::sum);
        if (Notifier.INSTANCE.isToggled() && Notifier.totems.getValue()) {
            int total = pops.get(player);
            ChatUtil.sendPersistent(
                    player.hashCode(),
                    player.getName() +
                            " has popped " +
                            ChatFormatting.RED +
                            total +
                            ChatFormatting.RESET +
                            " totem" +
                            (total > 1 ? "s" : "") +
                            ".");
        }
    }

    @SubscribeEvent
    public void onDeath(DeathEvent event) {
        EntityPlayer player = event.getPlayer();
        if (player == null) {
            return;
        }

        if (Notifier.INSTANCE.isToggled() && Notifier.totems.getValue()) {
            int total = pops.getOrDefault(player, -1);
            if (total != -1) {
                ChatUtil.sendPersistent(
                        player.hashCode(),
                        player.getName() +
                                " died after popping " +
                                ChatFormatting.RED +
                                total +
                                ChatFormatting.RESET +
                                " totem" +
                                (total > 1 ? "s" : "") +
                                ".");
            }
        }

        pops.remove(player);
    }

    public Integer getPops(EntityPlayer player) {
        return pops.getOrDefault(player, -1);
    }
}
