package cope.inferno.asm.duck;

import cope.inferno.core.manager.managers.relationships.impl.Relationship;
import cope.inferno.core.manager.managers.relationships.impl.Status;

public interface IEntityPlayer {
    /**
     * Gets the relationship object of this player
     * @return null if none exists, or the relationship object for this player
     */
    Relationship getRelationship();

    /**
     * Gets the relationship status of someone
     * @return The status of the relationship
     */
    Status getStatus();

    /**
     * Sets the relationship status of this player
     * @param status The new status state
     */
    void setStatus(Status status);

    /**
     * Check if the relationship status of the player is equal to an enum
     * @param status The status you would like to check
     * @return true if the parameter enum is equal to the player's relationship status to you
     */
    boolean isRelationship(Status status);
}
