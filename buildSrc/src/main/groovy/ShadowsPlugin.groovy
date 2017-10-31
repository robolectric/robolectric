import org.gradle.api.Plugin
import org.gradle.api.Project

import java.util.jar.JarFile

@SuppressWarnings("GroovyUnusedDeclaration")
class ShadowsPlugin implements Plugin<Project> {
    @Override
    void apply(Project project) {
        project.apply plugin: "idea"
        project.apply plugin: "net.ltgt.apt-idea"

        project.extensions.create("shadows", ShadowsPluginExtension)

        project.dependencies {
            apt project.project(":processor")
        }

        def compileJavaTask = project.tasks["compileJava"]
        compileJavaTask.doFirst {
            options.compilerArgs.add("-Aorg.robolectric.annotation.processing.shadowPackage=${project.shadows.packageName}")
        }

        def aptGeneratedSrcDir = new File(project.buildDir, 'generated/source/apt/main')

        project.sourceSets {
            generated {
                java {
                    srcDirs = [aptGeneratedSrcDir]
                }
            }
        }

        project.task('checkAssembly', description: "Check that jars have required contents") {
            def jars = [:]
            project.configurations.archives.artifacts.each {
                if (it.extension == 'jar') {
                    jars["${it.classifier}.${it.extension}"] = it.file
                }
            }

            project.tasks['checkAssembly'].doLast {
                def shadowPackageNameDir = project.shadows.packageName.replaceAll(/\./, '/')
                checkForFile(jars['javadoc.jar'], "${shadowPackageNameDir}/Shadows.html")
                checkForFile(jars['sources.jar'], "${shadowPackageNameDir}/Shadows.java")
            }
        }

        project.tasks['assemble'].finalizedBy 'checkAssembly'

        project.idea {
            module {
                apt {
                    // whether generated sources dirs are added as generated sources root
                    addGeneratedSourcesDirs = true
                    // whether the apt and testApt dependencies are added as module dependencies
                    addAptDependencies = true

                    // The following are mostly internal details; you shouldn't ever need to configure them.
                    // whether the compileOnly and testCompileOnly dependencies are added as module dependencies
                    addCompileOnlyDependencies = false // defaults to true in Gradle < 2.12
                    // the dependency scope used for apt and/or compileOnly dependencies (when enabled above)
                    mainDependenciesScope = "PROVIDED" // defaults to "COMPILE" in Gradle < 3.4, or when using the Gradle integration in IntelliJ IDEA
                }
            }

        }
    }

    static class ShadowsPluginExtension {
        String packageName
    }

    private void checkForFile(jar, String name) {
        def files = new JarFile(jar).entries().collect { it.name }.toSet()

        if (!files.contains(name)) {
            throw new RuntimeException("Missing file ${name} in ${jar}")
        }
    }
}
