package org.robolectric.annotation;

import android.app.Application;
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
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/** Configuration settings that can be used on a per-class or per-test basis. */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@SuppressWarnings(value = {"BadAnnotationImplementation", "ImmutableAnnotationChecker"})
public @interface Config {
  /**
   * TODO(vnayar): Create named constants for default values instead of magic numbers. Array named
   * constants must be avoided in order to dodge a JDK 1.7 bug. error: annotation Config is missing
   * value for the attribute &lt;clinit&gt; See <a
   * href="https://bugs.openjdk.java.net/browse/JDK-8013485">JDK-8013485</a>.
   */
  String NONE = "--none";

  String DEFAULT_VALUE_STRING = "--default";
  int DEFAULT_VALUE_INT = -1;
  float DEFAULT_FONT_SCALE = 1.0f;

  String DEFAULT_MANIFEST_NAME = "AndroidManifest.xml";
  Class<? extends Application> DEFAULT_APPLICATION = DefaultApplication.class;
  String DEFAULT_QUALIFIERS = "";

  int ALL_SDKS = -2;
  int TARGET_SDK = -3;
  int OLDEST_SDK = -4;
  int NEWEST_SDK = -5;

  /** The Android SDK level to emulate. This value will also be set as Build.VERSION.SDK_INT. */
  int @NonNull [] sdk() default {}; // DEFAULT_SDK

  /** The minimum Android SDK level to emulate when running tests on multiple API versions. */
  int minSdk() default -1;

  /** The maximum Android SDK level to emulate when running tests on multiple API versions. */
  int maxSdk() default -1;

  /**
   * The default font scale. In U+, users will have a slider to determine font scale. In all
   * previous APIs, font scales are either small (0.85f), normal (1.0f), large (1.15f) or huge
   * (1.3f)
   */
  float fontScale() default 1.0f;

  /**
   * The Android manifest file to load; Robolectric will look relative to the current directory.
   * Resources and assets will be loaded relative to the manifest.
   *
   * <p>If not specified, Robolectric defaults to {@code AndroidManifest.xml}.
   *
   * <p>If your project has no manifest or resources, use {@link Config#NONE}.
   *
   * @deprecated If you are using at least Android Studio 3.0 alpha 5 or Bazel's android_local_test
   *     please migrate to the preferred way to configure builds
   *     http://robolectric.org/getting-started/
   * @return The Android manifest file to load.
   */
  @Deprecated
  @NonNull String manifest() default DEFAULT_VALUE_STRING;

  /**
   * The {@link android.app.Application} class to use in the test, this takes precedence over any
   * application specified in the AndroidManifest.xml.
   *
   * @return The {@link android.app.Application} class to use in the test.
   */
  @Nullable Class<? extends Application> application() default
      DefaultApplication.class; // DEFAULT_APPLICATION

  /**
   * Qualifiers specifying device configuration for this test, such as "fr-normal-port-hdpi".
   *
   * <p>If the string is prefixed with '+', the qualifiers that follow are overlaid on any more
   * broadly-scoped qualifiers.
   *
   * @see <a href="http://robolectric.org/device-configuration">Device Configuration</a> for
   *     details.
   * @return Qualifiers used for device configuration and resource resolution.
   */
  @NonNull String qualifiers() default DEFAULT_QUALIFIERS;

  /**
   * A list of shadow classes to enable, in addition to those that are already present.
   *
   * @return A list of additional shadow classes to enable.
   */
  @NonNull Class<?> @NonNull [] shadows() default {}; // DEFAULT_SHADOWS

  /**
   * A list of instrumented packages, in addition to those that are already instrumented.
   *
   * @return A list of additional instrumented packages.
   */
  @NonNull String @NonNull [] instrumentedPackages() default {}; // DEFAULT_INSTRUMENTED_PACKAGES

  class Implementation implements Config {
    private final int @NonNull [] sdk;
    private final int minSdk;
    private final int maxSdk;
    private final float fontScale;
    private final @NonNull String manifest;
    private final @NonNull String qualifiers;
    private final @NonNull Class<?> @NonNull [] shadows;
    private final @NonNull String @NonNull [] instrumentedPackages;
    private final @Nullable Class<? extends Application> application;

    public static @Nullable Config fromProperties(@Nullable Properties properties) {
      if (properties == null || properties.isEmpty()) return null;
      return new Implementation(
          parseSdkArrayProperty(properties.getProperty("sdk", "")),
          parseSdkInt(properties.getProperty("minSdk", "-1")),
          parseSdkInt(properties.getProperty("maxSdk", "-1")),
          properties.getProperty("manifest", DEFAULT_VALUE_STRING),
          properties.getProperty("qualifiers", DEFAULT_QUALIFIERS),
          Float.parseFloat(properties.getProperty("fontScale", "1.0f")),
          parseClasses(properties.getProperty("shadows", "")),
          parseStringArrayProperty(properties.getProperty("instrumentedPackages", "")),
          parseApplication(
              properties.getProperty("application", DEFAULT_APPLICATION.getCanonicalName())));
    }

