package org.robolectric.plugins.config;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import javax.annotation.Nonnull;
import org.robolectric.pluginapi.config.Configurer;
import org.robolectric.plugins.PackagePropertiesLoader;

/**
 * The base class for all enumeration based single value robolectric configuration annotations.
 * Simply extending and providing a constructor is sufficient to allow for robolectric.properties
 * reading, reading system properties, and providing a default value.
 *
 * <p>The property name in system properties is the simple name of annotation being read, with the
 * first letter lower cased, unless {@link #propertyName()} is overridden, prefixed with
 * "robolectric."
 *
 * <p>The property name in robolectric is the simple name of annotation being read, with the first
 * letter lower cased, unless {@link #propertyName()} is overridden, note that there is no prefix.
 *
 * <p>All values are eagerly cached at construction of an instance.
 */
public abstract class SingleValueConfigurer<A extends Annotation, T extends Enum<T>>
    implements Configurer<T> {

  private static final String PROPERTY_CONTAINER = "robolectric";

  /** Shared cache of resolved {@code package-info} packages, keyed by package name. */
  static final Map<String, Package> PACKAGE_INFO_CACHE = new HashMap<>();

  private final Class<A> configurationAnnotation;
  private final Class<T> configurationType;
  private final T defaultValue;
  private final Properties systemProperties;
  private final PackagePropertiesLoader propertiesLoader;

  /**
   * Eagerly computed property name: {@link #configurationAnnotation}'s simple name with first
   * letter lower cased.
   *
   * <p>Overridable by implementing an override of {@link #propertyName()}
   */
  private final String propertyName;

  /**
   * Eagerly computed system property name: "robolectric." concatenated with the {@link
   * #propertyName}
   */
  private final String systemPropertyName;

  public SingleValueConfigurer(
      Class<A> annotationType,
      Class<T> configurationType,
      T defaultValue,
      PackagePropertiesLoader propertiesLoader,
      Properties systemProperties) {
    this.configurationAnnotation = annotationType;
    this.configurationType = configurationType;
    this.defaultValue = defaultValue;
    this.propertiesLoader = propertiesLoader;
    this.systemProperties = systemProperties;
    this.propertyName = propertyName();
    this.systemPropertyName = PROPERTY_CONTAINER + "." + propertyName;
  }

  /**
   * Computes the property name that will be used to look for values in a property file, or in
   * System properties
   */
  protected String propertyName() {
    return configurationAnnotation.getSimpleName().substring(0, 1).toLowerCase(Locale.ROOT)
        + configurationAnnotation.getSimpleName().substring(1);
  }

  @Override
  public Class<T> getConfigClass() {
    return configurationType;
  }

  @Override
  @Nonnull
  public T defaultConfig() {
    T systemPropValue = valueFrom(systemProperties.getProperty(systemPropertyName));
    return systemPropValue != null ? systemPropValue : defaultValue;
  }

  @Override
  public T getConfigFor(@Nonnull String packageName) {
    Package pkg = resolvePackageInfo(packageName);
    if (pkg != null) {
      A annotation = pkg.getAnnotation(configurationAnnotation);
      if (annotation != null) {
        return valueFrom(annotation);
      }
    }
    Properties props = propertiesLoader.getConfigProperties(packageName);
    if (props != null) {
      return valueFrom(props.getProperty(this.propertyName));
    }
    return null;
  }

  /**
   * Resolves the {@code package-info} {@link Package} for the given package name, or {@code null}
   * if it has none. The result is cached (including the absent case) and shared across all
   * configurers. This avoids repeated classpath scans for the same package-info.
   */
  private static Package resolvePackageInfo(String packageName) {
    if (PACKAGE_INFO_CACHE.containsKey(packageName)) {
      return PACKAGE_INFO_CACHE.get(packageName);
    }
    Package pkg = null;
    try {
      pkg = Class.forName(packageName + ".package-info").getPackage();
    } catch (ClassNotFoundException e) {
      // Ignore.
    }
    PACKAGE_INFO_CACHE.put(packageName, pkg);
    return pkg;
  }

  @Override
  public T getConfigFor(@Nonnull Class<?> testClass) {
    return valueFrom(testClass.getAnnotation(configurationAnnotation));
  }

  @Override
  public T getConfigFor(@Nonnull Method method) {
    return valueFrom(method.getAnnotation(configurationAnnotation));
  }

  protected final String getProperty(String property) {
    return systemProperties.getProperty(property);
  }

  @Nonnull
  @Override
  public T merge(@Nonnull T parentConfig, @Nonnull T childConfig) {
    // just take the childConfig - since annotation only has a single 'value' attribute
    return childConfig;
  }

  @SuppressWarnings("unchecked") // the type is compared before casting.
  protected T valueFrom(A annotation) {
    if (annotation == null) {
      return null;
    }
    try {
      Object value = annotation.annotationType().getMethod("value").invoke(annotation);
      if (configurationType.isAssignableFrom(value.getClass())) {
        return (T) value;
      } else {
        throw new RuntimeException(
            "The value() of annotation "
                + configurationAnnotation
                + " must be of type "
                + configurationType
                + " but was of type "
                + value.getClass());
      }
    } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
      throw new RuntimeException(
          "The annotation "
              + configurationType
              + " must have an accessible parameter value() of type "
              + configurationType,
          e);
    }
  }

  protected T valueFrom(String value) {
    // in case someone makes an enumeration that doesn't use the standard
    // uppercase naming convention.
    if (value == null) {
      return null;
    }
    for (T each : configurationType.getEnumConstants()) {
      if (each.name().equalsIgnoreCase(value)) {
        return each;
      }
    }
    throw new IllegalArgumentException("Unknown value for " + configurationType + ": " + value);
  }
}
