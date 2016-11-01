package org.robolectric.annotation;

import android.app.Application;

import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;

/**
 * Configuration settings that can be used on a per-class or per-test basis.
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Config {
  /**
   * TODO(vnayar): Create named constants for default values instead of magic numbers.
   * Array named contants must be avoided in order to dodge a JDK 1.7 bug.
   *   error: annotation Config is missing value for the attribute &lt;clinit&gt;
   * See <a href="https://bugs.openjdk.java.net/browse/JDK-8013485">JDK-8013485</a>.
   */
  String NONE = "--none";
  String DEFAULT_MANIFEST = "--default";
  String DEFAULT_PACKAGE_NAME = "";
  String DEFAULT_ABI_SPLIT = "";
  String DEFAULT_QUALIFIERS = "";
  String DEFAULT_RES_FOLDER = "res";
  String DEFAULT_ASSET_FOLDER = "assets";
  String DEFAULT_BUILD_FOLDER = "build";

  /**
   * The Android SDK level to emulate. This value will also be set as Build.VERSION.SDK_INT.
   */
  int[] sdk() default {};  // DEFAULT_SDK

  /**
   * The minimum Android SDK level to emulate when running tests on multiple API versions.
   */
  int minSdk() default -1;

  /**
   * The minimum Android SDK level to emulate when running tests on multiple API versions.
   */
  int maxSdk() default -1;

  /**
   * The Android manifest file to load; Robolectric will look relative to the current directory.
   * Resources and assets will be loaded relative to the manifest.
   *
   * If not specified, Robolectric defaults to {@code AndroidManifest.xml}.
   *
   * If your project has no manifest or resources, use {@link Config#NONE}.
   *
   * @return The Android manifest file to load.
   */
  String manifest() default DEFAULT_MANIFEST;

  /**
   * Reference to the BuildConfig class created by the Gradle build system.
   *
   * @return Reference to BuildConfig class.
   */
  Class<?> constants() default Void.class;  // DEFAULT_CONSTANTS

  /**
   * The {@link android.app.Application} class to use in the test, this takes precedence over any application
   * specified in the AndroidManifest.xml.
   *
   * @return The {@link android.app.Application} class to use in the test.
   */
  Class<? extends Application> application() default Application.class;  // DEFAULT_APPLICATION

  /**
   * Java package name where the "R.class" file is located. This only needs to be specified if you define
   * an {@code applicationId} associated with {@code productFlavors} or specify {@code applicationIdSuffix}
   * in your build.gradle.
   *
   * <p>If not specified, Robolectric defaults to the {@code applicationId}.</p>
   *
   * @return The java package name for R.class.
   */
  String packageName() default DEFAULT_PACKAGE_NAME;

  /**
   * The ABI split to use when locating resources and AndroidManifest.xml
   *
   * <p>You do not typically have to set this, unless you are utilizing the ABI split feature</p>
   *
   * @return The ABI split to test with
   */
  String abiSplit() default DEFAULT_ABI_SPLIT;

  /**
   * Qualifiers for the resource resolution, such as "fr-normal-port-hdpi".
   *
   * @return Qualifiers used for resource resolution.
   */
  String qualifiers() default DEFAULT_QUALIFIERS;

  /**
   * The directory from which to load resources.  This should be relative to the directory containing AndroidManifest.xml.
   *
   * <p>If not specified, Robolectric defaults to {@code res}.</p>
   *
   * @return Android resource directory.
   */
  String resourceDir() default DEFAULT_RES_FOLDER;

  /**
   * The directory from which to load assets. This should be relative to the directory containing AndroidManifest.xml.
   *
   * <p>If not specified, Robolectric defaults to {@code assets}.</p>
   *
   * @return Android asset directory.
   */
  String assetDir() default DEFAULT_ASSET_FOLDER;

  /**
   * The directory where application files are created during the application build process.
   *
   * <p>If not specified, Robolectric defaults to {@code build}.</p>
   *
   * @return Android build directory.
   */
  String buildDir() default DEFAULT_BUILD_FOLDER;

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
   * @return A list of Android Libraries.
   */
  String[] libraries() default {};  // DEFAULT_LIBRARIES;

  class Implementation implements Config {
    private final int[] sdk;
    private final int minSdk;
    private final int maxSdk;
    private final String manifest;
    private final String qualifiers;
    private final String resourceDir;
    private final String assetDir;
    private final String buildDir;
    private final String packageName;
    private final String abiSplit;
    private final Class<?> constants;
    private final Class<?>[] shadows;
    private final String[] instrumentedPackages;
    private final Class<? extends Application> application;
    private final String[] libraries;

    public static Config fromProperties(Properties properties) {
      if (properties == null || properties.size() == 0) return null;
      return new Implementation(
          parseIntArrayProperty(properties.getProperty("sdk", "")),
          Integer.parseInt(properties.getProperty("minSdk", "-1")),
          Integer.parseInt(properties.getProperty("maxSdk", "-1")),
          properties.getProperty("manifest", DEFAULT_MANIFEST),
          properties.getProperty("qualifiers", DEFAULT_QUALIFIERS),
          properties.getProperty("packageName", DEFAULT_PACKAGE_NAME),
          properties.getProperty("abiSplit", DEFAULT_ABI_SPLIT),
          properties.getProperty("resourceDir", DEFAULT_RES_FOLDER),
          properties.getProperty("assetDir", DEFAULT_ASSET_FOLDER),
          properties.getProperty("buildDir", DEFAULT_BUILD_FOLDER),
          parseClasses(properties.getProperty("shadows", "")),
          parseStringArrayProperty(properties.getProperty("instrumentedPackages", "")),
          parseApplication(properties.getProperty("application", "android.app.Application")),
          parseStringArrayProperty(properties.getProperty("libraries", "")),
          parseClass(properties.getProperty("constants", ""))
      );
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
      final String[] classNames = input.split("[, ]+");
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

    private static int[] parseIntArrayProperty(String property) {
      String[] parts = parseStringArrayProperty(property);
      int[] result = new int[parts.length];
      for (int i = 0; i < parts.length; i++) {
        result[i] = Integer.parseInt(parts[i]);
      }

      return result;
    }

    public Implementation(int[] sdk, int minSdk, int maxSdk, String manifest, String qualifiers, String packageName, String abiSplit, String resourceDir, String assetDir, String buildDir, Class<?>[] shadows, String[] instrumentedPackages, Class<? extends Application> application, String[] libraries, Class<?> constants) {
      this.sdk = sdk;
      this.minSdk = minSdk;
      this.maxSdk = maxSdk;
      this.manifest = manifest;
      this.qualifiers = qualifiers;
      this.packageName = packageName;
      this.abiSplit = abiSplit;
      this.resourceDir = resourceDir;
      this.assetDir = assetDir;
      this.buildDir = buildDir;
      this.shadows = shadows;
      this.instrumentedPackages = instrumentedPackages;
      this.application = application;
      this.libraries = libraries;
      this.constants = constants;
    }

    public Implementation(Config other) {
      this.sdk = other.sdk();
      this.minSdk = other.minSdk();
      this.maxSdk = other.maxSdk();
      this.manifest = other.manifest();
      this.qualifiers = other.qualifiers();
      this.packageName = other.packageName();
      this.abiSplit = other.abiSplit();
      this.resourceDir = other.resourceDir();
      this.assetDir = other.assetDir();
      this.buildDir = other.buildDir();
      this.constants = other.constants();
      this.shadows = other.shadows();
      this.instrumentedPackages = other.instrumentedPackages();
      this.application = other.application();
      this.libraries = other.libraries();
    }

    public Implementation(Config baseConfig, Config overlayConfig) {
      this.sdk = pickSdk(baseConfig.sdk(), overlayConfig.sdk(), new int[0]);
      this.minSdk = pick(baseConfig.minSdk(), overlayConfig.minSdk(), -1);
      this.maxSdk = pick(baseConfig.maxSdk(), overlayConfig.maxSdk(), -1);
      this.manifest = pick(baseConfig.manifest(), overlayConfig.manifest(), DEFAULT_MANIFEST);
      this.qualifiers = pick(baseConfig.qualifiers(), overlayConfig.qualifiers(), "");
      this.packageName = pick(baseConfig.packageName(), overlayConfig.packageName(), "");
      this.abiSplit = pick(baseConfig.abiSplit(), overlayConfig.abiSplit(), "");
      this.resourceDir = pick(baseConfig.resourceDir(), overlayConfig.resourceDir(), Config.DEFAULT_RES_FOLDER);
      this.assetDir = pick(baseConfig.assetDir(), overlayConfig.assetDir(), Config.DEFAULT_ASSET_FOLDER);
      this.buildDir = pick(baseConfig.buildDir(), overlayConfig.buildDir(), Config.DEFAULT_BUILD_FOLDER);
      this.constants = pick(baseConfig.constants(), overlayConfig.constants(), Void.class);

      Set<Class<?>> shadows = new HashSet<>();
      shadows.addAll(Arrays.asList(baseConfig.shadows()));
      shadows.addAll(Arrays.asList(overlayConfig.shadows()));
      this.shadows = shadows.toArray(new Class[shadows.size()]);

      Set<String> instrumentedPackages = new HashSet<>();
      instrumentedPackages.addAll(Arrays.asList(baseConfig.instrumentedPackages()));
      instrumentedPackages.addAll(Arrays.asList(overlayConfig.instrumentedPackages()));
      this.instrumentedPackages = instrumentedPackages.toArray(new String[instrumentedPackages.size()]);

      this.application = pick(baseConfig.application(), overlayConfig.application(), Application.class);

      Set<String> libraries = new HashSet<>();
      libraries.addAll(Arrays.asList(baseConfig.libraries()));
      libraries.addAll(Arrays.asList(overlayConfig.libraries()));
      this.libraries = libraries.toArray(new String[libraries.size()]);
    }

    private <T> T pick(T baseValue, T overlayValue, T nullValue) {
      return overlayValue != null ? (overlayValue.equals(nullValue) ? baseValue : overlayValue) : null;
    }

    private int[] pickSdk(int[] baseValue, int[] overlayValue, int[] nullValue) {
      return Arrays.equals(overlayValue, nullValue) ? baseValue : overlayValue;
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
    public Class<?> constants() {
      return constants;
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
    public String abiSplit() {
      return abiSplit;
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
    public String buildDir() {
      return buildDir;
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

    @NotNull @Override
    public Class<? extends Annotation> annotationType() {
      return Config.class;
    }
  }

  class Builder {
    private int[] sdk = new int[0];
    private int minSdk = -1;
    private int maxSdk = -1;
    private String manifest = Config.DEFAULT_MANIFEST;
    private String qualifiers = Config.DEFAULT_QUALIFIERS;
    private String packageName = Config.DEFAULT_PACKAGE_NAME;
    private String abiSplit = Config.DEFAULT_ABI_SPLIT;
    private String resourceDir = Config.DEFAULT_RES_FOLDER;
    private String assetDir = Config.DEFAULT_ASSET_FOLDER;
    private String buildDir = Config.DEFAULT_BUILD_FOLDER;
    private Class<?>[] shadows = new Class[0];
    private String[] instrumentedPackages = new String[0];
    private Class<? extends Application> application = Application.class; // todo: make a private default dummy
    private String[] libraries = new String[0];
    private Class<?> constants = Void.class;

    public Builder setSdk(int[] sdk) {
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

    public Builder setAbiSplit(String abiSplit) {
      this.abiSplit = abiSplit;
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

    public Builder setBuildDir(String buildDir) {
      this.buildDir = buildDir;
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

    public Builder setConstants(Class<?> constants) {
      this.constants = constants;
      return this;
    }

    public Implementation build() {
      return new Implementation(sdk, minSdk, maxSdk, manifest, qualifiers, packageName, abiSplit, resourceDir, assetDir, buildDir, shadows, instrumentedPackages, application, libraries, constants);
    }
  }
}
