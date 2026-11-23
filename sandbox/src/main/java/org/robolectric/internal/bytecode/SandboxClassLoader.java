package org.robolectric.internal.bytecode;

import static com.google.common.base.StandardSystemProperty.JAVA_CLASS_PATH;
import static com.google.common.base.StandardSystemProperty.PATH_SEPARATOR;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.Cleaner;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nullable;
import javax.inject.Inject;
import org.robolectric.internal.ClassTracker;
import org.robolectric.util.Logger;
import org.robolectric.util.PerfStatsCollector;

/**
 * Class loader that modifies the bytecode of Android classes to insert calls to Robolectric's
 * shadow classes.
 */
public class SandboxClassLoader extends URLClassLoader implements ClassTracker {
  // The directory where instrumented class files will be dumped
  private static final String DUMP_CLASSES_PROPERTY = "robolectric.dumpClassesDirectory";

  // Whether to log resources abd classes loaded from android-all jars.
  private static final boolean LOG_RESOURCE_USAGE = false;

  private static final AtomicInteger DUMP_CLASSES_COUNTER = new AtomicInteger();

  private final InstrumentationConfiguration config;
  private final ResourceProvider resourceProvider;
  private final ClassInstrumentor classInstrumentor;
  private final ClassNodeProvider classNodeProvider;
  private final String dumpClassesDirectory;
  // Set robolectric.cacheServiceLoaderResources=false if descriptor locations in the sandbox or its
  // parent class loader change. The cache stores URLs, so changes to the contents of an existing
  // descriptor remain visible.
  private final boolean cacheServiceLoaderResources =
      Boolean.parseBoolean(System.getProperty("robolectric.cacheServiceLoaderResources", "true"));
  private final Map<String, List<URL>> serviceLoaderResources = new HashMap<>();
  // Names that isClassLoaded found loaded. A class stays loaded for the life of its loader, and
  // Shadows.reset asks about the same classes after every test.
  private final Set<String> loadedClassNames = ConcurrentHashMap.newKeySet();
  private boolean isClosed;

  /**
   * Answers lookups against this loader's own URLs. Without it, every lookup the parent misses goes
   * on to probe each of those URLs, which name the same jars the parent has just probed, and a new
   * sandbox opens every one of them again the first time that happens.
   */
  @Nullable private volatile ClasspathIndex classpathIndex;

  /**
   * Answers the parent's share of a lookup from an index, when the parent is a loader whose search
   * is known exactly; null to ask the parent.
   */
  @Nullable private volatile ParentLookup parentLookup;

  /** Constructor for use by tests. */
  SandboxClassLoader(InstrumentationConfiguration config) {
    this(config, new UrlResourceProvider(), new ClassInstrumentor(new ShadowDecorator()));
  }

  @Inject
  public SandboxClassLoader(
      InstrumentationConfiguration config,
      ResourceProvider resourceProvider,
      ClassInstrumentor classInstrumentor) {
    this(
        Thread.currentThread().getContextClassLoader(),
        config,
        resourceProvider,
        classInstrumentor);
  }

  public SandboxClassLoader(
      ClassLoader erstwhileClassLoader,
      InstrumentationConfiguration config,
      ResourceProvider resourceProvider,
      ClassInstrumentor classInstrumentor) {
    super(getClassPathUrls(erstwhileClassLoader), erstwhileClassLoader);

    this.config = config;
    this.resourceProvider = resourceProvider;

    this.classInstrumentor = classInstrumentor;

    classNodeProvider =
        new ClassNodeProvider() {
          @Override
          protected byte[] getClassBytes(String internalClassName) throws ClassNotFoundException {
            return getByteCode(internalClassName);
          }
        };
    this.dumpClassesDirectory = System.getProperty(DUMP_CLASSES_PROPERTY, "");
    ClasspathIndex index = ClasspathIndex.acquire(getURLs());
    ParentLookup lookup = index == null ? null : ParentLookup.create(erstwhileClassLoader);
    this.classpathIndex = index;
    this.parentLookup = lookup;
    // Released by close(), or, for a sandbox nobody closes, once it is unreachable: as an unclosed
    // URLClassLoader's jars are closed once it is collected.
    this.indexReleaser =
        index == null
            ? null
            : CLEANER.register(
                this, new IndexReleaser(index, lookup == null ? null : lookup.index));
  }

