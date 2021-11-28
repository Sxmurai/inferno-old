package cope.inferno.impl.config;

import cope.inferno.impl.manager.FileManager;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public abstract class Config {
    protected final ScheduledExecutorService service = new ScheduledThreadPoolExecutor(1);
    protected final FileManager fileManager = FileManager.getInstance();

    private final String name;
    protected final Path path;

    public Config() {
        Define definition = this.getClass().getDeclaredAnnotation(Define.class);

        this.name = definition.value();
        this.path = this.fileManager.getBase().resolve(Paths.get("Inferno", definition.paths()));

        this.service.scheduleAtFixedRate(this::save, definition.saveInterval(), definition.saveInterval(), TimeUnit.MINUTES);
    }

    public abstract void save();
    public abstract void load();

    public String getName() {
        return name;
    }

    public Path getPath() {
        return path;
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Define {
        String value();
        String[] paths();
        long saveInterval() default 5L;
    }
}
