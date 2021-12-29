package cope.inferno.util.entity.inventory;

public enum Swap {
    /**
     * Do not swap
     */
    NONE,

    /**
     * Force swapping client-side
     */
    CLIENT,

    /**
     * Swaps to the slot server-side
     */
    PACKET
}
