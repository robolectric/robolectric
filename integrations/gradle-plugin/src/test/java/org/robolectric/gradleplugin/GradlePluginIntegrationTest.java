package org.robolectric.gradleplugin;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.gradleplugin.GradlePluginTest.mapOf;

import java.io.File;
import java.util.Map;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Before;
import org.junit.Test;

public class GradlePluginIntegrationTest {

  private File gradleProjectDir;
  private String androidSdkRoot;

  @Before
  public void setUp() throws Exception {
    gradleProjectDir = new File(getClass().getResource("/testProject").getFile());
    androidSdkRoot = Helpers.findAndroidSdkRoot();
  }

  @Test
  public void loadingProject() {
    Map<String, String> env = mapOf(
        "gradle-robolectric-plugin.classpath", System.getenv("gradle-robolectric-plugin.classpath"),
        "gradle-robolectric-plugin.sdkPath", System.getenv("gradle-robolectric-plugin.sdkPath"),
        "ANDROID_SDK_ROOT", androidSdkRoot
    );
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
