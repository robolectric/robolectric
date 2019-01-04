package org.robolectric.internal.dependency;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;

import java.net.URL;
import java.util.List;
import org.apache.maven.artifact.ant.DependenciesTask;
import org.apache.maven.artifact.ant.RemoteRepository;
import org.apache.maven.model.Dependency;
import org.apache.tools.ant.Project;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

@RunWith(JUnit4.class)
public class MavenDependencyResolverTest {

  private static final String REPOSITORY_URL = "https://default-repo";

  private static final String REPOSITORY_ID = "remote";

  private static final String REPOSITORY_USERNAME = "username";

  private static final String REPOSITORY_PASSWORD = "password";

  private DependenciesTask dependenciesTask;

  private Project project;

  @Before
  public void setUp() {
    dependenciesTask = spy(new DependenciesTask());
    doNothing().when(dependenciesTask).execute();
    doAnswer(new Answer() {
      @Override
      public Void answer(InvocationOnMock invocationOnMock) throws Throwable {
        invocationOnMock.callRealMethod();
        Object[] args = invocationOnMock.getArguments();
        project = (Project) args[0];
        project.setProperty("group1:artifact1:jar", "path1");
        project.setProperty("group2:artifact2:jar", "path2");
        project.setProperty("group3:artifact3:jar:classifier3", "path3");
        return null;
      }
    }).when(dependenciesTask).setProject(any(Project.class));
  }

  @Test
  public void getLocalArtifactUrl_shouldAddConfiguredRemoteRepository() {
    DependencyResolver dependencyResolver = createResolver();
    DependencyJar dependencyJar = new DependencyJar("group1", "artifact1", "", null);

    dependencyResolver.getLocalArtifactUrl(dependencyJar);

    List<RemoteRepository> repositories = dependenciesTask.getRemoteRepositories();

    assertEquals(1, repositories.size());
    RemoteRepository remoteRepository = repositories.get(0);
    assertEquals(REPOSITORY_URL, remoteRepository.getUrl());
    assertEquals(REPOSITORY_ID, remoteRepository.getId());
  }

  @Test
  public void getLocalArtifactUrl_shouldAddDependencyToDependenciesTask() {
    DependencyResolver dependencyResolver = createResolver();
    DependencyJar dependencyJar = new DependencyJar("group1", "artifact1", "3", null);

    dependencyResolver.getLocalArtifactUrl(dependencyJar);

    List<Dependency> dependencies = dependenciesTask.getDependencies();

    assertEquals(1, dependencies.size());
    Dependency dependency = dependencies.get(0);
    assertEquals("group1", dependency.getGroupId());
    assertEquals("artifact1", dependency.getArtifactId());
    assertEquals("3", dependency.getVersion());
    assertEquals("jar", dependency.getType());
    assertNull(dependency.getClassifier());
  }

  @Test
  public void getLocalArtifactUrl_shouldExecuteDependenciesTask() {
    DependencyResolver dependencyResolver = createResolver();
    DependencyJar dependencyJar = new DependencyJar("group1", "artifact1", "", null);

    dependencyResolver.getLocalArtifactUrl(dependencyJar);

    verify(dependenciesTask).execute();
  }

  @Test
  public void getLocalArtifactUrl_shouldReturnCorrectUrlForArtifactKey() {
    DependencyResolver dependencyResolver = createResolver();
    DependencyJar dependencyJar = new DependencyJar("group1", "artifact1", "", null);

    URL url = dependencyResolver.getLocalArtifactUrl(dependencyJar);

    assertEquals("file:/path1", url.toExternalForm());
  }

  @Test
  public void getLocalArtifactUrl_shouldReturnCorrectUrlForArtifactKeyWithClassifier() {
    DependencyResolver dependencyResolver = createResolver();
    DependencyJar dependencyJar = new DependencyJar("group3", "artifact3", "", "classifier3");

    URL url = dependencyResolver.getLocalArtifactUrl(dependencyJar);

    assertEquals("file:/path3", url.toExternalForm());
  }

  private DependencyResolver createResolver() {
    return new MavenDependencyResolver(REPOSITORY_URL, REPOSITORY_ID, REPOSITORY_USERNAME, REPOSITORY_PASSWORD) {
      @Override
      protected DependenciesTask createDependenciesTask() {
        return dependenciesTask;
      }
    };
  }
}
