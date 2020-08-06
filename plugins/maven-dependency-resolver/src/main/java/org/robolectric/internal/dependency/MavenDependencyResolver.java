package org.robolectric.internal.dependency;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.util.ArrayList;
import java.util.List;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.eclipse.aether.AbstractRepositoryListener;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositoryEvent;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.Authentication;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.LocalRepositoryManager;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResolutionException;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.util.repository.AuthenticationBuilder;
import org.robolectric.MavenRoboSettings;
import org.robolectric.util.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class MavenDependencyResolver implements DependencyResolver {
  private final String repositoryUrl;
  private final String repositoryId;
  private final String repositoryUserName;
  private final String repositoryPassword;

  public MavenDependencyResolver() {
    this(MavenRoboSettings.getMavenRepositoryUrl(), MavenRoboSettings.getMavenRepositoryId(), MavenRoboSettings
        .getMavenRepositoryUserName(), MavenRoboSettings.getMavenRepositoryPassword());
  }

  public MavenDependencyResolver(String repositoryUrl, String repositoryId, String repositoryUserName, String repositoryPassword) {
    this.repositoryUrl = repositoryUrl;
    this.repositoryId = repositoryId;
    this.repositoryUserName = repositoryUserName;
    this.repositoryPassword = repositoryPassword;
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
    RepositorySystem repositorySystem = createRepositorySystem();
    RepositorySystemSession session = createSession(repositorySystem);
    RemoteRepository.Builder repositoryBuilder =
        new RemoteRepository.Builder(this.repositoryId, "default", this.repositoryUrl);
    if (!Strings.isNullOrEmpty(this.repositoryUserName)) {
      Authentication auth =
          new AuthenticationBuilder()
              .addUsername(repositoryUserName)
              .addPassword(repositoryPassword)
              .build();
      repositoryBuilder.setAuthentication(auth);
    }
    List<RemoteRepository> repositories = ImmutableList.of(repositoryBuilder.build());
    List<ArtifactRequest> artifactRequests = new ArrayList<>();
    for (DependencyJar dependencyJar : dependencies) {
      Artifact artifact =
          new DefaultArtifact(
              dependencyJar.getGroupId(),
              dependencyJar.getArtifactId(),
              dependencyJar.getClassifier(),
              dependencyJar.getType(),
              dependencyJar.getVersion());
      ArtifactRequest artifactRequest = new ArtifactRequest();
      artifactRequest.setArtifact(artifact);
      artifactRequest.setRepositories(repositories);
      artifactRequests.add(artifactRequest);
    }
    final List<ArtifactResult> artifactResults = new ArrayList<>();
    whileLocked(
        () -> {
          try {
            artifactResults.addAll(repositorySystem.resolveArtifacts(session, artifactRequests));
          } catch (ArtifactResolutionException e) {
            throw new AssertionError("Failed to resolve artifact", e);
          }
        });

    @SuppressWarnings("unchecked")
    URL[] urls = new URL[dependencies.length];
    for (int i = 0; i < urls.length; i++) {
      try {
        urls[i] = artifactResults.get(i).getArtifact().getFile().toURI().toURL();
      } catch (MalformedURLException e) {
        throw new RuntimeException(e);
      }
    }
    return urls;
  }

  protected RepositorySystem createRepositorySystem() {
    DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
    locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
    locator.addService(TransporterFactory.class, HttpTransporterFactory.class);
    locator.setErrorHandler(
        new DefaultServiceLocator.ErrorHandler() {
          @Override
          public void serviceCreationFailed(Class<?> type, Class<?> impl, Throwable exception) {
            Logger.error("Service creation failed", exception);
          }
        });
    return locator.getService(RepositorySystem.class);
  }

  private RepositorySystemSession createSession(RepositorySystem system) {
    DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
    session.setIgnoreArtifactDescriptorRepositories(true);
    LocalRepository localRepo = new LocalRepository(getLocalRepositoryDir());
    LocalRepositoryManager localRepositoryManager =
        system.newLocalRepositoryManager(session, localRepo);
    session.setRepositoryListener(new LoggingRepositoryListener());
    session.setLocalRepositoryManager(localRepositoryManager);
    return session;
  }

  private void whileLocked(Runnable runnable) {
    File lockFile = new File(System.getProperty("user.home"), ".robolectric-download-lock");
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

  @Override
  public URL getLocalArtifactUrl(DependencyJar dependency) {
    URL[] urls = getLocalArtifactUrls(dependency);
    if (urls.length > 0) {
      return urls[0];
    }
    return null;
  }

  /** Locates the local maven repo. */
  private File getLocalRepositoryDir() {
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
        return node.getTextContent();
      }
    } catch (ParserConfigurationException | IOException | SAXException e) {
      Logger.error("Error reading settings.xml", e);
    }
    return null;
  }

  private static class LoggingRepositoryListener extends AbstractRepositoryListener {
    @Override
    public void artifactDownloading(RepositoryEvent event) {
      Logger.info(
          "Transferring "
              + event.getArtifact().toString()
              + " from "
              + event.getRepository().getId());
    }

    @Override
    public void artifactResolved(RepositoryEvent event) {
      if (!event.getExceptions().isEmpty()) {
        for (Exception exception : event.getExceptions()) {
          Logger.info("Error transferring  " + event.getArtifact().toString(), exception);
        }
      }
    }
  }
}
