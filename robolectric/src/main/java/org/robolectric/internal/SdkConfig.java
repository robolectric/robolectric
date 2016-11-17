package org.robolectric.internal;

import android.os.Build;
import org.robolectric.internal.dependency.DependencyJar;
import org.robolectric.util.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;

public class SdkConfig {
  private static final String ROBOLECTRIC_VERSION;
  private static final Map<Integer, SdkVersion> SUPPORTED_APIS;

  static {
    final Properties properties = getRobolectricVersionProperties();

    ROBOLECTRIC_VERSION = properties.getProperty("robolectric.version");

    SUPPORTED_APIS = Collections.unmodifiableMap(new HashMap<Integer, SdkVersion>() {
      private final double jdkVersion = Double.parseDouble(System.getProperty("java.specification.version"));

      {
        String knownSdks = properties.getProperty("robolectric.sdks");
        for (String sdkInfo : knownSdks.split(",")) {
          String[] parts = sdkInfo.split(":");
          addSdk(Integer.parseInt(parts[0]), parts[1], parts[2], parts[3]);
        }
      }

      private void addSdk(int sdkVersion, String androidVersion, String frameworkSdkBuildVersion, String minJdkVersion) {
        if (jdkVersion >= Double.parseDouble(minJdkVersion)) {
          put(sdkVersion, new SdkVersion(androidVersion, frameworkSdkBuildVersion));
        } else {
          Logger.info("Android SDK %s not supported on JDK %s (it requires %s)", sdkVersion, jdkVersion, minJdkVersion);
        }
      }
    });
  }

  public static final int FALLBACK_SDK_VERSION = Build.VERSION_CODES.JELLY_BEAN;
  public static final int MAX_SDK_VERSION = Collections.max(getSupportedApis());

  private final int apiLevel;
  private final SdkVersion sdkVersion;

  public static Set<Integer> getSupportedApis() {
    return SUPPORTED_APIS.keySet();
  }

  public SdkConfig(int apiLevel) {
    this.apiLevel = apiLevel;
    sdkVersion = SUPPORTED_APIS.get(apiLevel);
    if (sdkVersion == null) {
      throw new UnsupportedOperationException("Robolectric does not support API level " + apiLevel + ".");
    }
  }

  public int getApiLevel() {
    return apiLevel;
  }

  public String getAndroidVersion() {
    return sdkVersion.androidVersion;
  }

  public DependencyJar getAndroidSdkDependency() {
    return createDependency("org.robolectric", "android-all", sdkVersion.toString(), null);
  }

  public DependencyJar getCoreShadowsDependency() {
    return createDependency("org.robolectric", "shadows-core", ROBOLECTRIC_VERSION, null);
  }

  @Override
  public boolean equals(Object that) {
    return that == this || that instanceof SdkConfig && ((SdkConfig) that).sdkVersion.equals(sdkVersion);
  }

  @Override
  public String toString() {
    return "API Level " + apiLevel;
  }

  @Override
  public int hashCode() {
    return sdkVersion.hashCode();
  }

  private DependencyJar createDependency(String groupId, String artifactId, String version, String classifier) {
    return new DependencyJar(groupId, artifactId, version, classifier);
  }

  private static Properties getRobolectricVersionProperties() {
    ClassLoader classLoader = SdkVersion.class.getClassLoader();
    try (InputStream is = classLoader.getResourceAsStream("robolectric-version.properties")) {
      final Properties properties = new Properties();
      properties.load(is);
      return properties;
    } catch (IOException e) {
      throw new RuntimeException("Error loading robolectric-version.properties: " + e.getMessage());
    }
  }

  private static final class SdkVersion {
    private final String androidVersion;
    private final String robolectricVersion;

    SdkVersion(String androidVersion, String robolectricVersion) {
      this.androidVersion = androidVersion;
      this.robolectricVersion = robolectricVersion;
    }

    @Override
    public boolean equals(Object that) {
      return that == this || that instanceof SdkVersion && equals((SdkVersion) that);
    }

    public boolean equals(SdkVersion that) {
      return that == this ||
          Objects.equals(that.androidVersion, androidVersion) &&
              Objects.equals(that.robolectricVersion, robolectricVersion);
    }

    @Override
    public int hashCode() {
      return androidVersion.hashCode() * 31 + robolectricVersion.hashCode();
    }

    @Override
    public String toString() {
      return androidVersion + "-robolectric-" + robolectricVersion;
    }
  }
}
