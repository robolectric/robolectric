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

package org.robolectric.gradle.agp;

import com.android.SdkConstants;
import com.android.utils.FileUtils;
import com.google.common.io.Files;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.jar.JarOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import org.gradle.api.artifacts.transform.InputArtifact;
import org.gradle.api.artifacts.transform.TransformAction;
import org.gradle.api.artifacts.transform.TransformOutputs;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Classpath;
import org.gradle.work.DisableCachingByDefault;
import org.jetbrains.annotations.NotNull;

// TODO Keep the original Kotlin implementation when `buildSrc` is migrated to Kotlin.
@DisableCachingByDefault(because = "Copy task")
public abstract class ExtractAarTransform implements TransformAction<GenericTransformParameters> {
  @Classpath
  @InputArtifact
  public abstract Provider<FileSystemLocation> getAarFile();

  @Override
  public void transform(@NotNull TransformOutputs outputs) {
    // TODO: record transform execution span
    File inputFile = getAarFile().get().getAsFile();
    String inputFileNameWithoutExtension = Files.getNameWithoutExtension(inputFile.getName());
    File outputDir = outputs.dir(inputFileNameWithoutExtension);
    FileUtils.mkdirs(outputDir);
    new AarExtractor().extract(inputFile, outputDir);
  }
}

class AarExtractor {
  private static final String LIBS_PREFIX = SdkConstants.LIBS_FOLDER + '/';
  private static final int LIBS_PREFIX_LENGTH = LIBS_PREFIX.length();
  private static final int JARS_PREFIX_LENGTH = SdkConstants.FD_JARS.length() + 1;

  // Note:
  //  - A jar doesn't need a manifest entry, but if we ever want to create a manifest entry, be
  //    sure to set a fixed timestamp for it so that the jar is deterministic
  //  - This empty jar takes up only ~22 bytes, so we don't need to GC it at the end of the build.
  private static final byte[] emptyJar;

  /**
   * {@link StringBuilder} used to construct all paths. It gets truncated back to {@link
   * JARS_PREFIX_LENGTH} on every calculation.
   */
  private final StringBuilder stringBuilder = new StringBuilder(60);

  static {
    ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
    //noinspection EmptyTryBlock
    try (JarOutputStream outputStream = new JarOutputStream(byteArrayOutputStream)) {
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    emptyJar = byteArrayOutputStream.toByteArray();
  }

  AarExtractor() {
    stringBuilder.append(SdkConstants.FD_JARS);
    stringBuilder.append(File.separatorChar);
  }

  private String choosePathInOutput(@NotNull String entryName) {
    stringBuilder.setLength(JARS_PREFIX_LENGTH);

    if (entryName.equals(SdkConstants.FN_CLASSES_JAR)
        || entryName.equals(SdkConstants.FN_LINT_JAR)) {
      stringBuilder.append(entryName);

      return stringBuilder.toString();
    } else if (entryName.startsWith(LIBS_PREFIX)) {
      // In case we have libs/classes.jar we are going to rename them, due an issue in
      // Gradle.
      // TODO: stop doing this once this is fixed in gradle.
      String pathWithinLibs = entryName.substring(LIBS_PREFIX_LENGTH);

      if (pathWithinLibs.equals(SdkConstants.FN_CLASSES_JAR)) {
        stringBuilder.append(LIBS_PREFIX).append("classes-2" + SdkConstants.DOT_JAR);
      } else if (pathWithinLibs.equals(SdkConstants.FN_LINT_JAR)) {
        stringBuilder.append(LIBS_PREFIX).append("lint-2" + SdkConstants.DOT_JAR);
      } else {
        stringBuilder.append(LIBS_PREFIX).append(pathWithinLibs);
      }

      return stringBuilder.toString();
    } else {
      return entryName;
    }
  }

  /**
   * Extracts an AAR file into a directory.
   *
   * <p>Note: There are small adjustments made to the extracted contents. For example, classes.jar
   * inside the AAR will be extracted to jars/classes.jar, and if the jar does not exist, we will
   * create an empty classes.jar.
   */
  void extract(@NotNull File aar, @NotNull File outputDir) {
    try (ZipInputStream zipInputStream =
        new ZipInputStream(java.nio.file.Files.newInputStream(aar.toPath()))) {
      while (true) {
        ZipEntry entry = zipInputStream.getNextEntry();
        if (entry == null) {
          break;
        }

        if (entry.isDirectory() || entry.getName().contains("../") || entry.getName().isEmpty()) {
          continue;
        }

        String path = FileUtils.toSystemDependentPath(choosePathInOutput(entry.getName()));
        File outputFile = new File(outputDir, path);
        Files.createParentDirs(outputFile);
        Files.asByteSink(outputFile).writeFrom(zipInputStream);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    // If classes.jar does not exist, create an empty one
    File classesJar = resolve(outputDir, SdkConstants.FD_JARS + "/" + SdkConstants.FN_CLASSES_JAR);
    if (!classesJar.exists()) {
      try {
        Files.createParentDirs(classesJar);
        Files.write(emptyJar, classesJar);
      } catch (IOException e) {
        throw new RuntimeException(e);
      }
    }
  }

  @NotNull
  private File resolve(@NotNull File source, @NotNull String relative) {
    Path baseDir = source.toPath();
    Path relativeFile = Paths.get(relative);
    Path resolvedFile = baseDir.resolve(relativeFile);

    return resolvedFile.toFile();
  }
}
