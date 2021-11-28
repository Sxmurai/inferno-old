package cope.inferno.impl.features;

import net.minecraft.client.Minecraft;

public interface Wrapper {
    Minecraft mc = Minecraft.getMinecraft();

    default boolean fullNullCheck() {
        return mc.player != null && mc.world != null;
    }
}
