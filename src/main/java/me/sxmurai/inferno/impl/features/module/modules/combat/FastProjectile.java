package me.sxmurai.inferno.impl.features.module.modules.combat;

import me.sxmurai.inferno.impl.event.network.PacketEvent;
import me.sxmurai.inferno.impl.features.module.Module;
import me.sxmurai.inferno.impl.settings.Setting;
import me.sxmurai.inferno.util.entity.MovementUtil;
import net.minecraft.init.Items;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Module.Define(name = "FastProjectile", category = Module.Category.Combat)
@Module.Info(description = "Increases the velocity of arrows")
public class FastProjectile extends Module {
    public final Setting<Boolean> bypass = new Setting<>("Bypass", true);
    public final Setting<Boolean> limit = new Setting<>("Limit", true);
    public final Setting<Integer> spoofs = new Setting<>("Spoofs", 10, 1, 50);
    public final Setting<Double> boost = new Setting<>("Boost", 25.0, 1.0, 200.0);

    @Override
    public String getDisplayInfo() {
        return String.valueOf(this.boost.getValue());
    }

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if (event.getPacket() instanceof CPacketPlayerDigging) {
            CPacketPlayerDigging packet = event.getPacket();
            if (packet.getAction() == CPacketPlayerDigging.Action.RELEASE_USE_ITEM && mc.player.getActiveItemStack().getItem() == Items.BOW) {
                if (this.limit.getValue() && mc.player.getItemInUseMaxCount() < 20) {
                    return;
                }

                if (this.bypass.getValue()) {
                    mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, CPacketEntityAction.Action.START_SPRINTING));
                }

                for (int i = 0; i < this.spoofs.getValue(); ++i) {
                    double[] motion = MovementUtil.getDirectionalSpeed(this.boost.getValue() / 10.0);

                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX - motion[0], mc.player.posY, mc.player.posZ - motion[1], true));
                    mc.player.connection.sendPacket(new CPacketPlayer.Position(mc.player.posX + motion[0], mc.player.posY, mc.player.posZ + motion[1], false));
                }
            }
        }
    }
}
