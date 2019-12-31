package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.O;
import static org.robolectric.shadow.api.Shadow.directlyOn;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.annotation.TargetApi;
import android.os.Build;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

@Implements(value = Build.class)
public class ShadowBuild {

  private static String radioVersionOverride = null;

  /**
   * Sets the value of the {@link Build#DEVICE} field.
   *
   * It will be reset for the next test.
   */
  public static void setDevice(String device) {
    ReflectionHelpers.setStaticField(Build.class, "DEVICE", device);
  }

  /**
   * Sets the value of the {@link Build#FINGERPRINT} field.
   *
   * It will be reset for the next test.
   */
  public static void setFingerprint(String fingerprint) {
    ReflectionHelpers.setStaticField(Build.class, "FINGERPRINT", fingerprint);
  }

  /**
   * Sets the value of the {@link Build#ID} field.
   *
   * It will be reset for the next test.
   */
  public static void setId(String id) {
    ReflectionHelpers.setStaticField(Build.class, "ID", id);
  }

  /**
   * Sets the value of the {@link Build#MODEL} field.
   *
   * It will be reset for the next test.
   */
  public static void setModel(String model) {
    ReflectionHelpers.setStaticField(Build.class, "MODEL", model);
  }

  /**
   * Sets the value of the {@link Build#MANUFACTURER} field.
   *
   * It will be reset for the next test.
   */
  public static void setManufacturer(String manufacturer) {
    ReflectionHelpers.setStaticField(Build.class, "MANUFACTURER", manufacturer);
  }

  /**
   * Sets the value of the {@link Build.VERSION#CODENAME} field.
   *
   * It will be reset for the next test.
   */
  public static void setVersionCodename(String versionCodename) {
    ReflectionHelpers.setStaticField(Build.VERSION.class, "CODENAME", versionCodename);
  }

  /**
   * Sets the value of the {@link Build.VERSION#INCREMENTAL} field.
   *
   * It will be reset for the next test.
   */
  public static void setVersionIncremental(String versionIncremental) {
    ReflectionHelpers.setStaticField(Build.VERSION.class, "INCREMENTAL", versionIncremental);
  }

  /**
   * Sets the value of the {@link Build.VERSION#RELEASE} field.
   *
   * It will be reset for the next test.
   */
  public static void setVersionRelease(String release) {
    ReflectionHelpers.setStaticField(Build.VERSION.class, "RELEASE", release);
  }

  /**
   * Sets the value of the {@link Build#TAGS} field.
   *
   * It will be reset for the next test.
   */
  public static void setTags(String tags) {
    ReflectionHelpers.setStaticField(Build.class, "TAGS", tags);
  }

  /**
   * Sets the value of the {@link Build#TYPE} field.
   *
   * It will be reset for the next test.
   */
  public static void setType(String type) {
    ReflectionHelpers.setStaticField(Build.class, "TYPE", type);
  }

  /**
   * Sets the value of the {@link Build#SUPPORTED_64_BIT_ABIS} field. Available in Android L+.
   *
   * <p>It will be reset for the next test.
   */
  @TargetApi(LOLLIPOP)
  public static void setSupported64BitAbis(String[] supported64BitAbis) {
    ReflectionHelpers.setStaticField(Build.class, "SUPPORTED_64_BIT_ABIS", supported64BitAbis);
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
  protected static String getRadioVersion() {
    if (radioVersionOverride != null) {
      return radioVersionOverride;
    }
    return directlyOn(Build.class, "getRadioVersion");
  }

  @Implementation(minSdk = O)
  protected static String getSerial() {
    return Build.UNKNOWN;
  }

  @Resetter
  public static synchronized void reset() {
    radioVersionOverride = null;
    reflector(_Build_.class).__staticInitializer__();
    reflector(_VERSION_.class).__staticInitializer__();
  }

  /** Accessor interface for {@link Build}. */
  @ForType(Build.class)
  private interface _Build_ {

    @Static
    void __staticInitializer__();
  }

  /** Accessor interface for {@link Build.VERSION}. */
  @ForType(Build.VERSION.class)
  private interface _VERSION_ {

    @Static
    void __staticInitializer__();
  }

}
