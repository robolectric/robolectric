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

  private final Queue<Path> paths;
  private final Set<String> deletePaths;

  TempDirectory() {
    paths = new ArrayDeque<>();
    deletePaths = new LinkedHashSet<>();

    // Use a manual hook that actually clears the directory
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
    return instance.createImpl(false);
  }

  public static Path createDeleteOnExit() {
    return instance.createImpl(true);
  }

  public static void destroy(Path path) {
    if (path != null) {
      instance.destroyImpl(path);
    }
  }

  Path createImpl(boolean deleteOnExit) {
    Path empty = paths.poll();
    if (empty != null && Files.exists(empty)) return empty;

    try {
      Path directory = createTempDir("android-tmp");
      if (deleteOnExit) deleteOnExit(directory);
      return directory;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  void destroyImpl(Path path) {
    if (!Files.exists(path)) return;

    try {
      clearDirectory(path);
      paths.add(path);
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

  private Path createTempDir(String name) throws IOException {
    return Files.createTempDirectory(name + "-robolectric");
  }
}
