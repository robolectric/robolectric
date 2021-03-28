package org.robolectric.plugins;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Properties;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.internal.dependency.DependencyJar;
import org.robolectric.internal.dependency.DependencyResolver;
import org.robolectric.plugins.LegacyDependencyResolver.DefinitelyNotAClassLoader;
import org.robolectric.res.Fs;
import org.robolectric.util.TempDirectory;

@RunWith(JUnit4.class)
public class LegacyDependencyResolverTest {

  private static final String VERSION = "4.3_r2-robolectric-r1";
  private static final DependencyJar DEPENDENCY_COORDS =
      new DependencyJar("org.robolectric", "android-all", VERSION);

  private TempDirectory tempDirectory;
  private Properties properties;
  private DefinitelyNotAClassLoader mockClassLoader;

  @Before
  public void setUp() throws Exception {
    tempDirectory = new TempDirectory();
    properties = new Properties();
    mockClassLoader = mock(DefinitelyNotAClassLoader.class);
  }

  @After
  public void tearDown() throws Exception {
    tempDirectory.destroy();
  }

  @Test
  public void whenRobolectricDepsPropertiesProperty() throws Exception {
    Path depsPath = tempDirectory
        .createFile("deps.properties",
            "org.robolectric\\:android-all\\:" + VERSION + ": file-123.jar");
    Path jarPath = tempDirectory.createFile("file-123.jar", "...");

    properties.setProperty("robolectric-deps.properties", depsPath.toString());

    DependencyResolver resolver = new LegacyDependencyResolver(properties, mockClassLoader);

    URL jarUrl = resolver.getLocalArtifactUrl(DEPENDENCY_COORDS);
    assertThat(Fs.fromUrl(jarUrl)).isEqualTo(jarPath);
  }

  @Test
  public void whenRobolectricDepsPropertiesPropertyAndOfflineProperty() throws Exception {
    Path depsPath = tempDirectory
        .createFile("deps.properties",
            "org.robolectric\\:android-all\\:" + VERSION + ": file-123.jar");
    Path jarPath = tempDirectory.createFile("file-123.jar", "...");

    properties.setProperty("robolectric-deps.properties", depsPath.toString());
    properties.setProperty("robolectric.offline", "true");

    DependencyResolver resolver = new LegacyDependencyResolver(properties, mockClassLoader);

    URL jarUrl = resolver.getLocalArtifactUrl(DEPENDENCY_COORDS);
    assertThat(Fs.fromUrl(jarUrl)).isEqualTo(jarPath);
  }

  @Test
  public void whenRobolectricDepsPropertiesResource() throws Exception {
    Path depsPath = tempDirectory
        .createFile("deps.properties",
            "org.robolectric\\:android-all\\:" + VERSION + ": file-123.jar");

    when(mockClassLoader.getResource("robolectric-deps.properties")).thenReturn(meh(depsPath));
    DependencyResolver resolver = new LegacyDependencyResolver(properties, mockClassLoader);

    URL jarUrl = resolver.getLocalArtifactUrl(DEPENDENCY_COORDS);
    assertThat(Fs.fromUrl(jarUrl).toString()).endsWith("file-123.jar");
  }

  @Test
  public void whenRobolectricDependencyDirProperty() throws Exception {
    Path jarsPath = tempDirectory.create("jars");
    Path sdkJarPath = tempDirectory.createFile("jars/android-all-" + VERSION + ".jar", "...");

    properties.setProperty("robolectric.dependency.dir", jarsPath.toString());

    DependencyResolver resolver = new LegacyDependencyResolver(properties, mockClassLoader);

    URL jarUrl = resolver.getLocalArtifactUrl(DEPENDENCY_COORDS);
    assertThat(Fs.fromUrl(jarUrl)).isEqualTo(sdkJarPath);
  }

  @Test
  public void whenNoPropertiesOrResourceFile() throws Exception {
    when(mockClassLoader.getResource("robolectric-deps.properties")).thenReturn(null);
    when(mockClassLoader.loadClass("org.robolectric.internal.dependency.MavenDependencyResolver"))
        .thenReturn((Class) FakeMavenDependencyResolver.class);

    DependencyResolver resolver = new LegacyDependencyResolver(properties, mockClassLoader);

    URL jarUrl = resolver.getLocalArtifactUrl(DEPENDENCY_COORDS);
    assertThat(Fs.fromUrl(jarUrl))
        .isEqualTo(Paths.get("/some/fake/file.jar").toAbsolutePath());
  }

  public static class FakeMavenDependencyResolver implements DependencyResolver {
    @Override
    public URL getLocalArtifactUrl(DependencyJar dependency) {
      return meh(Paths.get("/some/fake/file.jar"));
    }
  }

  private static URL meh(Path path) {
    try {
      return path.toUri().toURL();
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }
}
