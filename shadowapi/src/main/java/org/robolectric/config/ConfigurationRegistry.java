package org.robolectric.config;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Holds configuration objects for the current test, computed using {@link Configurer}.
 *
 * <p>Configuration is computed before tests run, outside of their sandboxes. If the configuration
 * is needed from within a sandbox (when a test is executing), we need to transfer it to a class
 * that the SandboxClassLoader recognizes. We do this by serializing and deserializing in {@link
 * #maybeReloadInSandboxClassLoader(Object)}.
 */
public class ConfigurationRegistry {

  public static ConfigurationRegistry instance;

  /**
   * Returns the configuration object of the specified class, computed using
   * {@link Configurer}.
   */
  public static <T> T get(Class<T> configClass) {
    return instance.getInSandboxClassLoader(configClass);
  }

  private final Map<String, Object> configurations = new HashMap<>();

  public ConfigurationRegistry(Map<Class<?>, Object> configClassMap) {
    for (Class<?> classInParentLoader : configClassMap.keySet()) {
      Object configInParentLoader = configClassMap.get(classInParentLoader);
      configurations.put(classInParentLoader.getName(), configInParentLoader);
    }
  }

  private <T> T getInSandboxClassLoader(Class<T> someConfigClass) {
    Object configInParentLoader = configurations.get(someConfigClass.getName());
    if (configInParentLoader == null) {
      return null;
    }
    Object configInSandboxLoader = maybeReloadInSandboxClassLoader(configInParentLoader);
    return someConfigClass.cast(configInSandboxLoader);
  }

  /**
   * Reloads the value of the config in the current class loader. This has to be done in case of
   * custom {@link org.robolectric.pluginapi.config.Configurer} classes. If there is a custom
   * Configurer class, the config value will be initialized in the Application ClassLoader, before
   * any tests are run. However, because custom config classes will typically not be included in
   * {@link AndroidConfigurer}, they will be acquired by the Robolectric ClassLoader and redefined
   * whenever they are referenced during the lifecycle of a Robolectric test. This causes a problem
   * because an object of ConfigClass[ApplicationClassLoader] cannot be cast to a
   * ConfigClass[RobolectricClassLoader].
   *
   * <p>Note this logic is not required for built-in Config classes because there are rules in
   * {@link AndroidConfigurer} to exclude org.robolectric.annotation.* classes from being acquired
   * by Robolectric ClassLoaders.
   */
  @SuppressWarnings("BanSerializableRead")
  private static Object maybeReloadInSandboxClassLoader(Object configInParentLoader) {
    // Avoid reloading for built-in config from the org.robolectric.annotation package. This package
    // is excluded from instrumentation, so it will always exist in the Application ClassLoader.
    if (configInParentLoader.getClass().getName().startsWith("org.robolectric.annotation.")) {
      return configInParentLoader;
    }
    ByteArrayOutputStream buf = new ByteArrayOutputStream();
    try (ObjectOutputStream out = new ObjectOutputStream(buf)) {
      out.writeObject(configInParentLoader);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }

    byte[] bytes = buf.toByteArray();

    // ObjectInputStream loads classes in the current classloader by magic
    try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes))) {
      return in.readObject();
    } catch (IOException | ClassNotFoundException e) {
      throw new RuntimeException(e);
    }
  }
}
