package cope.inferno.impl.manager;

import cope.inferno.Inferno;
import cope.inferno.impl.config.Config;
import cope.inferno.impl.config.configs.FriendsConfig;
import cope.inferno.impl.config.configs.HudConfig;
import cope.inferno.impl.config.configs.ModulesConfig;
import cope.inferno.impl.config.configs.WallhackConfig;
import org.apache.commons.lang3.time.StopWatch;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

// this might be the msot dogshit code ive ever written but it works
public class ConfigManager {
    private static final ScheduledExecutorService SERVICE = Executors.newScheduledThreadPool(1);

    private final ArrayList<Config> configs = new ArrayList<>();

    private final ArrayList<String> profiles = new ArrayList<>();
    private String currentProfile = "default";

    public ConfigManager() {
        // global configs
        this.configs.add(new FriendsConfig());
        this.configs.add(new HudConfig());

        // per profile configs
        this.configs.add(new ModulesConfig());
        this.configs.add(new WallhackConfig());

        Inferno.LOGGER.info("Loading all profiles...");
        this.loadProfiles();
        Inferno.LOGGER.info("Loaded a total of {} profile(s)", this.profiles.size());
    }

    private void loadProfiles() {
        FileManager fileManager = FileManager.getInstance();
        Path profilesFolder = fileManager.getClientFolder().resolve("profiles");
        if (!Files.exists(profilesFolder)) {
            fileManager.makeDirectory(profilesFolder);
        }

        Path currentProfile = profilesFolder.resolve("current_profile.txt");
        if (!Files.exists(currentProfile)) {
            fileManager.write(currentProfile, "default"); // write to the current_profile.txt file
            fileManager.makeDirectory(profilesFolder.resolve("default")); // make the default profile
        } else {
            String text = fileManager.read(currentProfile);
            if (text == null || text.isEmpty()) {
                fileManager.write(currentProfile, text = "default");
            }

            this.currentProfile = text.replaceAll("\n", "");
        }

        if (Files.isDirectory(profilesFolder)) {
            for (File file : Objects.requireNonNull(profilesFolder.toFile().listFiles())) {
                if (file.isDirectory()) {
                    this.profiles.add(file.getName());
                }
            }
        }
    }

    public void load() {
        this.configs.forEach((config) -> {
            StopWatch stopwatch = new StopWatch();
            stopwatch.start();

            try {
                config.load();
            } catch (Exception e) {
                Inferno.LOGGER.error(e);
            }

            stopwatch.stop();
            Inferno.LOGGER.info("Loaded config {} in {}ms", config.getName(), stopwatch.getTime(TimeUnit.MILLISECONDS));
        });
    }

    public void save() {
        this.configs.forEach((config) -> {
            StopWatch stopwatch = new StopWatch();
            stopwatch.start();

            try {
                config.save();
            } catch (Exception e) {
                Inferno.LOGGER.error(e);
            }

            stopwatch.stop();
            Inferno.LOGGER.info("Saved config {} in {}ms", config.getName(), stopwatch.getTime(TimeUnit.MILLISECONDS));
        });
    }

    public void reset() {
        this.configs.forEach(Config::reset);
    }

    public void setCurrentProfile(String currentProfile) {
        this.currentProfile = currentProfile;
        Inferno.LOGGER.info("Setting profile to {}", this.currentProfile);
        FileManager.getInstance().write(FileManager.getInstance().getClientFolder().resolve("profiles").resolve("current_profile.txt"), this.currentProfile);
    }

    public ArrayList<String> getProfiles() {
        return profiles;
    }

    public String getCurrentProfile() {
        return currentProfile;
    }

    public Path getProfilePath() {
        Path path = FileManager.getInstance().getClientFolder().resolve("profiles").resolve(this.currentProfile);
        if (!Files.exists(path)) {
            Inferno.LOGGER.info("Profile set was not found. Putting back to the default config...");
            this.currentProfile = "default";
            return this.getProfilePath();
        }

        return path;
    }
}
