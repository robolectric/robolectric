package org.robolectric.internal.bytecode;

import static com.google.common.base.StandardSystemProperty.JAVA_CLASS_PATH;
import static com.google.common.base.StandardSystemProperty.PATH_SEPARATOR;
import static org.robolectric.util.ReflectionHelpers.ClassParameter.from;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import org.robolectric.util.Logger;
import org.robolectric.util.PerfStatsCollector;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.Util;

/**
 * Class loader that modifies the bytecode of Android classes to insert calls to Robolectric's
 * shadow classes.
 */
public class SandboxClassLoader extends URLClassLoader {
  private final ClassLoader systemClassLoader;
  private final ClassLoader urls;
  private final InstrumentationConfiguration config;
  private final ClassInstrumentor classInstrumentor;
  private final ClassNodeProvider classNodeProvider;

  public SandboxClassLoader(InstrumentationConfiguration config) {
    this(ClassLoader.getSystemClassLoader(), config);
  }

  public SandboxClassLoader(
      ClassLoader systemClassLoader, InstrumentationConfiguration config, URL... urls) {
    super(getClassPathUrls(systemClassLoader), systemClassLoader.getParent());
    this.systemClassLoader = systemClassLoader;

    this.config = config;
    this.urls = new URLClassLoader(urls, null);
    for (URL url : urls) {
      Logger.debug("Loading classes from: %s", url);
    }

    ClassInstrumentor.Decorator decorator = new ShadowDecorator();
    classInstrumentor = createClassInstrumentor(decorator);

    classNodeProvider = new ClassNodeProvider() {
      @Override
      protected byte[] getClassBytes(String internalClassName) throws ClassNotFoundException {
        return getByteCode(internalClassName);
      }
    };
  }

  private static URL[] getClassPathUrls(ClassLoader classloader) {
    if (classloader instanceof URLClassLoader) {
      return ((URLClassLoader) classloader).getURLs();
    }
    return parseJavaClassPath();
  }

  // TODO(b/65488446): Use a public API once one is available.
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

  protected ClassInstrumentor createClassInstrumentor(ClassInstrumentor.Decorator decorator) {
    return InvokeDynamic.ENABLED
        ? new InvokeDynamicClassInstrumentor(decorator)
        : new OldClassInstrumentor(decorator);
  }

  @Override
  public URL getResource(String name) {
    if (config.shouldAcquireResource(name)) {
      return urls.getResource(name);
    }
    URL fromParent = super.getResource(name);
    if (fromParent != null) {
      return fromParent;
    }
    return urls.getResource(name);
  }

  private InputStream getClassBytesAsStreamPreferringLocalUrls(String resName) {
    InputStream fromUrlsClassLoader = urls.getResourceAsStream(resName);
    if (fromUrlsClassLoader != null) {
      return fromUrlsClassLoader;
    }
    return super.getResourceAsStream(resName);
  }

  @Override
  protected Class<?> findClass(String name) throws ClassNotFoundException {
    if (config.shouldAcquire(name)) {
      return PerfStatsCollector.getInstance().measure("load sandboxed class",
          () -> maybeInstrumentClass(name));
    } else {
      return systemClassLoader.loadClass(name);
    }
  }

  protected Class<?> maybeInstrumentClass(String className) throws ClassNotFoundException {
    final byte[] origClassBytes = getByteCode(className);

    MutableClass mutableClass = PerfStatsCollector.getInstance().measure("analyze class",
        () -> classInstrumentor.analyzeClass(origClassBytes, config, classNodeProvider)
    );

    try {
      final byte[] bytes;
      if (config.shouldInstrument(mutableClass)) {
        bytes = PerfStatsCollector.getInstance().measure("instrument class",
            () -> classInstrumentor.instrumentToBytes(mutableClass)
        );
      } else {
        bytes = postProcessUninstrumentedClass(mutableClass, origClassBytes);
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

  protected byte[] postProcessUninstrumentedClass(
      MutableClass mutableClass, byte[] origClassBytes) {
    return origClassBytes;
  }

  @Override
  protected Package getPackage(String name) {
    Package aPackage = super.getPackage(name);
    if (aPackage != null) {
      return aPackage;
    }

    return ReflectionHelpers.callInstanceMethod(systemClassLoader, "getPackage",
        from(String.class, name));
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
