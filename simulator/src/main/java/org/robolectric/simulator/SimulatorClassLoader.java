package org.robolectric.simulator;

import com.google.auto.service.AutoService;
import com.google.common.collect.ImmutableList;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import javax.inject.Named;
import org.robolectric.internal.AndroidSandbox;
import org.robolectric.internal.bytecode.ClassInstrumentor;
import org.robolectric.internal.bytecode.InstrumentationConfiguration;
import org.robolectric.internal.bytecode.UrlResourceProvider;
import org.robolectric.pluginapi.Sdk;
import org.robolectric.util.Util;

/**
 * An override of {@link AndroidSandbox.SdkSandboxClassLoader} that supports extra local Jars in the
 * classpath.
 */
@AutoService(AndroidSandbox.SdkSandboxClassLoader.class)
public class SimulatorClassLoader extends AndroidSandbox.SdkSandboxClassLoader {

  private final URLClassLoader extraClassLoader;

  public SimulatorClassLoader(
      InstrumentationConfiguration config,
      @Named("runtimeSdk") Sdk runtimeSdk,
      ClassInstrumentor classInstrumentor,
      JarCollection jarCollection) {
    super(config, runtimeSdk, classInstrumentor);
    extraClassLoader = new UrlResourceProvider(jarCollection.getUrls());
  }

  @Override
  protected byte[] getByteCode(String className) throws ClassNotFoundException {
    String classFilename = className.replace('.', '/') + ".class";
    try (InputStream classBytesStream = extraClassLoader.getResourceAsStream(classFilename)) {
      if (classBytesStream == null) {
        return super.getByteCode(className);
      }
      return Util.readBytes(classBytesStream);
    } catch (IOException e) {
      throw new ClassNotFoundException("couldn't load " + className, e);
    }
  }

  /**
   * This override is required to support ServiceLoader plugins in the extra jars passed into the
   * simulator. It looks for all service-related metadata in META-INF/services/... in the extra jars
   * before falling back to the superclass implementation.
   */
  @Override
  public Enumeration<URL> findResources(String name) throws IOException {
    ArrayList<URL> urls = new ArrayList<>();
    urls.addAll(Collections.list(super.findResources(name)));
    urls.addAll(Collections.list(extraClassLoader.findResources(name)));
    return Collections.enumeration(urls);
  }

  /** Encapsulates a collection of Jar files. */
  public static class JarCollection {

    private final ImmutableList<Path> jarPaths;

    public JarCollection(List<Path> jarPaths) {
      this.jarPaths = ImmutableList.copyOf(jarPaths);
    }

    public URL[] getUrls() {
      return jarPaths.stream().map(JarCollection::toUrl).toArray(URL[]::new);
    }

    private static URL toUrl(Path path) {
      try {
        return path.toUri().toURL();
      } catch (MalformedURLException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
