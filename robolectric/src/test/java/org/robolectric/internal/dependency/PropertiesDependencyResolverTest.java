package org.robolectric.internal.dependency;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.net.URL;
import java.nio.file.Path;
import java.util.Properties;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.util.Util;

@RunWith(JUnit4.class)
public class PropertiesDependencyResolverTest {

  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();
  private DependencyJar exampleDep;
  private DependencyResolver mock;
  private boolean isWindoze;

  @Before
  public void setUp() throws Exception {
    exampleDep = new DependencyJar("com.group", "example", "1.3", null);
    mock = mock(DependencyResolver.class);
    isWindoze = File.separatorChar == '\\';
  }

  @Test
  public void whenAbsolutePathIsProvidedInProperties_shouldReturnFileUrl() throws Exception {
    String absolutePath = isWindoze ? "c:\\tmp\\file.jar" : "/tmp/file.jar";
    DependencyResolver resolver = new PropertiesDependencyResolver(
        propsFile("com.group:example:1.3", new File(absolutePath).getAbsoluteFile()), mock);

    URL url = resolver.getLocalArtifactUrl(exampleDep);
    if (isWindoze) {
      assertThat(url).isEqualTo(Util.url("c:\\tmp\\file.jar"));
    } else {
      assertThat(url).isEqualTo(Util.url("/tmp/file.jar"));
    }
  }

  @Test
  public void whenRelativePathIsProvidedInProperties_shouldReturnFileUrl() throws Exception {
    DependencyResolver resolver = new PropertiesDependencyResolver(
        propsFile("com.group:example:1.3", new File("path", "1")), mock);

    URL url = resolver.getLocalArtifactUrl(exampleDep);
    assertThat(url).isEqualTo(
        Util.url(new File(new File(temporaryFolder.getRoot(), "path"), "1").getPath()));
  }

  @Test
  public void whenMissingFromProperties_shouldDelegate() throws Exception {
    DependencyResolver resolver = new PropertiesDependencyResolver(
        propsFile("nothing", new File("interesting")), mock);

    when(mock.getLocalArtifactUrl(exampleDep)).thenReturn(new URL("file:///path/3"));
    URL url = resolver.getLocalArtifactUrl(exampleDep);
    assertThat(url).isEqualTo(new URL("file:///path/3")
    );
  }

  @Test
  public void whenDelegateIsNull_shouldGiveGoodMessage() throws Exception {
    DependencyResolver resolver = new PropertiesDependencyResolver(
        propsFile("nothing", new File("interesting")), null);

    try {
      resolver.getLocalArtifactUrl(exampleDep);
      fail("should have failed");
    } catch (Exception e) {
      assertThat(e.getMessage()).contains("no artifacts found for " + exampleDep);
    }
  }

  //////////////////

  private Path propsFile(String key, File value) throws IOException {
    Properties properties = new Properties();
    properties.setProperty(key, value.toString());
    return propsFile(properties);
  }

  private Path propsFile(Properties contents) throws IOException {
    File file = temporaryFolder.newFile("file.properties");
    try (Writer out = new BufferedWriter(new FileWriter(file))) {
      contents.store(out, "for tests");
    }
    return file.toPath();
  }
}