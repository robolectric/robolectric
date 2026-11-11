import groovy.util.Node
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.TaskAction
import org.gradle.plugins.ide.idea.model.IdeaModel
import org.gradle.work.DisableCachingByDefault
import org.robolectric.gradle.AndroidSdk
import org.robolectric.gradle.ShadowsPlugin.ShadowsPluginExtension

// For use of external initialization scripts...
project.extra["allSdks"] = AndroidSdk.ALL_SDKS

project.extra["configAnnotationProcessing"] = emptyList<Project>()

val thisVersion = project.property("thisVersion") as String

plugins {
  // Define versions for plugins used in subprojects. 'apply false' is used to load the plugin
  // into the root classpath without applying it to the root project, which ensures
  // consistent versioning and compatibility across subprojects.
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.android.library) apply false
  alias(libs.plugins.detekt) apply false
  alias(libs.plugins.error.prone)
  alias(libs.plugins.idea)
  alias(libs.plugins.robolectric.spotless)
  alias(libs.plugins.robolectric.javadoc)
  alias(libs.plugins.roborazzi) apply false
  alias(libs.plugins.compose.compiler) apply false
}

allprojects {
  group = "org.robolectric"
  version = thisVersion
}

project.afterEvaluate {
  @Suppress("UNCHECKED_CAST")
  val configAnnotationProcessing = project.extra["configAnnotationProcessing"] as List<Project>
  val ideaProject = rootProject.extensions.getByType<IdeaModel>().project
  ideaProject.ipr.withXml {
    val compilerConfiguration =
      asNode().children().filterIsInstance<Node>().first {
        it.name() == "component" && it.attribute("name") == "CompilerConfiguration"
      }

    // Prevent compiler from complaining about duplicate classes...
    val excludeFromCompile = compilerConfiguration.appendNode("excludeFromCompile")
    configAnnotationProcessing.forEach { subproject ->
      val buildDirectory = subproject.layout.buildDirectory.get().asFile
      excludeFromCompile.appendNode(
        "directory",
        mapOf(
          "url" to "file://$buildDirectory/classes/java/main/generated",
          "includeSubdirectories" to "true",
        ),
      )
    }

    // Replace the existing "annotationProcessing" tag with a new one...
    val annotationProcessingNode = Node(compilerConfiguration, "annotationProcessing")
    configAnnotationProcessing.forEach { subproject ->
      val profileNode =
        Node(
          annotationProcessingNode,
          "profile",
          mapOf("name" to "${subproject.name}_main", "enabled" to "true"),
        )
      profileNode.appendNode("module", mapOf("name" to "${subproject.name}_main"))
      profileNode.appendNode(
        "option",
        mapOf(
          "name" to "org.robolectric.annotation.processing.shadowPackage",
          "value" to subproject.extensions.getByType<ShadowsPluginExtension>().packageName.get(),
        ),
      )
      profileNode.appendNode(
        "processor",
        mapOf("name" to "org.robolectric.annotation.processing.RobolectricProcessor"),
      )

      val processorPathNode = Node(profileNode, "processorPath", mapOf("useClasspath" to "false"))
      project.project(":processor").configurations.named("runtime").configure {
        allArtifacts.forEach { artifact ->
          processorPathNode.appendNode("entry", mapOf("name" to artifact.file))
        }
        files.forEach { file -> processorPathNode.appendNode("entry", mapOf("name" to file)) }
      }

      profileNode.appendNode(processorPathNode)
      annotationProcessingNode.appendNode(profileNode)
    }

    compilerConfiguration.replaceNode(annotationProcessingNode)
  }
}

gradle.projectsEvaluated {
  val headerHtml =
    """
    <ul class="navList" style="font-size: 1.5em;">
      <li>
        Robolectric $thisVersion |&nbsp;
        <a href="/" target="_top">
          <img src="https://robolectric.org/images/logo-with-bubbles-down.png" style="max-height: 18pt; vertical-align: sub;"/>
        </a>
      </li>
    </ul>
    """
      .trimIndent()

  project.allprojects {
    tasks.withType<Javadoc> {
      options {
        this as StandardJavadocDocletOptions

        noTimestamp(true)
        links(
          "https://docs.oracle.com/en/java/javase/11/docs/api/",
          "https://developer.android.com/reference/",
        )
        // Set Javadoc source to JDK 8 to avoid unnamed module problem
        // when running 'aggregateJavadocs' with OpenJDK 13+, although
        // the source/target version has changed to JDK11.
        source("8")
        header = headerHtml
        footer = headerHtml
      }
    }
  }
}

