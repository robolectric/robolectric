pluginManagement {
  repositories {
    gradlePluginPortal()
    google()
    mavenCentral()
  }
}

dependencyResolutionManagement {
  repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS

  repositories {
    google()
    mavenCentral()
  }

  versionCatalogs { create("libs") { from(files("../gradle/libs.versions.toml")) } }
}
