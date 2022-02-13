package org.robolectric.shadows.util;

import com.almworks.sqlite4java.SQLite;
import com.almworks.sqlite4java.SQLiteException;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.io.ByteSource;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Initializes sqlite native libraries. */
public class SQLiteLibraryLoader {
  private static SQLiteLibraryLoader instance;
  private static final String SQLITE4JAVA = "sqlite4java";
  private static final String OS_WIN = "win32", OS_LINUX = "linux", OS_MAC = "osx";
  private static final String OS_ARCH_ARM64 = "aarch64";
  private static final String SYSTEM_PROPERTY_OS_NAME = "os.name";
  private static final String SYSTEM_PROPERTY_OS_ARCH = "os.arch";

  private final LibraryNameMapper libraryNameMapper;
  private boolean loaded;

  public SQLiteLibraryLoader() {
    this(DEFAULT_MAPPER);
  }

  public SQLiteLibraryLoader(LibraryNameMapper mapper) {
    libraryNameMapper = mapper;
  }

  private static final LibraryNameMapper DEFAULT_MAPPER = System::mapLibraryName;

  public static synchronized void load() {
    if (instance == null) {
      instance = new SQLiteLibraryLoader();
    }
    instance.doLoad();
  }

  public void doLoad() {
    if (loaded) {
      return;
    }
    final long startTime = System.currentTimeMillis();
    File tempDir = Files.createTempDir();
    tempDir.deleteOnExit();
    File extractedLibraryPath = new File(tempDir, getLibName());
    try (FileOutputStream outputStream = new FileOutputStream(extractedLibraryPath)) {
      getLibraryByteSource().copyTo(outputStream);
    } catch (IOException e) {
      throw new RuntimeException("Cannot extract SQLite library into " + extractedLibraryPath, e);
    }
    loadFromDirectory(tempDir);
    logWithTime("SQLite natives prepared in", startTime);
  }

  public String getLibClasspathResourceName() {
    return "sqlite4java/" + getNativesResourcesPathPart() + "/" + getLibName();
  }

  private ByteSource getLibraryByteSource() {
    return Resources.asByteSource(Resources.getResource(getLibClasspathResourceName()));
  }

  private void logWithTime(final String message, final long startTime) {
    log(message + " " + (System.currentTimeMillis() - startTime));
  }

  private void log(final String message) {
    org.robolectric.util.Logger.debug(message);
  }

  @VisibleForTesting
  public boolean isLoaded() {
    return loaded;
  }

  public static boolean isOsSupported() {
    String prefix = getOsPrefix();
    String arch = getArchitecture();
    // We know macOS with aarch64 arch is not supported by sqlite4java now.
    return !(OS_MAC.equals(prefix) && OS_ARCH_ARM64.equals(arch));
  }

  private void loadFromDirectory(final File libPath) {
    // configure less verbose logging
    Logger.getLogger("com.almworks.sqlite4java").setLevel(Level.WARNING);

    SQLite.setLibraryPath(libPath.getAbsolutePath());
    try {
      log(
          "SQLite version: library "
              + SQLite.getLibraryVersion()
              + " / core "
              + SQLite.getSQLiteVersion());
    } catch (SQLiteException e) {
      throw new RuntimeException(e);
    }
    loaded = true;
  }

  private String getLibName() {
    return libraryNameMapper.mapLibraryName(SQLITE4JAVA);
  }

  private String getNativesResourcesPathPart() {
    String prefix = getOsPrefix();
    String suffix = getArchitectureSuffix(prefix);
    if (suffix != null) {
      return prefix + "-" + suffix;
    } else {
      return prefix;
    }
  }

  private static String getOsPrefix() {
    String name = System.getProperty(SYSTEM_PROPERTY_OS_NAME).toLowerCase(Locale.US);
    if (name.contains("win")) {
      return OS_WIN;
    } else if (name.contains("linux")) {
      return OS_LINUX;
    } else if (name.contains("mac")) {
      return OS_MAC;
    } else {
      throw new UnsupportedOperationException(
          "Platform '" + name + "' is not supported by SQLite library");
    }
  }

  private static String getArchitectureSuffix(String prefix) {
    String arch = getArchitecture();
    switch (prefix) {
      case OS_MAC:
        // Current sqlite4java doesn't support macOS aarch64.
        if (!OS_ARCH_ARM64.equals(arch)) {
          return null;
        }
        break;
      case OS_LINUX:
        switch (arch) {
          case "i386":
          case "x86":
            return "i386";
          case "x86_64":
          case "amd64":
            return "amd64";
        }
        break;
      case OS_WIN:
        switch (arch) {
          case "x86":
            return "x86";
          case "x86_64":
          case "amd64":
            return "x64";
        }
        break;
    }
    throw new UnsupportedOperationException(
        "Architecture '" + arch + "' is not supported by SQLite library");
  }

  private static String getArchitecture() {
    return System.getProperty(SYSTEM_PROPERTY_OS_ARCH).toLowerCase(Locale.US).replaceAll("\\W", "");
  }

  public interface LibraryNameMapper {
    String mapLibraryName(String name);
  }
}
