package org.robolectric;

import org.apache.maven.artifact.ant.DependenciesTask;
import org.apache.maven.artifact.ant.RemoteRepository;
import org.apache.maven.model.Dependency;
import org.apache.tools.ant.Project;
import org.robolectric.util.Util;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

public class MavenCentral {
  private final Project project = new Project();

  public Map<String, URL> getLocalArtifactUrls(RobolectricTestRunner robolectricTestRunner, Dependency... dependencies) {
    DependenciesTask dependenciesTask = new DependenciesTask();
    if (robolectricTestRunner != null) {
      robolectricTestRunner.configureMaven(dependenciesTask);
    }
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
    Map<String, URL> urls = new HashMap<String, URL>();
    for (Map.Entry<String, String> entry : artifacts.entrySet()) {
      try {
        urls.put(entry.getKey(), Util.url(entry.getValue()));
      } catch (MalformedURLException e) {
        throw new RuntimeException(e);
      }

    }
    return urls;
  }

  public URL getLocalArtifactUrl(RobolectricTestRunner robolectricTestRunner, Dependency dependency) {
    Map<String, URL> map = getLocalArtifactUrls(robolectricTestRunner, dependency);
    return map.get(dependency.getGroupId() + ":" + dependency.getArtifactId() + ":" + dependency.getType());
  }
}
