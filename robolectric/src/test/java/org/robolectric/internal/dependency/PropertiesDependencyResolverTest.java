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
  public void whenAbsolutePathIsProvidedInProperties_shouldReturnFileUrls() throws Exception {
    DependencyResolver resolver = new PropertiesDependencyResolver(
        propsFile("com.group\\:example\\:1.3: /path/1:/path/2\n"), mock);

    URL[] urls = resolver.getLocalArtifactUrls(new DependencyJar("com.group", "example", "1.3", null));
    assertThat(urls).containsExactly(
        new URL("file:///path/1"),
        new URL("file:///path/2")
    );
  }

  @Test
  public void whenRelativePathIsProvidedInProperties_shouldReturnFileUrls() throws Exception {
    DependencyResolver resolver = new PropertiesDependencyResolver(
        propsFile("com.group\\:example\\:1.3: path/1:/path/2:path/3\n"), mock);

    URL[] urls = resolver.getLocalArtifactUrls(new DependencyJar("com.group", "example", "1.3", null));
    assertThat(urls).containsExactly(
        new URL("file://" + temporaryFolder.getRoot() + "/path/1"),
        new URL("file:///path/2"),
        new URL("file://" + temporaryFolder.getRoot() + "/path/3")
    );
  }

  @Test
  public void whenMissingFromProperties_shouldDelegate() throws Exception {
    DependencyResolver resolver = new PropertiesDependencyResolver(
        propsFile("nothing: interesting"), mock);

    DependencyJar dependencyJar =  new DependencyJar("com.group", "example", "1.3", null);
    when(mock.getLocalArtifactUrl(dependencyJar)).thenReturn(new URL("file:///path/3"));
    URL[] urls = resolver.getLocalArtifactUrls(dependencyJar);
    assertThat(urls).containsExactly(
        new URL("file:///path/3")
    );
  }

  //////////////////

  private FsFile propsFile(String contents) throws IOException {
    return Fs.newFile(temporaryFolder.newFile("file.properties", contents));
  }
}