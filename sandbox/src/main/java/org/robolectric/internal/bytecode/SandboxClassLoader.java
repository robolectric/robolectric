package org.robolectric.internal.bytecode;

import static com.google.common.base.StandardSystemProperty.JAVA_CLASS_PATH;
import static com.google.common.base.StandardSystemProperty.PATH_SEPARATOR;

import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicInteger;
import javax.inject.Inject;
import org.robolectric.util.Logger;
import org.robolectric.util.PerfStatsCollector;
import org.robolectric.util.Util;

/**
 * Class loader that modifies the bytecode of Android classes to insert calls to Robolectric's
 * shadow classes.
 */
public class SandboxClassLoader extends URLClassLoader {
  // The directory where instrumented class files will be dumped
  private static final String DUMP_CLASSES_PROPERTY = "robolectric.dumpClassesDirectory";
  private static final AtomicInteger DUMP_CLASSES_COUNTER = new AtomicInteger();

  private final InstrumentationConfiguration config;
  private final ResourceProvider resourceProvider;
  private final ClassInstrumentor classInstrumentor;
  private final ClassNodeProvider classNodeProvider;
  private final String dumpClassesDirectory;

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
  }

  private static URL[] getClassPathUrls(ClassLoader classloader) {
    if (classloader instanceof URLClassLoader) {
      return ((URLClassLoader) classloader).getURLs();
    }
    return parseJavaClassPath();
  }

  // TODO(https://github.com/google/guava/issues/2956): Use a public API once one is available.
  private static URL[] parseJavaClassPath() {
    ImmutableList.Builder<URL> urls = ImmutableList.builder();
    for (String entry : Splitter.on(PATH_SEPARATOR.value()).split(JAVA_CLASS_PATH.value())) {
      try {
        try {
          urls.add(new File(entry).toURI().toURL());
        } catch (SecurityException e) { // File.toURI checks to see if the file is a directory
          urls.add(new URL("file", null, new File(entry).getAbsolutePath()));
        }
      } catch (MalformedURLException e) {
        Logger.strict("malformed classpath entry: " + entry, e);
      }
    }
    return urls.build().toArray(new URL[0]);
  }

  @Override
  public URL getResource(String name) {
    if (config.shouldAcquireResource(name)) {
      return resourceProvider.getResource(name);
    }
    URL fromParent = super.getResource(name);
    if (fromParent != null) {
      return fromParent;
    }
    return resourceProvider.getResource(name);
  }

  private InputStream getClassBytesAsStreamPreferringLocalUrls(String resName) {
    InputStream fromUrlsClassLoader = resourceProvider.getResourceAsStream(resName);
    if (fromUrlsClassLoader != null) {
      return fromUrlsClassLoader;
    }
    return super.getResourceAsStream(resName);
  }

  @Override
  public Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
    synchronized (getClassLoadingLock(name)) {
      Class<?> loadedClass = findLoadedClass(name);
      if (loadedClass != null) {
        return loadedClass;
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

  protected Class<?> maybeInstrumentClass(String className) throws ClassNotFoundException {
    final byte[] origClassBytes = getByteCode(className);

    try {
      final byte[] bytes;
      ClassDetails classDetails = new ClassDetails(origClassBytes);
      if (config.shouldInstrument(classDetails)) {
        bytes = classInstrumentor.instrument(classDetails, config, classNodeProvider);
        maybeDumpClassBytes(classDetails, bytes);
      } else {
        bytes = postProcessUninstrumentedClass(classDetails);
      }
      ensurePackage(className);
      return defineClass(className, bytes, 0, bytes.length);
    } catch (Exception e) {
      throw new ClassNotFoundException("couldn't load " + className, e);
    } catch (OutOfMemoryError e) {
      System.err.println("[ERROR] couldn't load " + className + " in " + this);
      throw e;
    }
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

  protected byte[] postProcessUninstrumentedClass(ClassDetails classDetails) {
    return classDetails.getClassBytes();
  }

  protected byte[] getByteCode(String className) throws ClassNotFoundException {
    String classFilename = className.replace('.', '/') + ".class";
    try (InputStream classBytesStream = getClassBytesAsStreamPreferringLocalUrls(classFilename)) {
      if (classBytesStream == null) {
        throw new ClassNotFoundException(className);
      }

      return Util.readBytes(classBytesStream);
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
}
