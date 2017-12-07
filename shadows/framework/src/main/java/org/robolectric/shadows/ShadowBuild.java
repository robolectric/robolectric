package org.robolectric.shadows;

import static org.robolectric.shadow.api.Shadow.directlyOn;

import android.os.Build;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;

@Implements(value = Build.class)
public class ShadowBuild {

  private static String radioVersionOverride = null;

  /**
   * Sets the value of the Build.FINGERPRINT field.
   *
   * <p>It will be reset for the next test.
   */
  public static void setFingerprint(String fingerprint) {
    ReflectionHelpers.setStaticField(Build.class, "FINGERPRINT", fingerprint);
  }

  /**
   * Sets the value of the Build.ID field.
   *
   * <p>It will be reset for the next test.
   */
  public static void setId(String id) {
    ReflectionHelpers.setStaticField(Build.class, "ID", id);
  }

  /**
   * Sets the value of the Build.MODEL field.
   *
   * <p>It will be reset for the next test.
   */
  public static void setModel(String model) {
    ReflectionHelpers.setStaticField(Build.class, "MODEL", model);
  }

  /**
   * Sets the value of the Build.VERSION.CODENAME field.
   *
   * <p>It will be reset for the next test.
   */
  public static void setVersionCodename(String versionCodename) {
    ReflectionHelpers.setStaticField(Build.VERSION.class, "CODENAME", versionCodename);
  }

  /**
   * Sets the value of the Build.VERSION.INCREMENTAL field.
   *
   * <p>It will be reset for the next test.
   */
  public static void setVersionIncremental(String versionIncremental) {
    ReflectionHelpers.setStaticField(Build.VERSION.class, "INCREMENTAL", versionIncremental);
  }

  /**
   * Sets the value of the Build.VERSION.RELEASE field.
   *
   * <p>It will be reset for the next test.
   */
  public static void setVersionRelease(String release) {
    ReflectionHelpers.setStaticField(Build.VERSION.class, "RELEASE", release);
  }

  /**
   * Sets the value of the Build.TAGS field.
   *
   * <p>It will be reset for the next test.
   */
  public static void setTags(String tags) {
    ReflectionHelpers.setStaticField(Build.class, "TAGS", tags);
  }

  /**
   * Override return value from {@link Build#getRadioVersion()}
   *
   * @param radioVersion
   */
  public static void setRadioVersion(String radioVersion) {
    radioVersionOverride = radioVersion;
  }

  @Implementation
  public static String getRadioVersion() {
    if (radioVersionOverride != null) {
      return radioVersionOverride;
    }
    return directlyOn(Build.class, "getRadioVersion");
  }

  @Resetter
  public static synchronized void reset() {
    radioVersionOverride = null;
    ReflectionHelpers.callStaticMethod(Build.class, "__staticInitializer__");
    ReflectionHelpers.callStaticMethod(Build.VERSION.class, "__staticInitializer__");
    // performStaticInitialization(Build.class);
  }
}
