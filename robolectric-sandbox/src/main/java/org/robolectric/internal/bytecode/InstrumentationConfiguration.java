package org.robolectric.internal.bytecode;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.robolectric.annotation.internal.DoNotInstrument;
import org.robolectric.annotation.internal.Instrument;
import org.robolectric.internal.ShadowExtractor;
import org.robolectric.shadow.api.Shadow;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Configuration rules for {@link SandboxClassLoader}.
 */
public class InstrumentationConfiguration {
  public static Builder newBuilder() {
    return new Builder();
  }

  private static final Set<String> CLASSES_TO_ALWAYS_ACQUIRE = Sets.newHashSet(
      RobolectricInternals.class.getName(),
      InvokeDynamicSupport.class.getName(),
      Shadow.class.getName(),

      // these classes are deprecated and will be removed soon:
      ShadowExtractor.class.getName(),
      "org.robolectric.internal.Shadow",
      "org.robolectric.res.builder.DefaultPackageManager",
      "org.robolectric.res.builder.DefaultPackageManager$1",
      "org.robolectric.res.builder.DefaultPackageManager$IntentComparator",
      "org.robolectric.res.builder.DefaultPackageManager$RoboPackageInstaller",
      "org.robolectric.res.builder.DefaultPackageManager$RoboPackageInstaller$1",
      "org.robolectric.res.builder.RobolectricPackageManager",
      "org.robolectric.res.builder.StubPackageManager",
      "org.robolectric.util.AccessibilityUtil",
      "org.robolectric.util.ActivityController",
      "org.robolectric.util.ApplicationTestUtil",
      "org.robolectric.util.ContentProviderController",
      "org.robolectric.util.FragmentController",
      "org.robolectric.util.FragmentTestUtil",
      "org.robolectric.util.FragmentTestUtil$FragmentUtilActivity",
      "org.robolectric.util.IntentServiceController",
      "org.robolectric.util.ServiceController",
      "org.robolectric.util.concurrent.RoboExecutorService"
  );

  private final List<String> instrumentedPackages;
  private final Set<String> instrumentedClasses;
  private final Set<String> classesToNotInstrument;
  private final Map<String, String> classNameTranslations;
  private final Set<MethodRef> interceptedMethods;
  private final Set<String> classesToNotAcquire;
  private final Set<String> packagesToNotAcquire;
  private int cachedHashCode;

  private InstrumentationConfiguration(Map<String, String> classNameTranslations, Collection<MethodRef> interceptedMethods, Collection<String> instrumentedPackages, Collection<String> instrumentedClasses, Collection<String> classesToNotAcquire, Collection<String> packagesToNotAquire, Collection<String> classesToNotInstrument) {
    this.classNameTranslations = ImmutableMap.copyOf(classNameTranslations);
    this.interceptedMethods = ImmutableSet.copyOf(interceptedMethods);
    this.instrumentedPackages = ImmutableList.copyOf(instrumentedPackages);
    this.instrumentedClasses = ImmutableSet.copyOf(instrumentedClasses);
    this.classesToNotAcquire = ImmutableSet.copyOf(classesToNotAcquire);
    this.packagesToNotAcquire = ImmutableSet.copyOf(packagesToNotAquire);
    this.classesToNotInstrument = ImmutableSet.copyOf(classesToNotInstrument);
    this.cachedHashCode = 0;
  }

  /**
   * Determine if {@link SandboxClassLoader} should instrument a given class.
   *
   * @param   classInfo The class to check.
   * @return  True if the class should be instrumented.
   */
  public boolean shouldInstrument(ClassInfo classInfo) {
    return !(classInfo.isInterface()
              || classInfo.isAnnotation()
              || classInfo.hasAnnotation(DoNotInstrument.class))
          && (isInInstrumentedPackage(classInfo)
              || instrumentedClasses.contains(classInfo.getName())
              || classInfo.hasAnnotation(Instrument.class))
          && !(classesToNotInstrument.contains(classInfo.getName()));
  }

  /**
   * Determine if {@link SandboxClassLoader} should load a given class.
   *
   * @param   name The fully-qualified class name.
   * @return  True if the class should be loaded.
   */
  public boolean shouldAcquire(String name) {
    if (CLASSES_TO_ALWAYS_ACQUIRE.contains(name)) {
      return true;
    }

    // Internal android R class must be loaded from the framework resources in the framework jar.
    if (name.matches("com\\.android\\.internal\\.R(\\$.*)?")) {
      return true;
    }

    // Android SDK code almost universally refers to com.android.internal.R, except
    // when refering to android.R.stylable, as in HorizontalScrollView. arghgh.
    // See https://github.com/robolectric/robolectric/issues/521
    if (name.startsWith("android.R")) {
      return true;
    }

    // Hack. Fixes https://github.com/robolectric/robolectric/issues/1864
    if (name.equals("javax.net.ssl.DistinguishedNameParser")) {
      return true;
    }

    for (String packageName : packagesToNotAcquire) {
      if (name.startsWith(packageName)) return false;
    }

    boolean isRClass = name.matches(".*\\.R(|\\$[a-z]+)$");
    return !isRClass && !classesToNotAcquire.contains(name);

  }

