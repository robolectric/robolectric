package org.robolectric.internal.dependency;

import static java.nio.charset.StandardCharsets.UTF_8;

import com.google.common.base.Strings;
import com.google.common.hash.HashCode;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.common.util.concurrent.AsyncCallable;
import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Base64;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import org.robolectric.util.Logger;

/**
 * Class responsible for fetching artifacts from Maven. This uses a thread pool of size two in order
 * to parallelize downloads. It uses the Sun JSSE provider for downloading due to its seamless
 * integration with HTTPUrlConnection.
 */
@SuppressWarnings("UnstableApiUsage")
public class MavenArtifactFetcher {
  private final String repositoryUrl;
  private final String repositoryUserName;
  private final String repositoryPassword;
  private final File localRepositoryDir;
  private final ExecutorService executorService;
  private File stagingRepositoryDir;

  public MavenArtifactFetcher(
      String repositoryUrl,
      String repositoryUserName,
      String repositoryPassword,
      File localRepositoryDir,
      ExecutorService executorService) {
    this.repositoryUrl = repositoryUrl;
    this.repositoryUserName = repositoryUserName;
    this.repositoryPassword = repositoryPassword;
    this.localRepositoryDir = localRepositoryDir;
    this.executorService = executorService;
  }

  public void fetchArtifact(MavenJarArtifact artifact) {
    // Assume that if the file exists in the local repository, it has been fetched successfully.
    if (new File(localRepositoryDir, artifact.jarPath()).exists()) {
      Logger.info(String.format("Found %s in local maven repository", artifact));
      return;
    }
    this.stagingRepositoryDir = Files.createTempDir();
    this.stagingRepositoryDir.deleteOnExit();
    try {
      createArtifactSubdirectory(artifact, stagingRepositoryDir);
      Futures.whenAllSucceed(
              fetchToStagingRepository(artifact.pomSha512Path()),
              fetchToStagingRepository(artifact.pomPath()),
              fetchToStagingRepository(artifact.jarSha512Path()),
              fetchToStagingRepository(artifact.jarPath()))
          .callAsync(
              () -> {
                // double check that the artifact has not been installed
                if (new File(localRepositoryDir, artifact.jarPath()).exists()) {
                  removeArtifactFiles(stagingRepositoryDir, artifact);
                  return Futures.immediateFuture(null);
                }
                createArtifactSubdirectory(artifact, localRepositoryDir);
                boolean pomValid =
                    validateStagedFiles(artifact.pomPath(), artifact.pomSha512Path());
                if (!pomValid) {
                  throw new AssertionError("SHA512 mismatch for POM file fetched in " + artifact);
                }
                boolean jarValid =
                    validateStagedFiles(artifact.jarPath(), artifact.jarSha512Path());
                if (!jarValid) {
                  throw new AssertionError("SHA512 mismatch for JAR file fetched in " + artifact);
                }
                Logger.info(
                    String.format(
                        "Checksums validated, moving artifact %s to local maven directory",
                        artifact));
                commitFromStaging(artifact.pomSha512Path());
                commitFromStaging(artifact.pomPath());
                commitFromStaging(artifact.jarSha512Path());
                commitFromStaging(artifact.jarPath());
                removeArtifactFiles(stagingRepositoryDir, artifact);
                return Futures.immediateFuture(null);
              },
              executorService)
          .get();
    } catch (InterruptedException | ExecutionException | IOException e) {
      if (e instanceof InterruptedException) {
        Thread.currentThread().interrupt(); // Restore the interrupted status
      }
      removeArtifactFiles(stagingRepositoryDir, artifact);
      removeArtifactFiles(localRepositoryDir, artifact);
      Logger.error("Failed to fetch maven artifact " + artifact, e);
      throw new AssertionError("Failed to fetch maven artifact " + artifact, e);
    }
  }

  private void removeArtifactFiles(File repositoryDir, MavenJarArtifact artifact) {
    new File(repositoryDir, artifact.jarPath()).delete();
    new File(repositoryDir, artifact.jarSha512Path()).delete();
    new File(repositoryDir, artifact.pomPath()).delete();
    new File(repositoryDir, artifact.pomSha512Path()).delete();
  }

  private boolean validateStagedFiles(String filePath, String sha512Path) throws IOException {
    File tempFile = new File(this.stagingRepositoryDir, filePath);
    File sha512File = new File(this.stagingRepositoryDir, sha512Path);

    HashCode expected =
        HashCode.fromString(new String(Files.asByteSource(sha512File).read(), UTF_8));

    HashCode actual = Files.asByteSource(tempFile).hash(Hashing.sha512());
    return expected.equals(actual);
  }

  private void createArtifactSubdirectory(MavenJarArtifact artifact, File repositoryDir)
      throws IOException {
    File jarPath = new File(repositoryDir, artifact.jarPath());
    Files.createParentDirs(jarPath);
  }

  private URL getRemoteUrl(String path) {
    String url = this.repositoryUrl;
    if (!url.endsWith("/")) {
      url = url + "/";
    }
    try {
      return new URI(url + path).toURL();
    } catch (URISyntaxException | MalformedURLException e) {
      throw new AssertionError(e);
    }
  }

  private ListenableFuture<Void> fetchToStagingRepository(String path) {
    URL remoteUrl = getRemoteUrl(path);
    File destination = new File(this.stagingRepositoryDir, path);
    return createFetchToFileTask(remoteUrl, destination);
  }

  protected ListenableFuture<Void> createFetchToFileTask(URL remoteUrl, File tempFile) {
    return Futures.submitAsync(
        new FetchToFileTask(remoteUrl, tempFile, repositoryUserName, repositoryPassword),
        this.executorService);
  }

  private void commitFromStaging(String path) throws IOException {
    File source = new File(this.stagingRepositoryDir, path);
    File destination = new File(this.localRepositoryDir, path);
    Files.move(source, destination);
  }

  static class FetchToFileTask implements AsyncCallable<Void> {

    private final URL remoteURL;
    private final File localFile;
    private String repositoryUserName;
    private String repositoryPassword;

    public FetchToFileTask(
        URL remoteURL, File localFile, String repositoryUserName, String repositoryPassword) {
      this.remoteURL = remoteURL;
      this.localFile = localFile;
      this.repositoryUserName = repositoryUserName;
      this.repositoryPassword = repositoryPassword;
    }

    @Override
    public ListenableFuture<Void> call() throws Exception {
      URLConnection connection = remoteURL.openConnection();
      // Add authorization header if applicable.
      if (!Strings.isNullOrEmpty(this.repositoryUserName)) {
        String encoded =
            Base64.getEncoder()
                .encodeToString(
                    (this.repositoryUserName + ":" + this.repositoryPassword).getBytes(UTF_8));
        connection.setRequestProperty("Authorization", "Basic " + encoded);
      }

      Logger.info("Transferring " + remoteURL);
      try (InputStream inputStream = connection.getInputStream();
          FileOutputStream outputStream = new FileOutputStream(localFile)) {
        ByteStreams.copy(inputStream, outputStream);
      }
      return Futures.immediateFuture(null);
    }
  }
}
