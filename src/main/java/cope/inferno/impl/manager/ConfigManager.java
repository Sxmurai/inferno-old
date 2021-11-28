package cope.inferno.impl.manager;

import cope.inferno.Inferno;
import cope.inferno.impl.config.Config;
import cope.inferno.impl.config.configs.Friends;
import cope.inferno.impl.config.configs.Modules;
import cope.inferno.impl.event.network.SelfConnectionEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.apache.commons.lang3.time.StopWatch;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class ConfigManager {
    private static ConfigManager INSTANCE;

    private final Map<String, Config> configs = new HashMap<>();

    public ConfigManager() {
        Path configsPath = FileManager.getInstance().getClientFolder().resolve("configs");
        if (!FileManager.getInstance().exists(configsPath)) {
            FileManager.getInstance().makeDirectory(configsPath);
        }

        this.configs.put("modules", new Modules());
        this.configs.put("friends", new Friends());

        this.saveConfigs();
    }

    public void saveConfigs() {
        this.configs.forEach((name, config) -> {
            try {
                StopWatch stopwatch = new StopWatch();
                stopwatch.start();
                config.load();
                stopwatch.stop();

                Inferno.LOGGER.info("Loaded {} config in {}ms", name, stopwatch.getTime(TimeUnit.MILLISECONDS));
            } catch (Exception e) {
                Inferno.LOGGER.error("Error loading {} config. Stack trace is as follows:", name);
                e.printStackTrace();
            }
        });
    }

    @SubscribeEvent
    public void onSelfDisconnect(SelfConnectionEvent event) {
        if (event.getType() == SelfConnectionEvent.Type.Disconnect) {
            this.saveConfigs();
        }
    }

    public <T extends Config> T getConfig(String name) {
        for (Map.Entry<String, Config> entry : this.configs.entrySet()) {
            if (entry.getKey().equalsIgnoreCase(name)) {
                return (T) entry.getValue();
            }
        }

        return null;
    }

    public static ConfigManager getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ConfigManager();
        }

        return INSTANCE;
    }
}