    private static @Nullable Class<?> parseClass(@NonNull String className) {
      if (className.isEmpty()) return null;
      try {
        return Implementation.class.getClassLoader().loadClass(className);
      } catch (ClassNotFoundException e) {
        throw new RuntimeException("Could not load class: " + className);
      }
    }

    private static @NonNull Class<?> @NonNull [] parseClasses(@NonNull String input) {
      if (input.isEmpty()) return new Class[0];
      final String[] classNames = input.split("[, ]+", 0);
      final Class<?>[] classes = new Class[classNames.length];
      for (int i = 0; i < classNames.length; i++) {
        classes[i] = parseClass(classNames[i]);
      }
      return classes;
    }

    @SuppressWarnings("unchecked")
    private static <T extends Application> @Nullable Class<T> parseApplication(
        @NonNull String className) {
      return (Class<T>) parseClass(className);
    }

    private static @NonNull String @NonNull [] parseStringArrayProperty(@NonNull String property) {
      if (property.isEmpty()) return new String[0];
      return property.split("[, ]+");
    }

    private static int @NonNull [] parseSdkArrayProperty(@NonNull String property) {
      String[] parts = parseStringArrayProperty(property);
      int[] result = new int[parts.length];
      for (int i = 0; i < parts.length; i++) {
        result[i] = parseSdkInt(parts[i]);
      }

      return result;
    }

