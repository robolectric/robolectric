package org.robolectric.gradle.agp

import java.io.File
import java.util.zip.ZipEntry

/*
 * This class comes from AGP internals:
 * https://cs.android.com/android-studio/platform/tools/base/+/mirror-goog-studio-main:build-system/builder/src/main/java/com/android/builder/utils/ZipEntryUtils.kt
 */

/** Validates the raw zip entry name string to prevent traversal attacks. */
fun isValidZipEntryName(name: String): Boolean {
  return !name.contains(":") && name.split('/', '\\').none { it == ".." } && name.none { it < ' ' }
}

/**
 * Validates the name of a zip entry to prevent directory traversal attacks (e.g., Zip-Slip).
 *
 * This function returns true if the entry is safe. It specifically rejects:
 * - Traversal sequences (`..`) components.
 * - Absolute Windows paths with drives (`:`).
 * - Control characters (e.g., line feeds), which can be used for command injection.
 *
 * Note: This function does NOT reject absolute paths (e.g., leading `/`). This is intentional to
 * support legitimate zip-to-zip copying tasks in the build system (such as PackageAndroidArtifact)
 * where leading slashes are present and harmless. Extracting callers that write to the filesystem
 * MUST additionally use [isValidZipEntryPath] or equivalent boundary checks to ensure the resolved
 * path does not escape the output directory.
 *
 * @param entry The zip entry to validate.
 * @return `true` if the entry name is considered safe, `false` otherwise.
 */
fun isValidZipEntryName(entry: ZipEntry): Boolean {
  return isValidZipEntryName(entry.name)
}

/** Helper function to validate the path inside a zipfile does not leave the output directory. */
fun isValidZipEntryPath(filePath: File, outputDir: File): Boolean {
  return filePath.canonicalFile.toPath().startsWith(outputDir.canonicalFile.toPath())
}
