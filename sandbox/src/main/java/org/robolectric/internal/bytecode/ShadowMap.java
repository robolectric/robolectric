package org.robolectric.internal.bytecode;

import com.google.common.collect.ImmutableMap;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.robolectric.annotation.Implements;
import org.robolectric.internal.ShadowProvider;

/**
 * Maps from instrumented class to shadow class.
 *
 * We deal with class names rather than actual classes here, since a ShadowMap is built outside of
 * any sandboxes, but instrumented and shadowed classes must be loaded through a
 * {@link SandboxClassLoader}. We don't want to try to resolve those classes outside of a sandbox.
 *
 * Once constructed, instances are immutable.
 */
public class ShadowMap {
  public static final ShadowMap EMPTY = new ShadowMap(Collections.emptyMap(), Collections.emptyMap());

  private final ImmutableMap<String, String> defaultShadows;
  private final ImmutableMap<String, ShadowInfo> overriddenShadows;

  public static ShadowMap createFromShadowProviders(Iterable<ShadowProvider> shadowProviders) {
    Map<String, String> map = new HashMap<>();
    for (ShadowProvider provider : shadowProviders) {
       map.putAll(provider.getShadowMap());
    }
    return new ShadowMap(map, new HashMap<>());
  }

  ShadowMap(Map<String, String> defaultShadows, Map<String, ShadowInfo> overriddenShadows) {
    this.defaultShadows = ImmutableMap.copyOf(defaultShadows);
    this.overriddenShadows = ImmutableMap.copyOf(overriddenShadows);
  }

  public ShadowInfo getShadowInfo(Class<?> clazz, int apiLevel) {
    String instrumentedClassName = clazz.getName();
    ShadowInfo shadowInfo = overriddenShadows.get(instrumentedClassName);

    if (shadowInfo == null && clazz.getClassLoader() != null) {
      try {
        final String shadowName = defaultShadows.get(clazz.getCanonicalName());
        if (shadowName != null) {
          Class<?> shadowClass = clazz.getClassLoader().loadClass(shadowName);
          shadowInfo = obtainShadowInfo(shadowClass);
          if (!shadowInfo.shadowedClassName.equals(instrumentedClassName)) {
            // somehow we got the wrong shadow class?
            shadowInfo = null;
          }
        }
      } catch (ClassNotFoundException | IncompatibleClassChangeError e) {
        return null;
      }
    }

    if (shadowInfo != null && !shadowInfo.supportsSdk(apiLevel)) {
      return null;
    }

    return shadowInfo;
  }

  /**
   * @deprecated use {@link #obtainShadowInfo(Class)} instead
   */
  @Deprecated
  public static ShadowInfo getShadowInfo(Class<?> shadowClass) {
    return obtainShadowInfo(shadowClass);
  }

  public static ShadowInfo obtainShadowInfo(Class<?> clazz) {
    return obtainShadowInfo(clazz, false);
  }

  static ShadowInfo obtainShadowInfo(Class<?> clazz, boolean mayBeNonShadow) {
    Implements annotation = clazz.getAnnotation(Implements.class);
    if (annotation == null) {
      if (mayBeNonShadow) {
        return null;
      } else {
        throw new IllegalArgumentException(clazz + " is not annotated with @Implements");
      }
    }

    String className = annotation.className();
    if (className.isEmpty()) {
      className = annotation.value().getName();
    }
    return new ShadowInfo(className, clazz.getName(), annotation);
  }

  @SuppressWarnings("ReferenceEquality")
  public Set<String> getInvalidatedClasses(ShadowMap previous) {
    if (this == previous) return Collections.emptySet();

    Map<String, ShadowInfo> invalidated = new HashMap<>();
    invalidated.putAll(overriddenShadows);

    for (Map.Entry<String, ShadowInfo> entry : previous.overriddenShadows.entrySet()) {
      String className = entry.getKey();
      ShadowInfo previousConfig = entry.getValue();
      ShadowInfo currentConfig = invalidated.get(className);
      if (currentConfig == null) {
        invalidated.put(className, previousConfig);
      } else if (previousConfig.equals(currentConfig)) {
        invalidated.remove(className);
      }
    }

    return invalidated.keySet();
  }

  /**
   * @deprecated do not use
   */
  @Deprecated
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

    if (!overriddenShadows.equals(shadowMap.overriddenShadows)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    return overriddenShadows.hashCode();
  }

  public static class Builder {
    private final ImmutableMap<String, String> defaultShadows;
    private final Map<String, ShadowInfo> overriddenShadows;

    public Builder () {
      defaultShadows = ImmutableMap.of();
      overriddenShadows = new HashMap<>();
    }

    public Builder(ShadowMap shadowMap) {
      this.defaultShadows = shadowMap.defaultShadows;
      this.overriddenShadows = new HashMap<>(shadowMap.overriddenShadows);
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
      addShadowInfo(obtainShadowInfo(shadowClass));
      return this;
    }

    public Builder addShadowClass(String realClassName, Class<?> shadowClass,
        boolean callThroughByDefault, boolean inheritImplementationMethods,
        boolean looseSignatures) {
      addShadowClass(realClassName, shadowClass.getName(), callThroughByDefault,
          inheritImplementationMethods, looseSignatures);
      return this;
    }

    public Builder addShadowClass(Class<?> realClass, Class<?> shadowClass,
        boolean callThroughByDefault, boolean inheritImplementationMethods,
        boolean looseSignatures) {
      addShadowClass(realClass.getName(), shadowClass.getName(), callThroughByDefault,
          inheritImplementationMethods, looseSignatures);
      return this;
    }

    public Builder addShadowClass(String realClassName, String shadowClassName,
        boolean callThroughByDefault, boolean inheritImplementationMethods,
        boolean looseSignatures) {
      addShadowInfo(new ShadowInfo(realClassName, shadowClassName,
          callThroughByDefault, inheritImplementationMethods, looseSignatures, -1, -1));
      return this;
    }

    private void addShadowInfo(ShadowInfo shadowInfo) {
      overriddenShadows.put(shadowInfo.shadowedClassName, shadowInfo);
    }

    public ShadowMap build() {
      return new ShadowMap(defaultShadows, overriddenShadows);
    }
  }
}
