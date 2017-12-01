package org.robolectric.internal.bytecode;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;
import org.robolectric.annotation.Implements;
import org.robolectric.internal.ShadowProvider;

public class ShadowMap {
  public static final ShadowMap EMPTY = new ShadowMap(Collections.<String, ShadowConfig>emptyMap());
  private final Map<String, ShadowConfig> map;
  private static final Map<String, String> SHADOWS = new HashMap<>();

  static {
    for (ShadowProvider provider : ServiceLoader.load(ShadowProvider.class)) {
      SHADOWS.putAll(provider.getShadowMap());
    }
  }

  ShadowMap(Map<String, ShadowConfig> map) {
    this.map = new HashMap<>(map);
  }

  public ShadowConfig get(Class<?> clazz) {
    ShadowConfig shadowConfig = map.get(clazz.getName());

    if (shadowConfig == null && clazz.getClassLoader() != null) {
      Class<?> shadowClass = getShadowClass(clazz);
      if (shadowClass == null) {
        return null;
      }

      ShadowInfo shadowInfo = getShadowInfo(shadowClass);
      if (shadowInfo != null && shadowInfo.shadowedClassName.equals(clazz.getName())) {
        return shadowInfo.getShadowConfig();
      }
    }
    return shadowConfig;
  }

  private static Class<?> getShadowClass(Class<?> clazz) {
    try {
      final String className = clazz.getCanonicalName();
      if (className != null) {
        final String shadowName = SHADOWS.get(className);
        if (shadowName != null) {
          return clazz.getClassLoader().loadClass(shadowName);
        }
      }
    } catch (ClassNotFoundException e) {
      return null;
    } catch (IncompatibleClassChangeError e) {
      return null;
    }
    return null;
  }

  public static ShadowInfo getShadowInfo(Class<?> clazz) {
    Implements annotation = clazz.getAnnotation(Implements.class);
    if (annotation == null) {
      throw new IllegalArgumentException(clazz + " is not annotated with @Implements");
    }

    String className = annotation.className();
    if (className.isEmpty()) {
      className = annotation.value().getName();
    }
    return new ShadowInfo(className, new ShadowConfig(clazz.getName(), annotation));
  }

  @SuppressWarnings("ReferenceEquality")
  public Set<String> getInvalidatedClasses(ShadowMap previous) {
    if (this == previous) return Collections.emptySet();

    Map<String, ShadowConfig> invalidated = new HashMap<>();
    invalidated.putAll(map);

    for (Map.Entry<String, ShadowConfig> entry : previous.map.entrySet()) {
      String className = entry.getKey();
      ShadowConfig previousConfig = entry.getValue();
      ShadowConfig currentConfig = invalidated.get(className);
      if (currentConfig == null) {
        invalidated.put(className, previousConfig);
      } else if (previousConfig.equals(currentConfig)) {
        invalidated.remove(className);
      }
    }

    return invalidated.keySet();
  }

  public static String convertToShadowName(String className) {
    String shadowClassName =
        "org.robolectric.shadows.Shadow" + className.substring(className.lastIndexOf(".") + 1);
    shadowClassName = shadowClassName.replaceAll("\\$", "\\$Shadow");
    return shadowClassName;
  }

  public Builder newBuilder() {
    return new Builder(this);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    ShadowMap shadowMap = (ShadowMap) o;

    if (!map.equals(shadowMap.map)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return map.hashCode();
  }

  public static class Builder {
    private final Map<String, ShadowConfig> map;

    public Builder() {
      map = new HashMap<>();
    }

    public Builder(ShadowMap shadowMap) {
      this.map = new HashMap<>(shadowMap.map);
    }

    public Builder addShadowClasses(Class<?>... shadowClasses) {
      for (Class<?> shadowClass : shadowClasses) {
        addShadowClass(shadowClass);
      }
      return this;
    }

    public Builder addShadowClasses(Collection<Class<?>> shadowClasses) {
      for (Class<?> shadowClass : shadowClasses) {
        addShadowClass(shadowClass);
      }
      return this;
    }

    public Builder addShadowClass(Class<?> shadowClass) {
      ShadowInfo shadowInfo = getShadowInfo(shadowClass);
      if (shadowInfo != null) {
        addShadowConfig(shadowInfo.getShadowedClassName(), shadowInfo.getShadowConfig());
      }
      return this;
    }

    public Builder addShadowClass(String realClassName, Class<?> shadowClass, boolean callThroughByDefault, boolean inheritImplementationMethods, boolean looseSignatures) {
      addShadowClass(realClassName, shadowClass.getName(), callThroughByDefault, inheritImplementationMethods, looseSignatures);
      return this;
    }

    public Builder addShadowClass(Class<?> realClass, Class<?> shadowClass, boolean callThroughByDefault, boolean inheritImplementationMethods, boolean looseSignatures) {
      addShadowClass(realClass.getName(), shadowClass.getName(), callThroughByDefault, inheritImplementationMethods, looseSignatures);
      return this;
    }

    public Builder addShadowClass(String realClassName, String shadowClassName, boolean callThroughByDefault, boolean inheritImplementationMethods, boolean looseSignatures) {
      addShadowConfig(realClassName, new ShadowConfig(shadowClassName, callThroughByDefault, inheritImplementationMethods, looseSignatures, -1, -1));
      return this;
    }

    private void addShadowConfig(String realClassName, ShadowConfig shadowConfig) {
      map.put(realClassName, shadowConfig);
    }

    public ShadowMap build() {
      return new ShadowMap(map);
    }
  }

  public static class ShadowInfo {
    private final String shadowedClassName;
    private final ShadowConfig shadowConfig;

    ShadowInfo(String shadowedClassName, ShadowConfig shadowConfig) {
      this.shadowConfig = shadowConfig;
      this.shadowedClassName = shadowedClassName;
    }

    public String getShadowedClassName() {
      return shadowedClassName;
    }

    public ShadowConfig getShadowConfig() {
      return shadowConfig;
    }
  }
}
