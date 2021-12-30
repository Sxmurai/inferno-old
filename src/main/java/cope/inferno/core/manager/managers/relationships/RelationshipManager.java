package cope.inferno.core.manager.managers.relationships;

import cope.inferno.core.manager.BaseManager;
import cope.inferno.core.manager.managers.relationships.impl.Relationship;
import cope.inferno.core.manager.managers.relationships.impl.Status;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class RelationshipManager extends BaseManager {
    private final Set<Relationship> relationships = new HashSet<>();

    @Override
    public void init() {
        // nothing
    }

    public void add(UUID uuid, Status status) {
        if (status != Status.NEUTRAL && relationships.stream().noneMatch((r) -> r.getUuid().equals(uuid))) {
            relationships.add(new Relationship(uuid, null, status));
        }
    }

    public void remove(UUID uuid) {
        relationships.removeIf((r) -> r.getUuid().equals(uuid));
    }

    public Relationship getRelationship(UUID uuid) {
        return relationships.stream().filter((r) -> r.getUuid().equals(uuid)).findFirst().orElse(null);
    }

    public Relationship getRelationship(String alias) {
        return relationships.stream().filter((r) ->r.getAlias().equalsIgnoreCase(alias)).findFirst().orElse(null);
    }

    public Set<Relationship> getRelationships() {
        return relationships;
    }
}