  private static final Cleaner CLEANER = Cleaner.create();

  @Nullable private final Cleaner.Cleanable indexReleaser;

  /**
   * Releases a sandbox's indexes. Holds no reference to the sandbox or to any class loader, so that
   * the sandbox can be collected and cleaned.
   */
  private static final class IndexReleaser implements Runnable {
    private final ClasspathIndex index;
    @Nullable private final ClasspathIndex parentIndex;

    IndexReleaser(ClasspathIndex index, @Nullable ClasspathIndex parentIndex) {
      this.index = index;
      this.parentIndex = parentIndex;
    }

    @Override
    public void run() {
      index.release();
      if (parentIndex != null) {
        parentIndex.release();
      }
    }
  }

  /**
   * Stands in for {@code parent.getResource(name)}, for a parent whose search is known: its own
   * parent first, then its class path in order, answered from a {@link ClasspathIndex}.
   *
   * <p>A parent that is exactly a {@link URLClassLoader} searches its {@link
   * URLClassLoader#getURLs() URLs} and nothing more, so the index answers for it outright. The
   * application class loader searches {@code java.class.path}, but agents can append to its search
   * after startup, so for it the index can only answer when it finds the resource; a miss still
   * asks the parent, which costs the scan that this sandbox's own URLs no longer add a second one
   * to. The first answer the index gives from each jar is also checked against the parent's own,
   * and any disagreement turns the index off for that parent.
   */
  static final class ParentLookup {
    private final ClassLoader parent;
    private final ClassLoader grandparent;
    private final ClasspathIndex index;
    private final boolean missesAreFinal;

    /**
     * Packages of the named modules the parent defines, which it searches before its class path.
     * The application class loader defines some of the JDK's own modules (jdk.compiler, for one).
     */
    private final Set<String> modulePackages;

    private ParentLookup(
        ClassLoader parent,
        ClassLoader grandparent,
        ClasspathIndex index,
        boolean missesAreFinal,
        Set<String> modulePackages) {
      this.parent = parent;
      this.grandparent = grandparent;
      this.index = index;
      this.missesAreFinal = missesAreFinal;
      this.modulePackages = modulePackages;
    }

    @Nullable
    @SuppressWarnings("ReferenceEquality") // Identity is the question: exactly these loaders.
    static ParentLookup create(ClassLoader parent) {
      ClassLoader grandparent = parent.getParent();
      if (grandparent == null) {
        return null;
      }
      if (parent.getClass() == URLClassLoader.class) {
        ClasspathIndex index = ClasspathIndex.acquire(((URLClassLoader) parent).getURLs());
        return index == null
            ? null
            : new ParentLookup(parent, grandparent, index, true, Collections.emptySet());
      }
      if (parent == ClassLoader.getSystemClassLoader()
          && parent
              .getClass()
              .getName()
              .equals("jdk.internal.loader.ClassLoaders$AppClassLoader")) {
        URL[] urls = cachedApplicationClassPathUrls(JAVA_CLASS_PATH.value());
        ClasspathIndex index = urls == null ? null : ClasspathIndex.acquire(urls);
        return index == null
            ? null
            : new ParentLookup(parent, grandparent, index, false, modulePackagesOf(parent));
      }
      return null;
    }

    @Nullable
    URL getResource(String name) {
      if (index.isMismatched() || mightBeInModule(name)) {
        return parent.getResource(name);
      }
      URL url = grandparent.getResource(name);
      if (url != null) {
        return url;
      }
      url = index.findResource(name, missesAreFinal ? null : parent);
      if (url != null || missesAreFinal) {
        return url;
      }
      return parent.getResource(name);
    }

