import java.net.URI
import org.gradle.api.DefaultTask
import org.robolectric.gradle.AndroidSdk
import org.robolectric.gradle.PUBLISH_URL

plugins {
  alias(libs.plugins.application)
  alias(libs.plugins.java)
}

application { mainClass = "org.robolectric.preinstrumented.JarInstrumentor" }

java {
  sourceCompatibility = JavaVersion.VERSION_11
  targetCompatibility = JavaVersion.VERSION_11
}

dependencies {
  implementation(libs.guava)
  implementation(project(":sandbox"))

  testImplementation(libs.junit4)
  testImplementation(libs.mockito)
  testImplementation(libs.mockito.subclass)
}

val androidAllMavenLocal =
  providers.systemProperty("user.home").map { "$it/.m2/repository/org/robolectric/android-all" }
val instrumentTasks =
  sdksToInstrument().map { androidSdk ->
    tasks.register<InstrumentTask>("instrumentSdk${androidSdk.apiLevel}") {
      description =
        "Create the preinstrumented Android JAR file for API level ${androidSdk.apiLevel}"
      group = "robolectric"

      dependsOn(":prefetchSdk${androidSdk.apiLevel}")

      androidAllJarFile =
        layout.projectDirectory.file(
          androidAllMavenLocal.map { "$it/${androidSdk.version}/${androidSdk.jarFileName}" }
        )
      preinstrumentedJarFile = layout.buildDirectory.file(androidSdk.preinstrumentedJarFileName)
      runtimeClasspath.from(sourceSets.named("main").map { it.runtimeClasspath })
      mainClassName = application.mainClass

      doFirst { logger.debug("Instrumenting ${androidSdk.coordinates}") }
    }
  }

val instrumentAll =
  tasks.register("instrumentAll") {
    description = "Create the preinstrumented Android JAR file for the requested API levels"
    group = "robolectric"

    dependsOn("build", instrumentTasks)
  }

@CacheableTask
abstract class InstrumentTask : DefaultTask() {
  @get:InputFile
  @get:PathSensitive(PathSensitivity.NONE)
  abstract val androidAllJarFile: RegularFileProperty
  @get:OutputFile abstract val preinstrumentedJarFile: RegularFileProperty
  @get:Classpath abstract val runtimeClasspath: ConfigurableFileCollection
  @get:Input abstract val mainClassName: Property<String>

  @get:Inject abstract val execOperations: ExecOperations

  @TaskAction
  fun instrument() {
    execOperations
      .javaexec {
        classpath = runtimeClasspath
        mainClass = mainClassName
        args = listOf(androidAllJarFile, preinstrumentedJarFile).map { it.get().asFile.path }
      }
      .rethrowFailure()
      .assertNormalExitValue()
  }
}

val emptySourcesJar = tasks.register<Jar>("emptySourcesJar") { archiveClassifier.set("sources") }

val emptyJavadocJar = tasks.register<Jar>("emptyJavadocJar") { archiveClassifier.set("javadoc") }

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
        url = URI(PUBLISH_URL)

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

tasks.named<Delete>("clean") {
  AndroidSdk.ALL_SDKS.forEach { androidSdk ->
    delete(layout.buildDirectory.file(androidSdk.preinstrumentedJarFileName))
  }
}
