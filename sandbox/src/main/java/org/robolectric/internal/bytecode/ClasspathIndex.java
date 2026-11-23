package org.robolectric.internal.bytecode;

import com.google.common.annotations.VisibleForTesting;
import com.google.errorprone.annotations.concurrent.GuardedBy;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicIntegerArray;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;
import java.util.zip.ZipFile;
import javax.annotation.Nullable;
import org.robolectric.util.Logger;
import org.robolectric.util.PerfStatsCollector;

/**
 * An index of which elements of a class path can hold a resource, so that a lookup probes those
 * elements rather than every jar on the class path.
 *
 * <p>{@link URLClassLoader} looks a resource up by asking each jar on its class path in turn, so a
 * lookup costs time proportional to the position of the jar that has it, and a miss costs a probe
 * of every jar. With thousands of jars on the class path that adds up: every class a sandbox loads,
 * and every resource it fails to find, pays for it. Each new {@link URLClassLoader} also opens
 * every jar again the first time a lookup misses.
 *
 * <p>The index maps each directory inside the class path's jars (the part of an entry name before
 * its last {@code /}) to the jars that have entries in it; under {@code META-INF/}, where nearly
 * every jar has entries, it maps whole entry names instead. A lookup probes only those jars, plus
 * every directory on the class path, which can change at any time and so is never indexed. The
 * probes are made in class path order and the answer comes from the first that succeeds, so the
 * result is exactly what {@code new URLClassLoader(urls, null).findResource(name)} would return,
 * including the same {@link URL}: the jar probe itself is a {@link JarFile#getJarEntry} on a jar
 * opened the same way {@link URLClassLoader} opens it, and the URL of a hit is produced by a {@link
 * URLClassLoader} over just that element. Jars named by a {@code Class-Path} manifest attribute are
 * expanded where {@link URLClassLoader} would expand them, and repeated class path entries are
 * skipped as it skips them.
 *
 * <p>Indexes are shared between every sandbox on the same class path and reference counted. When
 * the last sandbox using one is closed, the index closes every jar it opened, so closing every
 * sandbox still releases every file handle, but keeps what it learned: a later sandbox on the same
 * class path checks that no jar has changed size or modification time since and reopens jars as it
 * probes them, instead of scanning them all again.
 *
 * <p>Building the index scans every jar once per JVM, which on a short class path costs more than
 * the lookups it saves. It is therefore off by default, and the {@value #PROPERTY} system property
 * turns it on:
 *
 * <ul>
 *   <li>{@code true} (or {@code on}): always index.
 *   <li>{@code false} (or {@code off}), or unset: never index; look resources up the way {@link
 *       URLClassLoader} does.
 *   <li>a number <i>N</i>: index a class path that names at least <i>N</i> jars, and leave shorter
 *       ones alone.
 * </ul>
 *
 * <p>From Gradle, set it on the test task, e.g. {@code systemProperty(
 * "robolectric.sandbox.classpathIndex", "300")}, or for an Android module in {@code
 * testOptions.unitTests.all { systemProperty(...) }}.
 */
final class ClasspathIndex {
  private static final String META_INF = "META-INF/";
  private static final String VERSIONS_PREFIX = "META-INF/versions/";

  /** How many indexes no sandbox is using to keep for reuse. */
  private static final int MAX_DORMANT = 4;

  @GuardedBy("ClasspathIndex.class")
  private static final Map<List<String>, ClasspathIndex> SHARED = new LinkedHashMap<>();

  private final Element[] elements;

  /** Elements that are directories, in class path order; every lookup has to probe them. */
  private final int[] directoryElements;

  /** For each key (see {@link #keyOf}), the jar elements with entries under it, in order. */
  private final Map<String, int[]> jarElementsByKey;

  /**
   * Jars the class path names that could not be opened when the index was built, and so are absent
   * from it; reusing the index requires that they are still in the same state.
   */
  private final List<FileStamp> skippedJars;

