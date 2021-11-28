package cope.inferno.impl.manager;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;

public class FileManager {
    private static final FileManager INSTANCE = new FileManager();

    private final Path base;

    private FileManager() {
        this.base = Paths.get("");
        if (!this.exists(this.getClientFolder())) {
            this.makeDirectory(this.getClientFolder());
        }
    }

    public String read(Path path) {
        try {
            return String.join("\n", Files.readAllLines(path));
        } catch (IOException e) {
            return null;
        }
    }

    public void write(Path path, String text) {
        try {
            Files.write(path, Collections.singletonList(text), StandardCharsets.UTF_8, this.exists(path) ? StandardOpenOption.WRITE : StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void delete(Path path) {
        try {
            Files.deleteIfExists(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void makeDirectory(Path path) {
        if (!this.exists(path) || !Files.isDirectory(path)) {
            if (this.exists(path)) {
                this.delete(path);
            }

            try {
                Files.createDirectory(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public boolean exists(Path path) {
        return Files.exists(path);
    }

    public Path getClientFolder() {
        return this.base.resolve("Inferno");
    }

    public Path getBase() {
        return base;
    }

    public static FileManager getInstance() {
        return INSTANCE;
    }
}
