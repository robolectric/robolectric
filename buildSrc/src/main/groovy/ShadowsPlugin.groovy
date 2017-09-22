import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.util.GFileUtils

@SuppressWarnings("GroovyUnusedDeclaration")
class ShadowsPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.extensions.create("shadows", ShadowsPluginExtension)

        project.configurations {
            robolectricProcessor
        }

        project.dependencies {
            robolectricProcessor project.project(":processor")
        }

        def generatedSourcesDir = "${project.buildDir}/generated-shadows"

        project.sourceSets.main.java.srcDirs += project.file(generatedSourcesDir)

        project.task("generateShadowProvider", type: JavaCompile, description: "Generate Shadows.shadowOf()s class") { task ->
            classpath = project.configurations.robolectricProcessor
            source = project.sourceSets.main.java
            destinationDir = project.file(generatedSourcesDir)

            doFirst {
                logger.info "Generating Shadows.java for ${project.name}…"

                // reset our classpath at the last minute, since other plugins might mutate
                //   compileJava's classpath and we want to pick up any changes…
                classpath = project.tasks['compileJava'].classpath + project.configurations.robolectricProcessor

                options.compilerArgs.addAll(
                        "-proc:only",
                        "-processor", "org.robolectric.annotation.processing.RobolectricProcessor",
                        "-Aorg.robolectric.annotation.processing.shadowPackage=${project.shadows.packageName}"
                )
            }

            doLast {
                def src = project.file("$generatedSourcesDir/META-INF/services/org.robolectric.internal.ShadowProvider")
                def dest = project.file("${project.buildDir}/resources/main/META-INF/services/org.robolectric.internal.ShadowProvider")

                GFileUtils.mkdirs(dest.getParentFile());
                GFileUtils.copyFile(src, dest);
            }
        }

        def compileJavaTask = project.tasks["compileJava"]
        compileJavaTask.dependsOn("generateShadowProvider")
    }

    static class ShadowsPluginExtension {
        String packageName
    }
}
