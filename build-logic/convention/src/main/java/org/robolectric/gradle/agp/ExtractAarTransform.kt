/*
 * Copyright (C) 2019 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

/*
 * This class comes from AGP internals:
 * https://cs.android.com/android-studio/platform/tools/base/+/mirror-goog-studio-main:build-system/gradle-core/src/main/java/com/android/build/gradle/internal/dependency/ExtractAarTransform.kt;bpv=0
 */

package org.robolectric.gradle.agp

import com.android.SdkConstants
import com.android.utils.FileUtils
import com.google.common.annotations.VisibleForTesting
import com.google.common.io.Files
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.jar.JarOutputStream
import java.util.zip.ZipInputStream
import org.gradle.api.artifacts.transform.InputArtifact
import org.gradle.api.artifacts.transform.TransformAction
import org.gradle.api.artifacts.transform.TransformOutputs
import org.gradle.api.file.FileSystemLocation
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.Classpath
import org.gradle.work.DisableCachingByDefault

/**
 * Transform that extracts an AAR file into a directory.
 *
 * Note: There are small adjustments made to the extracted contents (see [AarExtractor.extract]).
 */
@DisableCachingByDefault(because = "Copy task")
abstract class ExtractAarTransform : TransformAction<GenericTransformParameters> {

  @get:Classpath @get:InputArtifact abstract val aarFile: Provider<FileSystemLocation>

  override fun transform(outputs: TransformOutputs) {
    // TODO: record transform execution span
    val inputFile = aarFile.get().asFile
    val outputDir = outputs.dir(inputFile.nameWithoutExtension)
    FileUtils.mkdirs(outputDir)
    AarExtractor().extract(inputFile, outputDir)
  }
}

private const val LIBS_PREFIX = SdkConstants.LIBS_FOLDER + '/'
private const val LIBS_PREFIX_LENGTH = LIBS_PREFIX.length
private const val JARS_PREFIX_LENGTH = SdkConstants.FD_JARS.length + 1

@VisibleForTesting
internal class AarExtractor {

  /**
   * [StringBuilder] used to construct all paths. It gets truncated back to [JARS_PREFIX_LENGTH] on
   * every calculation.
   */
  private val stringBuilder =
    StringBuilder(60).apply {
      append(SdkConstants.FD_JARS)
      append(File.separatorChar)
    }

  private fun choosePathInOutput(entryName: String): String {
    stringBuilder.setLength(JARS_PREFIX_LENGTH)

    return when {
      entryName == SdkConstants.FN_CLASSES_JAR || entryName == SdkConstants.FN_LINT_JAR -> {
        stringBuilder.append(entryName)
        stringBuilder.toString()
      }
      entryName.startsWith(LIBS_PREFIX) -> {
        // In case we have libs/classes.jar we are going to rename them, due an issue in
        // Gradle.
        // TODO: stop doing this once this is fixed in gradle.
        when (val pathWithinLibs = entryName.substring(LIBS_PREFIX_LENGTH)) {
          SdkConstants.FN_CLASSES_JAR ->
            stringBuilder.append(LIBS_PREFIX).append("classes-2${SdkConstants.DOT_JAR}")
          SdkConstants.FN_LINT_JAR ->
            stringBuilder.append(LIBS_PREFIX).append("lint-2${SdkConstants.DOT_JAR}")
          else -> stringBuilder.append(LIBS_PREFIX).append(pathWithinLibs)
        }
        stringBuilder.toString()
      }
      else -> entryName
    }
  }

  /**
   * Extracts an AAR file into a directory.
   *
   * Note: There are small adjustments made to the extracted contents. For example, classes.jar
   * inside the AAR will be extracted to jars/classes.jar, and if the jar does not exist, we will
   * create an empty classes.jar.
   */
  fun extract(aar: File, outputDir: File) {
    ZipInputStream(aar.inputStream().buffered()).use { zipInputStream ->
      while (true) {
        val entry = zipInputStream.nextEntry ?: break
        if (entry.isDirectory || entry.name.contains("../") || entry.name.isEmpty()) {
          continue
        }
        val path = FileUtils.toSystemDependentPath(choosePathInOutput(entry.name))
        val outputFile = File(outputDir, path)
        Files.createParentDirs(outputFile)
        Files.asByteSink(outputFile).writeFrom(zipInputStream)
      }
    }

    // If classes.jar does not exist, create an empty one
    val classesJar = outputDir.resolve("${SdkConstants.FD_JARS}/${SdkConstants.FN_CLASSES_JAR}")
    if (!classesJar.exists()) {
      Files.createParentDirs(classesJar)
      classesJar.writeBytes(emptyJar)
    }
  }
}

private val emptyJar: ByteArray =
  // Note:
  //  - A jar doesn't need a manifest entry, but if we ever want to create a manifest entry, be
  //    sure to set a fixed timestamp for it so that the jar is deterministic.
  //  - This empty jar takes up only ~22 bytes, so we don't need to GC it at the end of the build.
  ByteArrayOutputStream().apply { JarOutputStream(this).use {} }.toByteArray()
