package org.robolectric.internal.dependency;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;
import java.util.Properties;

import com.google.common.base.Preconditions;
import org.apache.maven.artifact.ant.DependenciesTask;
import org.apache.maven.artifact.ant.RemoteRepository;
import org.apache.maven.model.Dependency;
import org.apache.tools.ant.Project;
import org.robolectric.RoboSettings;
import org.robolectric.util.Util;

public class MavenDependencyResolver implements DependencyResolver {
  private final Project project = new Project();
  private final String repositoryUrl;
  private final String repositoryId;
  private final Properties depsProp;

  public MavenDependencyResolver(Properties depsProps) {
    this(depsProps, RoboSettings.getMavenRepositoryUrl(), RoboSettings.getMavenRepositoryId());
  }

  public MavenDependencyResolver(Properties depsProp, String repositoryUrl, String repositoryId) {
    this.depsProp = depsProp;
    this.repositoryUrl = repositoryUrl;
    this.repositoryId = repositoryId;
  }

  /**
   * Get an array of local artifact URLs for the given dependencies. The order of the URLs is guaranteed to be the
   * same as the input order of dependencies, i.e., urls[i] is the local artifact URL for dependencies[i].
   */
  public URL[] getLocalArtifactUrls(DependencyJar... dependencies) {
    DependenciesTask dependenciesTask = createDependenciesTask();
    configureMaven(dependenciesTask);
    RemoteRepository remoteRepository = new RemoteRepository();
    remoteRepository.setUrl(repositoryUrl);
    remoteRepository.setId(repositoryId);
    dependenciesTask.addConfiguredRemoteRepository(remoteRepository);
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
    dependenciesTask.execute();

    @SuppressWarnings("unchecked")
    Hashtable<String, String> artifacts = project.getProperties();
    URL[] urls = new URL[dependencies.length];
    for (int i = 0; i < urls.length; i++) {
      try {
        urls[i] = Util.url(artifacts.get(key(dependencies[i])));
      } catch (MalformedURLException e) {
        throw new RuntimeException(e);
      }
    }
    return urls;
  }

  @Override
  public URL getLocalArtifactUrl(int apiLevel) {
    String artifact = depsProp.getProperty(Integer.toString(apiLevel));
    Preconditions.checkNotNull(artifact, "Could not find dependency entry for api level " + apiLevel);
    return getLocalArtifactUrl(DependencyJar.fromShortName(artifact));
  }

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
