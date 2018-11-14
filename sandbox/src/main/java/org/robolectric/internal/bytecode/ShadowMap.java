package org.robolectric.internal.bytecode;

import com.google.common.collect.ImmutableMap;
import java.lang.reflect.InvocationTargetException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import org.robolectric.annotation.Implements;
import org.robolectric.internal.ShadowProvider;
import org.robolectric.shadow.api.ShadowPicker;

/**
 * Maps from instrumented class to shadow class.
 *
 * We deal with class names rather than actual classes here, since a ShadowMap is built outside of
 * any sandboxes, but instrumented and shadowed classes must be loaded through a
 * {@link SandboxClassLoader}. We don't want to try to resolve those classes outside of a sandbox.
 *
 * Once constructed, instances are immutable.
 */
@SuppressWarnings("NewApi")
public class ShadowMap {

  static final ShadowMap EMPTY = new ShadowMap(ImmutableMap.of(), ImmutableMap.of());

  private final ImmutableMap<String, String> defaultShadows;
  private final ImmutableMap<String, ShadowInfo> overriddenShadows;
  private final ImmutableMap<String, String> shadowPickers;

  public static ShadowMap createFromShadowProviders(Iterable<ShadowProvider> shadowProviders) {
    final Map<String, String> shadowMap = new HashMap<>();
    final Map<String, String> shadowPickerMap = new HashMap<>();
    for (ShadowProvider provider : shadowProviders) {
       shadowMap.putAll(provider.getShadowMap());
       shadowPickerMap.putAll(provider.getShadowPickerMap());
    }
    return new ShadowMap(ImmutableMap.copyOf(shadowMap), Collections.emptyMap(),
        ImmutableMap.copyOf(shadowPickerMap));
  }

  ShadowMap(ImmutableMap<String, String> defaultShadows, Map<String, ShadowInfo> overriddenShadows) {
    this(defaultShadows, overriddenShadows, Collections.emptyMap());
  }

  private ShadowMap(ImmutableMap<String, String> defaultShadows,
      Map<String, ShadowInfo> overriddenShadows,
      Map<String, String> shadowPickers) {
    this.defaultShadows = defaultShadows;
    this.overriddenShadows = ImmutableMap.copyOf(overriddenShadows);
    this.shadowPickers = ImmutableMap.copyOf(shadowPickers);
  }

  public ShadowInfo getShadowInfo(Class<?> clazz, int apiLevel) {
    String instrumentedClassName = clazz.getName();

    ShadowInfo shadowInfo = overriddenShadows.get(instrumentedClassName);
    if (shadowInfo == null) {
      shadowInfo = checkShadowPickers(instrumentedClassName, clazz);
    }

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

  // todo: some caching would probably be nice here...
  private ShadowInfo checkShadowPickers(String instrumentedClassName, Class<?> clazz) {
    String shadowPickerClassName = shadowPickers.get(instrumentedClassName);
    if (shadowPickerClassName == null) {
      return null;
    }

    ClassLoader classLoader = clazz.getClassLoader();
    try {
      Class<? extends ShadowPicker<?>> shadowPickerClass =
          (Class<? extends ShadowPicker<?>>) classLoader.loadClass(shadowPickerClassName);
      ShadowPicker<?> shadowPicker = shadowPickerClass.getDeclaredConstructor().newInstance();
      Class<?> selectedShadowClass = shadowPicker.pickShadowClass();
      if (selectedShadowClass == null) {
        return obtainShadowInfo(Object.class, true);
      }
      ShadowInfo shadowInfo = obtainShadowInfo(selectedShadowClass);

      if (!shadowInfo.shadowedClassName.equals(instrumentedClassName)) {
        throw new IllegalArgumentException("Implemented class for "
            + selectedShadowClass.getName() + " (" + shadowInfo.shadowedClassName + ") != "
            + instrumentedClassName);
      }

      return shadowInfo;
    } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException
        | IllegalAccessException | InstantiationException e) {
      throw new RuntimeException("Failed to resolve shadow picker for " + instrumentedClassName,
          e);
    }
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
    if (this == previous && shadowPickers.isEmpty()) return Collections.emptySet();

    Map<String, ShadowInfo> invalidated = new HashMap<>(overriddenShadows);

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

    HashSet<String> classNames = new HashSet<>(invalidated.keySet());
    classNames.addAll(shadowPickers.keySet());
    return classNames;
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
    if (!(o instanceof ShadowMap)) return false;

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
    private final Map<String, String> shadowPickers;

    public Builder () {
      defaultShadows = ImmutableMap.of();
      overriddenShadows = new HashMap<>();
      shadowPickers = new HashMap<>();
    }

    public Builder(ShadowMap shadowMap) {
      this.defaultShadows = shadowMap.defaultShadows;
      this.overriddenShadows = new HashMap<>(shadowMap.overriddenShadows);
      this.shadowPickers = new HashMap<>(shadowMap.shadowPickers);
    }

    public Builder addShadowClasses(Class<?>... shadowClasses) {
      for (Class<?> shadowClass : shadowClasses) {
        addShadowClass(shadowClass);
      }
      return this;
    }

    Builder addShadowClass(Class<?> shadowClass) {
      addShadowInfo(obtainShadowInfo(shadowClass));
      return this;
    }

    Builder addShadowClass(
        String realClassName,
        String shadowClassName,
        boolean callThroughByDefault,
        boolean looseSignatures) {
      addShadowInfo(
          new ShadowInfo(
              realClassName, shadowClassName, callThroughByDefault, looseSignatures, -1, -1, null));
      return this;
    }

    private void addShadowInfo(ShadowInfo shadowInfo) {
      overriddenShadows.put(shadowInfo.shadowedClassName, shadowInfo);
      if (shadowInfo.hasShadowPicker()) {
        shadowPickers
            .put(shadowInfo.shadowedClassName, shadowInfo.getShadowPickerClass().getName());
      }
    }

    public ShadowMap build() {
      return new ShadowMap(defaultShadows, overriddenShadows, shadowPickers);
    }
  }
}
