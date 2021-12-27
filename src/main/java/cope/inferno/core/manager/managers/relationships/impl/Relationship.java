package cope.inferno.core.manager.managers.relationships.impl;

import java.util.UUID;

public class Relationship {
    private final UUID uuid;
    private String alias;
    private Status status;

    public Relationship(UUID uuid, String alias, Status status) {
        this.uuid = uuid;
        this.alias = alias;
        this.status = status;
    }

    public UUID getUuid() {
        return uuid;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Relationship && ((Relationship) o).uuid == uuid;
    }
}
