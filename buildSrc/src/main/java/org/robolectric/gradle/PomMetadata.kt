package org.robolectric.gradle

import org.gradle.api.Project
import org.gradle.api.publish.maven.MavenPublication

fun MavenPublication.applyPomMetadata(project: Project) {
  pom {
    name.set(project.name)
    description.set("An alternative Android testing framework.")
    url.set("http://robolectric.org")

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
        organizationUrl.set("http://google.com")
      }

      developer {
        name.set("Michael Hoisie")
        email.set("hoisie@google.com")
        organization.set("Google Inc.")
        organizationUrl.set("http://google.com")
      }

      developer {
        name.set("Christian Williams")
        email.set("antixian666@gmail.com")
      }
    }
  }
}
