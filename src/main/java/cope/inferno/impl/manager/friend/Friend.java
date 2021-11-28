package cope.inferno.impl.manager.friend;

import java.util.UUID;

public class Friend {
    private final UUID uuid;
    private String alias;

    public Friend(UUID uuid, String alias) {
        this.uuid = uuid;
        this.alias = alias;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getAlias() {
        return this.alias == null ? ("Friend" + this.hashCode()) : this.alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }
}
