package org.robolectric.internal.dependency;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.robolectric.res.Fs;
import org.robolectric.res.FsFile;

@RunWith(JUnit4.class)
public class PropertiesDependencyResolverTest {

  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();
  private DependencyResolver mock;

  @Before
  public void setUp() throws Exception {
    mock = mock(DependencyResolver.class);
  }

  @Test
  public void whenAbsolutePathIsProvidedInProperties_shouldReturnFileUrl() throws Exception {
    DependencyResolver resolver = new PropertiesDependencyResolver(
        propsFile("com.group\\:example\\:1.3: /path/1\n"), mock);

    URL url = resolver.getLocalArtifactUrl(new DependencyJar("com.group", "example", "1.3", null));
    assertThat(url).isEqualTo(new URL("file:///path/1"));
  }

  @Test
  public void whenRelativePathIsProvidedInProperties_shouldReturnFileUrl() throws Exception {
    DependencyResolver resolver = new PropertiesDependencyResolver(
        propsFile("com.group\\:example\\:1.3: path/1\n"), mock);

    URL url = resolver.getLocalArtifactUrl(new DependencyJar("com.group", "example", "1.3", null));
    assertThat(url).isEqualTo(new URL("file://" + temporaryFolder.getRoot() + "/path/1"));
  }

  @Test
  public void whenMissingFromProperties_shouldDelegate() throws Exception {
    DependencyResolver resolver = new PropertiesDependencyResolver(
        propsFile("nothing: interesting"), mock);

    DependencyJar dependencyJar =  new DependencyJar("com.group", "example", "1.3", null);
    when(mock.getLocalArtifactUrl(dependencyJar)).thenReturn(new URL("file:///path/3"));
    URL url = resolver.getLocalArtifactUrl(dependencyJar);
    assertThat(url).isEqualTo(new URL("file:///path/3")
    );
  }

  @Test
  public void whenDelegateIsNull_shouldGiveGoodMessage() throws Exception {
    DependencyResolver resolver = new PropertiesDependencyResolver(
        propsFile("nothing: interesting"), null);

    DependencyJar dependencyJar =  new DependencyJar("com.group", "example", "1.3", null);
    try {
      resolver.getLocalArtifactUrl(dependencyJar);
      fail("should have failed");
    } catch (Exception e) {
      assertThat(e.getMessage()).contains("no artifacts found for " + dependencyJar);
    }
  }

  //////////////////

  private FsFile propsFile(String contents) throws IOException {
    File file = temporaryFolder.newFile("file.properties");
    Files.asCharSink(file, Charsets.UTF_8).write(contents);
    return Fs.newFile(file);
  }
}