  @GuardedBy("ClasspathIndex.class")
  private int references;

  /** Elements whose answers have been checked against the loader they stand in for. */
  private final AtomicIntegerArray verified;

  private volatile boolean mismatched;

  private ClasspathIndex(
      Element[] elements,
      int[] directoryElements,
      Map<String, int[]> jarElementsByKey,
      List<FileStamp> skippedJars) {
    this.elements = elements;
    this.directoryElements = directoryElements;
    this.jarElementsByKey = jarElementsByKey;
    this.skippedJars = skippedJars;
    this.verified = new AtomicIntegerArray(elements.length);
  }

  static final String PROPERTY = "robolectric.sandbox.classpathIndex";

  /** Values of {@link #PROPERTY} that weren't understood, so that each is reported only once. */
  private static final Set<String> REPORTED_INVALID_VALUES = ConcurrentHashMap.newKeySet();

  /** Whether {@link #PROPERTY} asks for a class path with these URLs to be indexed. */
  @VisibleForTesting
  static boolean isEnabledFor(URL[] urls) {
    String value = System.getProperty(PROPERTY);
    if (value == null) {
      return false;
    }
    value = value.trim().toLowerCase(Locale.ROOT);
    switch (value) {
      case "":
      case "false":
      case "off":
        return false;
      case "true":
      case "on":
        return true;
      default:
        break;
    }
    int threshold;
    try {
      threshold = Integer.parseInt(value);
    } catch (NumberFormatException e) {
      if (REPORTED_INVALID_VALUES.add(value)) {
        Logger.warn(
            "Ignoring %s=%s: expected true, false or a number of jars; not indexing the class path",
            PROPERTY, value);
      }
      return false;
    }
    int jars = 0;
    for (URL url : urls) {
      // URLClassPath treats any URL whose path doesn't end in a slash as a jar.
      if (!url.getFile().endsWith("/") && ++jars >= threshold) {
        return true;
      }
    }
    return jars >= threshold;
  }

  /**
   * Returns the shared index for a class path, building it if there is none, or {@code null} if
   * {@link #PROPERTY} doesn't ask for this class path to be indexed or the class path has an
   * element the index cannot model faithfully (anything other than a local jar or directory). Every
   * index returned must be {@link #release() released}.
   */
  @Nullable
  static ClasspathIndex acquire(URL[] urls) {
    if (!isEnabledFor(urls)) {
      return null;
    }
    List<String> key = new ArrayList<>(urls.length);
    for (URL url : urls) {
      key.add(url.toExternalForm());
    }
    // Built under the lock: sandboxes are created concurrently, and every one of them would
    // otherwise open and scan every jar on the class path at the same time.
    synchronized (ClasspathIndex.class) {
      ClasspathIndex index = SHARED.get(key);
      if (index != null && index.references == 0) {
        if (index.isUnchanged()) {
          for (Element element : index.elements) {
            element.reopen();
          }
        } else {
          SHARED.remove(key);
          index = null;
        }
      }
      if (index == null) {
        index = PerfStatsCollector.getInstance().measure("ClasspathIndex.build", () -> build(urls));
        if (index == null) {
          return null;
        }
        SHARED.put(key, index);
      }
      index.references++;
      return index;
    }
  }

  /**
   * Releases a reference taken by {@link #acquire}. The last release closes every jar the index
   * holds open; the index itself is kept for reuse.
   */
  void release() {
    synchronized (ClasspathIndex.class) {
      if (--references > 0) {
        return;
      }
      for (Element element : elements) {
        element.close();
      }
      int dormant = 0;
      for (ClasspathIndex index : SHARED.values()) {
        if (index.references == 0) {
          dormant++;
        }
      }
      // Drop the least recently built indexes that nobody is using.
      Iterator<ClasspathIndex> it = SHARED.values().iterator();
      while (dormant > MAX_DORMANT && it.hasNext()) {
        if (it.next().references == 0) {
          it.remove();
          dormant--;
        }
      }
    }
  }

