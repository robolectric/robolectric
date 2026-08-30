package org.robolectric.gradle

import java.io.File
import java.util.concurrent.ConcurrentHashMap
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.services.BuildService
import org.gradle.api.services.BuildServiceParameters
import org.gradle.api.tasks.compile.JavaCompile
import org.gradle.kotlin.dsl.register
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.kotlin.dsl.withType

/**
 * Collects the Error Prone diagnostics emitted while compiling, so they can be reported together
 * instead of only as far as the first module that fails.
 *
 * Enabled by `-PerrorproneReport`. Each [JavaCompile] tees the compiler's messages into
 * `<module>/build/reports/errorprone/<task>.log`, and the root [CollectErrorProneIssuesTask] parses
 * those into `build/reports/errorprone/issues.json` and a `summary.txt` counting each check.
 */
class ErrorProneReportPlugin : Plugin<Project> {
  override fun apply(project: Project) {
    if (!project.providers.gradleProperty(ENABLE_PROPERTY).isPresent) return

    if (project == project.rootProject) {
      registerAggregateTask(project)
      return
    }

    // Applying a plugin is idempotent, so each module can bootstrap the root task without racing
    // another module to check whether it exists yet.
    project.rootProject.pluginManager.apply(ErrorProneReportPlugin::class.java)

    val collector =
      project.gradle.sharedServices.registerIfAbsent(
        "errorProneDiagnostics",
        DiagnosticCollector::class.java,
      ) {}
    val aggregate = project.rootProject.tasks.named(TASK_NAME)
    val reportDir = project.layout.buildDirectory.dir("reports/errorprone")

    project.tasks.withType<JavaCompile>().configureEach {
      val logFile = reportDir.map { it.file("$name.log").asFile }
      val taskPath = path

      // javac stops listing after 100 of each by default, which would truncate the report.
      options.compilerArgs.addAll(listOf("-Xmaxerrs", "10000", "-Xmaxwarns", "10000"))

      usesService(collector)
      // The aggregate has no inputs Gradle can see, so without this it is free to run before the
      // compilers have written anything and to report nothing at all.
      finalizedBy(aggregate)

      // The log is deliberately not declared as a task output. It is written by a logging
      // listener rather than by the task's own action, so an up-to-date or cached compile would
      // keep a stale one while never running the code that refreshes it -- and it would end up
      // in the compile task's build cache entry.
      //
      // It is written as the compiler emits rather than in doLast, because Error Prone reports
      // its findings as errors and a failing task never reaches doLast.
      doFirst {
        val log = logFile.get()
        log.parentFile.mkdirs()
        log.writeText("")
        val service = collector.get()
        service.register(taskPath, log)
        logging.addStandardErrorListener { service.append(taskPath, it.toString()) }
        logging.addStandardOutputListener { service.append(taskPath, it.toString()) }
      }
    }
  }

  private fun registerAggregateTask(root: Project) {
    root.tasks.register<CollectErrorProneIssuesTask>(TASK_NAME) {
      group = "verification"
      description = "Aggregates Error Prone diagnostics from every module into one report."
      logs.setFrom(
        root.fileTree(root.layout.projectDirectory) {
          include("**/build/reports/errorprone/*.log")
        }
      )
      projectRoot.set(root.layout.projectDirectory)
      reportDir.set(root.layout.buildDirectory.dir("reports/errorprone"))
      // The logs come from listeners the build cannot track, so this must never be skipped.
      outputs.upToDateWhen { false }
    }
  }

  /** Appends compiler output to each task's log as it is produced. */
  abstract class DiagnosticCollector : BuildService<BuildServiceParameters.None> {
    private val logs = ConcurrentHashMap<String, File>()

    fun register(taskPath: String, log: File) {
      logs[taskPath] = log
    }

    fun append(taskPath: String, text: String) {
      logs[taskPath]?.appendText(text)
    }
  }

  private companion object {
    const val ENABLE_PROPERTY = "errorproneReport"
    const val TASK_NAME = "collectErrorProneIssues"
  }
}
