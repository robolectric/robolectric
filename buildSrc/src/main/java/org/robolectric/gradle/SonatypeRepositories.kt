package org.robolectric.gradle

import java.net.URI
import org.gradle.api.publish.PublishingExtension

const val PUBLISH_URL =
  "https://ossrh-staging-api.central.sonatype.com/service/local/staging/deploy/maven2/"
const val PUBLISH_SNAPSHOTS_URL = "https://central.sonatype.com/repository/maven-snapshots/"

fun PublishingExtension.sonatypeRepositories(isSnapshotVersion: Boolean) {
  repositories {
    maven {
      name = "CentralPortal"
      val releasesRepoUrl = PUBLISH_URL
      val snapshotsRepoUrl = PUBLISH_SNAPSHOTS_URL

      url = if (isSnapshotVersion) URI(snapshotsRepoUrl) else URI(releasesRepoUrl)

      credentials {
        username = System.getProperty("sonatype-login", System.getenv("SONATYPE_LOGIN"))
        password = System.getProperty("sonatype-password", System.getenv("SONATYPE_PASSWORD"))
      }
    }
  }
}