  @VisibleForTesting
  static int sharedIndexCount() {
    synchronized (ClasspathIndex.class) {
      return SHARED.size();
    }
  }

  @VisibleForTesting
  static void clearShared() {
    synchronized (ClasspathIndex.class) {
      Iterator<ClasspathIndex> it = SHARED.values().iterator();
      while (it.hasNext()) {
        if (it.next().references == 0) {
          it.remove();
        }
      }
    }
  }

  @VisibleForTesting
  int referenceCount() {
    synchronized (ClasspathIndex.class) {
      return references;
    }
  }

  @VisibleForTesting
  int openJarCount() {
    int count = 0;
    for (Element element : elements) {
      if (element.isJarOpen()) {
        count++;
      }
    }
    return count;
  }

  /** Equivalent to {@link URLClassLoader#findResource} over the indexed class path. */
  @Nullable
  URL findResource(String name) {
    return findResource(name, null);
  }

  /**
   * Like {@link #findResource(String)}, but for an index that stands in for the class path search
   * of {@code verifyAgainst}. The first answer each element gives is checked against {@code
   * verifyAgainst.getResource(name)}; if they ever disagree the index stops answering for it, and
   * this and every later call returns what {@code verifyAgainst} does.
   */
  @Nullable
  URL findResource(String name, @Nullable ClassLoader verifyAgainst) {
    int[] jars = jarElementsByKey.get(keyOf(name));
    if (jars == null) {
      jars = new int[0];
    }
    int j = 0;
    int d = 0;
    while (j < jars.length || d < directoryElements.length) {
      int next =
          d >= directoryElements.length || (j < jars.length && jars[j] < directoryElements[d])
              ? jars[j++]
              : directoryElements[d++];
      URL url = elements[next].findResource(name);
      if (url != null) {
        if (verifyAgainst != null && verified.get(next) == 0) {
          URL actual = verifyAgainst.getResource(name);
          if (actual == null || !actual.toString().equals(url.toString())) {
            mismatched = true;
            return actual;
          }
          verified.set(next, 1);
        }
        return url;
      }
    }
    return null;
  }

  /** Whether an answer checked by {@link #findResource(String, ClassLoader)} ever disagreed. */
  boolean isMismatched() {
    return mismatched;
  }

  /** Equivalent to {@link URLClassLoader#findResources} over the indexed class path. */
  Enumeration<URL> findResources(String name) {
    int[] jars = jarElementsByKey.get(keyOf(name));
    if (jars == null) {
      jars = new int[0];
    }
    List<URL> result = new ArrayList<>();
    int j = 0;
    int d = 0;
    while (j < jars.length || d < directoryElements.length) {
      int next =
          d >= directoryElements.length || (j < jars.length && jars[j] < directoryElements[d])
              ? jars[j++]
              : directoryElements[d++];
      URL url = elements[next].findResource(name);
      if (url != null) {
        result.add(url);
      }
    }
    return Collections.enumeration(result);
  }

  /**
   * The key a resource or entry name is indexed under: the name with any trailing {@code /}
   * dropped, since {@link JarFile#getEntry} answers a lookup of {@code a/b} with an entry {@code
   * a/b/} too; then, outside {@code META-INF/}, everything before its last {@code /}.
   */
  static String keyOf(String name) {
    int end = name.length();
    while (end > 0 && name.charAt(end - 1) == '/') {
      end--;
    }
    if (name.startsWith(META_INF) && !name.startsWith(VERSIONS_PREFIX)) {
      return name.substring(0, end);
    }
    int slash = name.lastIndexOf('/', end - 1);
    return slash <= 0 ? "" : name.substring(0, slash);
  }

