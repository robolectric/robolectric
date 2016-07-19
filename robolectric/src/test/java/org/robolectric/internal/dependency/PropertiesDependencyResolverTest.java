package org.robolectric.internal.dependency;

import org.junit.Before;
import org.junit.Test;

import java.net.URL;
import java.util.Properties;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PropertiesDependencyResolverTest {

  private PropertiesDependencyResolver resolver;
  private Properties properties;
  private DependencyResolver mock;

  @Before
  public void setUp() throws Exception {
    properties = new Properties();
    mock = mock(DependencyResolver.class);
    resolver = new PropertiesDependencyResolver(properties, mock);
  }

  @Test
  public void whenPathIsProvidedInProperties_shouldReturnFileUrls() throws Exception {
    properties.setProperty("com.group:example:1.3", "/path/1:/path/2");
    URL[] urls = resolver.getLocalArtifactUrls(new DependencyJar("com.group", "example", "1.3", null));
    assertThat(urls).containsExactly(
        new URL("file:///path/1"),
        new URL("file:///path/2")
    );
  }

  @Test
  public void whenMissingFromProperties_shouldDelegate() throws Exception {
    DependencyJar dependencyJar = new DependencyJar("com.group", "example", "1.3", null);
    when(mock.getLocalArtifactUrl(dependencyJar)).thenReturn(new URL("file:///path/3"));
    URL[] urls = resolver.getLocalArtifactUrls(dependencyJar);
    assertThat(urls).containsExactly(
        new URL("file:///path/3")
    );
  }
}