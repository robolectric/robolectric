package org.robolectric.internal.dependency;

import android.os.Build;

import org.apache.maven.artifact.ant.Authentication;
import org.apache.maven.artifact.ant.DependenciesTask;
import org.apache.maven.artifact.ant.RemoteRepository;
import org.apache.maven.model.Dependency;
import org.apache.tools.ant.Project;
import org.robolectric.api.Sdk;
import org.robolectric.api.SdkProvider;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Hashtable;

public class MavenDependencyResolver implements SdkProvider {
  private static final MavenSdk[] AVAILABLE_SDKS = {
      new MavenSdk(Build.VERSION_CODES.JELLY_BEAN, "4.1.2_r1", "4.1.2_r1-robolectric-r1"),
      new MavenSdk(Build.VERSION_CODES.JELLY_BEAN_MR1, "4.2.2_r1.2", "4.2.2_r1.2-robolectric-r1"),
      new MavenSdk(Build.VERSION_CODES.JELLY_BEAN_MR2, "4.3_r2", "4.3_r2-robolectric-r1"),
      new MavenSdk(Build.VERSION_CODES.KITKAT, "4.4_r1", "4.4_r1-robolectric-r2"),
      new MavenSdk(Build.VERSION_CODES.LOLLIPOP, "5.0.2_r3", "5.0.2_r3-robolectric-r0"),
      new MavenSdk(Build.VERSION_CODES.LOLLIPOP_MR1, "5.1.1_r9", "5.1.1_r9-robolectric-r2"),
      new MavenSdk(Build.VERSION_CODES.M, "6.0.1_r3", "6.0.1_r3-robolectric-r1"),
      new MavenSdk(Build.VERSION_CODES.N, "7.0.0_r1", "7.0.0_r1-robolectric-r1"),
      new MavenSdk(Build.VERSION_CODES.N_MR1, "7.1.0_r7", "7.1.0_r7-robolectric-r1"),
      new MavenSdk(Build.VERSION_CODES.O, "8.0.0_r4", "8.0.0_r4-robolectric-r1"),
      new MavenSdk(Build.VERSION_CODES.O_MR1, "8.1.0", "8.1.0-robolectric-4611349"),
      new MavenSdk(Build.VERSION_CODES.P, "9", "9-robolectric-4913185-2"),
  };

  private final String repositoryUrl;
  private final String repositoryId;
  private final String repositoryUserName;
  private final String repositoryPassword;

  public MavenDependencyResolver() {
    this(
        System.getProperty("robolectric.dependency.repo.id", "sonatype"),
        System.getProperty("robolectric.dependency.repo.url", "https://oss.sonatype.org/content/groups/public/"),
        System.getProperty("robolectric.dependency.repo.username"),
        System.getProperty("robolectric.dependency.repo.password"));
  }

  public MavenDependencyResolver(String repositoryUrl, String repositoryId, String repositoryUserName, String repositoryPassword) {
    this.repositoryUrl = repositoryUrl;
    this.repositoryId = repositoryId;
    this.repositoryUserName = repositoryUserName;
    this.repositoryPassword = repositoryPassword;
  }

  @Override
  public Sdk[] availableSdks() {
    return AVAILABLE_SDKS;
  }

  @Override
  public URL getPathForSdk(Sdk sdk) {
    Dependency dependency = new Dependency();
    dependency.setGroupId("org.robolectric");
    dependency.setArtifactId("android-all");
    dependency.setVersion(((MavenSdk) sdk).artifactVersion);
    dependency.setType("jar");

    return getLocalArtifactUrl(dependency);
  }

  /**
   * Get an array of local artifact URLs for the given dependencies. The order of the URLs is guaranteed to be the
   * same as the input order of dependencies, i.e., urls[i] is the local artifact URL for dependencies[i].
   */
  private URL getLocalArtifactUrl(Dependency dependency) {
    DependenciesTask dependenciesTask = createDependenciesTask();
    RemoteRepository remoteRepository = new RemoteRepository();
    remoteRepository.setUrl(repositoryUrl);
    remoteRepository.setId(repositoryId);
    if (repositoryUserName != null || repositoryPassword != null) {
      Authentication authentication = new Authentication();
      authentication.setUserName(repositoryUserName);
      authentication.setPassword(repositoryPassword);
      remoteRepository.addAuthentication(authentication);
    }
    dependenciesTask.addConfiguredRemoteRepository(remoteRepository);
    final Project project = new Project();
    dependenciesTask.setProject(project);
    dependenciesTask.addDependency(dependency);
    configureMaven(dependenciesTask);
    dependenciesTask.execute();

    @SuppressWarnings("unchecked")
    Hashtable<String, String> artifacts = project.getProperties();
    try {
      return url(artifacts.get(key(dependency)));
    } catch (MalformedURLException e) {
      throw new RuntimeException(e);
    }
  }

  private String key(Dependency dependency) {
    String key = dependency.getGroupId() + ":" + dependency.getArtifactId() + ":" + dependency.getType();
    if (dependency.getClassifier() != null) {
      key += ":" + dependency.getClassifier();
    }
    return key;
  }

  /**
   * Override this if you like!
   */
  protected DependenciesTask createDependenciesTask() {
    return new DependenciesTask();
  }

  /**
   * Override this if you like!
   */
  protected void configureMaven(DependenciesTask dependenciesTask) {
    // maybe you want to override this method and some settings?
  }

  private static URL url(String path) throws MalformedURLException {
    //Starts with double backslash, is likely a UNC path
    if (path.startsWith("\\\\")) {
      path = path.replace("\\", "/");
    }
    return new URL("file:/" + (path.startsWith("/") ? "/" + path : path));
  }

  static class MavenSdk extends Sdk {
    private final String artifactVersion;

    public MavenSdk(int apiLevel, String androidVersion, String artifactVersion) {
      super(apiLevel);
      this.artifactVersion = artifactVersion;
    }
  }
}
