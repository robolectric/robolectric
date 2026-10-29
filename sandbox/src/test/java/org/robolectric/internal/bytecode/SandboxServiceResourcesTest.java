package org.robolectric.internal.bytecode;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.ServiceLoader;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

/** Covers service-location caching independently from provider state and resource contents. */
public class SandboxServiceResourcesTest {
  private static final String CACHE_PROPERTY = "robolectric.cacheServiceLoaderResources";
  private static final String SERVICE_RESOURCE = "META-INF/services/" + TestService.class.getName();

  @Rule public final TemporaryFolder temporaryFolder = new TemporaryFolder();

  private String originalProperty;

  @Before
  public void rememberProperty() {
    originalProperty = System.getProperty(CACHE_PROPERTY);
    System.clearProperty(CACHE_PROPERTY);
  }

  @After
  public void restoreProperty() {
    if (originalProperty == null) {
      System.clearProperty(CACHE_PROPERTY);
    } else {
      System.setProperty(CACHE_PROPERTY, originalProperty);
    }
  }

  @Test
  public void cachePreservesResourceOrderAndReturnsIndependentEnumerations() throws Exception {
    System.setProperty(CACHE_PROPERTY, "true");
    Path firstDirectory = temporaryFolder.newFolder().toPath();
    Path secondDirectory = temporaryFolder.newFolder().toPath();
    writeResource(firstDirectory, SERVICE_RESOURCE, FirstProvider.class.getName());
    writeResource(secondDirectory, SERVICE_RESOURCE, SecondProvider.class.getName());
    try (CountingClassLoader parent = parent(firstDirectory, secondDirectory);
        SandboxClassLoader loader = sandbox(parent)) {
      Enumeration<URL> first = loader.getResources(SERVICE_RESOURCE);
      URL firstUrl = first.nextElement();
      List<URL> remaining = Collections.list(first);
      List<URL> repeated = Collections.list(loader.getResources(SERVICE_RESOURCE));

      assertThat(repeated.get(0)).isEqualTo(firstUrl);
      assertThat(repeated.subList(1, repeated.size()))
          .containsExactlyElementsIn(remaining)
          .inOrder();
      URL secondUrl = secondDirectory.resolve(SERVICE_RESOURCE).toUri().toURL();
      assertThat(repeated).containsExactly(firstUrl, secondUrl, firstUrl, secondUrl).inOrder();
      assertThat(parent.serviceLookups).isEqualTo(1);
    }
  }

  @Test
  public void cacheRetainsMissingLocationsForOneSandboxOnly() throws Exception {
    System.setProperty(CACHE_PROPERTY, "true");
    Path directory = temporaryFolder.newFolder().toPath();
    try (CountingClassLoader parent = parent(directory);
        SandboxClassLoader first = sandbox(parent)) {
      assertThat(Collections.list(first.getResources(SERVICE_RESOURCE))).isEmpty();
      writeResource(directory, SERVICE_RESOURCE, FirstProvider.class.getName());
      assertThat(Collections.list(first.getResources(SERVICE_RESOURCE))).isEmpty();
      assertThat(parent.serviceLookups).isEqualTo(1);
      try (SandboxClassLoader second = sandbox(parent)) {
        assertThat(Collections.list(second.getResources(SERVICE_RESOURCE))).isNotEmpty();
      }
    }
  }

  @Test
  public void defaultBehaviorObservesNewServiceLocations() throws Exception {
    Path directory = temporaryFolder.newFolder().toPath();
    try (CountingClassLoader parent = parent(directory);
        SandboxClassLoader loader = sandbox(parent)) {
      assertThat(Collections.list(loader.getResources(SERVICE_RESOURCE))).isEmpty();
      writeResource(directory, SERVICE_RESOURCE, FirstProvider.class.getName());
      assertThat(Collections.list(loader.getResources(SERVICE_RESOURCE))).isNotEmpty();
      assertThat(parent.serviceLookups).isEqualTo(2);
    }
  }

  @Test
  public void failedDiscoveryIsRetried() throws Exception {
    System.setProperty(CACHE_PROPERTY, "true");
    Path directory = temporaryFolder.newFolder().toPath();
    writeResource(directory, SERVICE_RESOURCE, FirstProvider.class.getName());
    try (CountingClassLoader parent = parent(directory);
        SandboxClassLoader loader = sandbox(parent)) {
      parent.failNextServiceLookup = true;
      assertThrows(IOException.class, () -> loader.getResources(SERVICE_RESOURCE));
      assertThat(Collections.list(loader.getResources(SERVICE_RESOURCE))).isNotEmpty();
      assertThat(parent.serviceLookups).isEqualTo(2);
    }
  }

  @Test
  public void cacheKeepsProvidersFreshAndRereadsServiceContents() throws Exception {
    System.setProperty(CACHE_PROPERTY, "true");
    Path directory = temporaryFolder.newFolder().toPath();
    writeResource(directory, SERVICE_RESOURCE, FirstProvider.class.getName());
    try (CountingClassLoader parent = parent(directory);
        SandboxClassLoader loader = sandbox(parent)) {
      TestService first = ServiceLoader.load(TestService.class, loader).iterator().next();
      TestService repeated = ServiceLoader.load(TestService.class, loader).iterator().next();
      assertThat(first).isInstanceOf(FirstProvider.class);
      assertThat(repeated).isNotSameInstanceAs(first);

      writeResource(directory, SERVICE_RESOURCE, SecondProvider.class.getName());
      TestService updated = ServiceLoader.load(TestService.class, loader).iterator().next();
      assertThat(updated).isInstanceOf(SecondProvider.class);
      assertThat(parent.serviceLookups).isEqualTo(1);
    }
  }

