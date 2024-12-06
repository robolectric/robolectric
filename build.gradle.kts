import groovy.util.Node
import org.gradle.plugins.ide.idea.model.IdeaModel
import org.robolectric.gradle.ShadowsPlugin.ShadowsPluginExtension

// https://github.com/gradle/gradle/issues/21267
val axtCoreVersion by project.extra { libs.versions.androidx.test.core.get() }
val axtJunitVersion by project.extra { libs.versions.androidx.test.ext.junit.get() }
val axtMonitorVersion by project.extra { libs.versions.androidx.test.monitor.get() }
val axtRunnerVersion by project.extra { libs.versions.androidx.test.runner.get() }
val axtTruthVersion by project.extra { libs.versions.androidx.test.ext.truth.get() }
val espressoVersion by project.extra { libs.versions.androidx.test.espresso.get() }

// For use of external initialization scripts...
val allSdks by project.extra(AndroidSdk.ALL_SDKS)
val configAnnotationProcessing by project.extra(emptyList<Project>())

val thisVersion: String by project

plugins {
  alias(libs.plugins.android.library) apply false
  alias(libs.plugins.detekt) apply false
  alias(libs.plugins.error.prone)
  alias(libs.plugins.idea)
  alias(libs.plugins.kotlin.android) apply false
  alias(libs.plugins.kotlin.jvm) apply false
  alias(libs.plugins.robolectric.spotless)
  alias(libs.plugins.robolectric.javadoc)
  alias(libs.plugins.roborazzi) apply false
}

allprojects {
  group = "org.robolectric"
  version = thisVersion
}

project.afterEvaluate {
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
          "value" to project.extensions.getByType<ShadowsPluginExtension>().packageName,
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

rootProject.gradle.projectsEvaluated {
  rootProject.tasks.named<Javadoc>("aggregateJavadocs").configure { isFailOnError = false }
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
          "https://docs.oracle.com/javase/8/docs/api/",
          "https://developer.android.com/reference/",
        )
        // Set Javadoc source to JDK 8 to avoid unnamed module problem
        // when running 'aggregateJavadocs' with OpenJDK 13+.
        source("8")
        header = headerHtml
        footer = headerHtml
      }
    }
  }

  val aggregateJsondocs by
    tasks.registering(Copy::class) {
      project.subprojects
        .filter { it.pluginManager.hasPlugin(libs.plugins.robolectric.shadows.get().pluginId) }
        .forEach { subproject ->
          dependsOn(subproject.tasks.named("compileJava"))
          from(subproject.layout.buildDirectory.dir("docs/json"))
        }

      into(layout.buildDirectory.dir("docs/json"))
    }
}

val aggregateDocs by tasks.registering { dependsOn(":aggregateJavadocs", ":aggregateJsondocs") }

val prefetchSdks by
  tasks.registering {
    allSdks.forEach { androidSdk ->
      doLast {
        prefetchSdk(
          apiLevel = androidSdk.apiLevel,
          coordinates = androidSdk.coordinates,
          groupId = androidSdk.groupId,
          artifactId = androidSdk.artifactId,
          version = androidSdk.version,
        )
      }
    }
  }

val prefetchInstrumentedSdks by
  tasks.registering {
    allSdks.forEach { androidSdk ->
      doLast {
        prefetchSdk(
          apiLevel = androidSdk.apiLevel,
          coordinates = androidSdk.preinstrumentedCoordinates,
          groupId = androidSdk.groupId,
          artifactId = androidSdk.preinstrumentedArtifactId,
          version = androidSdk.preinstrumentedVersion,
        )
      }
    }
  }

fun prefetchSdk(
  apiLevel: Int,
  coordinates: String,
  groupId: String,
  artifactId: String,
  version: String,
) {
  println("Prefetching $coordinates...")

  // Prefetch into Maven local repo...
  project.exec {
    val mvnCommand =
      "mvn -q dependency:get -DrepoUrl=https://maven.google.com " +
        "-DgroupId=$groupId -DartifactId=$artifactId -Dversion=$version"

    commandLine(mvnCommand.split(" "))
  }

  // Prefetch into Gradle local cache...
  val config = configurations.create("sdk$apiLevel")
  dependencies.add("sdk$apiLevel", coordinates)

  // Causes dependencies to be resolved:
  config.files
}

val prefetchDependencies by
  tasks.registering {
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
