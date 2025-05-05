package org.robolectric.gradle

import java.net.URI
import org.gradle.api.publish.PublishingExtension

fun PublishingExtension.sonatypeRepositories(isSnapshotVersion: Boolean) {
  repositories {
    maven {
      name = "SonatypeOSS"
      val releasesRepoUrl = "https://oss.sonatype.org/service/local/staging/deploy/maven2/"
      val snapshotsRepoUrl = "https://oss.sonatype.org/content/repositories/snapshots/"

      url = if (isSnapshotVersion) URI(snapshotsRepoUrl) else URI(releasesRepoUrl)

      credentials {
        username = System.getProperty("sonatype-login", System.getenv("SONATYPE_LOGIN"))
        password = System.getProperty("sonatype-password", System.getenv("SONATYPE_PASSWORD"))
      }
    }
  }
}
