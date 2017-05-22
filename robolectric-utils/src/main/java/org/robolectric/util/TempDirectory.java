package org.robolectric.util;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayDeque;
import java.util.LinkedHashSet;
import java.util.Queue;
import java.util.Set;

public class TempDirectory {
  private static final TempDirectory instance = new TempDirectory();

  private final Set<String> deletePaths;

  TempDirectory() {
    deletePaths = new LinkedHashSet<>();

    // Use a manual hook that actually clears the directory
    // This is necessary because File.deleteOnExit won't delete non empty directories
    Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
      @Override public void run() {
        for (String file : deletePaths) {
          try {
            Path path = Paths.get(file);
            clearDirectory(path);
            Files.delete(path);
          } catch (IOException ignored) {
          }
        }
      }
    }));
  }

  public static Path create() {
    return instance.createImpl();
  }

  /**
   * @deprecated Use {@link #create()} instead.
   */
  @Deprecated
  public static Path createDeleteOnExit() {
    return create();
  }

  public static void destroy(Path path) {
    if (path != null) {
      instance.destroyImpl(path);
    }
  }

  private Path createImpl() {
    try {
      Path directory = Files.createTempDirectory("robolectric");
      deleteOnExit(directory);
      return directory;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  void destroyImpl(Path path) {
    if (!Files.exists(path)) return;

    try {
      clearDirectory(path);
    } catch (IOException ignored) {
      // We failed to clear the directory, just try again at exit
    }

    deleteOnExit(path);
  }

  private void clearDirectory(final Path directory) throws IOException {
    Files.walkFileTree(directory, new SimpleFileVisitor<Path>() {
      @Override
      public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        Files.delete(file);
        return FileVisitResult.CONTINUE;
      }

      @Override
      public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
        if (!dir.equals(directory)) {
          Files.delete(dir);
        }
        return FileVisitResult.CONTINUE;
      }
    });
  }

  private void deleteOnExit(Path path) {
    deletePaths.add(path.toString());
  }
}
