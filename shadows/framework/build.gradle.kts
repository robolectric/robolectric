plugins {
  alias(libs.plugins.robolectric.deployed.java.module)
  alias(libs.plugins.robolectric.java.module)
  alias(libs.plugins.robolectric.shadows)
}

shadows {
  packageName = "org.robolectric"
  sdkCheckMode = "ERROR"
}

val sqlite4java = configurations.create("sqlite4java")
val sqlite4javaVersion = libs.versions.sqlite4java.get()

val copySqliteNatives by
  tasks.registering(Copy::class) {
    from(sqlite4java) {
      include("**/*.dll")
      include("**/*.so")
      include("**/*.dylib")

      rename { filename ->
        val filenameMatch = "^([^\\-]+)-(.+)-${sqlite4javaVersion}\\.(.+)".toRegex().find(filename)
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
    into(project.file(layout.buildDirectory.dir("resources/main/sqlite4java")))
  }

tasks.jar.configure { dependsOn(copySqliteNatives) }

tasks.javadoc.configure { dependsOn(copySqliteNatives) }

val axtMonitorVersion: String by rootProject.extra

dependencies {
  api(project(":annotations"))
  api(project(":nativeruntime"))
  api(project(":resources"))
  api(project(":pluginapi"))
  api(project(":sandbox"))
  api(project(":shadowapi"))
  api(project(":utils"))
  api(project(":utils:reflector"))

  api("androidx.test:monitor:$axtMonitorVersion@aar")

  implementation(libs.error.prone.annotations)
  compileOnly(libs.findbugs.jsr305)
  api(libs.sqlite4java)
  compileOnly(AndroidSdk.MAX_SDK.coordinates)
  api(libs.icu4j)
  api(libs.auto.value.annotations)
  annotationProcessor(libs.auto.value)

  sqlite4java(libs.bundles.sqlite4java.native)
}
