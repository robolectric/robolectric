package org.robolectric;

import org.apache.maven.artifact.ant.DependenciesTask;
import org.apache.maven.artifact.ant.RemoteRepository;
import org.apache.maven.model.Dependency;
import org.apache.tools.ant.Project;
import org.robolectric.util.Util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;

public class MavenDependencyResolver implements DependencyResolver {
  private final Project project = new Project();

  /**
   * Get an array of local artifact URLs for the given dependencies. The order of the URLs is guaranteed to be the
   * same as the input order of dependencies, i.e., urls[i] is the local artifact URL for dependencies[i].
   */
  @Override
  public URL[] getLocalArtifactUrls(Dependency... dependencies) {
    DependenciesTask dependenciesTask = new DependenciesTask();
    configureMaven(dependenciesTask);
    RemoteRepository sonatypeRepository = new RemoteRepository();
    sonatypeRepository.setUrl("https://oss.sonatype.org/content/groups/public/");
    sonatypeRepository.setId("sonatype");
    dependenciesTask.addConfiguredRemoteRepository(sonatypeRepository);
    dependenciesTask.setProject(project);
    for (Dependency dependency : dependencies) {
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
  public URL getLocalArtifactUrl(Dependency dependency) {
    URL[] urls = getLocalArtifactUrls(dependency);
    if (urls.length > 0) {
      return urls[0];
    }
    return null;
  }

  private String key(Dependency dependency) {
    return dependency.getGroupId() + ":" + dependency.getArtifactId() + ":" + dependency.getType();
  }

  protected void configureMaven(DependenciesTask dependenciesTask) {
    // maybe you want to override this method and some settings?
  }
}
