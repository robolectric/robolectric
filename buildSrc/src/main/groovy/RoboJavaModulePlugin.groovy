import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.maven.MavenDeployment
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile

class RoboJavaModulePlugin implements Plugin<Project> {
    Boolean deploy = false;

    Closure doApply = {
        apply plugin: "java"
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8

        tasks.withType(JavaCompile) { task ->
            sourceCompatibility = JavaVersion.VERSION_1_8
            targetCompatibility = JavaVersion.VERSION_1_8

            // Show all warnings except boot classpath
            configure(options) {
                if (System.properties["lint"] != null && System.properties["lint"] != "false") {
                    compilerArgs << "-Xlint:all"        // Turn on all warnings
                }
                compilerArgs << "-Xlint:-options"       // Turn off "missing" bootclasspath warning
                encoding = "utf-8"                      // Make sure source encoding is UTF-8
                incremental = true
            }

            def noRebuild = System.getenv('NO_REBUILD') == "true"
            if (noRebuild) {
                println "[NO_REBUILD] $task will be skipped!"
                task.outputs.upToDateWhen { true }
                task.onlyIf { false }
            }
        }

        // it's weird that compileOnly deps aren't included for test compilation; fix that:
        project.sourceSets {
            test.compileClasspath += project.configurations.compileOnly
        }

        ext.mavenArtifactName = project.path.substring(1).split(/:/).join("-")

        task('provideBuildClasspath', type: ProvideBuildClasspathTask) {
            File outDir = project.sourceSets['test'].output.resourcesDir
            outFile = new File(outDir, 'robolectric-deps.properties')
        }

        test {
            dependsOn provideBuildClasspath

            exclude "**/*\$*" // otherwise gradle runs static inner classes like TestRunnerSequenceTest$SimpleTest
            testLogging {
                exceptionFormat "full"
                showCauses true
                showExceptions true
                showStackTraces true
                showStandardStreams true
                events = ["failed", "skipped"]
            }

            minHeapSize = "1024m"
            maxHeapSize = "4096m"

            if (System.env['GRADLE_MAX_PARALLEL_FORKS'] != null) {
                maxParallelForks = Integer.parseInt(System.env['GRADLE_MAX_PARALLEL_FORKS'])
            }

            def forwardedSystemProperties = System.properties
                    .findAll { k,v -> k.startsWith("robolectric.") }
                    .collect { k,v -> "-D$k=$v" }
            jvmArgs = ["-XX:MaxPermSize=1024m"] + forwardedSystemProperties

            doFirst {
                if (!forwardedSystemProperties.isEmpty()) {
                    println "Running tests with ${forwardedSystemProperties}"
                }
            }

            rootProject.tasks['aggregateTestReports'].reportOn binResultsDir
            finalizedBy ':aggregateTestReports'
        }

        if (owner.deploy) {
            project.apply plugin: "signing"
            project.apply plugin: "maven"
            project.apply plugin: 'ch.raffael.pegdown-doclet'

            task('sourcesJar', type: Jar, dependsOn: classes) {
                classifier "sources"
                from sourceSets.main.allJava
            }

            javadoc {
                failOnError = false
                source = sourceSets.main.allJava
            }

            task('javadocJar', type: Jar, dependsOn: javadoc) {
                classifier "javadoc"
                from javadoc.destinationDir
            }

            signing {
                required { !version.endsWith("SNAPSHOT") && gradle.taskGraph.hasTask("uploadArchives") }
                sign configurations.archives
            }

            def skipJavadoc = System.getenv('SKIP_JAVADOC') == "true"
            artifacts {
                archives jar
                archives sourcesJar
                if (!skipJavadoc) {
                    archives javadocJar
                }
            }

            // for maven local install:
            archivesBaseName = mavenArtifactName

            uploadArchives {
                repositories {
                    mavenDeployer {
                        pom.artifactId = mavenArtifactName
                        pom.project {
                            name project.name
                            description = "An alternative Android testing framework."
                            url = "http://robolectric.org"

                            licenses {
                                license {
                                    name "The MIT License"
                                    url "https://opensource.org/licenses/MIT"
                                }
                            }

                            scm {
                                url "git@github.com:robolectric/robolectric.git"
                                connection "scm:git:git://github.com/robolectric/robolectric.git"
                                developerConnection "scm:git:https://github.com/robolectric/robolectric.git"
                            }

                            developers {
                                developer {
                                    name "Christian Williams"
                                    email "christianw@google.com"
                                    organization = "Google Inc."
                                    organizationUrl "http://google.com"
                                }

                                developer {
                                    name "Jonathan Gerrish"
                                    email "jongerrish@google.com"
                                    organization = "Google Inc."
                                    organizationUrl "http://google.com"
                                }
                            }
                        }

                        def url = project.version.endsWith("-SNAPSHOT") ?
                                "https://oss.sonatype.org/content/repositories/snapshots" :
                                "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
                        repository(url: url) {
                            authentication(
                                    userName: System.properties["sonatype-login"] ?: System.env['sonatypeLogin'],
                                    password: System.properties["sonatype-password"] ?: System.env['sonatypePassword']
                            )
                        }

                        beforeDeployment { MavenDeployment deployment -> signing.signPom(deployment) }
                    }
                }
            }
        }
    }

    @Override
    void apply(Project project) {
        doApply.delegate = project
        doApply.resolveStrategy = Closure.DELEGATE_ONLY
        doApply()
    }
}