    /**
     * Whether the parent might answer from one of its named modules rather than its class path.
     * Mirrors {@code BuiltinClassLoader.findResource}: a resource in one of their packages comes
     * from the module, and one in no package (a directory, a top-level file, or anything under a
     * name that isn't a package) is looked for in every module first. The JDK's modules have no
     * resources outside their packages but {@code module-info.class} and their directories.
     */
    private boolean mightBeInModule(String name) {
      if (modulePackages.isEmpty()) {
        return false;
      }
      int slash = name.lastIndexOf('/');
      if (slash == name.length() - 1 || name.endsWith("module-info.class")) {
        return true;
      }
      return slash != -1 && modulePackages.contains(name.substring(0, slash).replace('/', '.'));
    }

    @SuppressWarnings("ReferenceEquality")
    private static Set<String> modulePackagesOf(ClassLoader loader) {
      Set<String> packages = new HashSet<>();
      for (Module module : ModuleLayer.boot().modules()) {
        if (module.getClassLoader() == loader) {
          packages.addAll(module.getPackages());
        }
      }
      return packages;
    }

    private static final class ParsedClassPath {
      final String classPath;
      @Nullable final URL[] urls;

      ParsedClassPath(String classPath, @Nullable URL[] urls) {
        this.classPath = classPath;
        this.urls = urls;
      }
    }

    /** The class path {@link #applicationClassPathUrls} last ran on, and its result. */
    private static final AtomicReference<ParsedClassPath> lastApplicationClassPath =
        new AtomicReference<>();

    /** Making thousands of paths canonical takes tens of milliseconds; do it once per value. */
    @Nullable
    private static URL[] cachedApplicationClassPathUrls(@Nullable String classPath) {
      if (classPath == null) {
        return null;
      }
      ParsedClassPath last = lastApplicationClassPath.get();
      if (last == null || !last.classPath.equals(classPath)) {
        last = new ParsedClassPath(classPath, applicationClassPathUrls(classPath));
        lastApplicationClassPath.set(last);
      }
      return last.urls;
    }

    /**
     * The URLs the application class loader builds from a class path, or null if any would need
     * percent-encoding that this doesn't reproduce. Mirrors {@code URLClassPath(String, false)}:
     * every element, empty ones included, is made canonical and turned into a file URL, with a
     * trailing slash for a directory.
     */
    @Nullable
    @VisibleForTesting
    static URL[] applicationClassPathUrls(@Nullable String classPath) {
      if (classPath == null || File.separatorChar != '/') {
        return null;
      }
      List<URL> urls = new ArrayList<>();
      try {
        for (String element : Splitter.on(File.pathSeparator).split(classPath)) {
          File file = new File(element).getCanonicalFile();
          String path = file.getAbsolutePath();
          for (int i = 0; i < path.length(); i++) {
            char c = path.charAt(i);
            boolean plain =
                (c >= 'a' && c <= 'z')
                    || (c >= 'A' && c <= 'Z')
                    || (c >= '0' && c <= '9')
                    || "/-_.+~".indexOf(c) >= 0;
            if (!plain) {
              return null;
            }
          }
          if (!path.endsWith("/") && file.isDirectory()) {
            path = path + "/";
          }
          @SuppressWarnings("deprecation") // Matches URLClassPath.
          URL url = new URL("file", "", path);
          urls.add(url);
        }
      } catch (IOException e) {
        return null;
      }
      return urls.toArray(new URL[0]);
    }
  }

  /** Equivalent to {@code super.getResource(name)}: the parent, then this loader's own URLs. */
  @Nullable
  private URL getResourceFromParentOrSelf(String name) {
    ParentLookup lookup = parentLookup;
    URL url = lookup != null ? lookup.getResource(name) : getParent().getResource(name);
    return url != null ? url : findResource(name);
  }

  @Override
  public URL findResource(String name) {
    ClasspathIndex index = classpathIndex;
    if (index != null) {
      return index.findResource(name);
    }
    return super.findResource(name);
  }

