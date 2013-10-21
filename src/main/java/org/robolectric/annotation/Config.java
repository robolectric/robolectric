package org.robolectric.annotation;

import android.os.Build;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Properties;

/**
 * Indicate that robolectric should look for values that is specific by those qualifiers
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
public @interface Config {
  @SuppressWarnings("UnusedDeclaration")
  public static final String NONE = "--none";
  public static final String DEFAULT = "--default";
  public static final int DEFAULT_SDK_LEVEL = Build.VERSION_CODES.JELLY_BEAN;

  /**
   * The Android SDK level to emulate. If not specified, Robolectric defaults to API 16.
   */
  int emulateSdk() default DEFAULT_SDK_LEVEL;

  /**
   * The Android manifest file to load; Robolectric will look relative to the current directory.
   * Resources and assets will be loaded relative to the manifest.
   *
   * If not specified, Robolectric defaults to {@code AndroidManifest.xml}.
   *
   * If your project has no manifest or resources, use {@link Config#NONE}.
   */
  String manifest() default DEFAULT;

  /**
   * Qualifiers for the resource resolution, such as "fr-normal-port-hdpi".
   *
   * @see <a href="http://developer.android.com/guide/topics/resources/providing-resources.html">Providing Resources</a> in the Android Developer docs for more information.
   */
  String qualifiers() default "";

  /**
   * The Android SDK level to report in Build.VERSION.SDK_INT.
   *
   * @see <a href="http://en.wikipedia.org/wiki/Android_version_history">Android Version History</a>.
   */
  int reportSdk() default -1;

  /**
   * A list of shadow classes to enable, in addition to those that are already present.
   */
  Class<?>[] shadows() default {};

  public class Implementation implements Config {
    private final int emulateSdk;
    private final String manifest;
    private final String qualifiers;
    private final int reportSdk;
    private final Class<?>[] shadows;

    public static Config fromProperties(Properties configProperties) {
      if (configProperties == null || configProperties.size() == 0) return null;
      return new Implementation(
          Integer.parseInt(configProperties.getProperty("emulateSdk", "-1")),
          configProperties.getProperty("manifest", DEFAULT),
          configProperties.getProperty("qualifiers", ""),
          Integer.parseInt(configProperties.getProperty("reportSdk", "-1")),
          parseClasses(configProperties.getProperty("shadows", ""))
      );
    }

    private static Class<?>[] parseClasses(String classList) {
      if (classList.length() == 0) return new Class[0];
      String[] classNames = classList.split("[, ]+");
      Class[] classes = new Class[classNames.length];
      for (int i = 0; i < classNames.length; i++) {
        try {
          classes[i] = Implementation.class.getClassLoader().loadClass(classNames[i]);
        } catch (ClassNotFoundException e) {
          throw new RuntimeException(e);
        }
      }
      return classes;
    }

    public Implementation(int emulateSdk, String manifest, String qualifiers, int reportSdk, Class<?>[] shadows) {
      this.emulateSdk = emulateSdk;
      this.manifest = manifest;
      this.qualifiers = qualifiers;
      this.reportSdk = reportSdk;
      this.shadows = shadows;
    }

    public Implementation(Config baseConfig, Config overlayConfig) {
      this.emulateSdk = pick(baseConfig.emulateSdk(), overlayConfig.emulateSdk(), DEFAULT_SDK_LEVEL);
      this.manifest = pick(baseConfig.manifest(), overlayConfig.manifest(), DEFAULT);
      this.qualifiers = pick(baseConfig.qualifiers(), overlayConfig.qualifiers(), "");
      this.reportSdk = pick(baseConfig.reportSdk(), overlayConfig.reportSdk(), -1);
      ArrayList<Class<?>> shadows = new ArrayList<Class<?>>();
      shadows.addAll(Arrays.asList(baseConfig.shadows()));
      shadows.addAll(Arrays.asList(overlayConfig.shadows()));
      this.shadows = shadows.toArray(new Class[shadows.size()]);
    }

    private <T> T pick(T baseValue, T overlayValue, T nullValue) {
      return overlayValue.equals(nullValue) ? baseValue : overlayValue;
    }

    @Override public int emulateSdk() {
      return emulateSdk;
    }

    @Override public String manifest() {
      return manifest;
    }

    @Override public String qualifiers() {
      return qualifiers;
    }

    @Override public int reportSdk() {
      return reportSdk;
    }

    @Override public Class<?>[] shadows() {
      return shadows;
    }

    @NotNull @Override public Class<? extends Annotation> annotationType() {
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

      return true;
    }

    @Override
    public int hashCode() {
      int result = emulateSdk;
      result = 31 * result + qualifiers.hashCode();
      result = 31 * result + reportSdk;
      result = 31 * result + Arrays.hashCode(shadows);
      return result;
    }
  }
}
