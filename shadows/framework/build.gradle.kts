import org.robolectric.gradle.AndroidSdk

plugins {
  alias(libs.plugins.robolectric.deployed.java.module)
  alias(libs.plugins.robolectric.java.module)
  alias(libs.plugins.robolectric.shadows)
}

shadows {
  packageName = "org.robolectric"
  sdkCheckMode = "ERROR"
}

val sqlite4java = configurations.register("sqlite4java")
val sqlite4javaVersion = libs.versions.sqlite4java

val copySqliteNatives =
  tasks.register<Copy>("copySqliteNatives") {
    from(sqlite4java) {
      include("**/*.dll")
      include("**/*.so")
      include("**/*.dylib")

      val sqliteVersion = sqlite4javaVersion.get()
      rename { filename ->
        val filenameMatch = "^([^\\-]+)-(.+)-$sqliteVersion\\.(.+)".toRegex().find(filename)
        if (filenameMatch != null) {
          val platformFilename = filenameMatch.groupValues[1]
          val platformFolder = filenameMatch.groupValues[2]
          val platformExtension = filenameMatch.groupValues[3]

          "$platformFolder/$platformFilename.$platformExtension"
        } else {
          filename
        }
      }
    }
    into(project.layout.buildDirectory.dir("resources/main/sqlite4java"))
  }

tasks.jar.configure { dependsOn(copySqliteNatives) }

tasks.javadoc.configure { dependsOn(copySqliteNatives) }

dependencies {
  api(project(":annotations"))
  api(project(":nativeruntime"))
  api(project(":resources"))
  api(project(":sandbox"))
  api(project(":shadowapi"))
  api(project(":utils"))
  api(project(":utils:reflector"))

  api(variantOf(libs.androidx.test.monitor) { artifactType("aar") })
  compileOnly(libs.findbugs.jsr305)
  api(libs.sqlite4java)
  compileOnly(AndroidSdk.MAX_SDK.coordinates)
  api(libs.icu4j)
  api(libs.auto.value.annotations)
  compileOnly(libs.auto.service.annotations)
  annotationProcessor(libs.auto.value)
  annotationProcessor(libs.auto.service)

  sqlite4java(libs.bundles.sqlite4java.native)

  testImplementation(project(":robolectric"))
  testImplementation(libs.junit4)
  testImplementation(libs.truth)
  testImplementation(AndroidSdk.MAX_SDK.coordinates)
}
