package cope.inferno.impl.config;

import cope.inferno.Inferno;
import cope.inferno.impl.manager.FileManager;

import java.nio.file.Files;
import java.nio.file.Path;

public abstract class Config {
    protected final String name;
    protected final String path;

    public Config(String name, String extension) {
        this.name = name;
        this.path = name + extension;
    }

    public abstract void load();
    public abstract void save();
    public abstract void reset();

    public String parse() {
        Path path = this.getPath();
        if (!Files.exists(path)) {
            return null;
        }

        return FileManager.getInstance().read(path);
    }

    public String getName() {
        return this.name;
    }

    public Path getPath() {
        return Inferno.configManager.getProfilePath().resolve(this.path);
    }
}
