package org.robolectric.gradle

import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication

fun MavenPublication.applyPomMetadata(project: Project) {
  pom {
    name.set(project.name)
    description.set("An alternative Android testing framework.")
    url.set("https://robolectric.org")

    licenses {
      license {
        name.set("The MIT License")
        url.set("https://opensource.org/licenses/MIT")
      }
    }

    scm {
      url.set("https://github.com/robolectric/robolectric")
      connection.set("scm:git:git://github.com/robolectric/robolectric.git")
      developerConnection.set("scm:git:git@github.com:robolectric/robolectric.git")
    }

    developers {
      developer {
        name.set("Brett Chabot")
        email.set("brettchabot@google.com")
        organization.set("Google Inc.")
        organizationUrl.set("https://google.com")
      }

      developer {
        name.set("Michael Hoisie")
        email.set("hoisie@google.com")
        organization.set("Google Inc.")
        organizationUrl.set("https://google.com")
      }

      developer {
        name.set("Christian Williams")
        email.set("antixian666@gmail.com")
      }
    }

    issueManagement {
      system.set("GitHub Issues")
      url.set("https://github.com/robolectric/robolectric/issues")
    }

    ciManagement {
      system.set("GitHub Actions")
      url.set("https://github.com/robolectric/robolectric/actions")
    }

    // TODO Simplify this once https://github.com/gradle/gradle/issues/28759 is released
    withXml {
      asNode().appendNode("distributionManagement").apply {
        appendNode("repository").apply {
          appendNode("id", "sonatype-nexus")
          appendNode("name", "Sonatype Nexus")
          appendNode("url", "https://oss.sonatype.org/service/local/staging/deploy/maven2/")
        }

        appendNode("snapshotRepository").apply {
          appendNode("id", "sonatype-nexus-snapshots")
          appendNode("name", "Sonatype Nexus Snapshots")
          appendNode("url", "https://oss.sonatype.org/content/repositories/snapshots/")
        }
      }
    }
  }
}
