package org.robolectric.gradle

import java.io.File
import org.gradle.api.DefaultTask
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.InputFiles
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction

/** Parses the per-module Error Prone logs into an aggregated report. */
abstract class CollectErrorProneIssuesTask : DefaultTask() {

  /** Per-module logs written by [ErrorProneReportPlugin]. */
  @get:InputFiles
  @get:PathSensitive(PathSensitivity.RELATIVE)
  abstract val logs: ConfigurableFileCollection

  /** Used only to report paths relative to the repository. */
  @get:Internal abstract val projectRoot: DirectoryProperty

  @get:OutputDirectory abstract val reportDir: DirectoryProperty

  @TaskAction
  fun collect() {
    val logs = logs.files.filter { it.isFile }.sorted()
    val root = projectRoot.get().asFile
    // Gradle's per-task logging is process-global while tasks overlap, so a parallel build can
    // capture one module's diagnostics into another module's log. Attribution does not matter for
    // a repository-wide report, but the duplicates it produces do, hence a global distinct().
    val all = logs.flatMap { parse(it, root) }.distinct().sortedWith(ORDER)
    // javac's own lint categories are lowercase (removal, deprecation); Error Prone checks are
    // UpperCamelCase. Both arrive on the same stream, so separate them here.
    val (issues, lint) = all.partition { it.check.firstOrNull()?.isUpperCase() == true }
    val dir = reportDir.get().asFile.apply { mkdirs() }

    File(dir, "issues.json").writeText(toJson(issues))
    File(dir, "summary.txt").writeText(toSummary(issues, lint))

    val errors = issues.count { it.severity == "error" }
    logger.lifecycle(
      "Error Prone: ${issues.size} issue(s) ($errors error(s)) across " +
        "${issues.map { it.check }.distinct().size} check(s); report in ${dir.absolutePath}"
    )
  }

  private fun parse(log: File, root: File): List<Issue> =
    DIAGNOSTIC.findAll(log.readText())
      .map {
        val (file, line, severity, check, message) = it.destructured
        Issue(relativize(file, root), line.toInt(), severity, check, message.trim())
      }
      .toList()

  private fun relativize(path: String, root: File): String = runCatching {
    File(path).relativeTo(root).path
  }
    .getOrDefault(path)

  private fun toSummary(issues: List<Issue>, lint: List<Issue>): String {
    if (issues.isEmpty()) return "No Error Prone issues found.\n"
    val errors = issues.filter { it.severity == "error" }
    val counts = issues.groupingBy { it.check }.eachCount().entries.sortedWith(BY_COUNT)
    val width = counts.maxOf { it.key.length }
    return buildString {
      append("${issues.size} Error Prone issue(s): ")
      append("${errors.size} error(s), ${issues.size - errors.size} warning(s)\n")
      if (errors.isNotEmpty()) {
        append("\nErrors (these fail the build):\n\n")
        errors.forEach { append("  ${it.file}:${it.line}\n    [${it.check}] ${it.message}\n") }
      }
      append("\nBy check:\n\n")
      counts.forEach { append("  %-${width}s  %d%n".format(it.key, it.value)) }
      append("\nAll issues:\n\n")
      issues.forEach { append("  ${it.file}:${it.line}\n    [${it.check}] ${it.message}\n") }
      if (lint.isNotEmpty()) {
        val byCat = lint.groupingBy { it.check }.eachCount()
        append("\n${lint.size} javac lint warning(s) also captured, not Error Prone: $byCat\n")
      }
    }
  }

  private fun toJson(issues: List<Issue>): String =
    issues.joinToString(separator = ",\n", prefix = "[\n", postfix = "\n]\n") {
      """  {"check": "${esc(it.check)}", "severity": "${esc(it.severity)}", """ +
        """"file": "${esc(it.file)}", "line": ${it.line}, "message": "${esc(it.message)}"}"""
    }

  private fun esc(s: String) =
    s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", "\\n").replace("\t", "\\t")

  private data class Issue(
    val file: String,
    val line: Int,
    val severity: String,
    val check: String,
    val message: String,
  )

  private companion object {
    /** e.g. `/path/Foo.java:42: warning: [UnusedVariable] the message` */
    private val DIAGNOSTIC =
      Regex("""^(\S+\.java):(\d+): (error|warning): \[(\w+)\] (.*)$""", RegexOption.MULTILINE)

    private val ORDER = compareBy<Issue>({ it.check }, { it.file }, { it.line })
    private val BY_COUNT =
      compareByDescending<Map.Entry<String, Int>> { it.value }.thenBy { it.key }
  }
}
