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
        sourceCompatibility = JavaVersion.VERSION_1_7
        targetCompatibility = JavaVersion.VERSION_1_7

        tasks.withType(JavaCompile) {
            sourceCompatibility = JavaVersion.VERSION_1_7
            targetCompatibility = JavaVersion.VERSION_1_7

            // Show all warnings except boot classpath
            configure(options) {
                if (System.properties["lint"] != null && System.properties["lint"] != "false") {
                    compilerArgs << "-Xlint:all"        // Turn on all warnings
                }
                compilerArgs << "-Xlint:-options"       // Turn off "missing" bootclasspath warning
                encoding = "utf-8"                      // Make sure source encoding is UTF-8
                incremental = true
            }
        }

        // it's weird that compileOnly deps aren't included for test compilation; fix that:
        project.sourceSets {
            test.compileClasspath += project.configurations.compileOnly
        }

        def mavenArtifactName = {
            def projNameParts = project.name.split(/\//) as List
            if (projNameParts[0] == "robolectric-shadows") {
                projNameParts = projNameParts.drop(1)
                return projNameParts.join("-")
            } else {
                return project.name
            }
        }()
        ext.mavenArtifactName = mavenArtifactName

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

            minHeapSize = "2048m"
            maxHeapSize = "4096m"

            def forwardedSystemProperties = System.properties
                    .findAll { k,v -> k.startsWith("robolectric.") }
                    .collect { k,v -> "-D$k=$v" }
            jvmArgs = ["-XX:MaxPermSize=1024m"] + forwardedSystemProperties

            doFirst {
                if (!forwardedSystemProperties.isEmpty()) {
                    println "Running tests with ${forwardedSystemProperties}"
                }
            }
        }

        if (owner.deploy) {
            project.apply plugin: "signing"
            project.apply plugin: "maven"

            task('sourcesJar', type: Jar, dependsOn: classes) {
                classifier "sources"
                from sourceSets.main.allJava
            }

            javadoc.failOnError = false

            task('javadocJar', type: Jar, dependsOn: javadoc) {
                classifier "javadoc"
                from javadoc.destinationDir
            }

            signing {
                required { !version.endsWith("SNAPSHOT") && gradle.taskGraph.hasTask("uploadArchives") }
                sign configurations.archives
            }

            def skipJavadoc = System.getenv()["SKIP_JAVADOC"] == "true"
            artifacts {
                archives jar
                archives sourcesJar
                if (!skipJavadoc) {
                    archives javadocJar
                }
            }

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
                                    organization {
                                        name "Google Inc."
                                    }
                                    organizationUrl "http://google.com"
                                }

                                developer {
                                    name "Jonathan Gerrish"
                                    email "jongerrish@google.com"
                                    organization {
                                        name "Google Inc."
                                    }
                                    organizationUrl "http://google.com"
                                }
                            }
                        }

                        def url = project.version.endsWith("-SNAPSHOT") ?
                                "https://oss.sonatype.org/content/repositories/snapshots" :
                                "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
                        repository(url: url) {
                            authentication(
                                    userName: System.properties["sonatype-login"],
                                    password: System.properties["sonatype-password"]
                            )
                        }

                        beforeDeployment { MavenDeployment deployment -> signing.signPom(deployment) }
                    }
                }
            }


            project.apply plugin: CheckApiChangesPlugin

            checkApiChanges {
                baseArtifact "${project.group}:${mavenArtifactName}:${apiCompatVersion}"
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