tasks.register<Copy>("aggregateJsondocs") {
  project.subprojects
    .filter { it.pluginManager.hasPlugin(libs.plugins.robolectric.shadows.get().pluginId) }
    .forEach { subproject ->
      dependsOn(subproject.tasks.named("compileJava"))
      from(subproject.layout.buildDirectory.dir("docs/json"))
    }

  into(layout.buildDirectory.dir("docs/json"))
}

val aggregateDocs =
  tasks.register("aggregateDocs") { dependsOn(":aggregateJavadocs", ":aggregateJsondocs") }

@Suppress("UNCHECKED_CAST") val allSdks = project.extra["allSdks"] as List<AndroidSdk>

val mavenLocal = providers.systemProperty("user.home").map { "$it/.m2/repository" }

val prefetchSdkTasks = allSdks.map { androidSdk ->
  val configuration = configurations.register("sdk${androidSdk.apiLevel}")
  dependencies.add(configuration.name, androidSdk.coordinates)

  tasks.register<PrefetchSdkTask>("prefetchSdk${androidSdk.apiLevel}") {
    description = "Prefetch the 'android-all' artifact for API level ${androidSdk.apiLevel}"
    group = "robolectric"

    apiLevel = androidSdk.apiLevel
    coordinates = androidSdk.coordinates
    groupId = androidSdk.groupId
    artifactId = androidSdk.artifactId
    version = androidSdk.version
    sdkFiles.from(configuration)
    mavenLocalRepository = mavenLocal
  }
}

val prefetchInstrumentedSdkTasks = allSdks.map { androidSdk ->
  val configuration = configurations.register("sdkInstrumented${androidSdk.apiLevel}")
  dependencies.add(configuration.name, androidSdk.preinstrumentedCoordinates)

  tasks.register<PrefetchSdkTask>("prefetchInstrumentedSdk${androidSdk.apiLevel}") {
    description =
      "Prefetch the 'android-all-instrumented' artifact for API level ${androidSdk.apiLevel}"
    group = "robolectric"

    apiLevel = androidSdk.apiLevel
    coordinates = androidSdk.preinstrumentedCoordinates
    groupId = androidSdk.groupId
    artifactId = androidSdk.preinstrumentedArtifactId
    version = androidSdk.preinstrumentedVersion
    sdkFiles.from(configuration)
    mavenLocalRepository = mavenLocal
  }
}

val prefetchSdks =
  tasks.register("prefetchSdks") {
    description = "Prefetch the 'android-all' artifact for all the supported API levels"
    group = "robolectric"

    dependsOn(prefetchSdkTasks)
  }

val prefetchInstrumentedSdks =
  tasks.register("prefetchInstrumentedSdks") {
    description =
      "Prefetch the 'android-all-instrumented' artifact for all the supported API levels"
    group = "robolectric"

    dependsOn(prefetchInstrumentedSdkTasks)
  }

@DisableCachingByDefault
abstract class PrefetchSdkTask : DefaultTask() {
  @get:Input abstract val apiLevel: Property<Int>
  @get:Input abstract val coordinates: Property<String>
  @get:Input abstract val groupId: Property<String>
  @get:Input abstract val artifactId: Property<String>
  @get:Input abstract val version: Property<String>
  @get:InputFiles abstract val sdkFiles: ConfigurableFileCollection
  @get:Internal abstract val mavenLocalRepository: Property<String>

  @TaskAction
  fun prefetchSdk() {
    logger.lifecycle("Prefetching {}...", coordinates.get())

    // Resolving the configuration fetches the jar, and the preinstrumented build reads it from the
    // Maven local repository, so copy it there.
    val jarFileName = "${artifactId.get()}-${version.get()}.jar"
    val jarFile = sdkFiles.files.single { it.name == jarFileName }
    val mavenLocalJarFile =
      File(
        mavenLocalRepository.get(),
        "${groupId.get().replace('.', '/')}/${artifactId.get()}/${version.get()}/$jarFileName",
      )
    if (jarFile != mavenLocalJarFile && !mavenLocalJarFile.exists()) {
      jarFile.copyTo(mavenLocalJarFile)
    }
  }
}

val prefetchDependencies =
  tasks.register("prefetchDependencies") {
    doLast {
      allprojects.forEach { p ->
        p.configurations.forEach { config ->
          // Causes dependencies to be resolved:
          if (config.isCanBeResolved) {
            try {
              config.files
            } catch (e: ResolveException) {
              // Ignore resolution issues for the ':integration_tests' and ':testapp' projects, sigh
              if (!p.path.startsWith(":integration_tests:") && !p.path.startsWith(":testapp")) {
                throw e
              }
            }
          } // End config resolution
        } // End configurations
      } // End allprojects
    } // End doLast
  } // End task registration

// The following line sets the CodeQL GitHub Action to use JDK 21:
// languageVersion = JavaLanguageVersion.of(21)
// See https://github.com/github/codeql-action/issues/1855
