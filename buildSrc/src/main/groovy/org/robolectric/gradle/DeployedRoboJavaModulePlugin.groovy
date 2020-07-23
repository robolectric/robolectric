package org.robolectric.gradle


import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.maven.MavenDeployment
import org.gradle.api.tasks.bundling.Jar

class DeployedRoboJavaModulePlugin implements Plugin<Project> {
    Closure doApply = {
        project.apply plugin: "signing"
        project.apply plugin: "maven"

        task('sourcesJar', type: Jar, dependsOn: classes) {
            classifier "sources"
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
            classifier "javadoc"
            from javadoc.destinationDir
        }

        signing {
            required { !version.endsWith("SNAPSHOT") && gradle.taskGraph.hasTask("uploadArchives") }
            sign configurations.archives
        }

        def skipJavadoc = System.getenv('SKIP_JAVADOC') == "true"
        artifacts {
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

    @Override
    void apply(Project project) {
        doApply.delegate = project
        doApply.resolveStrategy = Closure.DELEGATE_ONLY
        doApply()
    }
}
