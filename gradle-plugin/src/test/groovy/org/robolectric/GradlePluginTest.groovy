package org.robolectric;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.testkit.runner.GradleRunner;
import org.junit.Test;

import static com.google.common.truth.Truth.assertThat;

class GradlePluginTest {
    @Test
    public void greeterPluginAddsGreetingTaskToProject() {
        Project project = ProjectBuilder.builder().build();
        project.getPluginManager().apply("org.robolectric");

        assertThat(project.tasks.hello).isInstanceOf(Task.class);
    }
    
    @Test
    public void loadingProject() {
        def gradleProjectDir = new File(getClass().getResource("/gradle-project").getFile())
        def gradleRunner = GradleRunner.create()
                .withPluginClasspath()
                .withGradleVersion("3.1")
                .withProjectDir(gradleProjectDir)
                .withArguments("--stacktrace", "clean", "test")
        gradleRunner.debug = true
        gradleRunner.forwardOutput()
        gradleRunner.build()
//        def project = ProjectBuilder.builder().withProjectDir(gradleProjectDir).build()
//        project.exec()
    }
}
