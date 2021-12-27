package cope.inferno.util.internal.fs;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Collections;
import java.util.List;

public class FileUtil {
    public static final Path BASE_PATH = Paths.get("");
    public static final Path INFERNO_FOLDER = BASE_PATH.resolve("Inferno");

    /**
     * Checks to see if a path exists
     * @param path The path
     * @return true if it exists, false if it does not
     */
    public static boolean exists(Path path) {
        return Files.exists(path);
    }

    /**
     * Checks to see if a file is a directory
     * @param path The path you want to check
     * @return true if it is a directory, false if it is not
     */
    public static boolean isDirectory(Path path) {
        return Files.isDirectory(path);
    }

    /**
     * Checks to see if a file is readable
     * @param path The path you want to check
     * @return true if we can read it, false if we cannot
     */
    public static boolean isReadable(Path path) {
        return Files.isReadable(path);
    }

    /**
     * Checks to see if we have permissions to write to a file/directory
     * @param path The path
     * @return true if we can write, false if we cannot
     */
    public static boolean isWritable(Path path) {
        return Files.isWritable(path);
    }

    /**
     * Creates a file
     * @param path The path to create a file at
     * @param deleteIfExists If to delete the file if it already exists
     */
    public static void touch(Path path, boolean deleteIfExists) {
        // just like how i touched your mother last night

        if (!exists(path)) {
            return;
        }

        if (deleteIfExists && exists(path)) {
            delete(path);
        }

        try {
            Files.createFile(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Creates a directory
     * @param path Where to create that directory
     * @param deleteIfExists If to delete the directory if it already exists
     */
    public static void mkDir(Path path, boolean deleteIfExists) {
        if (deleteIfExists && exists(path)) {
            try {
                Files.delete(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        if (!exists(path)) {
            try {
                Files.createDirectory(path);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Writes to a file
     * @param path The path of the file to write to
     * @param content The new contents of the file
     */
    public static void write(Path path, String content) {
        if (!isWritable(path)) {
            return;
        }

        if (!exists(path)) {
            touch(path, false);
        }

        try {
            Files.write(path, Collections.singleton(content), StandardOpenOption.WRITE);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads the contents of a file
     * @param path The path of the file
     * @return null if the file does not exist/contents are empty, or a string of all the lines joined with a newline
     */
    public static String read(Path path) {
        if (!exists(path) || !isReadable(path)) {
            return null;
        }

        List<String> lines;
        try {
            lines = Files.readAllLines(path);
        } catch (IOException e) {
            return null;
        }

        if (lines == null || lines.isEmpty()) {
            return null;
        }

        return String.join("\n", lines);
    }

    /**
     * Deletes a file at the path provided
     * @param path The path
     */
    public static void delete(Path path) {
        if (!exists(path)) {
            return;
        }

        if (isDirectory(path)) {
            // we have to clear the directory of all files before deleting the directory.
            // if we do not, it will throw an exception and not delete the folder
            deleteAllInPath(path);
        }

        try {
            Files.delete(path);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Deletes all files from a path recursively
     * @param path The directory path
     */
    public static void deleteAllInPath(Path path) {
        if (!exists(path) || !isDirectory(path)) {
            return;
        }

        File[] files = getAllFilesInDirectory(path);
        if (files == null || files.length == 0) {
            delete(path); // if its null or the file length is 0, it should be an empty directory.
            return;
        }

        for (File file : files) {
            Path filePath = file.toPath();
            if (isDirectory(filePath)) {
                deleteAllInPath(filePath);
            } else {
                delete(filePath);
            }
        }
    }

    /**
     * Retrieves all files inside a directory.
     * @param path The directory to get the contents of
     * @return null if something went wrong, or an array of File objects
     */
    public static File[] getAllFilesInDirectory(Path path) {
        if (!exists(path) || !isDirectory(path)) {
            return null;
        }

        return path.toFile().listFiles();
    }
}
