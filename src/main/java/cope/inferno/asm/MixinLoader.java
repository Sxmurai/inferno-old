package cope.inferno.asm;

import cope.inferno.core.Inferno;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;
import org.spongepowered.asm.mixin.transformer.Config;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.stream.Collectors;

@IFMLLoadingPlugin.Name("MixinLoader")
@IFMLLoadingPlugin.MCVersion("1.12.2")
public class MixinLoader implements IFMLLoadingPlugin {
    public MixinLoader() {
        Inferno.LOGGER.info("Initializing mixin bootstrap...");

        MixinBootstrap.init();

        MixinEnvironment.getCurrentEnvironment().setObfuscationContext("searge");
        MixinEnvironment.getCurrentEnvironment().setSide(MixinEnvironment.Side.CLIENT);

        Mixins.addConfiguration("mixins.inferno.json");

        Inferno.LOGGER.info(
                "Added mixin configurations: {}",
                Mixins.getConfigs().stream().map(Config::getName).collect(Collectors.joining(", "))
        );
    }

    @Override
    public String[] getASMTransformerClass() {
        return new String[0];
    }

    @Override
    public String getModContainerClass() {
        return null;
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {

    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }
}
