package cope.inferno.impl.features.module.modules.combat;

import cope.inferno.impl.event.entity.DeathEvent;
import cope.inferno.impl.event.entity.TotemPopEvent;
import cope.inferno.impl.features.module.Module;
import cope.inferno.impl.settings.Setting;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Module.Define(name = "AutoGG", category = Module.Category.Combat)
@Module.Info(description = "Says something to the person you killed.")
public class AutoGG extends Module {
    public final Setting<Boolean> death = new Setting<>("Death", true);
    public final Setting<String> deathMessage = new Setting<>("DeathMessage", "GG <player>! Inferno owns me and all!");

    public final Setting<Boolean> totemPop = new Setting<>("TotemPop", true);
    public final Setting<String> totemPopMessage = new Setting<>("TotemPopMessage", "Keep on popping <player>!");

    @SubscribeEvent
    public void onDeath(DeathEvent event) {
        if (this.death.getValue()) {
            if (event.getPlayer() == AutoCrystal.INSTANCE.target || event.getPlayer() == Aura.INSTANCE.target) {
                mc.player.sendChatMessage(this.formatMessage(event.getPlayer(), this.deathMessage.getValue()));
            }
        }
    }

    @SubscribeEvent
    public void onTotemPop(TotemPopEvent event) {
        if (totemPop.getValue()) {
            if (event.getPlayer() == AutoCrystal.INSTANCE.target || event.getPlayer() == Aura.INSTANCE.target) {
                mc.player.sendChatMessage(this.formatMessage(event.getPlayer(), this.totemPopMessage.getValue()));
            }
        }
    }

    private String formatMessage(EntityPlayer player, String message) {
        return message.replaceAll("<player>", player.getName());
    }
}
