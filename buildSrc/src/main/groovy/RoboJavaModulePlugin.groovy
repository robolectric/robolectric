import org.gradle.api.JavaVersion
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar
import org.gradle.api.tasks.compile.JavaCompile
import org.robolectric.gradle.AarDepsPlugin

class RoboJavaModulePlugin implements Plugin<Project> {
    Boolean deploy = false;

    Closure doApply = {
        project.apply plugin: "java-library"
        project.apply plugin: "net.ltgt.errorprone"

        project.apply plugin: AarDepsPlugin

        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8

        project.dependencies {
            errorprone("com.google.errorprone:error_prone_core:$errorproneVersion")
            errorproneJavac("com.google.errorprone:javac:$errorproneJavacVersion")
        }

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
            }

            def noRebuild = System.getenv('NO_REBUILD') == "true"
            if (noRebuild) {
                println "[NO_REBUILD] $task will be skipped!"
                task.outputs.upToDateWhen { true }
                task.onlyIf { false }
            }
        }

        ext.mavenArtifactName = project.path.substring(1).split(/:/).join("-")

        task('provideBuildClasspath', type: ProvideBuildClasspathTask) {
            File outDir = project.sourceSets['test'].output.resourcesDir
            outFile = new File(outDir, 'robolectric-deps.properties')
        }

        tasks['test'].dependsOn provideBuildClasspath

        test {
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
                maxParallelForks = Integer.parseInt(System.env['GRADLE_MAX_PARALLEL_FORKS'] as String)
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
            project.apply plugin: "maven-publish"
            project.apply plugin: 'ch.raffael.pegdown-doclet'

            task('sourcesJar', type: Jar) {
                from sourceSets.main.allJava
                archiveClassifier = "sources"
            }

            javadoc {
                failOnError = false
                source = sourceSets.main.allJava
                options.noTimestamp = true
                options.header = "<ul class=\"navList\"><li>Robolectric $thisVersion | <a href=\"/\">Home</a></li></ul>"
                options.footer = "<ul class=\"navList\"><li>Robolectric $thisVersion | <a href=\"/\">Home</a></li></ul>"
                options.bottom = "<link rel=\"stylesheet\" href=\"https://robolectric.org/assets/css/main.css\">"
                options.version = thisVersion

                if(JavaVersion.current().isJava9Compatible()) {
                    options.addBooleanOption('html5', true)
                }
            }

            task('javadocJar', type: Jar, dependsOn: javadoc) {
                from javadoc.destinationDir
                archiveClassifier = "javadoc"
            }

            // for maven local install:
            archivesBaseName = mavenArtifactName

            publishing {
                publications {
                    mavenJava(MavenPublication) {
                        from components.java

                        def skipJavadoc = System.getenv('SKIP_JAVADOC') == "true"
                        artifact sourcesJar
                        if (!skipJavadoc) {
                            artifact javadocJar
                        }

                        artifactId = mavenArtifactName
                        pom {
                            name = project.name
                            description = "An alternative Android testing framework."
                            url = "http://robolectric.org"

                            licenses {
                                license {
                                    name = "The MIT License"
                                    url = "https://opensource.org/licenses/MIT"
                                }
                            }

                            scm {
                                url = "git@github.com:robolectric/robolectric.git"
                                connection = "scm:git:git://github.com/robolectric/robolectric.git"
                                developerConnection = "scm:git:https://github.com/robolectric/robolectric.git"
                            }

                            developers {
                                developer {
                                    name = "Brett Chabot"
                                    email = "brettchabot@google.com"
                                    organization = "Google Inc."
                                    organizationUrl = "http://google.com"
                                }

                                developer {
                                    name = "Jonathan Gerrish"
                                    email = "jongerrish@google.com"
                                    organization = "Google Inc."
                                    organizationUrl = "http://google.com"
                                }

                                developer {
                                    name = "Christian Williams"
                                    email = "christianw@google.com"
                                    organization = "Google Inc."
                                    organizationUrl = "http://google.com"
                                }
                            }
                        }
                    }
                }

                repositories {
                    maven {
                        def releasesRepoUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
                        def snapshotsRepoUrl = "https://oss.sonatype.org/content/repositories/snapshots"
                        url = project.version.endsWith("-SNAPSHOT") ? snapshotsRepoUrl : releasesRepoUrl

                        credentials {
                            username = System.properties["sonatype-login"] ?: System.env['sonatypeLogin']
                            password = System.properties["sonatype-password"] ?: System.env['sonatypePassword']
                        }

//                        beforeDeployment { MavenDeployment deployment -> signing.signPom(deployment) }
                    }
                }
            }

            signing {
                required { !version.endsWith("SNAPSHOT") && gradle.taskGraph.hasTask("uploadArchives") }
                sign publishing.publications.mavenJava
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