  /** Whether every jar the index read, or failed to read, is as it was then. */
  private boolean isUnchanged() {
    for (Element element : elements) {
      if (element.stamp != null && !element.stamp.isCurrent()) {
        return false;
      }
    }
    for (FileStamp stamp : skippedJars) {
      if (!stamp.isCurrent()) {
        return false;
      }
    }
    return true;
  }

  @Nullable
  private static ClasspathIndex build(URL[] urls) {
    boolean disableClassPathUrlCheck = isTrueish("jdk.net.URLClassPath.disableClassPathURLCheck");
    List<Element> elements = new ArrayList<>();
    List<Integer> directories = new ArrayList<>();
    List<FileStamp> skipped = new ArrayList<>();
    Map<String, List<Integer>> jarsByKey = new HashMap<>();
    Set<String> seen = new HashSet<>();
    Deque<URL> unopened = new ArrayDeque<>(Arrays.asList(urls));
    // Opening and listing the jars is nearly all of the work, and each jar is independent of the
    // others, so do it for every jar the class path names up front, in parallel. Only the order
    // they are put together in, and any jars named by Class-Path attributes, are left for below.
    Map<File, JarScan> scans = new ConcurrentHashMap<>();
    Arrays.stream(urls)
        .parallel()
        .map(ClasspathIndex::jarFileOf)
        .filter(Objects::nonNull)
        .distinct()
        .forEach(path -> scans.put(path, JarScan.of(path)));
    try {
      URL url;
      while ((url = unopened.pollFirst()) != null) {
        if (!"file".equals(url.getProtocol())
            || (url.getHost() != null && !url.getHost().isEmpty())
            || url.getRef() != null) {
          closeAll(elements);
          closeAll(scans.values());
          return null;
        }
        // URLClassPath skips a URL it already has a loader for. For a local file URL with no
        // fragment, the string it compares is the file part.
        String file = url.getFile();
        if (seen.contains(file)) {
          continue;
        }
        if (file.endsWith("/")) {
          seen.add(file);
          directories.add(elements.size());
          elements.add(new Element(url, null, null, false));
          continue;
        }
        File path = jarFileOf(url);
        if (path == null) {
          closeAll(elements);
          closeAll(scans.values());
          return null;
        }
        JarScan scan = scans.remove(path);
        if (scan == null) {
          scan = JarScan.of(path);
        }
        // URLClassPath silently skips a jar it cannot open, without remembering it as seen.
        if (scan.jar == null) {
          skipped.add(scan.stamp);
          continue;
        }
        seen.add(file);
        int index = elements.size();
        URL[] classPath = classPathOf(scan, url, disableClassPathUrlCheck);
        elements.add(new Element(url, path, scan.stamp, classPath.length > 0, scan.jar));
        for (String jarKey : scan.keys) {
          jarsByKey.computeIfAbsent(jarKey, k -> new ArrayList<>()).add(index);
        }

        // The jars named by Class-Path come next, before the rest of the class path.
        for (int i = classPath.length - 1; i >= 0; i--) {
          unopened.addFirst(classPath[i]);
        }
      }
    } catch (IOException | RuntimeException e) {
      // URLClassPath would skip a jar with an unparseable Class-Path attribute; rather than model
      // that, don't index this class path at all.
      closeAll(elements);
      closeAll(scans.values());
      return null;
    }
    // Jars named more than once, past their first appearance.
    closeAll(scans.values());

    Map<String, int[]> frozen = new HashMap<>(jarsByKey.size() * 2);
    for (Map.Entry<String, List<Integer>> entry : jarsByKey.entrySet()) {
      frozen.put(entry.getKey(), toArray(entry.getValue()));
    }
    return new ClasspathIndex(
        elements.toArray(new Element[0]), toArray(directories), frozen, skipped);
  }

