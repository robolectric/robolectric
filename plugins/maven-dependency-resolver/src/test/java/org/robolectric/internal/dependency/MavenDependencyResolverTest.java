package org.robolectric.internal.dependency;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

import com.google.common.collect.Iterables;
import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResult;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

@RunWith(JUnit4.class)
public class MavenDependencyResolverTest {

  private static final String REPOSITORY_URL = "https://default-repo";

  private static final String REPOSITORY_ID = "remote";

  private static final String REPOSITORY_USERNAME = "username";

  private static final String REPOSITORY_PASSWORD = "password";

  private DependencyResolver dependencyResolver;

  @Rule public MockitoRule mockitoRule = MockitoJUnit.rule();

  @Mock RepositorySystem repositorySystem;

  @Mock Artifact artifact;

  @Captor ArgumentCaptor<Collection<ArtifactRequest>> captor;

  @Before
  public void setUp() throws Exception {
    File resultFile = File.createTempFile("MavenDependencyResolverTest", null);
    resultFile.deleteOnExit();
    doReturn(resultFile).when(artifact).getFile();
    doAnswer(
            invocation -> {
              ArrayList<ArtifactResult> results = new ArrayList<>();
              List<ArtifactRequest> requests = invocation.getArgument(1);
              for (int i = 0; i < requests.size(); i++) {
                ArtifactResult result = new ArtifactResult(requests.get(i));
                result.setArtifact(artifact);
                results.add(result);
              }
              return results;
            })
        .when(repositorySystem)
        .resolveArtifacts(any(RepositorySystemSession.class), anyList());

    dependencyResolver = createResolver();
  }

  @Test
  public void getLocalArtifactUrl_shouldAddConfiguredRemoteRepository() throws Exception {
    DependencyJar dependencyJar = new DependencyJar("group1", "artifact1", "", null);

    dependencyResolver.getLocalArtifactUrl(dependencyJar);

    verify(repositorySystem).resolveArtifacts(any(RepositorySystemSession.class), captor.capture());
    Collection<ArtifactRequest> requests = captor.getValue();
    assertThat(requests).hasSize(1);
    ArtifactRequest artifactRequest = Iterables.get(requests, 0);
    List<RemoteRepository> repositories = artifactRequest.getRepositories();
    assertThat(repositories).hasSize(1);
    RemoteRepository repository = repositories.get(0);
    assertThat(repository.getUrl()).isEqualTo(REPOSITORY_URL);
    assertThat(repository.getId()).isEqualTo(REPOSITORY_ID);
  }

  @Test
  public void getLocalArtifactUrl_shouldPerformArtifactRequest() throws Exception {
    DependencyJar dependencyJar = new DependencyJar("group1", "artifact1", "3", null);

    dependencyResolver.getLocalArtifactUrl(dependencyJar);
    verify(repositorySystem).resolveArtifacts(any(RepositorySystemSession.class), captor.capture());

    Collection<ArtifactRequest> requests = captor.getValue();

    assertThat(requests).hasSize(1);
    Artifact artifact = Iterables.get(requests, 0).getArtifact();
    assertThat(artifact.getGroupId()).isEqualTo("group1");
    assertThat(artifact.getArtifactId()).isEqualTo("artifact1");
    assertThat(artifact.getVersion()).isEqualTo("3");
    assertThat(artifact.getExtension()).isEqualTo("jar");
    assertThat(artifact.getClassifier()).isEmpty();
  }

  @Test
  public void getLocalArtifactUrl_shouldExecuteArtifactRequest() throws Exception {
    DependencyJar dependencyJar = new DependencyJar("group1", "artifact1", "3", null);
    dependencyResolver.getLocalArtifactUrl(dependencyJar);
    verify(repositorySystem).resolveArtifacts(any(RepositorySystemSession.class), anyList());
  }

  private DependencyResolver createResolver() {
    return new MavenDependencyResolver(
        REPOSITORY_URL, REPOSITORY_ID, REPOSITORY_USERNAME, REPOSITORY_PASSWORD) {
      @Override
      protected RepositorySystem createRepositorySystem() {
        return repositorySystem;
      }
    };
  }
}