  @Override
  public Enumeration<URL> findResources(String name) throws IOException {
    ClasspathIndex index = classpathIndex;
    if (index != null) {
      return index.findResources(name);
    }
    return super.findResources(name);
  }

  @Override
  protected void addURL(URL url) {
    synchronized (serviceLoaderResources) {
      super.addURL(url);
      serviceLoaderResources.clear();
    }
    // The index no longer covers every URL; go back to searching them directly.
    releaseClasspathIndex();
  }

  private void releaseClasspathIndex() {
    classpathIndex = null;
    parentLookup = null;
    if (indexReleaser != null) {
      // Runs the release at most once, however often this is called.
      indexReleaser.clean();
    }
  }

  private static URL[] getClassPathUrls(ClassLoader classloader) {
    URL[] urls = null;
    if (classloader instanceof URLClassLoader) {
      urls = ((URLClassLoader) classloader).getURLs();
    } else {
      urls = parseJavaClassPath();
    }
    PerfStatsCollector.getInstance()
        .recordCount("SandboxClassLoader.getClassPathUrls", urls.length);
    return urls;
  }

  // TODO(https://github.com/google/guava/issues/2956): Use a public API once one is available.
  private static URL[] parseJavaClassPath() {
    ImmutableList.Builder<URL> urls = ImmutableList.builder();
    for (String entry : Splitter.on(PATH_SEPARATOR.value()).split(JAVA_CLASS_PATH.value())) {
      try {
        try {
          urls.add(new File(entry).toURI().toURL());
        } catch (SecurityException e) { // File.toURI checks to see if the file is a directory
          urls.add(new URI("file", null, new File(entry).getAbsolutePath()).toURL());
        }
      } catch (MalformedURLException | URISyntaxException e) {
        Logger.strict("malformed classpath entry: " + entry, e);
      }
    }
    return urls.build().toArray(new URL[0]);
  }

  @Override
  public Enumeration<URL> getResources(String name) throws IOException {
    if (!cacheServiceLoaderResources || !name.startsWith("META-INF/services/")) {
      return super.getResources(name);
    }
    synchronized (serviceLoaderResources) {
      if (isClosed) {
        return super.getResources(name);
      }
      List<URL> resources = serviceLoaderResources.get(name);
      if (resources == null) {
        resources = Collections.list(super.getResources(name));
        serviceLoaderResources.put(name, resources);
      }
      // Only the URLs are cached. Each ServiceLoader reads the descriptors through its own cursor.
      return Collections.enumeration(resources);
    }
  }

  @Override
  public URL getResource(String name) {
    if (config.shouldAcquireResource(name)) {
      return getResourceUrl(name);
    }

    // Some tools (e.g. Android Studio Coverage) load Android class files as resources to perform
    // instrumentation on them. If this occurs, we want to ensure to use the classes in android-all.
    // See https://github.com/robolectric/robolectric/issues/3023 for more details.
    URL fromLocal = getResourceUrl(name);
    if (fromLocal != null && name.endsWith(".class")) {
      // Remove the ".class" suffix and convert to fully qualified name.
      String className = name.substring(0, name.length() - 6).replace('/', '.');
      if (config.shouldAcquire(className)) {
        return fromLocal;
      }
    }

    URL fromParent = getResourceFromParentOrSelf(name);
    if (fromParent != null) {
      return fromParent;
    }
    return fromLocal;
  }

  protected URL getResourceUrl(String name) {
    URL result = resourceProvider.getResource(name);
    if (LOG_RESOURCE_USAGE && result != null) {
      PerfStatsCollector.getInstance().incrementCount("SandboxClassLoader.getResource " + name);
    }
    return result;
  }