  /** The jar file a class path URL names, or null if it doesn't name a local file. */
  @Nullable
  private static File jarFileOf(URL url) {
    if (!"file".equals(url.getProtocol())
        || (url.getHost() != null && !url.getHost().isEmpty())
        || url.getRef() != null
        || url.getFile().endsWith("/")) {
      return null;
    }
    try {
      return new File(url.toURI());
    } catch (URISyntaxException | IllegalArgumentException e) {
      return null;
    }
  }

  /** Mirrors how URLClassPath resolves a jar's {@code Class-Path} attribute. */
  private static URL[] classPathOf(JarScan scan, URL base, boolean disableCheck)
      throws IOException {
    if (scan.manifestFailure != null) {
      throw scan.manifestFailure;
    }
    String value = scan.classPath;
    if (value == null) {
      return new URL[0];
    }
    List<URL> urls = new ArrayList<>();
    StringTokenizer tokens = new StringTokenizer(value);
    while (tokens.hasMoreTokens()) {
      String path = tokens.nextToken();
      @SuppressWarnings("deprecation") // Matches URLClassPath.
      URL url = new URL(base, path);
      if (!disableCheck && path.indexOf(':') >= 0 && !"file".equalsIgnoreCase(url.getProtocol())) {
        continue;
      }
      urls.add(url);
    }
    return urls.toArray(new URL[0]);
  }

  private static boolean isTrueish(String property) {
    String value = System.getProperty(property);
    return value != null && (value.equals("true") || value.isEmpty());
  }

  private static int[] toArray(List<Integer> list) {
    int[] array = new int[list.size()];
    for (int i = 0; i < array.length; i++) {
      array[i] = list.get(i);
    }
    return array;
  }

  private static void closeAll(List<Element> elements) {
    for (Element element : elements) {
      element.close();
    }
  }

  private static void closeAll(Collection<JarScan> scans) {
    for (JarScan scan : scans) {
      if (scan.jar != null) {
        try {
          scan.jar.close();
        } catch (IOException e) {
          // ignore
        }
      }
    }
  }

  /** A jar opened as URLClassPath opens it, and the keys of its entries. */
  private static final class JarScan {
    final FileStamp stamp;
    @Nullable final JarFile jar;
    final Set<String> keys;
    @Nullable final String classPath;
    @Nullable final IOException manifestFailure;

    private JarScan(
        FileStamp stamp,
        @Nullable JarFile jar,
        Set<String> keys,
        @Nullable String classPath,
        @Nullable IOException manifestFailure) {
      this.stamp = stamp;
      this.jar = jar;
      this.keys = keys;
      this.classPath = classPath;
      this.manifestFailure = manifestFailure;
    }

    static JarScan of(File path) {
      FileStamp stamp = FileStamp.of(path);
      JarFile jar;
      try {
        jar = Element.open(path);
      } catch (IOException | SecurityException e) {
        return new JarScan(stamp, null, Collections.emptySet(), null, null);
      }
      Set<String> keys = new HashSet<>();
      Enumeration<JarEntry> entries = jar.entries();
      while (entries.hasMoreElements()) {
        String name = entries.nextElement().getName();
        keys.add(keyOf(name));
        // A multi-release jar answers for a name with a versioned entry too.
        if (name.startsWith(VERSIONS_PREFIX)) {
          int slash = name.indexOf('/', VERSIONS_PREFIX.length());
          if (slash != -1) {
            keys.add(keyOf(name.substring(slash + 1)));
          }
        }
      }
      String classPath = null;
      IOException manifestFailure = null;
      try {
        Manifest manifest = jar.getManifest();
        if (manifest != null) {
          classPath = manifest.getMainAttributes().getValue(Attributes.Name.CLASS_PATH);
        }
      } catch (IOException e) {
        manifestFailure = e;
      }
      return new JarScan(stamp, jar, keys, classPath, manifestFailure);
    }
  }

  /** A file's size and modification time, or that it didn't exist. */
  private static final class FileStamp {
    private final File file;
    private final long size;
    private final long lastModified;

