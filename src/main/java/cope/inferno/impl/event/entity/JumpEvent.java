package cope.inferno.impl.event.entity;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.Event;

public class JumpEvent extends Event {
    private final EntityPlayer player;

    public JumpEvent(EntityPlayer player) {
        this.player = player;
    }

    public EntityPlayer getPlayer() {
        return player;
    }
}
