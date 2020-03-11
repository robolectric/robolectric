package org.robolectric.gradleplugin;

import static com.google.common.truth.Truth.assertThat;

import java.io.File;
import java.util.Map;
import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Before;
import org.junit.Test;

public class GradlePluginTest {

  private File gradleProjectDir;

  @Before
  public void setUp() throws Exception {
    gradleProjectDir = new File(getClass().getResource("/testProject").getFile());
  }

  @Test
  public void greeterPluginAddsGreetingTaskToProject() {
    Project project = ProjectBuilder.builder()
        .withProjectDir(gradleProjectDir).build();
    project.getPluginManager().apply("org.robolectric");

    assertThat(project.getTasks().getByName("robolectricDownloadAndroidSdks"))
        .isInstanceOf(DownloadAndroidSdks.class);
  }

  @Test
  public void loadingProject() {
    GradleRunner gradleRunner = GradleRunner.create()
        .withProjectDir(gradleProjectDir)
        .withPluginClasspath()
        .withArguments("--stacktrace", "clean", "test")
        .withEnvironment(Map.of("gradle-robolectric-plugin.classpath",
            System.getenv("gradle-robolectric-plugin.classpath")))
//        .withDebug(true)
        .forwardOutput();
    BuildResult buildResult = gradleRunner.buildAndFail();
    System.out.println("buildResult = " + buildResult.getOutput());
    System.out.println(buildResult.getTasks());
    assertThat(buildResult.getOutput()).contains("EverythingWorkedException");
//        def project = ProjectBuilder.builder().withProjectDir(gradleProjectDir).build()
//        project.exec()
  }
}
