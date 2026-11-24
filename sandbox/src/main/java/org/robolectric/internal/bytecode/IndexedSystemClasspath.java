package org.robolectric.internal.bytecode;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;
import javax.annotation.Nullable;

/**
 * Finds files on the class path of this JVM as its system class loader does, through a {@link
 * ClasspathIndex} that is created in the background if the class path is long enough to be worth
 * indexing.
 */
final class IndexedSystemClasspath {
  private final List<String> entries;
  private final int minimumEntries;
  private final Function<URL[], ClasspathIndex> indexer;
  private final Executor executor;
  private final AtomicBoolean started = new AtomicBoolean();
  // Null until the index is ready, and for good if the class path is not indexed.
  @Nullable private volatile ClasspathIndex index;

  /**
   * @param classPath the value of the java.class.path system property, or null if it is unset
   * @param minimumEntries the number of class path entries from which the class path is indexed
   * @param indexer creates the index of a class path, or returns null if the class path cannot be
   *     indexed
   * @param executor runs the indexing
   */
  IndexedSystemClasspath(
      @Nullable String classPath,
      int minimumEntries,
      Function<URL[], ClasspathIndex> indexer,
      Executor executor) {
    this.entries = Splitter.on(File.pathSeparatorChar).splitToList(Strings.nullToEmpty(classPath));
    this.minimumEntries = minimumEntries;
    this.indexer = indexer;
    this.executor = executor;
  }

  /** Runs indexing on a daemon thread, so that it neither delays a lookup nor keeps the JVM up. */
  static Executor daemonThread() {
    return command -> {
      Thread thread = new Thread(command, "robolectric-classpath-index");
      thread.setDaemon(true);
      thread.start();
    };
  }

  /** Starts to index the class path, unless it is short or this was called before. */
  void startIndexing() {
    if (entries.size() < minimumEntries || !started.compareAndSet(false, true)) {
      return;
    }
    executor.execute(() -> {
      try {
        index = indexer.apply(toUrls(entries));
      } catch (MalformedURLException | RuntimeException e) {
        // Without an index every lookup is left to the caller.
      }
    });
  }

  /**
   * Opens the file that the system class loader finds, without asking every class path entry for
   * it, or returns null if this cannot tell: there is no index, or it is not ready yet. The caller
   * then asks the system class loader, which also knows about anything that was added to the class
   * path after the JVM started.
   */
  @Nullable
  InputStream getResourceAsStream(String name) {
    ClasspathIndex index = this.index;
    // The system class loader asks the platform class loader before its class path.
    if (index == null || ClassLoader.getPlatformClassLoader().getResource(name) != null) {
      return null;
    }
    try {
      return index.getResourceAsStream(name);
    } catch (IOException e) {
      return null;
    }
  }

  private static URL[] toUrls(List<String> entries) throws MalformedURLException {
    URL[] urls = new URL[entries.size()];
    for (int i = 0; i < urls.length; i++) {
      urls[i] = new File(entries.get(i)).toURI().toURL();
    }
    return urls;
  }
}
