package org.robolectric.internal.dependency;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.google.common.io.Files;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.MoreExecutors;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
@SuppressWarnings("UnstableApiUsage")
public class MavenDependencyResolverTest {
  private static final File REPOSITORY_DIR;
  private static final String REPOSITORY_URL;
  private static final String REPOSITORY_USERNAME = "username";
  private static final String REPOSITORY_PASSWORD = "password";
  private static final HashFunction SHA512 = Hashing.sha512();

  private static DependencyJar[] successCases =
      new DependencyJar[] {
        new DependencyJar("group", "artifact", "1"),
        new DependencyJar("org.group2", "artifact2-name", "2.4.5"),
        new DependencyJar("org.robolectric", "android-all", "10-robolectric-5803371"),
      };

  static {
    try {
      REPOSITORY_DIR = Files.createTempDir();
      REPOSITORY_DIR.deleteOnExit();
      REPOSITORY_URL = REPOSITORY_DIR.toURI().toURL().toString();

      for (DependencyJar dependencyJar : successCases) {
        addTestArtifact(dependencyJar);
      }
    } catch (Exception e) {
      throw new AssertionError(e);
    }
  }

  private File localRepositoryDir;
  private ExecutorService executorService;
  private MavenDependencyResolver mavenDependencyResolver;
  private TestMavenArtifactFetcher mavenArtifactFetcher;

  @Before
  public void setUp() throws Exception {
    executorService = MoreExecutors.newDirectExecutorService();
    localRepositoryDir = Files.createTempDir();
    localRepositoryDir.deleteOnExit();
    mavenArtifactFetcher =
        new TestMavenArtifactFetcher(
            REPOSITORY_URL,
            REPOSITORY_USERNAME,
            REPOSITORY_PASSWORD,
            localRepositoryDir,
            executorService);
    mavenDependencyResolver = new TestMavenDependencyResolver();
  }

  @Test
  public void getLocalArtifactUrl_placesFilesCorrectlyForSingleURL() throws Exception {
    DependencyJar dependencyJar = successCases[0];
    mavenDependencyResolver.getLocalArtifactUrl(dependencyJar);
    assertThat(mavenArtifactFetcher.getNumRequests()).isEqualTo(4);
    MavenJarArtifact artifact = new MavenJarArtifact(dependencyJar);
    checkJarArtifact(artifact);
  }

  @Test
  public void getLocalArtifactUrl_placesFilesCorrectlyForMultipleURL() throws Exception {
    mavenDependencyResolver.getLocalArtifactUrls(successCases);
    assertThat(mavenArtifactFetcher.getNumRequests()).isEqualTo(4 * successCases.length);
    for (DependencyJar dependencyJar : successCases) {
      MavenJarArtifact artifact = new MavenJarArtifact(dependencyJar);
      checkJarArtifact(artifact);
    }
  }

  /** Checks the case where the existing artifact directory is valid. */
  @Test
  public void getLocalArtifactUrl_handlesExistingArtifactDirectory() throws Exception {
    DependencyJar dependencyJar = new DependencyJar("group", "artifact", "1");
    MavenJarArtifact mavenJarArtifact = new MavenJarArtifact(dependencyJar);
    File jarFile = new File(localRepositoryDir, mavenJarArtifact.jarPath());
    Files.createParentDirs(jarFile);
    assertThat(jarFile.getParentFile().isDirectory()).isTrue();
    mavenDependencyResolver.getLocalArtifactUrl(dependencyJar);
    checkJarArtifact(mavenJarArtifact);
  }

  /**
   * Checks the case where there is some existing artifact metadata in the artifact directory, but
   * not the JAR.
   */
  @Test
  public void getLocalArtifactUrl_handlesExistingMetadataFile() throws Exception {
    DependencyJar dependencyJar = new DependencyJar("group", "artifact", "1");
    MavenJarArtifact mavenJarArtifact = new MavenJarArtifact(dependencyJar);
    File pomFile = new File(localRepositoryDir, mavenJarArtifact.pomPath());
    pomFile.getParentFile().mkdirs();
    Files.write(new byte[0], pomFile);
    assertThat(pomFile.exists()).isTrue();
    mavenDependencyResolver.getLocalArtifactUrl(dependencyJar);
    checkJarArtifact(mavenJarArtifact);
  }

  private void checkJarArtifact(MavenJarArtifact artifact) throws Exception {
    File jar = new File(localRepositoryDir, artifact.jarPath());
    File pom = new File(localRepositoryDir, artifact.pomPath());
    File jarSha512 = new File(localRepositoryDir, artifact.jarSha512Path());
    File pomSha512 = new File(localRepositoryDir, artifact.pomSha512Path());
    assertThat(jar.exists()).isTrue();
    assertThat(readFile(jar)).isEqualTo(artifact.toString() + " jar contents");
    assertThat(pom.exists()).isTrue();
    assertThat(readFile(pom)).isEqualTo(artifact.toString() + " pom contents");
    assertThat(jarSha512.exists()).isTrue();
    assertThat(readFile(jarSha512)).isEqualTo(sha512(artifact.toString() + " jar contents"));
    assertThat(pom.exists()).isTrue();
    assertThat(readFile(pomSha512)).isEqualTo(sha512(artifact.toString() + " pom contents"));
  }