    private static int parseSdkInt(@NonNull String part) {
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

    private static void validate(@NonNull Config config) {
      //noinspection ConstantConditions
      if (config.sdk() != null
          && config.sdk().length > 0
          && (config.minSdk() != DEFAULT_VALUE_INT || config.maxSdk() != DEFAULT_VALUE_INT)) {
        throw new IllegalArgumentException(
            "sdk and minSdk/maxSdk may not be specified together"
                + " (sdk="
                + Arrays.toString(config.sdk())
                + ", minSdk="
                + config.minSdk()
                + ", maxSdk="
                + config.maxSdk()
                + ")");
      }

      if (config.minSdk() > DEFAULT_VALUE_INT
          && config.maxSdk() > DEFAULT_VALUE_INT
          && config.minSdk() > config.maxSdk()) {
        throw new IllegalArgumentException(
            "minSdk may not be larger than maxSdk"
                + " (minSdk="
                + config.minSdk()
                + ", maxSdk="
                + config.maxSdk()
                + ")");
      }
    }

    public Implementation(
        int @NonNull [] sdk,
        int minSdk,
        int maxSdk,
        @NonNull String manifest,
        @NonNull String qualifiers,
        float fontScale,
        @NonNull Class<?> @NonNull [] shadows,
        @NonNull String @NonNull [] instrumentedPackages,
        @Nullable Class<? extends Application> application) {
      this.sdk = sdk;
      this.minSdk = minSdk;
      this.maxSdk = maxSdk;
      this.manifest = manifest;
      this.qualifiers = qualifiers;
      this.fontScale = fontScale;
      this.shadows = shadows;
      this.instrumentedPackages = instrumentedPackages;
      this.application = application;

      validate(this);
    }

    @Override
    public int @NonNull [] sdk() {
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
    public @NonNull String manifest() {
      return manifest;
    }

    @Override
    public float fontScale() {
      return fontScale;
    }

    @Override
    public @Nullable Class<? extends Application> application() {
      return application;
    }

    @Override
    public @NonNull String qualifiers() {
      return qualifiers;
    }

    @Override
    public @NonNull Class<?> @NonNull [] shadows() {
      return shadows;
    }

    @Override
    public @NonNull String @NonNull [] instrumentedPackages() {
      return instrumentedPackages;
    }

    @Override
    public @NonNull Class<? extends Annotation> annotationType() {
      return Config.class;
    }

    @Override
    public @NonNull String toString() {
      return "Implementation{"
          + "sdk="
          + Arrays.toString(sdk)
          + ", minSdk="
          + minSdk
          + ", maxSdk="
          + maxSdk
          + ", manifest='"
          + manifest
          + '\''
          + ", qualifiers='"
          + qualifiers
          + '\''
          + ", shadows="
          + Arrays.toString(shadows)
          + ", instrumentedPackages="
          + Arrays.toString(instrumentedPackages)
          + ", application="
          + application
          + '}';
    }
  }

  class Builder {
    protected int @NonNull [] sdk = new int[0];
    protected int minSdk = -1;
    protected int maxSdk = -1;
    protected float fontScale = 1.0f;
    protected @NonNull String manifest = Config.DEFAULT_VALUE_STRING;
    protected @NonNull String qualifiers = Config.DEFAULT_QUALIFIERS;
    protected @NonNull Class<?> @NonNull [] shadows = new Class[0];
    protected @NonNull String @NonNull [] instrumentedPackages = new String[0];
    protected @Nullable Class<? extends Application> application = DEFAULT_APPLICATION;

    public Builder() {}

    public Builder(@NonNull Config config) {
      sdk = config.sdk();
      minSdk = config.minSdk();
      maxSdk = config.maxSdk();
      manifest = config.manifest();
      qualifiers = config.qualifiers();
      fontScale = config.fontScale();
      shadows = config.shadows();
      instrumentedPackages = config.instrumentedPackages();
      application = config.application();
    }

    public @NonNull Builder setSdk(int... sdk) {
      this.sdk = sdk;
      return this;
    }

    public @NonNull Builder setMinSdk(int minSdk) {
      this.minSdk = minSdk;
      return this;
    }

    public @NonNull Builder setMaxSdk(int maxSdk) {
      this.maxSdk = maxSdk;
      return this;
    }

    public @NonNull Builder setManifest(@NonNull String manifest) {
      this.manifest = manifest;
      return this;
    }

    public @NonNull Builder setQualifiers(@NonNull String qualifiers) {
      this.qualifiers = qualifiers;
      return this;
    }

    public @NonNull Builder setFontScale(float fontScale) {
      this.fontScale = fontScale;
      return this;
    }

    public @NonNull Builder setShadows(@NonNull Class<?>... shadows) {
      this.shadows = shadows;
      return this;
    }

    public @NonNull Builder setInstrumentedPackages(@NonNull String... instrumentedPackages) {
      this.instrumentedPackages = instrumentedPackages;
      return this;
    }

    public @NonNull Builder setApplication(@Nullable Class<? extends Application> application) {
      this.application = application;
      return this;
    }

    /**
     * This returns actual default values where they exist, in the sense that we could use the
     * values, rather than markers like {@code -1} or {@code --default}.
     */
    public static @NonNull Builder defaults() {
      return new Builder().setManifest(DEFAULT_MANIFEST_NAME);
    }

    public @NonNull Builder overlay(@NonNull Config overlayConfig) {
      int[] overlaySdk = overlayConfig.sdk();
      int overlayMinSdk = overlayConfig.minSdk();
      int overlayMaxSdk = overlayConfig.maxSdk();
      float overlayFontScale = overlayConfig.fontScale();

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

      this.fontScale = pick(this.fontScale, overlayFontScale, DEFAULT_FONT_SCALE);

      String qualifiersOverlayValue = overlayConfig.qualifiers();
      if (!qualifiersOverlayValue.isEmpty()) {
        if (qualifiersOverlayValue.startsWith("+")) {
          this.qualifiers += " " + qualifiersOverlayValue;
        } else {
          this.qualifiers = qualifiersOverlayValue;
        }
      }

      List<Class<?>> shadows = new ArrayList<>(Arrays.asList(this.shadows));
      shadows.addAll(Arrays.asList(overlayConfig.shadows()));
      this.shadows = shadows.toArray(new Class[0]);

      Set<String> instrumentedPackages = new HashSet<>();
      instrumentedPackages.addAll(Arrays.asList(this.instrumentedPackages));
      instrumentedPackages.addAll(Arrays.asList(overlayConfig.instrumentedPackages()));
      this.instrumentedPackages = instrumentedPackages.toArray(new String[0]);
      this.application = pick(this.application, overlayConfig.application(), DEFAULT_APPLICATION);

      return this;
    }

    private <T> @Nullable T pick(
        @Nullable T baseValue, @Nullable T overlayValue, @NonNull T nullValue) {
      return overlayValue != null
          ? (overlayValue.equals(nullValue) ? baseValue : overlayValue)
          : null;
    }

    private int[] pickSdk(
        int @NonNull [] baseValue, int @NonNull [] overlayValue, int @NonNull [] nullValue) {
      return Arrays.equals(overlayValue, nullValue) ? baseValue : overlayValue;
    }

    public @NonNull Implementation build() {
      return new Implementation(
          sdk,
          minSdk,
          maxSdk,
          manifest,
          qualifiers,
          fontScale,
          shadows,
          instrumentedPackages,
          application);
    }

    public static boolean isDefaultApplication(@Nullable Class<? extends Application> clazz) {
      return clazz == null
          || clazz.getCanonicalName().equals(DEFAULT_APPLICATION.getCanonicalName());
    }
  }
}
