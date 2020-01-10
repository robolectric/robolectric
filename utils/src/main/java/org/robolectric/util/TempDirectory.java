package org.robolectric.util;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@SuppressWarnings({"NewApi", "AndroidJdkLibsChecker"})
public class TempDirectory {
  private static final Set<Path> allPaths = new HashSet<>();

  private static Collection<Path> pathsToDelete() {
    synchronized (allPaths) {
      return new ArrayList<>(allPaths);
    }
  }

  public static void destroy() {
    for (Path path : pathsToDelete()) {
      try {
        clearDirectory(path);
        Files.delete(path);
      } catch (IOException ignored) {
      }
    }
  }

  private static void clearDirectory(final Path directory) throws IOException {
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
  
  private final Path basePath;

  public TempDirectory() {
    this("test-dir");
  }

  public TempDirectory(String name) {
    try {
      basePath = Files.createTempDirectory("robolectric-" + name);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    synchronized (allPaths) {
      if (allPaths.isEmpty()) {
        // Use a manual hook that actually clears the directory
        // This is necessary because File.deleteOnExit won't delete non empty directories
        Runtime.getRuntime().addShutdownHook(new Thread(TempDirectory::destroy));
      }

      allPaths.add(basePath);
    }
  }

  public Path createFile(String name, String contents) {
    Path path = basePath.resolve(name);
    try (Writer out = Files.newBufferedWriter(path)) {
      out.write(contents);
    } catch (IOException e) {
      throw new RuntimeException("failed writing to " + name, e);
    }
    return path;
  }

  public Path create(String name) {
    Path path = basePath.resolve(name);
    try {
      Files.createDirectory(path);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return path;
  }

  public Path createIfNotExists(String name) {
    Path path = basePath.resolve(name);
    try {
      Files.createDirectory(path);
    } catch (FileAlreadyExistsException e) {
      // that's ok
      return path;
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    return path;
  }
}
