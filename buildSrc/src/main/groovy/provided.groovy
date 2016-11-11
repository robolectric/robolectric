import org.gradle.api.Plugin
import org.gradle.api.Project

class ProvidedPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.configurations {
            provided
        }

        project.sourceSets {
            main.compileClasspath += project.configurations.provided
            test.compileClasspath += project.configurations.provided
            test.runtimeClasspath += project.configurations.provided
        }

    }
}