package org.robolectric.annotation;

import android.app.Application;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
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
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Config {
  String NONE = "--none";
  String DEFAULT = "--default";
  String DEFAULT_RES_FOLDER = "res";
  String DEFAULT_ASSET_FOLDER = "assets";

  /**
   * The Android SDK level to emulate. If not specified, Robolectric defaults to API 16.
   *
   * @return The Android SDK level to emulate.
   */
  int emulateSdk() default -1;

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
  String manifest() default DEFAULT;

  /**
   * Reference to the BuildConfig class created by the Gradle build system.
   *
   * @return Reference to BuildConfig class.
   */
  Class<?> constants() default Void.class;

  /**
   * The {@link android.app.Application} class to use in the test, this takes precedence over any application
   * specified in the AndroidManifest.xml.
   *
   * @return The {@link android.app.Application} class to use in the test.
   */
  Class<? extends Application> application() default Application.class;

  /**
   * Qualifiers for the resource resolution, such as "fr-normal-port-hdpi".
   *
   * @return Qualifiers used for resource resolution.
   */
  String qualifiers() default "";

  /**
   * The directory from which to load resources.  This should be relative to the directory containing AndroidManifest.xml.
   *
   * <p>
   * If not specified, Robolectric defaults to {@code res}.
   *
   * @return Android resource directory.
   */
  String resourceDir() default DEFAULT_RES_FOLDER;

  /**
   * The directory from which to load assets. This should be relative to the directory containing AndroidManifest.xml.
   *
   * <p>
   * If not specified, Robolectric defaults to {@code assets}.
   *
   * @return Android asset directory.
   */
  String assetDir() default DEFAULT_ASSET_FOLDER;

  /**
   * The Android SDK level to report in Build.VERSION.SDK_INT.
   *
   * @return The Android SDK level to report.
   */
  int reportSdk() default -1;

  /**
   * A list of shadow classes to enable, in addition to those that are already present.
   *
   * @return A list of additional shadow classes to enable.
   */
  Class<?>[] shadows() default {};

  /**
   * A list of folders containing Android Libraries on which this project depends.
   *
   * @return A list of Android Libraries.
   */
  String[] libraries() default {};

  class Implementation implements Config {
    private final int reportSdk;
    private final int emulateSdk;
    private final String manifest;
    private final String qualifiers;
    private final String resourceDir;
    private final String assetDir;
    private final Class<?> constants;
    private final Class<?>[] shadows;
    private final Class<? extends Application> application;
    private final String[] libraries;

    public static Config fromProperties(Properties properties) {
      if (properties == null || properties.size() == 0) return null;
      return new Implementation(
          Integer.parseInt(properties.getProperty("emulateSdk", "-1")),
          properties.getProperty("manifest", DEFAULT),
          properties.getProperty("qualifiers", ""),
          properties.getProperty("resourceDir", Config.DEFAULT_RES_FOLDER),
          properties.getProperty("assetDir", Config.DEFAULT_ASSET_FOLDER),
          Integer.parseInt(properties.getProperty("reportSdk", "-1")),
          parseClasses(properties.getProperty("shadows", "")),
          parseApplication(properties.getProperty("application", "android.app.Application")),
          parsePaths(properties.getProperty("libraries", "")),
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

    private static String[] parsePaths(String pathList) {
      if (pathList.isEmpty()) return new String[0];
      return pathList.split("[, ]+");
    }

    public Implementation(int emulateSdk, String manifest, String qualifiers, String resourceDir, String assetDir, int reportSdk, Class<?>[] shadows, Class<? extends Application> application, String[] libraries, Class<?> constants) {
      this.emulateSdk = emulateSdk;
      this.manifest = manifest;
      this.qualifiers = qualifiers;
      this.resourceDir = resourceDir;
      this.assetDir = assetDir;
      this.reportSdk = reportSdk;
      this.shadows = shadows;
      this.application = application;
      this.libraries = libraries;
      this.constants = constants;
    }

    public Implementation(Config other) {
      this.reportSdk = other.reportSdk();
      this.emulateSdk = other.emulateSdk();
      this.manifest = other.manifest();
      this.qualifiers = other.qualifiers();
      this.resourceDir = other.resourceDir();
      this.assetDir = other.assetDir();
      this.constants = other.constants();
      this.shadows = other.shadows();
      this.application = other.application();
      this.libraries = other.libraries();
    }

    public Implementation(Config baseConfig, Config overlayConfig) {
      this.emulateSdk = pick(baseConfig.emulateSdk(), overlayConfig.emulateSdk(), -1);
      this.manifest = pick(baseConfig.manifest(), overlayConfig.manifest(), DEFAULT);
      this.qualifiers = pick(baseConfig.qualifiers(), overlayConfig.qualifiers(), "");
      this.resourceDir = pick(baseConfig.resourceDir(), overlayConfig.resourceDir(), Config.DEFAULT_RES_FOLDER);
      this.assetDir = pick(baseConfig.assetDir(), overlayConfig.assetDir(), Config.DEFAULT_ASSET_FOLDER);
      this.reportSdk = pick(baseConfig.reportSdk(), overlayConfig.reportSdk(), -1);
      this.constants = pick(baseConfig.constants(), overlayConfig.constants(), null);

      Set<Class<?>> shadows = new HashSet<>();
      shadows.addAll(Arrays.asList(baseConfig.shadows()));
      shadows.addAll(Arrays.asList(overlayConfig.shadows()));
      this.shadows = shadows.toArray(new Class[shadows.size()]);

      this.application = pick(baseConfig.application(), overlayConfig.application(), null);

      Set<String> libraries = new HashSet<>();
      libraries.addAll(Arrays.asList(baseConfig.libraries()));
      libraries.addAll(Arrays.asList(overlayConfig.libraries()));
      this.libraries = libraries.toArray(new String[libraries.size()]);
    }

    private <T> T pick(T baseValue, T overlayValue, T nullValue) {
      return overlayValue != null ? (overlayValue.equals(nullValue) ? baseValue : overlayValue) : null;
    }

    @Override
    public int emulateSdk() {
      return emulateSdk;
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
    public String resourceDir() {
      return resourceDir;
    }

    @Override
    public String assetDir() {
      return assetDir;
    }

    @Override
    public int reportSdk() {
      return reportSdk;
    }

    @Override
    public Class<?>[] shadows() {
      return shadows;
    }

    @Override
    public String[] libraries() {
      return libraries;
    }

    @NotNull @Override
    public Class<? extends Annotation> annotationType() {
      return Config.class;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      Implementation other = (Implementation) o;

      if (emulateSdk != other.emulateSdk) return false;
      if (reportSdk != other.reportSdk) return false;
      if (!qualifiers.equals(other.qualifiers)) return false;
      if (!Arrays.equals(shadows, other.shadows)) return false;
      if (application != other.application) return false;

      return true;
    }

    @Override
    public int hashCode() {
      int result = emulateSdk;
      result = 31 * result + qualifiers.hashCode();
      result = 31 * result + reportSdk;
      result = 31 * result + Arrays.hashCode(shadows);
      result = 31 * result + application.hashCode();
      return result;
    }
  }
}
