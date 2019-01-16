package org.robolectric.plugins;

import com.google.auto.service.AutoService;
import com.google.common.annotations.VisibleForTesting;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;
import javax.annotation.Nonnull;
import org.robolectric.pluginapi.Sdk;
import org.robolectric.pluginapi.SdkProvider;

/**
 * An {@link SdkProvider} that loads Android SDK jars from local filesystem location given by value
 * of 'robolectric.dependency.dir' system property
 */
@SuppressWarnings("NewApi")
@AutoService(SdkProvider.class)
public class LocalSdkProvider implements SdkProvider {

  private final Map<Integer, Sdk> sdks = new HashMap<>();

  public LocalSdkProvider() {
    // TODO: implement this to read robolectric.dependency.dir
  }

  @VisibleForTesting
  LocalSdkProvider(Path dir) throws IOException {
    try (DirectoryStream<Path> stream =
        Files.newDirectoryStream(dir, "*.jar")) {
      for (Path jarPath: stream) {
        try {
          Sdk sdk = loadSdkFromJar(jarPath);
          if (sdk != null) {
            sdks.put(sdk.getApiLevel(), sdk);
          }
        } catch (IOException e) {}
      }
    }
  }

  private static Sdk loadSdkFromJar(Path jarPath) throws IOException {
    JarFile jarFile = new JarFile(jarPath.toString());
    ZipEntry buildPropEntry = jarFile.getEntry("build.prop");
    if (buildPropEntry != null) {
      Properties buildProp = new Properties();
      try (InputStream is = jarFile.getInputStream(buildPropEntry)) {
        buildProp.load(is);
        return loadSdk(jarPath, buildProp);
      }
    }
    return null;
  }

  private static Sdk loadSdk(Path jarPath, Properties buildProp) {
    return new LocalSdk(
        Integer.parseInt(buildProp.getProperty("ro.build.version.sdk")),
        buildProp.getProperty("ro.build.version.release"),
        buildProp.getProperty("ro.build.version.codename"),
        jarPath);
    // TODO: figure out java version
  }

  @Override
  public Sdk getMaxKnownSdk() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Sdk getMaxSupportedSdk() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Sdk getSdk(int apiLevel) {
    return sdks.get(apiLevel);
  }

  @Override
  public Collection<Sdk> getSupportedSdks() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Collection<Sdk> getKnownSdks() {
    return sdks.values();
  }

  private static class LocalSdk implements Sdk {

    private final int apiLevel;
    private final String androidVersion;
    private final String androidCodeName;
    private final Path jarPath;

    private LocalSdk(int apiLevel, String androidVersion, String androidCodeName,
        Path jarPath) {
      this.apiLevel = apiLevel;
      this.androidVersion = androidVersion;
      this.androidCodeName = androidCodeName;
      this.jarPath = jarPath;
    }

    @Override
    public int getApiLevel() {
      return apiLevel;
    }

    @Override
    public String getAndroidVersion() {
      return androidVersion;
    }

    @Override
    public String getAndroidCodeName() {
      return androidCodeName;
    }

    @Override
    public Path getJarPath() {
      return jarPath;
    }

    @Override
    public boolean isKnown() {
      // TODO: handle
      return true;
    }

    @Override
    public boolean isSupported() {
      // TODO: handle
      return true;
    }

    @Override
    public int compareTo(@Nonnull Sdk o) {
      return apiLevel - o.getApiLevel();
    }
  }
}
