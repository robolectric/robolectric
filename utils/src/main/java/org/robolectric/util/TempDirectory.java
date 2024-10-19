package org.robolectric.util;

import static java.util.concurrent.TimeUnit.SECONDS;

import com.google.common.annotations.VisibleForTesting;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.DirectoryStream;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * A helper class for working with temporary directories. All temporary directories created by this
 * class are automatically removed in a JVM shutdown hook.
 */
@SuppressWarnings({"NewApi", "AndroidJdkLibsChecker"})
public class TempDirectory {
  /**
   * The number of concurrent deletions which should take place, too high and it'll become I/O
   * bound, to low and it'll take a long time to complete. 5 is an estimate of a decent balance,
   * feel free to experiment.
   */
  private static final int DELETE_THREAD_POOL_SIZE = 5;

  private static final String TEMP_DIR_PREFIX = "robolectric-";

  static final String OBSOLETE_MARKER_FILE_NAME = ".obsolete";

  /* Set to track the undeleted TempDirectory instances which we need to erase. */
  private static final Set<TempDirectory> tempDirectoriesToDelete = new HashSet<>();

  /* Prior temp directories in Windows that were unable to be deleted */
  private static final Set<Path> obsoleteTempDirectoriesToDelete =
      Collections.synchronizedSet(new HashSet<>());

  static {
    if (OsUtil.isWindows()) {
      TempDirectory.findObsoleteWindowsTempDirectoriesInBackground();
    }
  }

  private final Path basePath;

  public TempDirectory() {
    this("test-dir");
  }

  public TempDirectory(String name) {
    try {
      basePath = Files.createTempDirectory(TEMP_DIR_PREFIX + name);
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

  public Path getBasePath() {
    return basePath;
  }

  static void clearAllDirectories() {
    ExecutorService deletionExecutorService = Executors.newFixedThreadPool(DELETE_THREAD_POOL_SIZE);
    synchronized (tempDirectoriesToDelete) {
      for (TempDirectory undeletedDirectory : tempDirectoriesToDelete) {
        deletionExecutorService.execute(undeletedDirectory::destroy);
      }
    }
    if (OsUtil.isWindows()) {
      synchronized (obsoleteTempDirectoriesToDelete) {
        for (Path obsoletePath : obsoleteTempDirectoriesToDelete) {
          deletionExecutorService.execute(() -> destroyObsoleteTempDirectory(obsoletePath));
        }
      }
    }
    deletionExecutorService.shutdown();
    try {
      deletionExecutorService.awaitTermination(10, SECONDS);
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
    } catch (IOException e) {
      if (OsUtil.isWindows()) {
        // Windows is much more protective of files that have been opened in native code. For
        // instance, unlike in Mac and Linux, it's not possible to delete nativeruntime files
        // (dlls, fonts, icu data) in the same process where they were opened. Because of
        // this, we need extra cleanup logic for Windows, and we avoid logging to prevent noise
        // and confusion.
        createFile(OBSOLETE_MARKER_FILE_NAME, "");
      } else {
        Logger.error("Failed to destroy temp directory", e);
      }
    }
  }

  private static void clearDirectory(final Path directory) throws IOException {
    Files.walkFileTree(
        directory,
        new SimpleFileVisitor<Path>() {
          @Override
          public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
              throws IOException {
            // Avoid deleting the obsolete temp directory marker
            if (!(OsUtil.isWindows()
                && file.getFileName().toString().equals(OBSOLETE_MARKER_FILE_NAME))) {
              Files.delete(file);
            }
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

  private static void findObsoleteWindowsTempDirectoriesInBackground() {
    Thread thread = new Thread(TempDirectory::collectObsoleteWindowsTempDirectories);
    thread.setDaemon(true);
    thread.start();
  }

  @VisibleForTesting
  static void collectObsoleteWindowsTempDirectories() {
    Path tmpDir = Paths.get(System.getProperty("java.io.tmpdir"));
    try (DirectoryStream<Path> stream = Files.newDirectoryStream(tmpDir, TEMP_DIR_PREFIX + "*")) {
      for (Path entry : stream) {
        if (Files.isDirectory(entry) && Files.exists(entry.resolve(OBSOLETE_MARKER_FILE_NAME))) {
          obsoleteTempDirectoriesToDelete.add(entry);
        }
      }
    } catch (IOException ignored) {
      // Ignore
    }
  }

  private static void destroyObsoleteTempDirectory(Path basePath) {
    try {
      clearDirectory(basePath);
      Files.deleteIfExists(
          basePath.resolve(OBSOLETE_MARKER_FILE_NAME)); // Delete the obsolete marker
      Files.delete(basePath); // Delete the directory
    } catch (IOException e) {
      // Ignored
    }
  }
}
