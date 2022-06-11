package org.robolectric.util;

import static java.util.concurrent.TimeUnit.SECONDS;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressWarnings({"NewApi", "AndroidJdkLibsChecker"})
public class TempDirectory {
  /**
   * The number of concurrent deletions which should take place, too high and it'll become I/O
   * bound, to low and it'll take a long time to complete. 5 is an estimate of a decent balance,
   * feel free to experiment.
   */
  private static final int DELETE_THREAD_POOL_SIZE = 5;

  /** Set to track the undeleted TempDirectory instances which we need to erase. */
  private static final Set<TempDirectory> tempDirectoriesToDelete = new HashSet<>();

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

    synchronized (tempDirectoriesToDelete) {
      // If we haven't initialised the shutdown hook we should set everything up.
      if (tempDirectoriesToDelete.size() == 0) {
        // Use a manual hook that actually clears the directory
        // This is necessary because File.deleteOnExit won't delete non empty directories
        Runtime.getRuntime().addShutdownHook(new Thread(TempDirectory::clearAllDirectories));
      }

      tempDirectoriesToDelete.add(this);
    }
  }

  static void clearAllDirectories() {
    ExecutorService deletionExecutorService = Executors.newFixedThreadPool(DELETE_THREAD_POOL_SIZE);
    synchronized (tempDirectoriesToDelete) {
      for (TempDirectory undeletedDirectory : tempDirectoriesToDelete) {
        deletionExecutorService.execute(undeletedDirectory::destroy);
      }
    }
    deletionExecutorService.shutdown();
    try {
      deletionExecutorService.awaitTermination(5, SECONDS);
    } catch (InterruptedException e) {
      deletionExecutorService.shutdownNow();
      // Preserve interrupt status
      Thread.currentThread().interrupt();
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

  public void destroy() {
    try {
      clearDirectory(basePath);
      Files.delete(basePath);
    } catch (IOException ignored) {
      Logger.error("Failed to destroy temp directory", ignored);
    }
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
}
