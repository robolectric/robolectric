package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;

import android.os.Build;
import android.provider.DeviceConfig;
import android.provider.DeviceConfig.Properties;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;

/**
 * A stub implementation of {@link DeviceConfig} that does not use a content resolver. This shadow
 * does not require the caller to have READ_DEVICE_CONFIG or WRITE_DEVICE_CONFIG permissions.
 */
@Implements(value = DeviceConfig.class, isInAndroidSdk = false, minSdk = Q)
public class ShadowDeviceConfig {
  private static final Map<String, Map<String, Value>> settingsConfigPropertiesMap =
      new HashMap<>();

  @Implementation(minSdk = R)
  protected static Properties getProperties(String namespace, String... names) {
    Properties.Builder propertiesBuilder = new Properties.Builder(namespace);
    for (String propertyName : names) {
      if (propertyExistsInternal(namespace, propertyName)) {
        propertiesBuilder.setString(
            propertyName, getPropertyInternal(namespace, propertyName).getValue());
      }
    }
    return propertiesBuilder.build();
  }

  @Implementation(minSdk = Q)
  protected static String getProperty(String namespace, String name) {
    return propertyExistsInternal(namespace, name)
        ? getPropertyInternal(namespace, name).getValue()
        : null;
  }

  @Implementation(minSdk = Q)
  protected static String getString(String namespace, String name, String defaultValue) {
    String value = getProperty(namespace, name);
    return value != null ? value : defaultValue;
  }

  @Implementation(minSdk = Q)
  protected static boolean getBoolean(String namespace, String name, boolean defaultValue) {
    String value = getProperty(namespace, name);
    return value != null ? Boolean.parseBoolean(value) : defaultValue;
  }

  @Implementation(minSdk = Q)
  protected static int getInt(String namespace, String name, int defaultValue) {
    String value = getProperty(namespace, name);
    if (value == null) {
      return defaultValue;
    }
    try {
      return Integer.parseInt(value);
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  @Implementation(minSdk = Q)
  protected static float getFloat(String namespace, String name, float defaultValue) {
    String value = getProperty(namespace, name);
    if (value == null) {
      return defaultValue;
    }
    try {
      return Float.parseFloat(value);
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  @Implementation(minSdk = Q)
  protected static long getLong(String namespace, String name, long defaultValue) {
    String value = getProperty(namespace, name);
    if (value == null) {
      return defaultValue;
    }
    try {
      return Long.parseLong(value);
    } catch (NumberFormatException e) {
      return defaultValue;
    }
  }

  @Implementation(minSdk = Q)
  protected static boolean setProperty(
      String namespace, String name, String value, boolean makeDefault) {
    if (!settingsConfigPropertiesMap.containsKey(namespace)) {
      settingsConfigPropertiesMap.put(namespace, new HashMap<>());
    }
    Map<String, Value> namespacePropertyMap = settingsConfigPropertiesMap.get(namespace);
    if (namespacePropertyMap.containsKey(name)) {
      namespacePropertyMap.get(name).setValue(value);
      if (makeDefault) {
        namespacePropertyMap.get(name).setDefaultValue(value);
      }
    } else {
      settingsConfigPropertiesMap.get(namespace).put(name, new Value(value));
    }
    return true;
  }

  @Implementation(minSdk = R)
  protected static boolean setProperties(Properties properties) {
    if (!settingsConfigPropertiesMap.containsKey(properties.getNamespace())) {
      settingsConfigPropertiesMap.put(properties.getNamespace(), new HashMap<>());
    }
    Map<String, Value> propertiesMap = settingsConfigPropertiesMap.get(properties.getNamespace());
    for (String key : properties.getKeyset()) {
      String val = properties.getString(key, "");
      if (propertiesMap.containsKey(key)) {
        propertiesMap.get(key).setValue(val);
      } else {
        propertiesMap.put(key, new Value(val));
      }
    }
    return true;
  }

  @Implementation(minSdk = Q)
  protected static void resetToDefaults(int resetMode, String namespace) {
    Collection<Map<String, Value>> namespacePropertyMapsToReset;
    if (namespace == null) {
      namespacePropertyMapsToReset = settingsConfigPropertiesMap.values();
    } else {
      if (settingsConfigPropertiesMap.containsKey(namespace)) {
        namespacePropertyMapsToReset =
            Collections.singletonList(settingsConfigPropertiesMap.get(namespace));
      } else {
        // Nothing to reset
        return;
      }
    }
    for (Map<String, Value> propertyMap : namespacePropertyMapsToReset) {
      for (Value propertyValue : propertyMap.values()) {
        propertyValue.resetToDefault();
      }
    }
  }

  @Resetter
  public static void reset() {
    Object lock = ReflectionHelpers.getStaticField(DeviceConfig.class, "sLock");
    //noinspection SynchronizationOnLocalVariableOrMethodParameter
    synchronized (lock) {
      if (RuntimeEnvironment.getApiLevel() == Build.VERSION_CODES.Q) {
        Map singleListeners =
            ReflectionHelpers.getStaticField(DeviceConfig.class, "sSingleListeners");
        singleListeners.clear();
      }

      Map listeners = ReflectionHelpers.getStaticField(DeviceConfig.class, "sListeners");
      listeners.clear();

      Map namespaces = ReflectionHelpers.getStaticField(DeviceConfig.class, "sNamespaces");
      namespaces.clear();
    }
    settingsConfigPropertiesMap.clear();
  }

  private static boolean propertyExistsInternal(String namespace, String name) {
    if (settingsConfigPropertiesMap.containsKey(namespace)) {
      return settingsConfigPropertiesMap.get(namespace).containsKey(name);
    }
    return false;
  }

  private static Value getPropertyInternal(String namespace, String name) {
    if (propertyExistsInternal(namespace, name)) {
      return settingsConfigPropertiesMap.get(namespace).get(name);
    }
    return null;
  }

  private static class Value {
    private String currentValue;
    private String defaultValue;

    Value(String currentValue) {
      this.currentValue = currentValue;
      defaultValue = currentValue;
    }

    String getValue() {
      return currentValue;
    }

    void setValue(String value) {
      currentValue = value;
    }

    void setDefaultValue(String defaultValue) {
      this.defaultValue = defaultValue;
    }

    void resetToDefault() {
      currentValue = defaultValue;
    }
  }
}
