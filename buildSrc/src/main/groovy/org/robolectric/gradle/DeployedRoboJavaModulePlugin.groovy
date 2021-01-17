package org.robolectric.gradle

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication
import org.gradle.api.tasks.bundling.Jar

class DeployedRoboJavaModulePlugin implements Plugin<Project> {
    Closure doApply = {
        project.apply plugin: "signing"
        project.apply plugin: "maven-publish"

        task('sourcesJar', type: Jar, dependsOn: classes) {
            archiveClassifier = "sources"
            from sourceSets.main.allJava
        }

        javadoc {
            failOnError = false
            source = sourceSets.main.allJava
            options.noTimestamp = true
            options.header = "<ul class=\"navList\"><li>Robolectric $thisVersion | <a href=\"/\">Home</a></li></ul>"
            options.footer = "<ul class=\"navList\"><li>Robolectric $thisVersion | <a href=\"/\">Home</a></li></ul>"
            options.bottom = "<link rel=\"stylesheet\" href=\"https://robolectric.org/assets/css/main.css\">"
            options.version = thisVersion
        }

        task('javadocJar', type: Jar, dependsOn: javadoc) {
            archiveClassifier = "javadoc"
            from javadoc.destinationDir
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
                                name = "Michael Hoisie"
                                email = "hoisie@google.com"
                                organization = "Google Inc."
                                organizationUrl = "http://google.com"
                            }

                            developer {
                                name = "Christian Williams"
                                email = "antixian666@gmail.com"
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
                }
            }
        }

        signing {
            required { !version.endsWith("SNAPSHOT") && gradle.taskGraph.hasTask("uploadArchives") }
            sign publishing.publications.mavenJava
        }
    }

    @Override
    void apply(Project project) {
        doApply.delegate = project
        doApply.resolveStrategy = Closure.DELEGATE_ONLY
        doApply()
    }
}
