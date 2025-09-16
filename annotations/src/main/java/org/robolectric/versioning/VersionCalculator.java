package org.robolectric.versioning;

import static android.os.Build.VERSION_CODES.CUR_DEVELOPMENT;

import java.io.IOException;
import java.util.Properties;
import java.util.jar.JarFile;
import java.util.zip.ZipEntry;

/**
 * Calculates Android versioning info from android-all jar files.
 *
 * <p>TODO: consider making this a pluginapi
 */
public class VersionCalculator {

  /**
   * Placeholder constant for the next major SDK int after {@link
   * android.os.Build.VERSION_CODES.BAKLAVA}.
   *
   * <p>This constant is intended to be used in Shadows @Implementation sdk clauses to provide
   * preliminary support for the next major SDK.
   */
  public static final int POST_BAKLAVA = CUR_DEVELOPMENT;

  public static class SdkInfo {
    public final int apiLevel;
    public final boolean isReleased;

    public SdkInfo(int apiLevel, boolean isReleased) {
      this.apiLevel = apiLevel;
      this.isReleased = isReleased;
    }
  }

  public SdkInfo calculateSdkInfo(JarFile androidAlljarFile) {
    try {
      ZipEntry buildProp = androidAlljarFile.getEntry("build.prop");
      Properties buildProps = new Properties();
      buildProps.load(androidAlljarFile.getInputStream(buildProp));
      if (buildProps.getProperty("ro.build.version.codename").equals("REL")) {
        String sdkVersionString = buildProps.getProperty("ro.build.version.sdk");
        return new SdkInfo(Integer.parseInt(sdkVersionString), true);
      } else {
        return new SdkInfo(CUR_DEVELOPMENT, false);
      }
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
  }

  public Integer getApiLevelForCodeName(String codename) {
    // support tests within android platform that still use codename Baklava as alias for running
    // on current SDK
    // TODO: remove this logic, or at minimum, move to an android-platform specific extension
    return "Baklava".equalsIgnoreCase(codename) ? POST_BAKLAVA : null;
  }
}