  @Test
  public void getLocalArtifactUrl_doesNotFetchWhenArtifactsExist() throws Exception {
    DependencyJar dependencyJar = new DependencyJar("group", "artifact", "1");
    MavenJarArtifact mavenJarArtifact = new MavenJarArtifact(dependencyJar);
    File artifactFile = new File(localRepositoryDir, mavenJarArtifact.jarPath());
    artifactFile.getParentFile().mkdirs();
    Files.write(new byte[0], artifactFile);
    assertThat(artifactFile.exists()).isTrue();
    mavenDependencyResolver.getLocalArtifactUrl(dependencyJar);
    assertThat(mavenArtifactFetcher.getNumRequests()).isEqualTo(0);
  }

  @Test
  public void getLocalArtifactUrl_handlesFileNotFound() throws Exception {
    DependencyJar dependencyJar = new DependencyJar("group", "missing-artifact", "1");

    assertThrows(
        AssertionError.class, () -> mavenDependencyResolver.getLocalArtifactUrl(dependencyJar));
  }

  @Test
  public void getLocalArtifactUrl_handlesInvalidSha512() throws Exception {
    DependencyJar dependencyJar = new DependencyJar("group", "artifact-invalid-sha512", "1");
    addTestArtifactInvalidSha512(dependencyJar);
    assertThrows(
        AssertionError.class, () -> mavenDependencyResolver.getLocalArtifactUrl(dependencyJar));
  }

  class TestMavenDependencyResolver extends MavenDependencyResolver {

    @Override
    protected MavenArtifactFetcher createMavenFetcher(
        String repositoryUrl,
        String repositoryUserName,
        String repositoryPassword,
        File localRepositoryDir,
        ExecutorService executorService) {
      return mavenArtifactFetcher;
    }

    @Override
    protected ExecutorService createExecutorService() {
      return executorService;
    }

    @Override
    protected File getLocalRepositoryDir() {
      return localRepositoryDir;
    }

    @Override
    protected File createLockFile() {
      try {
        return File.createTempFile("MavenDependencyResolverTest", null);
      } catch (IOException e) {
        throw new AssertionError(e);
      }
    }
  }

  static class TestMavenArtifactFetcher extends MavenArtifactFetcher {
    private ExecutorService executorService;
    private int numRequests;

    public TestMavenArtifactFetcher(
        String repositoryUrl,
        String repositoryUserName,
        String repositoryPassword,
        File localRepositoryDir,
        ExecutorService executorService) {
      super(
          repositoryUrl,
          repositoryUserName,
          repositoryPassword,
          localRepositoryDir,
          executorService);
      this.executorService = executorService;
    }

    @Override
    protected ListenableFuture<Void> createFetchToFileTask(URL remoteUrl, File tempFile) {
      return Futures.submitAsync(
          new FetchToFileTask(remoteUrl, tempFile, null, null) {
            @Override
            public ListenableFuture<Void> call() throws Exception {
              numRequests += 1;
              return super.call();
            }
          },
          executorService);
    }

    public int getNumRequests() {
      return numRequests;
    }
  }

  static void addTestArtifact(DependencyJar dependencyJar) throws IOException {
    MavenJarArtifact mavenJarArtifact = new MavenJarArtifact(dependencyJar);
    try {
      Files.createParentDirs(new File(REPOSITORY_DIR, mavenJarArtifact.jarPath()));
      String jarContents = mavenJarArtifact.toString() + " jar contents";
      Files.write(
          jarContents.getBytes(StandardCharsets.UTF_8),
          new File(REPOSITORY_DIR, mavenJarArtifact.jarPath()));
      Files.write(
          sha512(jarContents).getBytes(),
          new File(REPOSITORY_DIR, mavenJarArtifact.jarSha512Path()));
      String pomContents = mavenJarArtifact.toString() + " pom contents";
      Files.write(
          pomContents.getBytes(StandardCharsets.UTF_8),
          new File(REPOSITORY_DIR, mavenJarArtifact.pomPath()));
      Files.write(
          sha512(pomContents).getBytes(),
          new File(REPOSITORY_DIR, mavenJarArtifact.pomSha512Path()));
    } catch (MalformedURLException e) {
      throw new AssertionError(e);
    }
  }

  static void addTestArtifactInvalidSha512(DependencyJar dependencyJar) throws IOException {
    MavenJarArtifact mavenJarArtifact = new MavenJarArtifact(dependencyJar);
    try {
      Files.createParentDirs(new File(REPOSITORY_DIR, mavenJarArtifact.jarPath()));
      String jarContents = mavenJarArtifact.toString() + " jar contents";
      Files.write(jarContents.getBytes(), new File(REPOSITORY_DIR, mavenJarArtifact.jarPath()));
      Files.write(
          sha512("No the same content").getBytes(),
          new File(REPOSITORY_DIR, mavenJarArtifact.jarSha512Path()));
      String pomContents = mavenJarArtifact.toString() + " pom contents";
      Files.write(pomContents.getBytes(), new File(REPOSITORY_DIR, mavenJarArtifact.pomPath()));
      Files.write(
          sha512("Really not the same content").getBytes(),
          new File(REPOSITORY_DIR, mavenJarArtifact.pomSha512Path()));
    } catch (MalformedURLException e) {
      throw new AssertionError(e);
    }
  }

  static String sha512(String contents) {
    return SHA512.hashString(contents, StandardCharsets.UTF_8).toString();
  }

  static String readFile(File file) throws IOException {
    return new String(Files.asByteSource(file).read(), StandardCharsets.UTF_8);
  }
}