    private FileStamp(File file, long size, long lastModified) {
      this.file = file;
      this.size = size;
      this.lastModified = lastModified;
    }

    static FileStamp of(File file) {
      try {
        BasicFileAttributes attributes =
            Files.readAttributes(file.toPath(), BasicFileAttributes.class);
        return new FileStamp(file, attributes.size(), attributes.lastModifiedTime().toMillis());
      } catch (IOException | RuntimeException e) {
        return new FileStamp(file, -1, -1);
      }
    }

    boolean isCurrent() {
      FileStamp now = of(file);
      return now.size == size && now.lastModified == lastModified;
    }
  }

  /** One element of the expanded class path: a jar, or a directory. */
  private static final class Element {
    private final URL url;
    @Nullable private final File path;
    @Nullable private final FileStamp stamp;
    private final boolean hasClassPath;
    private final String urlPrefix;

    /** The jar, opened as URLClassPath opens it; null for a directory, or until reopened. */
    @Nullable private volatile JarFile jar;

    /** A loader over just this element, which builds the URLs exactly as URLClassLoader does. */
    @Nullable private volatile URLClassLoader loader;

    /** Set while no sandbox holds the index, so that a straggling lookup can't reopen the jar. */
    private boolean closed;

    Element(URL url, @Nullable File path, @Nullable FileStamp stamp, boolean hasClassPath) {
      this(url, path, stamp, hasClassPath, null);
    }

    Element(
        URL url,
        @Nullable File path,
        @Nullable FileStamp stamp,
        boolean hasClassPath,
        @Nullable JarFile jar) {
      this.url = url;
      this.path = path;
      this.stamp = stamp;
      this.hasClassPath = hasClassPath;
      this.urlPrefix = "jar:" + url + "!/";
      this.jar = jar;
    }

    static JarFile open(File path) throws IOException {
      return new JarFile(path, true, ZipFile.OPEN_READ, JarFile.runtimeVersion());
    }

    @Nullable
    URL findResource(String name) {
      try {
        if (path != null) {
          JarFile jar = jar();
          if (jar == null || jar.getJarEntry(name) == null) {
            return null;
          }
        }
        URL found = loader().findResource(name);
        // The element's own loader expands the jar's Class-Path too, so if the jar's entry turns
        // out to be one URLClassPath declines, it can answer from a jar named there. Only an
        // answer from the jar itself is this element's.
        if (found != null && hasClassPath && !found.toString().startsWith(urlPrefix)) {
          return null;
        }
        return found;
      } catch (IllegalStateException e) {
        // The jar was closed under us, by the last sandbox using the index being closed.
        return null;
      }
    }

    @Nullable
    private JarFile jar() {
      JarFile j = jar;
      if (j != null) {
        return j;
      }
      synchronized (this) {
        if (jar == null && !closed) {
          try {
            jar = open(path);
          } catch (IOException | SecurityException e) {
            return null;
          }
        }
        return jar;
      }
    }

    private URLClassLoader loader() {
      URLClassLoader l = loader;
      if (l != null) {
        return l;
      }
      synchronized (this) {
        if (loader == null) {
          // Once closed, answer from a throwaway loader rather than one that would outlive the
          // index's last user; only a lookup racing with that close can get here.
          if (closed) {
            return new URLClassLoader(new URL[0], null);
          }
          loader = new URLClassLoader(new URL[] {url}, null);
        }
        return loader;
      }
    }

    synchronized void reopen() {
      closed = false;
    }

    synchronized boolean isJarOpen() {
      return jar != null;
    }

    synchronized void close() {
      closed = true;
      if (jar != null) {
        try {
          jar.close();
        } catch (IOException e) {
          // ignore
        }
        jar = null;
      }
      if (loader != null) {
        try {
          loader.close();
        } catch (IOException e) {
          // ignore
        }
        loader = null;
      }
    }
  }
}
