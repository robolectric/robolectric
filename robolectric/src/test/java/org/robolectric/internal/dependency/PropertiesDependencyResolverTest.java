package org.robolectric.internal.dependency;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.robolectric.res.Fs;
import org.robolectric.res.FsFile;
import org.robolectric.test.TemporaryFolder;

import java.io.IOException;
import java.net.URL;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
    return Fs.newFile(temporaryFolder.newFile("file.properties", contents));
  }
}