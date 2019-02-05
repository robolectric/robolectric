package org.robolectric.plugins;

import static com.google.common.truth.Truth.assertThat;
import static com.google.common.truth.Truth8.assertThat;

import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Properties;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.internal.dependency.DependencyJar;
import org.robolectric.internal.dependency.DependencyResolver;
import org.robolectric.res.Fs;
import org.robolectric.util.TempDirectory;

@RunWith(JUnit4.class)
public class LegacyDependencyResolverTest {

  private static final String VERSION = "4.3_r2-robolectric-r1";
  private static final DependencyJar DEPENDENCY_COORDS =
      new DependencyJar("org.robolectric", "android-all", VERSION);
  private TempDirectory tempDirectory;

  @Before
  public void setUp() throws Exception {
    tempDirectory = new TempDirectory();
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

    DependencyResolver resolver =
        new LegacyDependencyResolver(props("robolectric-deps.properties", depsPath.toString()));

    URL jarUrl = resolver.getLocalArtifactUrl(DEPENDENCY_COORDS);
    assertThat(Fs.fromUrl(jarUrl)).isEqualTo(jarPath);
  }

  @Test
  public void whenRobolectricDepsPropertiesPropertyAndOfflineProperty() throws Exception {
    Path depsPath = tempDirectory
        .createFile("deps.properties",
            "org.robolectric\\:android-all\\:" + VERSION + ": file-123.jar");
    Path jarPath = tempDirectory.createFile("file-123.jar", "...");

    DependencyResolver resolver =
        new LegacyDependencyResolver(props("robolectric-deps.properties", depsPath.toString(),
            "robolectric.offline", "true"), new EmptyClassLoader());

    URL jarUrl = resolver.getLocalArtifactUrl(DEPENDENCY_COORDS);
    assertThat(Fs.fromUrl(jarUrl)).isEqualTo(jarPath);
  }

  @Test
  public void whenRobolectricDepsPropertiesResource() throws Exception {
    // there's already a robolectric-deps.properties for these tests, just make sure we're using it.
    DependencyResolver resolver =
        new LegacyDependencyResolver(new Properties());

    URL jarUrl = resolver.getLocalArtifactUrl(DEPENDENCY_COORDS);
    assertThat(Fs.fromUrl(jarUrl).toString()).endsWith("robolectric-r1.jar");
  }

  @Test
  public void whenRobolectricDependencyDirProperty() throws Exception {
    Path jarsPath = tempDirectory.create("jars");
    Path sdkJarPath = tempDirectory.createFile("jars/android-all-" + VERSION + ".jar", "...");

    DependencyResolver resolver =
        new LegacyDependencyResolver(props("robolectric.dependency.dir", jarsPath.toString()));

    URL jarUrl = resolver.getLocalArtifactUrl(DEPENDENCY_COORDS);
    assertThat(Fs.fromUrl(jarUrl)).isEqualTo(sdkJarPath);
  }

  @Test
  public void whenNoPropertiesOrResourceFile() throws Exception {
    DependencyResolver resolver =
        new LegacyDependencyResolver(new Properties(), new EmptyClassLoader());

    URL jarUrl = resolver.getLocalArtifactUrl(DEPENDENCY_COORDS);
    assertThat(Fs.fromUrl(jarUrl)).isEqualTo(Fs.fromUrl("file:///some/fake/file.jar"));
  }

  private Properties props(String key, String value, String key2, String value2) {
    Properties props = props(key, value);
    props.setProperty(key2, value2);
    return props;
  }

  private Properties props(String key, String value) {
    Properties properties = new Properties();
    properties.setProperty(key, value);
    return properties;
  }

  private static class EmptyClassLoader extends URLClassLoader {

    public EmptyClassLoader() {
      super(new URL[0]);
    }

    @Override
    public URL getResource(String name) {
      return null;
    }

    @Override
    public Class<?> loadClass(String name) {
      return FakeMavenDependencyResolver.class;
    }
  }

  public static class FakeMavenDependencyResolver implements DependencyResolver {

    @Override
    public URL getLocalArtifactUrl(DependencyJar dependency) {
      try {
        return new URL("file:///some/fake/file.jar");
      } catch (MalformedURLException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
