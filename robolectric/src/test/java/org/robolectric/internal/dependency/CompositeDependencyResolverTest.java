package org.robolectric.internal.dependency;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runners.model.InitializationError;
import org.robolectric.test.TemporaryFolder;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CompositeDependencyResolverTest {

  @Rule public TemporaryFolder temporaryFolder = new TemporaryFolder();

  private DependencyJar[] dependencies = new DependencyJar[]{
      createDependency("group1", "artifact1"),
      createDependency("group2", "artifact2"),
  };
  private DependencyJar dependency = dependencies[0];
  private URL url1;
  private URL url2;

  @Before
  public void setUp() throws InitializationError, MalformedURLException {
    url1 = new URL("http://localhost");
    url2 = new URL("http://127.0.0.1");
  }

  @Test
  public void constructorShouldAcceptNullList() throws Exception {
    CompositeDependencyResolver resolver = new CompositeDependencyResolver(null);
    assertEquals(resolver.getLocalArtifactUrl(dependency), null);
  }

  @Test
  public void constructorShouldAcceptEmptyList() throws Exception {
    CompositeDependencyResolver resolver = new CompositeDependencyResolver(new ArrayList<DependencyResolver>());
    assertEquals(resolver.getLocalArtifactUrl(dependency), null);
  }

  @Test
  public void dependencyResolutionShallRespectInputOrder() throws Exception {
    List<DependencyResolver> resolverList = new ArrayList<>();
    DependencyResolver resolver1 = mock(DependencyResolver.class);
    when(resolver1.getLocalArtifactUrl(dependency)).thenReturn(url1);
    resolverList.add(resolver1);
    DependencyResolver resolver2 = mock(DependencyResolver.class);
    when(resolver2.getLocalArtifactUrl(dependency)).thenReturn(url2);
    resolverList.add(resolver2);
    CompositeDependencyResolver resolver = new CompositeDependencyResolver(resolverList);
    assertEquals(resolver.getLocalArtifactUrl(dependency), url1);
  }

  @Test
  public void dependencyResolutionShallProceedToNextResolver() throws Exception {
    List<DependencyResolver> resolverList = new ArrayList<>();
    DependencyResolver resolver1 = mock(DependencyResolver.class);
    when(resolver1.getLocalArtifactUrl(dependency)).thenReturn(null);
    resolverList.add(resolver1);
    DependencyResolver resolver2 = mock(DependencyResolver.class);
    when(resolver2.getLocalArtifactUrl(dependency)).thenReturn(url2);
    resolverList.add(resolver2);
    CompositeDependencyResolver resolver = new CompositeDependencyResolver(resolverList);
    assertEquals(resolver.getLocalArtifactUrl(dependency), url2);
  }

  @Test
  public void dependencyResolutionShallReturnNullIfNoResolverCanResolveDependency() throws Exception {
    List<DependencyResolver> resolverList = new ArrayList<>();
    DependencyResolver resolver1 = mock(DependencyResolver.class);
    when(resolver1.getLocalArtifactUrl(dependency)).thenReturn(null);
    resolverList.add(resolver1);
    DependencyResolver resolver2 = mock(DependencyResolver.class);
    when(resolver2.getLocalArtifactUrl(dependency)).thenReturn(null);
    resolverList.add(resolver2);
    CompositeDependencyResolver resolver = new CompositeDependencyResolver(resolverList);
    assertEquals(resolver.getLocalArtifactUrl(dependency), null);
  }

  private DependencyJar createDependency(final String groupId, final String artifactId) {
    return new DependencyJar(groupId, artifactId, null, "") {

      @Override
      public boolean equals(Object o) {
        if(!(o instanceof DependencyJar)) return false;

        DependencyJar d = (DependencyJar) o;

        return this.getArtifactId().equals(d.getArtifactId()) && this.getGroupId().equals(groupId);
      }
    };
  }
}
