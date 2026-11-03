// A small Android project that tests a Robolectric release before it is published. The release
// workflow runs it with `./gradlew -p .github/release-check test -ProbolectricVersion=<version>`.
// With SONATYPE_LOGIN and SONATYPE_PASSWORD set, Robolectric is resolved from the deployments
// staged on the Central Portal, otherwise from Maven Central.

import java.util.Base64

pluginManagement {
  repositories {
    google()
    mavenCentral()
    gradlePluginPortal()
  }
}

dependencyResolutionManagement {
  repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
  repositories {
    val login = System.getenv("SONATYPE_LOGIN")
    val password = System.getenv("SONATYPE_PASSWORD")
    if (!login.isNullOrEmpty() && !password.isNullOrEmpty()) {
      maven {
        name = "CentralPortalStaging"
        url = uri("https://central.sonatype.com/api/v1/publisher/deployments/download/")
        credentials(HttpHeaderCredentials::class) {
          name = "Authorization"
          value = "Basic " + Base64.getEncoder().encodeToString("$login:$password".toByteArray())
        }
        authentication { create<HttpHeaderAuthentication>("header") }
        content { includeGroup("org.robolectric") }
      }
    }
    google()
    mavenCentral()
  }

  // Use the same versions as Robolectric's own build.
  versionCatalogs { create("libs") { from(files("../../gradle/libs.versions.toml")) } }
}

rootProject.name = "release-check"
