package org.robolectric.annotation;

import android.app.Application;
import android.content.pm.PackageInfo;
import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import javax.annotation.Nonnull;

/**
 * Configuration settings that can be used on a per-class or per-test basis.
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@SuppressWarnings(value = {"BadAnnotationImplementation", "ImmutableAnnotationChecker"})
public @interface Config {
  /**
   * TODO(vnayar): Create named constants for default values instead of magic numbers.
   * Array named constants must be avoided in order to dodge a JDK 1.7 bug.
   *   error: annotation Config is missing value for the attribute &lt;clinit&gt;
   * See <a href="https://bugs.openjdk.java.net/browse/JDK-8013485">JDK-8013485</a>.
   */
  String NONE = "--none";
  String DEFAULT_VALUE_STRING = "--default";
  int DEFAULT_VALUE_INT = -1;

  String DEFAULT_MANIFEST_NAME = "AndroidManifest.xml";
  Class<? extends Application> DEFAULT_APPLICATION = DefaultApplication.class;
  String DEFAULT_PACKAGE_NAME = "";
  String DEFAULT_QUALIFIERS = "";
  String DEFAULT_RES_FOLDER = "res";
  String DEFAULT_ASSET_FOLDER = "assets";

  int ALL_SDKS = -2;
  int TARGET_SDK = -3;
  int OLDEST_SDK = -4;
  int NEWEST_SDK = -5;

  /**
   * The Android SDK level to emulate. This value will also be set as Build.VERSION.SDK_INT.
   */
  int[] sdk() default {};  // DEFAULT_SDK

  /**
   * The minimum Android SDK level to emulate when running tests on multiple API versions.
   */
  int minSdk() default -1;

  /**
   * The maximum Android SDK level to emulate when running tests on multiple API versions.
   */
  int maxSdk() default -1;

  /**
   * The Android manifest file to load; Robolectric will look relative to the current directory.
   * Resources and assets will be loaded relative to the manifest.
   *
   * If not specified, Robolectric defaults to {@code AndroidManifest.xml}.
   *
   * If your project has no manifest or resources, use {@link Config#NONE}.
   * @deprecated If you are using at least Android Studio 3.0 alpha 5 or Bazel's android_local_test
   * please migrate to the preferred way to configure
   * builds http://robolectric.org/getting-started/
   *
   * @return The Android manifest file to load.
   */
  @Deprecated
  String manifest() default DEFAULT_VALUE_STRING;

  /**
   * The {@link android.app.Application} class to use in the test, this takes precedence over any application
   * specified in the AndroidManifest.xml.
   *
   * @return The {@link android.app.Application} class to use in the test.
   */
  Class<? extends Application> application() default DefaultApplication.class;  // DEFAULT_APPLICATION

  /**
   * Java package name where the "R.class" file is located. This only needs to be specified if you
   * define an {@code applicationId} associated with {@code productFlavors} or specify {@code
   * applicationIdSuffix} in your build.gradle.
   *
   * <p>If not specified, Robolectric defaults to the {@code applicationId}.
   *
   * @return The java package name for R.class.
   * @deprecated To change your package name please override the applicationId in your build system.
   *     Changing package name here is broken as the package name will no longer match the package
   *     name encoded in the arsc resources file. If you are looking to simulate another application
   *     you can create another applications Context using {@link
   *     android.content.Context#createPackageContext(String, int)}. Note that you must add this
   *     package to {@link org.robolectric.shadows.ShadowPackageManager#addPackage(PackageInfo)}
   *     first.
   */
  @Deprecated
  String packageName() default DEFAULT_PACKAGE_NAME;

  /**
   * Qualifiers specifying device configuration for this test, such as "fr-normal-port-hdpi".
   *
   * If the string is prefixed with '+', the qualifiers that follow are overlayed on any more
   * broadly-scoped qualifiers.
   *
   * See [Device Configuration](http://robolectric.org/device-configuration/) for details.
   *
   * @return Qualifiers used for device configuration and resource resolution.
   */
  String qualifiers() default DEFAULT_QUALIFIERS;

  /**
   * The directory from which to load resources.  This should be relative to the directory containing AndroidManifest.xml.
   *
   * If not specified, Robolectric defaults to {@code res}.
   * @deprecated If you are using at least Android Studio 3.0 alpha 5 or Bazel's android_local_test
   * please migrate to the preferred way to configure
   *
   * @return Android resource directory.
   */
  @Deprecated
  String resourceDir() default DEFAULT_RES_FOLDER;

  /**
   * The directory from which to load assets. This should be relative to the directory containing AndroidManifest.xml.
   *
   * If not specified, Robolectric defaults to {@code assets}.
   * @deprecated If you are using at least Android Studio 3.0 alpha 5 or Bazel's android_local_test
   * please migrate to the preferred way to configure
   *
   * @return Android asset directory.
   */
  @Deprecated
  String assetDir() default DEFAULT_ASSET_FOLDER;

  /**
   * A list of shadow classes to enable, in addition to those that are already present.
   *
   * @return A list of additional shadow classes to enable.
   */
  Class<?>[] shadows() default {};  // DEFAULT_SHADOWS

  /**
   * A list of instrumented packages, in addition to those that are already instrumented.
   *
   * @return A list of additional instrumented packages.
   */
  String[] instrumentedPackages() default {};  // DEFAULT_INSTRUMENTED_PACKAGES

  /**
   * A list of folders containing Android Libraries on which this project depends.
   *
   * @deprecated If you are using at least Android Studio 3.0 alpha 5 or Bazel's android_local_test
   * please migrate to the preferred way to configure
   *
   * @return A list of Android Libraries.
   */
  @Deprecated
  String[] libraries() default {};  // DEFAULT_LIBRARIES;

  class Implementation implements Config {
    private final int[] sdk;
    private final int minSdk;
    private final int maxSdk;
    private final String manifest;
    private final String qualifiers;
    private final String resourceDir;
    private final String assetDir;
    private final String packageName;
    private final Class<?>[] shadows;
    private final String[] instrumentedPackages;
    private final Class<? extends Application> application;
    private final String[] libraries;

    public static Config fromProperties(Properties properties) {
      if (properties == null || properties.size() == 0) return null;
      return new Implementation(
          parseSdkArrayProperty(properties.getProperty("sdk", "")),
          parseSdkInt(properties.getProperty("minSdk", "-1")),
          parseSdkInt(properties.getProperty("maxSdk", "-1")),
          properties.getProperty("manifest", DEFAULT_VALUE_STRING),
          properties.getProperty("qualifiers", DEFAULT_QUALIFIERS),
          properties.getProperty("packageName", DEFAULT_PACKAGE_NAME),
          properties.getProperty("resourceDir", DEFAULT_RES_FOLDER),
          properties.getProperty("assetDir", DEFAULT_ASSET_FOLDER),
          parseClasses(properties.getProperty("shadows", "")),
          parseStringArrayProperty(properties.getProperty("instrumentedPackages", "")),
          parseApplication(
              properties.getProperty("application", DEFAULT_APPLICATION.getCanonicalName())),
          parseStringArrayProperty(properties.getProperty("libraries", "")));
    }

    private static Class<?> parseClass(String className) {
      if (className.isEmpty()) return null;
      try {
        return Implementation.class.getClassLoader().loadClass(className);
      } catch (ClassNotFoundException e) {
        throw new RuntimeException("Could not load class: " + className);
      }
    }

    private static Class<?>[] parseClasses(String input) {
      if (input.isEmpty()) return new Class[0];
      final String[] classNames = input.split("[, ]+", 0);
      final Class[] classes = new Class[classNames.length];
      for (int i = 0; i < classNames.length; i++) {
        classes[i] = parseClass(classNames[i]);
      }
      return classes;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Application> Class<T> parseApplication(String className) {
      return (Class<T>) parseClass(className);
    }

    private static String[] parseStringArrayProperty(String property) {
      if (property.isEmpty()) return new String[0];
      return property.split("[, ]+");
    }

    private static int[] parseSdkArrayProperty(String property) {
      String[] parts = parseStringArrayProperty(property);
      int[] result = new int[parts.length];
      for (int i = 0; i < parts.length; i++) {
        result[i] = parseSdkInt(parts[i]);
      }

      return result;
    }

    private static int parseSdkInt(String part) {
      String spec = part.trim();
      switch (spec) {
        case "ALL_SDKS":
          return Config.ALL_SDKS;
        case "TARGET_SDK":
          return Config.TARGET_SDK;
        case "OLDEST_SDK":
          return Config.OLDEST_SDK;
        case "NEWEST_SDK":
          return Config.NEWEST_SDK;
        default:
          return Integer.parseInt(spec);
      }
    }

    private static void validate(Config config) {
      //noinspection ConstantConditions
      if (config.sdk() != null && config.sdk().length > 0 &&
          (config.minSdk() != DEFAULT_VALUE_INT || config.maxSdk() != DEFAULT_VALUE_INT)) {
        throw new IllegalArgumentException("sdk and minSdk/maxSdk may not be specified together" +
            " (sdk=" + Arrays.toString(config.sdk()) + ", minSdk=" + config.minSdk() + ", maxSdk=" + config.maxSdk() + ")");
      }

      if (config.minSdk() > DEFAULT_VALUE_INT && config.maxSdk() > DEFAULT_VALUE_INT && config.minSdk() > config.maxSdk()) {
        throw new IllegalArgumentException("minSdk may not be larger than maxSdk" +
            " (minSdk=" + config.minSdk() + ", maxSdk=" + config.maxSdk() + ")");
      }
    }

    public Implementation(
        int[] sdk,
        int minSdk,
        int maxSdk,
        String manifest,
        String qualifiers,
        String packageName,
        String resourceDir,
        String assetDir,
        Class<?>[] shadows,
        String[] instrumentedPackages,
        Class<? extends Application> application,
        String[] libraries) {
      this.sdk = sdk;
      this.minSdk = minSdk;
      this.maxSdk = maxSdk;
      this.manifest = manifest;
      this.qualifiers = qualifiers;
      this.packageName = packageName;
      this.resourceDir = resourceDir;
      this.assetDir = assetDir;
      this.shadows = shadows;
      this.instrumentedPackages = instrumentedPackages;
      this.application = application;
      this.libraries = libraries;

      validate(this);
    }

    @Override
    public int[] sdk() {
      return sdk;
    }

    @Override
    public int minSdk() {
      return minSdk;
    }

    @Override
    public int maxSdk() {
      return maxSdk;
    }

    @Override
    public String manifest() {
      return manifest;
    }

    @Override
    public Class<? extends Application> application() {
      return application;
    }

    @Override
    public String qualifiers() {
      return qualifiers;
    }

    @Override
    public String packageName() {
      return packageName;
    }

    @Override
    public String resourceDir() {
      return resourceDir;
    }

    @Override
    public String assetDir() {
      return assetDir;
    }

    @Override
    public Class<?>[] shadows() {
      return shadows;
    }

    @Override
    public String[] instrumentedPackages() {
      return instrumentedPackages;
    }

    @Override
    public String[] libraries() {
      return libraries;
    }

    @Nonnull @Override
    public Class<? extends Annotation> annotationType() {
      return Config.class;
    }
  }

  class Builder {
    protected int[] sdk = new int[0];
    protected int minSdk = -1;
    protected int maxSdk = -1;
    protected String manifest = Config.DEFAULT_VALUE_STRING;
    protected String qualifiers = Config.DEFAULT_QUALIFIERS;
    protected String packageName = Config.DEFAULT_PACKAGE_NAME;
    protected String resourceDir = Config.DEFAULT_RES_FOLDER;
    protected String assetDir = Config.DEFAULT_ASSET_FOLDER;
    protected Class<?>[] shadows = new Class[0];
    protected String[] instrumentedPackages = new String[0];
    protected Class<? extends Application> application = DEFAULT_APPLICATION;
    protected String[] libraries = new String[0];

    public Builder() {
    }

    public Builder(Config config) {
      sdk = config.sdk();
      minSdk = config.minSdk();
      maxSdk = config.maxSdk();
      manifest = config.manifest();
      qualifiers = config.qualifiers();
      packageName = config.packageName();
      resourceDir = config.resourceDir();
      assetDir = config.assetDir();
      shadows = config.shadows();
      instrumentedPackages = config.instrumentedPackages();
      application = config.application();
      libraries = config.libraries();
    }

    public Builder setSdk(int... sdk) {
      this.sdk = sdk;
      return this;
    }

    public Builder setMinSdk(int minSdk) {
      this.minSdk = minSdk;
      return this;
    }

    public Builder setMaxSdk(int maxSdk) {
      this.maxSdk = maxSdk;
      return this;
    }

    public Builder setManifest(String manifest) {
      this.manifest = manifest;
      return this;
    }

    public Builder setQualifiers(String qualifiers) {
      this.qualifiers = qualifiers;
      return this;
    }

    public Builder setPackageName(String packageName) {
      this.packageName = packageName;
      return this;
    }

    public Builder setResourceDir(String resourceDir) {
      this.resourceDir = resourceDir;
      return this;
    }

    public Builder setAssetDir(String assetDir) {
      this.assetDir = assetDir;
      return this;
    }

    public Builder setShadows(Class<?>[] shadows) {
      this.shadows = shadows;
      return this;
    }

    public Builder setInstrumentedPackages(String[] instrumentedPackages) {
      this.instrumentedPackages = instrumentedPackages;
      return this;
    }

    public Builder setApplication(Class<? extends Application> application) {
      this.application = application;
      return this;
    }

    public Builder setLibraries(String[] libraries) {
      this.libraries = libraries;
      return this;
    }

    /**
     * This returns actual default values where they exist, in the sense that we could use
     * the values, rather than markers like {@code -1} or {@code --default}.
     */
    public static Builder defaults() {
      return new Builder()
          .setManifest(DEFAULT_MANIFEST_NAME)
          .setResourceDir(DEFAULT_RES_FOLDER)
          .setAssetDir(DEFAULT_ASSET_FOLDER);
    }

    public Builder overlay(Config overlayConfig) {
      int[] overlaySdk = overlayConfig.sdk();
      int overlayMinSdk = overlayConfig.minSdk();
      int overlayMaxSdk = overlayConfig.maxSdk();

      //noinspection ConstantConditions
      if (overlaySdk != null && overlaySdk.length > 0) {
        this.sdk = overlaySdk;
        this.minSdk = overlayMinSdk;
        this.maxSdk = overlayMaxSdk;
      } else {
        if (overlayMinSdk != DEFAULT_VALUE_INT || overlayMaxSdk != DEFAULT_VALUE_INT) {
          this.sdk = new int[0];
        } else {
          this.sdk = pickSdk(this.sdk, overlaySdk, new int[0]);
        }
        this.minSdk = pick(this.minSdk, overlayMinSdk, DEFAULT_VALUE_INT);
        this.maxSdk = pick(this.maxSdk, overlayMaxSdk, DEFAULT_VALUE_INT);
      }
      this.manifest = pick(this.manifest, overlayConfig.manifest(), DEFAULT_VALUE_STRING);

      String qualifiersOverlayValue = overlayConfig.qualifiers();
      if (qualifiersOverlayValue != null && !qualifiersOverlayValue.equals("")) {
        if (qualifiersOverlayValue.startsWith("+")) {
          this.qualifiers = this.qualifiers + " " + qualifiersOverlayValue;
        } else {
          this.qualifiers = qualifiersOverlayValue;
        }
      }

      this.packageName = pick(this.packageName, overlayConfig.packageName(), "");
      this.resourceDir = pick(this.resourceDir, overlayConfig.resourceDir(), Config.DEFAULT_RES_FOLDER);
      this.assetDir = pick(this.assetDir, overlayConfig.assetDir(), Config.DEFAULT_ASSET_FOLDER);

      List<Class<?>> shadows = new ArrayList<>(Arrays.asList(this.shadows));
      shadows.addAll(Arrays.asList(overlayConfig.shadows()));
      this.shadows = shadows.toArray(new Class[shadows.size()]);

      Set<String> instrumentedPackages = new HashSet<>();
      instrumentedPackages.addAll(Arrays.asList(this.instrumentedPackages));
      instrumentedPackages.addAll(Arrays.asList(overlayConfig.instrumentedPackages()));
      this.instrumentedPackages = instrumentedPackages.toArray(new String[instrumentedPackages.size()]);

      this.application = pick(this.application, overlayConfig.application(), DEFAULT_APPLICATION);

      Set<String> libraries = new HashSet<>();
      libraries.addAll(Arrays.asList(this.libraries));
      libraries.addAll(Arrays.asList(overlayConfig.libraries()));
      this.libraries = libraries.toArray(new String[libraries.size()]);

      return this;
    }

    private <T> T pick(T baseValue, T overlayValue, T nullValue) {
      return overlayValue != null ? (overlayValue.equals(nullValue) ? baseValue : overlayValue) : null;
    }

    private int[] pickSdk(int[] baseValue, int[] overlayValue, int[] nullValue) {
      return Arrays.equals(overlayValue, nullValue) ? baseValue : overlayValue;
    }

    public Implementation build() {
      return new Implementation(
          sdk,
          minSdk,
          maxSdk,
          manifest,
          qualifiers,
          packageName,
          resourceDir,
          assetDir,
          shadows,
          instrumentedPackages,
          application,
          libraries);
    }

    public static boolean isDefaultApplication(Class<? extends Application> clazz) {
      return clazz == null || clazz.getCanonicalName().equals(DEFAULT_APPLICATION.getCanonicalName());
    }
  }
}