  private InputStream getClassBytesAsStreamPreferringLocalUrls(String className) {
    String resName = className.replace('.', '/') + ".class";
    InputStream fromUrlsClassLoader = resourceProvider.getResourceAsStream(resName);
    if (fromUrlsClassLoader != null) {
      if (LOG_RESOURCE_USAGE) {
        String packageName = className.replace('/', '.');
        int lastDotIndex = packageName.lastIndexOf('.');
        if (lastDotIndex != -1) {
          packageName = packageName.substring(0, lastDotIndex);
        }
        PerfStatsCollector.getInstance()
            .incrementCount("SandboxClassLoader.packageLoaded " + packageName);
      }
      return fromUrlsClassLoader;
    }
    InputStream fromAlternateClassLoader = getClassBytesFromAlternateClassLoader(resName);
    if (fromAlternateClassLoader != null) {
      return fromAlternateClassLoader;
    }
    return super.getResourceAsStream(resName);
  }

  /**
   * This exists to be overridden by the simulator class loader. It allows the simulator class
   * loader to prefer classes from the deploy jar (e.g. compiled resource jars) over classes from
   * the runtime classpath.
   */
  protected InputStream getClassBytesFromAlternateClassLoader(String classResName) {
    return null;
  }

  @Override
  public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
    synchronized (getClassLoadingLock(name)) {
      Class<?> loadedClass = findLoadedClass(name);
      if (loadedClass != null) {
        return loadedClass;
      }
      if (isClosed) {
        throw new ClassNotFoundException("This ClassLoader is closed");
      }
      if (config.shouldAcquire(name)) {
        loadedClass =
            PerfStatsCollector.getInstance()
                .measure("load sandboxed class", () -> maybeInstrumentClass(name));
      } else {
        loadedClass = getParent().loadClass(name);
      }

      if (resolve) {
        resolveClass(loadedClass);
      }

      return loadedClass;
    }
  }

  @Override
  public boolean isClassLoaded(String className) {
    if (className == null) {
      return false;
    }
    if (loadedClassNames.contains(className)) {
      return true;
    }
    if (findLoadedClass(className) == null) {
      return false;
    }
    loadedClassNames.add(className);
    return true;
  }

  private Class<?> maybeInstrumentClass(String className) throws ClassNotFoundException {
    byte[] classBytes = getByteCode(className);
    ClassDetails classDetails = new ClassDetails(classBytes);
    if (config.shouldInstrument(classDetails)) {
      classBytes = classInstrumentor.instrument(classDetails, config, classNodeProvider);
      maybeDumpClassBytes(classDetails, classBytes);
    }
    ensurePackage(className);
    return defineClass(className, classBytes, 0, classBytes.length);
  }

  private void maybeDumpClassBytes(ClassDetails classDetails, byte[] classBytes) {
    if (!Strings.isNullOrEmpty(dumpClassesDirectory)) {
      String outputClassName =
          classDetails.getName() + "-robo-instrumented-" + DUMP_CLASSES_COUNTER.getAndIncrement();
      Path path = Paths.get(dumpClassesDirectory, outputClassName + ".class");
      try {
        Files.write(path, classBytes);
      } catch (IOException e) {
        throw new AssertionError(e);
      }
    }
  }

  private byte[] getByteCode(String className) throws ClassNotFoundException {
    try (InputStream classBytesStream = getClassBytesAsStreamPreferringLocalUrls(className)) {
      if (classBytesStream == null) {
        throw new ClassNotFoundException(className);
      }

      return classBytesStream.readAllBytes();
    } catch (IOException e) {
      throw new ClassNotFoundException("couldn't load " + className, e);
    }
  }

  private void ensurePackage(final String className) {
    int lastDotIndex = className.lastIndexOf('.');
    if (lastDotIndex != -1) {
      String pckgName = className.substring(0, lastDotIndex);
      Package pckg = getPackage(pckgName);
      if (pckg == null) {
        definePackage(pckgName, null, null, null, null, null, null, null);
      }
    }
  }

  @Override
  public void close() throws IOException {
    synchronized (serviceLoaderResources) {
      try {
        super.close();
        releaseClasspathIndex();
        resourceProvider.close();
      } finally {
        // A resource-provider failure can follow a successful URLClassLoader close.
        isClosed = true;
        serviceLoaderResources.clear();
      }
    }
  }
}
