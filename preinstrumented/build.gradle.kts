import java.net.URI

plugins {
  alias(libs.plugins.application)
  alias(libs.plugins.java)
}

val javaMainClass = "org.robolectric.preinstrumented.JarInstrumentor"

application { mainClass.set(javaMainClass) }

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

dependencies {
  implementation(libs.guava)
  implementation(project(":sandbox"))

  testImplementation(libs.junit4)
  testImplementation(libs.mockito)
}

val instrumentAll by
  tasks.registering {
    dependsOn(":prefetchSdks", "build")

    doLast {
      val androidAllMavenLocal =
        "${System.getProperty("user.home")}/.m2/repository/org/robolectric/android-all"
      sdksToInstrument().forEach { androidSdk ->
        logger.debug("Instrumenting ${androidSdk.coordinates}")

        val inputPath = "$androidAllMavenLocal/${androidSdk.version}/${androidSdk.jarFileName}"
        val outputPath =
          layout.buildDirectory.file(androidSdk.preinstrumentedJarFileName).get().asFile.path

        javaexec {
          classpath = sourceSets.getByName("main").runtimeClasspath
          mainClass.set(javaMainClass)
          args = listOf(inputPath, outputPath)
        }
      }
    }
  }

val emptySourcesJar by tasks.registering(Jar::class) { archiveClassifier.set("sources") }

val emptyJavadocJar by tasks.registering(Jar::class) { archiveClassifier.set("javadoc") }

// Avoid publishing the preinstrumented jars by default. They are published
// manually when the instrumentation configuration changes to maximize Gradle
// and Maven caching.
if (System.getenv("PUBLISH_PREINSTRUMENTED_JARS") == "true") {
  pluginManager.apply("maven-publish")
  pluginManager.apply("signing")

  extensions.configure<PublishingExtension> {
    publications {
      sdksToInstrument().forEach { androidSdk ->
        register<MavenPublication>("sdk${androidSdk.apiLevel}") {
          artifact(
            layout.buildDirectory.file(androidSdk.preinstrumentedJarFileName).get().asFile.path
          )
          artifactId = "android-all-instrumented"
          artifact(emptySourcesJar)
          artifact(emptyJavadocJar)
          version = androidSdk.preinstrumentedVersion

          pom {
            name = "Google Android ${androidSdk.androidVersion} instrumented android-all library"
            description =
              "Google Android ${androidSdk.androidVersion} framework jars transformed with Robolectric instrumentation."
            url = "https://source.android.com/"
            inceptionYear = "2008"

            licenses {
              license {
                name = "Apache 2.0"
                url = "http://www.apache.org/licenses/LICENSE-2.0"
                comments =
                  "While the EULA for the Android SDK restricts distribution of those binaries, the source code is licensed under Apache 2.0 which allows compiling binaries from source and then distributing those versions."
                distribution = "repo"
              }
            }

            scm {
              url = "https://android.googlesource.com/platform/manifest.git"
              connection = "https://android.googlesource.com/platform/manifest.git"
            }

            developers { developer { name = "The Android Open Source Projects" } }
          }
        }
      }
    }

    repositories {
      maven {
        url = URI("https://oss.sonatype.org/service/local/staging/deploy/maven2/")

        credentials {
          username = System.getProperty("sonatype-login") ?: System.getenv("SONATYPE_LOGIN")
          password = System.getProperty("sonatype-password") ?: System.getenv("SONATYPE_PASSWORD")
        }
      }
    }

    project.extensions.configure<SigningExtension> {
      // Skip signing if a signing key is not configured.
      setRequired { hasProperty("signing.keyId") }

      sdksToInstrument().forEach { androidSdk ->
        sign(publications.getByName("sdk${androidSdk.apiLevel}"))
      }
    }
  }

  // Workaround for https://github.com/gradle/gradle/issues/26132
  // For some reason, Gradle has inferred that all publishing tasks depend on all signing tasks,
  // so we must explicitly declare this here.
  afterEvaluate {
    tasks.configureEach {
      if (name.startsWith("publishSdk")) {
        sdksToInstrument().forEach { androidSdk ->
          dependsOn(tasks.named("signSdk${androidSdk.apiLevel}Publication"))
        }
      }
    }
  }
}

fun sdksToInstrument(): List<AndroidSdk> {
  val sdkFilter =
    System.getenv("PREINSTRUMENTED_SDK_VERSIONS").orEmpty().split(',').mapNotNull {
      it.toIntOrNull()
    }
  if (sdkFilter.isNotEmpty()) {
    return AndroidSdk.ALL_SDKS.filter { it.apiLevel in sdkFilter }
  }

  return AndroidSdk.ALL_SDKS
}

tasks.named("clean") {
  doFirst {
    AndroidSdk.ALL_SDKS.forEach { androidSdk ->
      delete(layout.buildDirectory.file(androidSdk.preinstrumentedJarFileName))
    }
  }
}
