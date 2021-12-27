package cope.inferno.util.internal;

import cope.inferno.core.Inferno;
import net.minecraft.client.Minecraft;

public interface Wrapper {
    Minecraft mc = Minecraft.getMinecraft();

    default boolean nullCheck() {
        return mc.world != null && mc.player != null;
    }

    default Inferno getInferno() {
        return Inferno.INSTANCE;
    }
}
