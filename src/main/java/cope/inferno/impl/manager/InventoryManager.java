package cope.inferno.impl.manager;

import cope.inferno.impl.event.network.PacketEvent;
import cope.inferno.impl.features.Wrapper;
import net.minecraft.inventory.ClickType;
import net.minecraft.network.play.client.CPacketHeldItemChange;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class InventoryManager implements Wrapper {
    private int serverHotbarSlot = -1;

    @SubscribeEvent
    public void onPacketSend(PacketEvent.Send event) {
        if (event.getPacket() instanceof CPacketHeldItemChange) {
            this.serverHotbarSlot = ((CPacketHeldItemChange) event.getPacket()).getSlotId();
        }
    }

    public void swap(int slot, Swap swap) {
        if (swap == Swap.None) {
            return;
        }

        mc.player.connection.sendPacket(new CPacketHeldItemChange(slot));
        if (swap == Swap.Legit) {
            mc.player.inventory.currentItem = slot;
        }

        mc.playerController.updateController();
    }

    public void sync() {
        if (mc.player.inventory.currentItem != this.serverHotbarSlot) {
            this.swap(this.serverHotbarSlot, Swap.Legit);
        }
    }

    public void click(int slot, ClickType type) {
        mc.playerController.windowClick(0, slot, 0, type, mc.player);
    }

    public int getServerHotbarSlot() {
        return serverHotbarSlot;
    }

    public enum Swap {
        None, Legit, Silent
    }
}