  public Set<MethodRef> methodsToIntercept() {
    return Collections.unmodifiableSet(interceptedMethods);
  }

  /**
   * Map from a requested class to an alternate stand-in, or not.
   *
   * @return Mapping of class name translations.
   */
  public Map<String, String> classNameTranslations() {
    return Collections.unmodifiableMap(classNameTranslations);
  }

  public boolean containsStubs(ClassInfo classInfo) {
    return classInfo.getName().startsWith("com.google.android.maps.");
  }

  private boolean isInInstrumentedPackage(ClassInfo classInfo) {
    final String className = classInfo.getName();
    for (String instrumentedPackage : instrumentedPackages) {
      if (className.startsWith(instrumentedPackage)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    InstrumentationConfiguration that = (InstrumentationConfiguration) o;

    if (!classNameTranslations.equals(that.classNameTranslations)) return false;
    if (!classesToNotAcquire.equals(that.classesToNotAcquire)) return false;
    if (!instrumentedPackages.equals(that.instrumentedPackages)) return false;
    if (!instrumentedClasses.equals(that.instrumentedClasses)) return false;
    if (!interceptedMethods.equals(that.interceptedMethods)) return false;


    return true;
  }

  @Override
  public int hashCode() {
    if (cachedHashCode != 0) {
      return cachedHashCode;
    }

    int result = instrumentedPackages.hashCode();
    result = 31 * result + instrumentedClasses.hashCode();
    result = 31 * result + classNameTranslations.hashCode();
    result = 31 * result + interceptedMethods.hashCode();
    result = 31 * result + classesToNotAcquire.hashCode();
    cachedHashCode = result;
    return result;
  }

  public static final class Builder {
    private final Collection<String> instrumentedPackages = new HashSet<>();
    private final Collection<MethodRef> interceptedMethods = new HashSet<>();
    private final Map<String, String> classNameTranslations = new HashMap<>();
    private final Collection<String> classesToNotAcquire = new HashSet<>();
    private final Collection<String> packagesToNotAcquire = new HashSet<>();
    private final Collection<String> instrumentedClasses = new HashSet<>();
    private final Collection<String> classesToNotInstrument = new HashSet<>();

    public Builder() {
    }

    public Builder(InstrumentationConfiguration classLoaderConfig) {
      instrumentedPackages.addAll(classLoaderConfig.instrumentedPackages);
      interceptedMethods.addAll(classLoaderConfig.interceptedMethods);
      classNameTranslations.putAll(classLoaderConfig.classNameTranslations);
      classesToNotAcquire.addAll(classLoaderConfig.classesToNotAcquire);
      packagesToNotAcquire.addAll(classLoaderConfig.packagesToNotAcquire);
      instrumentedClasses.addAll(classLoaderConfig.instrumentedClasses);
      classesToNotInstrument.addAll(classLoaderConfig.classesToNotInstrument);
    }

    public Builder doNotAcquireClass(Class<?> clazz) {
      doNotAcquireClass(clazz.getName());
      return this;
    }

    public Builder doNotAcquireClass(String className) {
      this.classesToNotAcquire.add(className);
      return this;
    }

    public Builder doNotAcquirePackage(String packageName) {
      this.packagesToNotAcquire.add(packageName);
      return this;
    }

    public Builder addClassNameTranslation(String fromName, String toName) {
      classNameTranslations.put(fromName, toName);
      return this;
    }

    public Builder addInterceptedMethod(MethodRef methodReference) {
      interceptedMethods.add(methodReference);
      return this;
    }

    public Builder addInstrumentedClass(String name) {
      instrumentedClasses.add(name);
      return this;
    }

    public Builder addInstrumentedPackage(String packageName) {
      instrumentedPackages.add(packageName);
      return this;
    }

    public Builder doNotInstrumentClass(String className) {
      this.classesToNotInstrument.add(className);
      return this;
    }

    public InstrumentationConfiguration build() {
      return new InstrumentationConfiguration(
          classNameTranslations, interceptedMethods, instrumentedPackages,
          instrumentedClasses, classesToNotAcquire, packagesToNotAcquire, classesToNotInstrument);
    }
  }
}
