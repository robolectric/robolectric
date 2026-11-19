package org.robolectric.shadows;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import javax.annotation.Nullable;
import org.robolectric.RuntimeEnvironment;

/**
 * Utility for loading split APKs from bundletool-generated archives or directories.
 *
 * <p>Bundletool (https://developer.android.com/tools/bundletool) generates {@code .apks} archives
 * containing split APKs for an Android App Bundle. This utility extracts split APKs from such
 * archives and prepares them for use with {@link ShadowPackageManager#installPackageWithSplitApks}.
 *
 * <h3>Bundletool .apks archive format:</h3>
 *
 * <pre>
 * app.apks (ZIP archive)
 * ├── splits/
 * │   ├── base-master.apk
 * │   ├── base-xxhdpi.apk
 * │   ├── base-arm64_v8a.apk
 * │   └── base-en.apk
 * ├── toc.pb
 * └── (other metadata)
 * </pre>
 *
 * <h3>Usage example:</h3>
 *
 * <pre>{@code
 * // From a .apks archive
 * Map<String, String> splits = BundletoolSplitApkLoader.loadFromApksArchive(
 *     Paths.get("app.apks"));
 * shadowOf(packageManager).installPackageWithSplitApks(packageInfo, splits);
 *
 * // From a directory of APK files
 * Map<String, String> splits = BundletoolSplitApkLoader.loadFromDirectory(
 *     Paths.get("splits/"));
 * shadowOf(packageManager).installPackageWithSplitApks(packageInfo, splits);
 * }</pre>
 */
public final class BundletoolSplitApkLoader {

  private BundletoolSplitApkLoader() {}

  /**
   * Loads split APKs from a bundletool-generated {@code .apks} archive.
   *
   * <p>Extracts all {@code .apk} files found under the {@code splits/} directory in the archive.
   * Split names are derived from the APK filenames (e.g., {@code base-xxhdpi.apk} → {@code
   * "base-xxhdpi"}).
   *
   * <p>The base APK ({@code base-master.apk}) is included in the returned map with the key {@code
   * "base-master"} or whatever its filename indicates. Callers can remove or handle the base APK
   * separately if needed.
   *
   * @param apksArchivePath path to the {@code .apks} ZIP archive
   * @return a map from split name to the path of the extracted APK file
   * @throws IOException if the archive cannot be read
   */
  public static Map<String, String> loadFromApksArchive(Path apksArchivePath) throws IOException {
    return loadFromApksArchive(apksArchivePath, null);
  }

  /**
   * Loads split APKs from a bundletool-generated {@code .apks} archive, extracting only splits
   * matching the given prefix.
   *
   * @param apksArchivePath path to the {@code .apks} ZIP archive
   * @param splitPrefix optional prefix filter (e.g., "base-" to only load base module splits). If
   *     null, all splits are loaded.
   * @return a map from split name to the path of the extracted APK file
   * @throws IOException if the archive cannot be read
   */
  public static Map<String, String> loadFromApksArchive(
      Path apksArchivePath, @Nullable String splitPrefix) throws IOException {
    Map<String, String> splitPaths = new LinkedHashMap<>();
    Path extractDir =
        RuntimeEnvironment.getTempDirectory()
            .createIfNotExists("bundletool-" + apksArchivePath.getFileName());

    try (ZipFile zipFile = new ZipFile(apksArchivePath.toFile())) {
      Enumeration<? extends ZipEntry> entries = zipFile.entries();
      while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();
        String name = entry.getName();

        // Look for APK files in the splits/ directory
        if (entry.isDirectory() || !name.endsWith(".apk")) {
          continue;
        }

        // Handle both "splits/base-master.apk" and "base-master.apk" layouts
        String fileName = name;
        int lastSlash = name.lastIndexOf('/');
        if (lastSlash >= 0) {
          fileName = name.substring(lastSlash + 1);
        }

        String splitName = fileName.substring(0, fileName.length() - 4); // Remove .apk

        if (splitPrefix != null && !splitName.startsWith(splitPrefix)) {
          continue;
        }

        // Extract the APK file, guarding against Zip Slip path traversal.
        Path extractedApk = extractDir.resolve(fileName).normalize();
        if (!extractedApk.startsWith(extractDir.toAbsolutePath().normalize())) {
          throw new IOException(
              "Zip entry escapes target directory (Zip Slip): " + entry.getName());
        }
        try (InputStream is = zipFile.getInputStream(entry)) {
          Files.copy(is, extractedApk, StandardCopyOption.REPLACE_EXISTING);
        }

        splitPaths.put(splitName, extractedApk.toAbsolutePath().toString());
      }
    }

    return splitPaths;
  }

  /**
   * Loads split APKs from a directory containing APK files.
   *
   * <p>Each {@code .apk} file in the directory is treated as a split. The split name is derived
   * from the filename (without the {@code .apk} extension).
   *
   * @param directory path to a directory containing split APK files
   * @return a map from split name to the absolute path of the APK file
   * @throws IOException if the directory cannot be read
   */
  public static Map<String, String> loadFromDirectory(Path directory) throws IOException {
    return loadFromDirectory(directory, null);
  }

  /**
   * Loads split APKs from a directory, filtering by prefix.
   *
   * @param directory path to a directory containing split APK files
   * @param splitPrefix optional prefix filter. If null, all APK files are loaded.
   * @return a map from split name to the absolute path of the APK file
   * @throws IOException if the directory cannot be read
   */
  public static Map<String, String> loadFromDirectory(Path directory, @Nullable String splitPrefix)
      throws IOException {
    Map<String, String> splitPaths = new LinkedHashMap<>();

    Files.list(directory)
        .filter(p -> p.toString().endsWith(".apk"))
        .sorted()
        .forEach(
            apkPath -> {
              String fileName = apkPath.getFileName().toString();
              String splitName = fileName.substring(0, fileName.length() - 4);
              if (splitPrefix == null || splitName.startsWith(splitPrefix)) {
                splitPaths.put(splitName, apkPath.toAbsolutePath().toString());
              }
            });

    return splitPaths;
  }

  /**
   * Creates a simulated bundletool {@code .apks} archive containing the specified split APK files.
   *
   * <p>This is useful for testing the {@link #loadFromApksArchive} method or for creating test
   * fixtures that mimic real bundletool output.
   *
   * @param splitApkPaths map from split name to the path of the split APK file
   * @return the path to the created {@code .apks} archive
   */
  public static Path createApksArchive(Map<String, String> splitApkPaths) throws IOException {
    Path dir = RuntimeEnvironment.getTempDirectory().createIfNotExists("bundletool-archives");
    Path archivePath = dir.resolve("test-bundle.apks");

    try (java.util.zip.ZipOutputStream zos =
        new java.util.zip.ZipOutputStream(Files.newOutputStream(archivePath))) {
      for (Map.Entry<String, String> entry : splitApkPaths.entrySet()) {
        String entryName = "splits/" + entry.getKey() + ".apk";
        zos.putNextEntry(new ZipEntry(entryName));
        Files.copy(Path.of(entry.getValue()), zos);
        zos.closeEntry();
      }
      zos.finish();
    }

    return archivePath;
  }
}
