package org.robolectric.internal.dependency;

import com.google.common.base.Strings;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.robolectric.MavenRoboSettings;
import org.robolectric.util.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * This class is mainly responsible for fetching Android framework JAR dependencies from
 * MavenCentral. Initially the fetching was being done with maven-ant-tasks, but that dependency
 * become outdated and unmaintained and had security vulnerabilities.
 *
 * <p>There was an initial attempt to use maven-resolver for this, but that depends on a newer
 * version of Apache Http Client that is not compatible with the one expected to be on the classpath
 * for Android 16-18.
 *
 * <p>This uses only basic {@link java.net.HttpURLConnection} for fetching. In general using an HTTP
 * client library here could create conflicts with the ones in the Android system.
 *
 * @see <a href="https://maven.apache.org/ant-tasks/">maven-ant-tasks</a>
 * @see <a href="https://maven.apache.org/resolver/index.html">Maven Resolver</a></a>
 */
public class MavenDependencyResolver implements DependencyResolver {

  private final ExecutorService executorService;
  private final MavenArtifactFetcher mavenArtifactFetcher;
  private final File localRepositoryDir;

  public MavenDependencyResolver() {
    this(
        MavenRoboSettings.getMavenRepositoryUrl(),
        MavenRoboSettings.getMavenRepositoryId(),
        MavenRoboSettings.getMavenRepositoryUserName(),
        MavenRoboSettings.getMavenRepositoryPassword(),
        MavenRoboSettings.getMavenProxyHost(),
        MavenRoboSettings.getMavenProxyPort());
  }

  public MavenDependencyResolver(
      String repositoryUrl,
      String repositoryId,
      String repositoryUserName,
      String repositoryPassword,
      String proxyHost,
      int proxyPort) {
    this.executorService = createExecutorService();
    this.localRepositoryDir = getLocalRepositoryDir();
    this.mavenArtifactFetcher =
        createMavenFetcher(
            repositoryUrl,
            repositoryUserName,
            repositoryPassword,
            proxyHost,
            proxyPort,
            localRepositoryDir,
            this.executorService);
  }

  @Override
  public URL[] getLocalArtifactUrls(DependencyJar dependency) {
    return getLocalArtifactUrls(new DependencyJar[] {dependency});
  }

  /**
   * Get an array of local artifact URLs for the given dependencies. The order of the URLs is guaranteed to be the
   * same as the input order of dependencies, i.e., urls[i] is the local artifact URL for dependencies[i].
   */
  @SuppressWarnings("NewApi")
  public URL[] getLocalArtifactUrls(DependencyJar... dependencies) {
    List<MavenJarArtifact> artifacts = new ArrayList<>(dependencies.length);
    whileLocked(
        () -> {
          for (DependencyJar dependencyJar : dependencies) {
            MavenJarArtifact artifact = new MavenJarArtifact(dependencyJar);
            artifacts.add(artifact);
            mavenArtifactFetcher.fetchArtifact(artifact);
          }
        });
    URL[] urls = new URL[dependencies.length];
    try {
      for (int i = 0; i < artifacts.size(); i++) {
        MavenJarArtifact artifact = artifacts.get(i);
        urls[i] = new File(localRepositoryDir, artifact.jarPath()).toURI().toURL();
      }
    } catch (MalformedURLException e) {
      throw new AssertionError(e);
    }
    return urls;
  }

  private void whileLocked(Runnable runnable) {
    File lockFile = createLockFile();
    try (RandomAccessFile raf = new RandomAccessFile(lockFile, "rw")) {
      try (FileChannel channel = raf.getChannel()) {
        try (FileLock ignored = channel.lock()) {
          runnable.run();
        }
      }
    } catch (IOException e) {
      throw new IllegalStateException("Couldn't create lock file " + lockFile, e);
    } finally {
      lockFile.delete();
    }
  }

  protected File createLockFile() {
    return new File(System.getProperty("user.home"), ".robolectric-download-lock");
  }

  @Override
  public URL getLocalArtifactUrl(DependencyJar dependency) {
    URL[] urls = getLocalArtifactUrls(dependency);
    if (urls.length > 0) {
      return urls[0];
    }
    return null;
  }

  /** Locates the local maven repo. */
  protected File getLocalRepositoryDir() {
    String localRepoDir = System.getProperty("maven.repo.local");
    if (!Strings.isNullOrEmpty(localRepoDir)) {
      return new File(localRepoDir);
    }
    File mavenHome = new File(System.getProperty("user.home"), ".m2");
    String settingsRepoDir = getLocalRepositoryFromSettings(mavenHome);
    if (!Strings.isNullOrEmpty(settingsRepoDir)) {
      return new File(settingsRepoDir);
    }
    return new File(mavenHome, "repository");
  }

  private String getLocalRepositoryFromSettings(File mavenHome) {
    File mavenSettings = new File(mavenHome, "settings.xml");
    if (!mavenSettings.exists() || !mavenSettings.isFile()) {
      return null;
    }
    try {
      DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      Document document = builder.parse(mavenSettings);
      NodeList nodeList = document.getElementsByTagName("localRepository");

      if (nodeList.getLength() != 0) {
        Node node = nodeList.item(0);
        String repository = node.getTextContent();

        if (repository == null) {
          return null;
        }
        return repository.trim();
      }
    } catch (ParserConfigurationException | IOException | SAXException e) {
      Logger.error("Error reading settings.xml", e);
    }
    return null;
  }

  protected MavenArtifactFetcher createMavenFetcher(
      String repositoryUrl,
      String repositoryUserName,
      String repositoryPassword,
      String proxyHost,
      int proxyPort,
      File localRepositoryDir,
      ExecutorService executorService) {
    return new MavenArtifactFetcher(
        repositoryUrl,
        repositoryUserName,
        repositoryPassword,
        proxyHost,
        proxyPort,
        localRepositoryDir,
        executorService);
  }

  protected ExecutorService createExecutorService() {
    return Executors.newFixedThreadPool(2);
  }
}
