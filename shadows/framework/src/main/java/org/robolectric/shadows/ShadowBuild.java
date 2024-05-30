package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.M;
import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.S;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.annotation.TargetApi;
import android.os.Build;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

@Implements(value = Build.class)
public class ShadowBuild {

  private static String radioVersionOverride = null;
  private static String serialOverride = Build.UNKNOWN;

  /**
   * Sets the value of the {@link Build#BOARD} field.
   *
   * <p>It will be reset for the next test.
   */
  public static void setBoard(String board) {
    ReflectionHelpers.setStaticField(Build.class, "BOARD", board);
  }

  /**
   * Sets the value of the {@link Build#DEVICE} field.
   *
   * <p>It will be reset for the next test.
   */
  public static void setDevice(String device) {
    ReflectionHelpers.setStaticField(Build.class, "DEVICE", device);
  }

  /**
   * Sets the value of the {@link Build#FINGERPRINT} field.
   *
   * <p>It will be reset for the next test.
   */
  public static void setFingerprint(String fingerprint) {
    ReflectionHelpers.setStaticField(Build.class, "FINGERPRINT", fingerprint);
  }

  /**
   * Sets the value of the {@link Build#ID} field.
   *
   * <p>It will be reset for the next test.
   */
  public static void setId(String id) {
    ReflectionHelpers.setStaticField(Build.class, "ID", id);
  }

  /**
   * Sets the value of the {@link Build#PRODUCT} field.
   *
   * <p>It will be reset for the next test.
   */
  public static void setProduct(String product) {
    ReflectionHelpers.setStaticField(Build.class, "PRODUCT", product);
  }

  /**
   * Sets the value of the {@link Build#IS_DEBUGGABLE} field.
   *
   * <p>It will be reset for the next test.
   */
  public static void setDebuggable(Boolean isDebuggable) {
    ReflectionHelpers.setStaticField(Build.class, "IS_DEBUGGABLE", isDebuggable);
  }

  /**
   * Sets the value of the {@link Build#MODEL} field.
   *
   * <p>It will be reset for the next test.
   */
  public static void setModel(String model) {
    ReflectionHelpers.setStaticField(Build.class, "MODEL", model);
  }

  /**
   * Sets the value of the {@link Build#MANUFACTURER} field.
   *
   * <p>It will be reset for the next test.
   */
  public static void setManufacturer(String manufacturer) {
    ReflectionHelpers.setStaticField(Build.class, "MANUFACTURER", manufacturer);
  }

  /**
   * Sets the value of the {@link Build#BRAND} field.
   *
   * <p>It will be reset for the next test.
   */
  public static void setBrand(String brand) {
    ReflectionHelpers.setStaticField(Build.class, "BRAND", brand);
  }

  /**
   * Sets the value of the {@link Build#HARDWARE} field.
   *
   * <p>It will be reset for the next test.
   */
  public static void setHardware(String hardware) {
    ReflectionHelpers.setStaticField(Build.class, "HARDWARE", hardware);
  }

  /** Override return value from {@link Build#getSerial()}. */
  public static void setSerial(String serial) {
    serialOverride = serial;
  }

  /**
   * Sets the value of the {@link Build.VERSION#CODENAME} field.
   *
   * <p>It will be reset for the next test.
   */
  public static void setVersionCodename(String versionCodename) {
    ReflectionHelpers.setStaticField(Build.VERSION.class, "CODENAME", versionCodename);
  }

  /**
   * Sets the value of the {@link Build.VERSION#INCREMENTAL} field.
   *
   * <p>It will be reset for the next test.
   */
  public static void setVersionIncremental(String versionIncremental) {
    ReflectionHelpers.setStaticField(Build.VERSION.class, "INCREMENTAL", versionIncremental);
  }

  /**
   * Sets the value of the {@link Build.VERSION#MEDIA_PERFORMANCE_CLASS} field. Available in Android
   * S+.
   *
   * <p>It will be reset for the next test.
   */
  @TargetApi(S)
  public static void setVersionMediaPerformanceClass(int performanceClass) {
    ReflectionHelpers.setStaticField(
        Build.VERSION.class, "MEDIA_PERFORMANCE_CLASS", performanceClass);
  }

  /**
   * Sets the value of the {@link Build.VERSION#RELEASE} field.
   *
   * <p>It will be reset for the next test.
   */
  public static void setVersionRelease(String release) {
    ReflectionHelpers.setStaticField(Build.VERSION.class, "RELEASE", release);
  }

  /**
   * Sets the value of the {@link Build.VERSION#SECURITY_PATCH} field. Available in Android M+.
   *
   * <p>It will be reset for the next test.
   */
  @TargetApi(M)
  public static void setVersionSecurityPatch(String securityPatch) {
    ReflectionHelpers.setStaticField(Build.VERSION.class, "SECURITY_PATCH", securityPatch);
  }

  /**
   * Sets the value of the {@link Build#TAGS} field.
   *
   * <p>It will be reset for the next test.
   */
  public static void setTags(String tags) {
    ReflectionHelpers.setStaticField(Build.class, "TAGS", tags);
  }

  /**
   * Sets the value of the {@link Build#TYPE} field.
   *
   * <p>It will be reset for the next test.
   */
  public static void setType(String type) {
    ReflectionHelpers.setStaticField(Build.class, "TYPE", type);
  }

  /**
   * Sets the value of the {@link Build#SUPPORTED_32_BIT_ABIS} field.
   *
   * <p>It will be reset for the next test.
   */
  public static void setSupported32BitAbis(String[] supported32BitAbis) {
    ReflectionHelpers.setStaticField(Build.class, "SUPPORTED_32_BIT_ABIS", supported32BitAbis);
  }

  /**
   * Sets the value of the {@link Build#SUPPORTED_64_BIT_ABIS} field.
   *
   * <p>It will be reset for the next test.
   */
  public static void setSupported64BitAbis(String[] supported64BitAbis) {
    ReflectionHelpers.setStaticField(Build.class, "SUPPORTED_64_BIT_ABIS", supported64BitAbis);
  }

  /**
   * Sets the value of the {@link Build#SUPPORTED_ABIS} field.
   *
   * <p>It will be reset for the next test.
   */
  public static void setSupportedAbis(String[] supportedAbis) {
    ReflectionHelpers.setStaticField(Build.class, "SUPPORTED_ABIS", supportedAbis);
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
    return reflector(_Build_.class).getRadioVersion();
  }

  @Implementation(minSdk = O)
  protected static String getSerial() {
    return serialOverride;
  }

  @Resetter
  public static synchronized void reset() {
    radioVersionOverride = null;
    serialOverride = Build.UNKNOWN;
    reflector(_Build_.class).__staticInitializer__();
    reflector(_VERSION_.class).__staticInitializer__();
  }

  /** Reflector interface for {@link Build}. */
  @ForType(Build.class)
  private interface _Build_ {

    @Static
    void __staticInitializer__();

    @Static
    @Direct
    String getRadioVersion();
  }

  /** Reflector interface for {@link Build.VERSION}. */
  @ForType(Build.VERSION.class)
  private interface _VERSION_ {

    @Static
    void __staticInitializer__();
  }
}
