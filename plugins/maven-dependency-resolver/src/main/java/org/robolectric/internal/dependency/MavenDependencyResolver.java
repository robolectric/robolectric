package org.robolectric.internal.dependency;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.file.Paths;
import java.util.Hashtable;
import org.apache.maven.artifact.ant.Authentication;
import org.apache.maven.artifact.ant.DependenciesTask;
import org.apache.maven.artifact.ant.LocalRepository;
import org.apache.maven.artifact.ant.RemoteRepository;
import org.apache.maven.model.Dependency;
import org.apache.tools.ant.Project;
import org.robolectric.MavenRoboSettings;

public class MavenDependencyResolver implements DependencyResolver {
  private final String repositoryUrl;
  private final String repositoryId;
  private final String repositoryUserName;
  private final String repositoryPassword;
  private final String repositoryLocalPath;

  public MavenDependencyResolver() {
    this(MavenRoboSettings.getMavenRepositoryUrl(), MavenRoboSettings.getMavenRepositoryId(), MavenRoboSettings
        .getMavenRepositoryUserName(), MavenRoboSettings.getMavenRepositoryPassword(),
        System.getProperty("robolectric.dependency.repo.localpath"));
  }

  public MavenDependencyResolver(String repositoryUrl, String repositoryId, String repositoryUserName, String repositoryPassword, String repositoryLocalPath) {
    this.repositoryUrl = repositoryUrl;
    this.repositoryId = repositoryId;
    this.repositoryUserName = repositoryUserName;
    this.repositoryPassword = repositoryPassword;
    this.repositoryLocalPath = repositoryLocalPath;
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
    DependenciesTask dependenciesTask = createDependenciesTask();
    configureMaven(dependenciesTask);
    RemoteRepository remoteRepository = new RemoteRepository();
    remoteRepository.setUrl(repositoryUrl);
    remoteRepository.setId(repositoryId);
    if (repositoryLocalPath != null) {
      LocalRepository localRepository = new LocalRepository();
      localRepository.setPath(new File(repositoryLocalPath));
      dependenciesTask.addLocalRepository(localRepository);
    }
    if (repositoryUserName != null || repositoryPassword != null) {
      Authentication authentication = new Authentication();
      authentication.setUserName(repositoryUserName);
      authentication.setPassword(repositoryPassword);
      remoteRepository.addAuthentication(authentication);
    }
    dependenciesTask.addConfiguredRemoteRepository(remoteRepository);
    final Project project = new Project();
    dependenciesTask.setProject(project);
    for (DependencyJar dependencyJar : dependencies) {
      Dependency dependency = new Dependency();
      dependency.setArtifactId(dependencyJar.getArtifactId());
      dependency.setGroupId(dependencyJar.getGroupId());
      dependency.setType(dependencyJar.getType());
      dependency.setVersion(dependencyJar.getVersion());
      if (dependencyJar.getClassifier() != null) {
        dependency.setClassifier(dependencyJar.getClassifier());
      }
      dependenciesTask.addDependency(dependency);
    }

    whileLocked(dependenciesTask::execute);

    @SuppressWarnings("unchecked")
    Hashtable<String, String> artifacts = project.getProperties();
    URL[] urls = new URL[dependencies.length];
    for (int i = 0; i < urls.length; i++) {
      try {
        urls[i] = Paths.get(artifacts.get(key(dependencies[i]))).toUri().toURL();
      } catch (MalformedURLException e) {
        throw new RuntimeException(e);
      }
    }
    return urls;
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

  private String key(DependencyJar dependency) {
    String key = dependency.getGroupId() + ":" + dependency.getArtifactId() + ":" + dependency.getType();
    if(dependency.getClassifier() != null) {
      key += ":" + dependency.getClassifier();
    }
    return key;
  }

  protected DependenciesTask createDependenciesTask() {
    return new DependenciesTask();
  }

  protected void configureMaven(DependenciesTask dependenciesTask) {
    // maybe you want to override this method and some settings?
  }
}