  @Test
  public void cacheLeavesOrdinaryResourceDiscoveryDynamic() throws Exception {
    System.setProperty(CACHE_PROPERTY, "true");
    Path directory = temporaryFolder.newFolder().toPath();
    try (CountingClassLoader parent = parent(directory);
        SandboxClassLoader loader = sandbox(parent)) {
      assertThat(Collections.list(loader.getResources("ordinary-resource.txt"))).isEmpty();
      writeResource(directory, "ordinary-resource.txt", "created during the test");
      assertThat(Collections.list(loader.getResources("ordinary-resource.txt"))).isNotEmpty();
    }
  }

  @Test
  public void addingClasspathUrlInvalidatesCachedServiceLocations() throws Exception {
    System.setProperty(CACHE_PROPERTY, "true");
    Path firstDirectory = temporaryFolder.newFolder().toPath();
    Path additionalDirectory = temporaryFolder.newFolder().toPath();
    writeResource(additionalDirectory, SERVICE_RESOURCE, FirstProvider.class.getName());
    try (CountingClassLoader parent = parent(firstDirectory);
        MutableSandboxClassLoader loader = sandbox(parent)) {
      assertThat(Collections.list(loader.getResources(SERVICE_RESOURCE))).isEmpty();
      loader.addClasspathUrl(additionalDirectory.toUri().toURL());
      assertThat(Collections.list(loader.getResources(SERVICE_RESOURCE)))
          .containsExactly(additionalDirectory.resolve(SERVICE_RESOURCE).toUri().toURL());
    }
  }

  @Test
  public void closingSandboxReleasesCachedLocalResources() throws Exception {
    System.setProperty(CACHE_PROPERTY, "true");
    Path directory = temporaryFolder.newFolder().toPath();
    writeResource(directory, SERVICE_RESOURCE, FirstProvider.class.getName());
    try (CountingClassLoader parent = parent(directory)) {
      SandboxClassLoader loader = sandbox(parent);
      // The inherited lookup visits this directory through both its parent and its own URLs.
      assertThat(Collections.list(loader.getResources(SERVICE_RESOURCE))).hasSize(2);
      loader.close();
      assertThat(Collections.list(loader.getResources(SERVICE_RESOURCE))).hasSize(1);
    }
  }

  @Test
  public void failingCloseReleasesCachedLocalResources() throws Exception {
    System.setProperty(CACHE_PROPERTY, "true");
    Path directory = temporaryFolder.newFolder().toPath();
    writeResource(directory, SERVICE_RESOURCE, FirstProvider.class.getName());
    IOException failure = new IOException("simulated resource-provider close failure");
    UrlResourceProvider resourceProvider =
        new UrlResourceProvider() {
          @Override
          public void close() throws IOException {
            super.close();
            throw failure;
          }
        };
    try (CountingClassLoader parent = parent(directory)) {
      SandboxClassLoader loader =
          new SandboxClassLoader(
              parent,
              InstrumentationConfiguration.newBuilder().build(),
              resourceProvider,
              new ClassInstrumentor(new ShadowDecorator()));
      assertThat(Collections.list(loader.getResources(SERVICE_RESOURCE))).hasSize(2);
      assertThat(assertThrows(IOException.class, loader::close)).isSameInstanceAs(failure);
      assertThat(Collections.list(loader.getResources(SERVICE_RESOURCE))).hasSize(1);
    }
  }

  private static MutableSandboxClassLoader sandbox(ClassLoader parent) {
    InstrumentationConfiguration configuration =
        InstrumentationConfiguration.newBuilder()
            .doNotAcquireClass(TestService.class)
            .doNotAcquireClass(FirstProvider.class)
            .doNotAcquireClass(SecondProvider.class)
            .build();
    return new MutableSandboxClassLoader(parent, configuration);
  }

  /**
   * Exposes the inherited protected mutation through a subclass on both released and cached
   * loaders.
   */
  private static final class MutableSandboxClassLoader extends SandboxClassLoader {
    MutableSandboxClassLoader(ClassLoader parent, InstrumentationConfiguration configuration) {
      super(
          parent,
          configuration,
          new UrlResourceProvider(),
          new ClassInstrumentor(new ShadowDecorator()));
    }

    void addClasspathUrl(URL url) {
      super.addURL(url);
    }
  }

  private CountingClassLoader parent(Path... directories) throws IOException {
    URL[] urls = new URL[directories.length];
    for (int index = 0; index < directories.length; index++) {
      urls[index] = directories[index].toUri().toURL();
    }
    return new CountingClassLoader(urls, getClass().getClassLoader());
  }

  private static void writeResource(Path directory, String name, String contents)
      throws IOException {
    Path path = directory.resolve(name);
    Files.createDirectories(path.getParent());
    Files.write(path, (contents + "\n").getBytes(StandardCharsets.UTF_8));
  }

  private static final class CountingClassLoader extends URLClassLoader {
    private int serviceLookups;
    private boolean failNextServiceLookup;

    CountingClassLoader(URL[] urls, ClassLoader parent) {
      super(urls, parent);
    }

    @Override
    public Enumeration<URL> getResources(String name) throws IOException {
      if (name.startsWith("META-INF/services/")) {
        serviceLookups++;
        if (failNextServiceLookup) {
          failNextServiceLookup = false;
          throw new IOException("simulated service discovery failure");
        }
      }
      return super.getResources(name);
    }
  }

  public interface TestService {}

  public static final class FirstProvider implements TestService {}

  public static final class SecondProvider implements TestService {}
}
