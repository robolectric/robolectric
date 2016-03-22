package org.robolectric.internal.bytecode;

import android.R;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.TestLifecycle;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.internal.DoNotInstrument;
import org.robolectric.annotation.internal.Instrument;
import org.robolectric.internal.*;
import org.robolectric.internal.dependency.DependencyJar;
import org.robolectric.internal.fakes.RoboCharsets;
import org.robolectric.internal.fakes.RoboExtendedResponseCache;
import org.robolectric.internal.fakes.RoboResponseSource;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.ResourceLoader;
import org.robolectric.res.ResourcePath;
import org.robolectric.res.builder.XmlBlock;
import org.robolectric.util.TempDirectory;
import org.robolectric.util.Transcript;

import java.util.*;

/**
 * Configuration rules for {@link org.robolectric.internal.bytecode.InstrumentingClassLoader}.
 */
public class InstrumentationConfiguration {

  public static final class Builder {

    private final Collection<String> instrumentedPackages = new HashSet<>();
    private final Collection<MethodRef> interceptedMethods = new HashSet<>();
    private final Map<String, String> classNameTranslations = new HashMap<>();
    private final Collection<String> classesToNotAcquire = new HashSet<>();
    private final Collection<String> packagesToNotAcquire = new HashSet<>();
    private final Collection<String> instrumentedClasses = new HashSet<>();
    private final Collection<String> classesToNotInstrument = new HashSet<>();

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

    public Builder withConfig(Config config) {
      for (Class<?> clazz : config.shadows()) {
        Implements annotation = clazz.getAnnotation(Implements.class);
        if (annotation == null) {
          throw new IllegalArgumentException(clazz + " is not annotated with @Implements");
        }

        String className = annotation.className();
        if (className.isEmpty()) {
          className = annotation.value().getName();
        }

        if (!className.isEmpty()) {
          addInstrumentedClass(className);
        }
      }
      for (String packageName : config.instrumentedPackages()) {
        addInstrumentedPackage(packageName);
      }
      return this;
    }

    public InstrumentationConfiguration build() {
      interceptedMethods.addAll(Intrinsics.allRefs());
      classesToNotAcquire.addAll(stringify(
          TestLifecycle.class,
          ShadowWrangler.class,
          AndroidManifest.class,
          R.class,
          InstrumentingClassLoader.class,
          SdkEnvironment.class,
          SdkConfig.class,
          RobolectricTestRunner.class,
          RobolectricTestRunner.HelperTestRunner.class,
          ResourcePath.class,
          ResourceLoader.class,
          XmlBlock.class,
          ClassHandler.class,
          ClassHandler.Plan.class,
          ShadowInvalidator.class,
          RealObject.class,
          Implements.class,
          Implementation.class,
          Instrument.class,
          DoNotInstrument.class,
          Config.class,
          Transcript.class,
          DirectObjectMarker.class,
          DependencyJar.class,
          ParallelUniverseInterface.class,
          ShadowedObject.class,
          TempDirectory.class
      ));
      packagesToNotAcquire.addAll(Arrays.asList(
          "java.",
          "javax.",
          "sun.",
          "com.sun.",
          "org.w3c.",
          "org.xml.",
          "org.junit",
          "org.hamcrest",
          "org.specs2",  // allows for android projects with mixed scala\java tests to be
          "scala.",      //  run with Maven Surefire (see the RoboSpecs project on github)
          "kotlin.",
          "com.almworks.sqlite4java" // Fix #958: SQLite native library must be loaded once.
      ));
      classNameTranslations.put("java.net.ExtendedResponseCache", RoboExtendedResponseCache.class.getName());
      classNameTranslations.put("java.net.ResponseSource", RoboResponseSource.class.getName());
      classNameTranslations.put("java.nio.charset.Charsets", RoboCharsets.class.getName());

      instrumentedPackages.addAll(Arrays.asList("dalvik.", "libcore.", "android.", "com.android.internal.", "org.apache.http.", "org.kxml2."));
      for (ShadowProvider provider : ServiceLoader.load(ShadowProvider.class)) {
        instrumentedPackages.addAll(Arrays.asList(provider.getProvidedPackageNames()));
      }

      return new InstrumentationConfiguration(classNameTranslations, interceptedMethods, instrumentedPackages, instrumentedClasses, classesToNotAcquire, packagesToNotAcquire, classesToNotInstrument);
    }
  }

  public static Builder newBuilder() {
    return new Builder();
  }

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
   * Determine if {@link org.robolectric.internal.bytecode.InstrumentingClassLoader} should instrument a given class.
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
   * Determine if {@link org.robolectric.internal.bytecode.InstrumentingClassLoader} should load a given class.
   *
   * @param   name The fully-qualified class name.
   * @return  True if the class should be loaded.
   */
  public boolean shouldAcquire(String name) {
    // the org.robolectric.res and org.robolectric.manifest packages live in the base classloader, but not its tests; yuck.
    int lastDot = name.lastIndexOf('.');
    String pkgName = name.substring(0, lastDot == -1 ? 0 : lastDot);
    if (pkgName.equals("org.robolectric.res") || (pkgName.equals("org.robolectric.manifest"))) {
      return name.contains("Test");
    }

    if (name.matches("com\\.android\\.internal\\.R(\\$.*)?")) return true;

    // Android SDK code almost universally refers to com.android.internal.R, except
    // when refering to android.R.stylable, as in HorizontalScrollView. arghgh.
    // See https://github.com/robolectric/robolectric/issues/521
    if (name.equals("android.R$styleable")) {
      return true;
    }

    // Hack. Fixes https://github.com/robolectric/robolectric/issues/1864
    if (name.equals("javax.net.ssl.DistinguishedNameParser")) {
      return true;
    }

    for (String packageName : packagesToNotAcquire) {
      if (name.startsWith(packageName)) return false;
    }

    return !(name.matches(".*\\.R(|\\$[a-z]+)$") || classesToNotAcquire.contains(name));
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

  private static Collection<String> stringify(Class... classes) {
    ArrayList<String> strings = new ArrayList<>();
    for (Class aClass : classes) {
      strings.add(aClass.getName());
    }
    return strings;
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
}
