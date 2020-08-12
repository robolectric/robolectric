package org.robolectric.gradleplugin;

import static com.google.common.truth.Truth.assertThat;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.DependencySet;
import org.gradle.api.internal.project.DefaultProject;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

public class GradlePluginTest {

  private File gradleProjectDir;

  @Before
  public void setUp() throws Exception {
    gradleProjectDir = Helpers.findTestProject();
  }

  @Test
  public void pluginAddsTaskToDownloadAndroidSdks() {
    Project project = ProjectBuilder.builder()
        .withProjectDir(gradleProjectDir).build();
    project.getPluginManager().apply("org.robolectric");

    assertThat(project.getTasks().getByName("robolectricDownloadAndroidSdks"))
        .isInstanceOf(DownloadAndroidSdks.class);
  }

  @Ignore("evaluating project doesn't work") @Test
  public void pluginAddsRobolectricToTestImplementationDependencies() {
    Project project = ProjectBuilder.builder()
        .withProjectDir(gradleProjectDir).build();
    project.getPluginManager().apply("org.robolectric");
    ((DefaultProject) project).evaluate();

    DependencySet testIntegrationDeps =
        project.getConfigurations().getByName("testIntegration").getDependencies();

    List<Dependency> deps = new ArrayList<>();
    testIntegrationDeps.forEach(dependency -> {
          if ("org.robolectric".equals(dependency.getGroup())) {
            deps.add(dependency);
          }
        }
    );
    System.out.println("deps = " + deps);
  }

  static <K, V> Map<K, V> mapOf(K k1, V v1) {
    HashMap<K, V> map = new HashMap<>();
    map.put(k1, v1);
    return map;
  }
  
  static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2) {
    HashMap<K, V> map = new HashMap<>();
    map.put(k1, v1);
    map.put(k2, v2);
    return map;
  }

  static <K, V> Map<K, V> mapOf(K k1, V v1, K k2, V v2, K k3, V v3) {
    HashMap<K, V> map = new HashMap<>();
    map.put(k1, v1);
    map.put(k2, v2);
    map.put(k3, v3);
    return map;
  }
}
