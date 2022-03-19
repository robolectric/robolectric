package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;

import android.os.FileObserver;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.concurrent.GuardedBy;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

/**
 * A shadow implementation of FileObserver that uses java.nio.file.WatchService.
 *
 * <p>Currently only supports MODIFY, DELETE and CREATE (CREATE will encompass also events that
 * would normally register as MOVED_FROM, and DELETE will encompass also events that would normally
 * register as MOVED_TO). Other event types will be silently ignored.
 */
@Implements(FileObserver.class)
public class ShadowFileObserver {
  @RealObject private FileObserver realFileObserver;

  private final WatchService watchService;
  private final Map<String, WatchedDirectory> watchedDirectories = new HashMap<>();
  private final Map<WatchKey, Path> watchedKeys = new HashMap<>();

  private WatchEvent.Kind<?>[] watchEvents = new WatchEvent.Kind<?>[0];

  @GuardedBy("this")
  private WatcherRunnable watcherRunnable = null;

  public ShadowFileObserver() {
    try {
      this.watchService = FileSystems.getDefault().newWatchService();
    } catch (IOException ioException) {
      throw new RuntimeException(ioException);
    }
  }

  @Override
  @Implementation
  protected void finalize() throws Throwable {
    stopWatching();
  }

  private void setMask(int mask) {
    Set<WatchEvent.Kind<Path>> watchEventsSet = new HashSet<>();
    if ((mask & FileObserver.MODIFY) != 0) {
      watchEventsSet.add(StandardWatchEventKinds.ENTRY_MODIFY);
    }
    if ((mask & FileObserver.DELETE) != 0) {
      watchEventsSet.add(StandardWatchEventKinds.ENTRY_DELETE);
    }
    if ((mask & FileObserver.CREATE) != 0) {
      watchEventsSet.add(StandardWatchEventKinds.ENTRY_CREATE);
    }
    watchEvents = watchEventsSet.toArray(new WatchEvent.Kind<?>[0]);
  }

  private void addFile(File file) {
    List<File> list = new ArrayList<>(1);
    list.add(file);
    addFiles(list);
  }

  private void addFiles(List<File> files) {
    // Break all watched files into their directories.
    for (File file : files) {
      Path path = file.toPath();
      if (Files.isDirectory(path)) {
        WatchedDirectory watchedDirectory = new WatchedDirectory(path);
        watchedDirectories.put(path.toString(), watchedDirectory);
      } else {
        Path directory = path.getParent();
        String filename = path.getFileName().toString();
        WatchedDirectory watchedDirectory = watchedDirectories.get(directory.toString());
        if (watchedDirectory == null) {
          watchedDirectory = new WatchedDirectory(directory);
        }
        watchedDirectory.addFile(filename);
        watchedDirectories.put(directory.toString(), watchedDirectory);
      }
    }
  }

  @Implementation
  protected void __constructor__(String path, int mask) {
    setMask(mask);
    addFile(new File(path));
  }

  @Implementation(minSdk = Q)
  protected void __constructor__(List<File> files, int mask) {
    setMask(mask);
    addFiles(files);
  }

  /**
   * Represents a directory to watch, including specific files in that directory (or the entire
   * directory contents if no file is specified).
   */
  private class WatchedDirectory {
    @GuardedBy("this")
    private WatchKey watchKey = null;

    private final Path dirPath;
    private final Set<String> watchedFiles = new HashSet<>();

    WatchedDirectory(Path dirPath) {
      this.dirPath = dirPath;
    }

    void addFile(String filename) {
      watchedFiles.add(filename);
    }

    synchronized void register() throws IOException {
      unregister();
      this.watchKey = dirPath.register(watchService, watchEvents);
      watchedKeys.put(watchKey, dirPath);
    }

    synchronized void unregister() {
      if (this.watchKey != null) {
        watchedKeys.remove(watchKey);
        watchKey.cancel();
        this.watchKey = null;
      }
    }
  }

  @Implementation
  protected synchronized void startWatching() throws IOException {
    // If we're already watching, startWatching is a no-op.
    if (watcherRunnable != null) {
      return;
    }

    // If we don't have any supported events to watch for, don't do anything.
    if (watchEvents.length == 0) {
      return;
    }

    for (WatchedDirectory watchedDirectory : watchedDirectories.values()) {
      watchedDirectory.register();
    }

    watcherRunnable =
        new WatcherRunnable(realFileObserver, watchedDirectories, watchedKeys, watchService);
    Thread thread = new Thread(watcherRunnable, "ShadowFileObserver");
    thread.start();
  }

  @Implementation
  protected void stopWatching() {
    for (WatchedDirectory watchedDirectory : watchedDirectories.values()) {
      watchedDirectory.unregister();
    }

    synchronized (this) {
      if (watcherRunnable != null) {
        watcherRunnable.stop();
        watcherRunnable = null;
      }
    }
  }

  private static int fileObserverEventFromWatcherEvent(WatchEvent.Kind<?> kind) {
    if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
      return FileObserver.CREATE;
    } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
      return FileObserver.DELETE;
    } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
      return FileObserver.MODIFY;
    }
    return 0;
  }

  /** Runnable implementation that processes all events for keys queued to the watcher. */
  private static class WatcherRunnable implements Runnable {
    @GuardedBy("this")
    private boolean shouldStop = false;

    private final FileObserver realFileObserver;
    private final Map<String, WatchedDirectory> watchedDirectories;
    private final Map<WatchKey, Path> watchedKeys;
    private final WatchService watchService;

    public WatcherRunnable(
        FileObserver realFileObserver,
        Map<String, WatchedDirectory> watchedDirectories,
        Map<WatchKey, Path> watchedKeys,
        WatchService watchService) {
      this.realFileObserver = realFileObserver;
      this.watchedDirectories = watchedDirectories;
      this.watchedKeys = watchedKeys;
      this.watchService = watchService;
    }

    public synchronized void stop() {
      this.shouldStop = true;
    }

    public synchronized boolean shouldContinue() {
      return !shouldStop;
    }

    @SuppressWarnings("unchecked")
    private WatchEvent<Path> castToPathWatchEvent(WatchEvent<?> untypedWatchEvent) {
      return (WatchEvent<Path>) untypedWatchEvent;
    }

    @Override
    public void run() {
      while (shouldContinue()) {
        // wait for key to be signalled
        WatchKey key;
        try {
          key = watchService.take();
        } catch (InterruptedException x) {
          return;
        }

        Path dir = watchedKeys.get(key);
        if (dir != null) {
          WatchedDirectory watchedDirectory = watchedDirectories.get(dir.toString());
          List<WatchEvent<?>> events = key.pollEvents();

          for (WatchEvent<?> event : events) {
            WatchEvent.Kind<?> kind = event.kind();

            // Ignore OVERFLOW events
            if (kind == StandardWatchEventKinds.OVERFLOW) {
              continue;
            }

            WatchEvent<Path> ev = castToPathWatchEvent(event);
            Path fileName = ev.context().getFileName();

            if (watchedDirectory.watchedFiles.isEmpty()) {
              realFileObserver.onEvent(
                  fileObserverEventFromWatcherEvent(kind), fileName.toString());
            } else {
              for (String watchedFile : watchedDirectory.watchedFiles) {
                if (fileName.toString().equals(watchedFile)) {
                  realFileObserver.onEvent(
                      fileObserverEventFromWatcherEvent(kind), fileName.toString());
                }
              }
            }
          }
        }
        boolean valid = key.reset();
        if (!valid) {
          return;
        }
      }
    }
  }
}
