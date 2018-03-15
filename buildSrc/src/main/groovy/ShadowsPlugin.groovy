import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.compile.JavaCompile

import java.util.jar.JarFile

@SuppressWarnings("GroovyUnusedDeclaration")
class ShadowsPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.apply plugin: "net.ltgt.apt"
        project.apply plugin: 'idea'

        project.extensions.create("shadows", ShadowsPluginExtension)

        project.dependencies {
            apt project.project(":processor")
        }

        def compileJavaTask = project.tasks["compileJava"]
        compileJavaTask.doFirst {
            options.compilerArgs.add("-Aorg.robolectric.annotation.processing.shadowPackage=${project.shadows.packageName}")
        }

        // this doesn't seem to have any effect in IDEA yet, unfortunately...
        def aptGeneratedSrcDir = new File(project.buildDir, 'generated/source/apt/main')
        project.idea.module.generatedSourceDirs << aptGeneratedSrcDir

        // include generated sources in javadoc and source jars
        project.tasks['javadoc'].source(aptGeneratedSrcDir)
        project.tasks['sourcesJar'].from(project.fileTree(aptGeneratedSrcDir))

        // verify that we have the apt-generated files in our javadoc and sources jars
        project.tasks['javadocJar'].doLast { task ->
            def shadowPackageNameDir = project.shadows.packageName.replaceAll(/\./, '/')
            checkForFile(task.archivePath, "${shadowPackageNameDir}/Shadows.html")
        }

        project.tasks['sourcesJar'].doLast { task ->
            def shadowPackageNameDir = project.shadows.packageName.replaceAll(/\./, '/')
            checkForFile(task.archivePath, "${shadowPackageNameDir}/Shadows.java")
        }

        project.rootProject.configAnnotationProcessing += project

        /* Prevents sporadic compilation error:
         * 'Bad service configuration file, or exception thrown while constructing
         *  Processor object: javax.annotation.processing.Processor: Error reading
         *  configuration file'
         *
         * See https://discuss.gradle.org/t/gradle-not-compiles-with-solder-tooling-jar/7583/20
         */
        project.tasks.withType(JavaCompile) { options.fork = true }
}

    static class ShadowsPluginExtension {
        String packageName
    }

    private static void checkForFile(jar, String name) {
        def files = new JarFile(jar).entries().collect { it.name }.toSet()

        if (!files.contains(name)) {
            throw new RuntimeException("Missing file ${name} in ${jar}")
        }
    }
}
