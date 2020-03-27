package org.robolectric.gradleplugin;

import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.util.HashMap;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.gradleplugin.GradlePluginTest.mapOf;

public class GradlePluginIntegrationTest {

  private File gradleProjectDir;

  @Before
  public void setUp() throws Exception {
    gradleProjectDir = new File(getClass().getResource("/testProject").getFile());
  }

  @Test
  public void loadingProject() {
    HashMap<String, String> env = new HashMap<>(System.getenv());
    env.putAll(mapOf(
            "gradle-robolectric-plugin.classpath", System.getenv("gradle-robolectric-plugin.classpath"),
            "gradle-robolectric-plugin.sdkPath", System.getenv("gradle-robolectric-plugin.sdkPath")
    ));
    System.out.println("env = " + env);
    GradleRunner gradleRunner = GradleRunner.create()
        .withProjectDir(gradleProjectDir)
        .withPluginClasspath()
        .withArguments("--stacktrace", "-i", "clean", "test")
        .withEnvironment(env)
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
