package cope.inferno.impl.event.network;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.Event;

import java.util.UUID;

public class ConnectionEvent extends Event {
    private final Type type;
    private final String name;
    private final UUID uuid;
    private final EntityPlayer player;

    public ConnectionEvent(Type type, String name, UUID uuid, EntityPlayer player) {
        this.type = type;
        this.name = name;
        this.uuid = uuid;
        this.player = player;
    }

    public Type getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public UUID getUuid() {
        return uuid;
    }

    public EntityPlayer getPlayer() {
        return player;
    }

    public enum Type {
        JOIN, LEAVE
    }
}
