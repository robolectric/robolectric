import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.util.GFileUtils

class ShadowsPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.apply plugin: "org.inferred.processors"

        project.extensions.create("shadows", ShadowsPluginExtension)

        project.dependencies {
            processor project.project(":robolectric-processor")
        }

        def generatedSourcesDir = "${project.buildDir}/generated-shadows"

        project.sourceSets.main.java.srcDirs += project.file(generatedSourcesDir)

        def compileJava = project.tasks["compileJava"]
        project.afterEvaluate {
            compileJava.options.compilerArgs.addAll(
                    "-Aorg.robolectric.annotation.processing.shadowPackage=${project.shadows.packageName}"
            )
        }

        compileJava.doFirst {
            logger.info "Generating Shadows.java for ${project.name}…"
        }
    }
}

class ShadowsPluginExtension {
    String packageName
}
