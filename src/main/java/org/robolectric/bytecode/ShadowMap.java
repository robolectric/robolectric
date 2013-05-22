package org.robolectric.bytecode;

import org.robolectric.annotation.Implements;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ShadowMap {
  public static final ShadowMap EMPTY = new ShadowMap(Collections.<String, ShadowConfig>emptyMap());
  private static final Set<String> unloadableClassNames = new HashSet<String>();

  private final Map<String, ShadowConfig> map;

  ShadowMap(Map<String, ShadowConfig> map) {
    this.map = new HashMap<String, ShadowConfig>(map);
  }

  private static void warnAbout(String unloadableClassName) {
    boolean alreadyReported;
    synchronized (unloadableClassNames) {
      alreadyReported = unloadableClassNames.add(unloadableClassName);
    }
    if (alreadyReported) {
      System.out.println("Warning: an error occurred while binding shadow class: " + unloadableClassName);
    }
  }

  private static ShadowInfo getShadowInfo(Class<?> shadowClass) {
    Implements implementsAnnotation = shadowClass.getAnnotation(Implements.class);
    if (implementsAnnotation == null) {
      throw new IllegalArgumentException(shadowClass + " is not annotated with @Implements");
    }

    String className = implementsAnnotation.className();
    try {
      if (className.isEmpty()) {
        className = implementsAnnotation.value().getName();
      }
      ShadowConfig shadowConfig = createShadowConfig(shadowClass.getName(),
          implementsAnnotation.callThroughByDefault(),
          implementsAnnotation.inheritImplementationMethods(),
          implementsAnnotation.looseSignatures());
      return new ShadowInfo(className, shadowConfig);
    } catch (TypeNotPresentException typeLoadingException) {
      String unloadableClassName = shadowClass.getSimpleName();
      if (typeLoadingException.typeName().startsWith("com.google.android.maps")) {
        warnAbout(unloadableClassName);
        return null;
      } else if (isIgnorableClassLoadingException(typeLoadingException)) {
        //this allows users of the robolectric.jar file to use the non-Google APIs version of the api
        warnAbout(unloadableClassName);
      } else {
        throw typeLoadingException;
      }
    }
    return null;
  }

  private static ShadowConfig createShadowConfig(String shadowClassName,
      boolean callThroughByDefault, boolean inheritImplementationMethods, boolean looseSignatures) {
    return new ShadowConfig(shadowClassName, callThroughByDefault, inheritImplementationMethods, looseSignatures);
  }

  private static boolean isIgnorableClassLoadingException(Throwable typeLoadingException) {
    if (typeLoadingException != null) {
      if (typeLoadingException instanceof NoClassDefFoundError
          || typeLoadingException instanceof ClassNotFoundException
          || typeLoadingException instanceof TypeNotPresentException) {
        return isIgnorableClassLoadingException(typeLoadingException.getCause());
      }
    }
    return false;
  }

  public ShadowConfig get(Class<?> clazz) {
    String className = clazz.getName();
    ShadowConfig shadowConfig = map.get(className);
    ClassLoader classLoader = clazz.getClassLoader();
    if (shadowConfig == null && classLoader != null) {
      String shadowClassName = convertToShadowName(className);
      Class<?> shadowClass;
      try {
        shadowClass = classLoader.loadClass(shadowClassName);
      } catch (ClassNotFoundException e) {
        return null;
      }
      ShadowInfo shadowInfo = getShadowInfo(shadowClass);
      if (shadowInfo != null && shadowInfo.shadowedClassName.equals(className)) {
        return shadowInfo.getShadowConfig();
      }
    }
    return shadowConfig;
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
      map = new HashMap<String, ShadowConfig>();
    }

    public Builder(ShadowMap shadowMap) {
      this.map = new HashMap<String, ShadowConfig>(shadowMap.map);
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
      addShadowConfig(realClassName, createShadowConfig(shadowClassName, callThroughByDefault, inheritImplementationMethods, looseSignatures));
      return this;
    }

    private void addShadowConfig(String realClassName, ShadowConfig shadowConfig) {
      map.put(realClassName, shadowConfig);
    }

    public ShadowMap build() {
      return new ShadowMap(map);
    }

  }

  private static class ShadowInfo {
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
