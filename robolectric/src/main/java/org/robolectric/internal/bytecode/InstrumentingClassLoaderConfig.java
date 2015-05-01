package org.robolectric.internal.bytecode;

import android.R;
import org.robolectric.internal.ShadowedObject;
import org.robolectric.internal.ShadowProvider;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.internal.dependency.DependencyJar;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.internal.SdkConfig;
import org.robolectric.internal.SdkEnvironment;
import org.robolectric.TestLifecycle;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.internal.fakes.RoboExtendedResponseCache;
import org.robolectric.internal.fakes.RoboCharsets;
import org.robolectric.internal.fakes.RoboResponseSource;
import org.robolectric.annotation.internal.DoNotInstrument;
import org.robolectric.annotation.internal.Instrument;
import org.robolectric.internal.ParallelUniverseInterface;
import org.robolectric.res.ResourceLoader;
import org.robolectric.res.ResourcePath;
import org.robolectric.util.TempDirectory;
import org.robolectric.util.Transcript;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

/**
 * Configuration rules for {@link org.robolectric.internal.bytecode.InstrumentingClassLoader}.
 */
public class InstrumentingClassLoaderConfig {

  public static final class Builder {

    private final Collection<String> instrumentedPackages = new HashSet<>();
    private final Collection<MethodRef> interceptedMethods = new HashSet<>();
    private final Map<String, String> classNameTranslations = new HashMap<>();
    private final Collection<String> classesToNotAquire = new HashSet<>();

    public Builder doNotAquireClass(String className) {
      this.classesToNotAquire.add(className);
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

    public InstrumentingClassLoaderConfig build() {
      interceptedMethods.addAll(Arrays.asList(
          new MethodRef(LinkedHashMap.class, "eldest"),
          new MethodRef(System.class, "loadLibrary"),
          new MethodRef("android.os.StrictMode", "trackActivity"),
          new MethodRef("android.os.StrictMode", "incrementExpectedActivityCount"),
          new MethodRef("java.lang.AutoCloseable", "*"),
          new MethodRef("android.util.LocaleUtil", "getLayoutDirectionFromLocale"),
          new MethodRef("com.android.internal.policy.PolicyManager", "*"),
          new MethodRef("android.view.FallbackEventHandler", "*"),
          new MethodRef("android.view.IWindowSession", "*"),
          new MethodRef("java.lang.System", "nanoTime"),
          new MethodRef("java.lang.System", "currentTimeMillis"),
          new MethodRef("java.lang.System", "arraycopy"),
          new MethodRef("java.lang.System", "logE"),
          new MethodRef("java.util.Locale", "adjustLanguageCode")
      ));
      classesToNotAquire.addAll(stringify(
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
          ClassHandler.class,
          ClassHandler.Plan.class,
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
      classNameTranslations.put("java.net.ExtendedResponseCache", RoboExtendedResponseCache.class.getName());
      classNameTranslations.put("java.net.ResponseSource", RoboResponseSource.class.getName());
      classNameTranslations.put("java.nio.charset.Charsets", RoboCharsets.class.getName());

      instrumentedPackages.addAll(Arrays.asList("dalvik.", "libcore.", "android.", "com.android.internal.", "com.google.android.gms.", "org.apache.http."));
      for (ShadowProvider provider : ServiceLoader.load(ShadowProvider.class)) {
        instrumentedPackages.addAll(Arrays.asList(provider.getProvidedPackageNames()));
      }

      return new InstrumentingClassLoaderConfig(classNameTranslations, interceptedMethods, instrumentedPackages, classesToNotAquire);
    }
  }

  public static Builder newBuilder() {
    return new Builder();
  }

  private final List<String> instrumentedPackages = new ArrayList<>();
  private final Map<String, String> classNameTranslations = new HashMap<>();
  private final HashSet<MethodRef> interceptedMethods = new HashSet<>();
  private final Set<String> classesToNotAquire = new HashSet<>();

  private InstrumentingClassLoaderConfig(Map<String, String> classNameTranslations, Collection<MethodRef> interceptedMethods, Collection<String> instrumentedPackages, Collection<String> classesToNotAquire) {
    this.classNameTranslations.putAll(classNameTranslations);
    this.interceptedMethods.addAll(interceptedMethods);
    this.instrumentedPackages.addAll(instrumentedPackages);
    this.classesToNotAquire.addAll(classesToNotAquire);
  }

  /**
   * Determine if {@link org.robolectric.internal.bytecode.InstrumentingClassLoader} should instrument a given class.
   *
   * @param   classInfo The class to check.
   * @return  True if the class should be instrumented.
   */
  public boolean shouldInstrument(ClassInfo classInfo) {
    return !(classInfo.isInterface() || classInfo.isAnnotation() || classInfo.hasAnnotation(DoNotInstrument.class)) && (isInInstrumentedPackage(classInfo) || classInfo.hasAnnotation(Instrument.class));
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
    if (name.equals("android.R$styleable")) return true;

    return !(
        name.matches(".*\\.R(|\\$[a-z]+)$")
            || classesToNotAquire.contains(name)
            || name.startsWith("java.")
            || name.startsWith("javax.")
            || name.startsWith("sun.")
            || name.startsWith("com.sun.")
            || name.startsWith("org.w3c.")
            || name.startsWith("org.xml.")
            || name.startsWith("org.junit")
            || name.startsWith("org.hamcrest")
            || name.startsWith("org.specs2")  // allows for android projects with mixed scala\java tests to be
            || name.startsWith("scala.")      //  run with Maven Surefire (see the RoboSpecs project on github)
            || name.startsWith("kotlin.")
            || name.startsWith("com.almworks.sqlite4java") // Fix #958: SQLite native library must be loaded once.
    );
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

    InstrumentingClassLoaderConfig that = (InstrumentingClassLoaderConfig) o;

    if (!classNameTranslations.equals(that.classNameTranslations)) return false;
    if (!classesToNotAquire.equals(that.classesToNotAquire)) return false;
    if (!instrumentedPackages.equals(that.instrumentedPackages)) return false;
    if (!interceptedMethods.equals(that.interceptedMethods)) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = instrumentedPackages.hashCode();
    result = 31 * result + classNameTranslations.hashCode();
    result = 31 * result + interceptedMethods.hashCode();
    result = 31 * result + classesToNotAquire.hashCode();
    return result;
  }

  /**
   * Reference to a specific method on a class.
   */
  public static class MethodRef {
    public final String className;
    public final String methodName;

    public MethodRef(Class<?> clazz, String methodName) {
      this(clazz.getName(), methodName);
    }

    public MethodRef(String className, String methodName) {
      this.className = className;
      this.methodName = methodName;
    }

    @Override
    public boolean equals(Object o) {
      if (this == o) return true;
      if (o == null || getClass() != o.getClass()) return false;

      MethodRef methodRef = (MethodRef) o;

      if (!className.equals(methodRef.className)) return false;
      if (!methodName.equals(methodRef.methodName)) return false;

      return true;
    }

    @Override
    public int hashCode() {
      int result = className.hashCode();
      result = 31 * result + methodName.hashCode();
      return result;
    }

    @Override
    public String toString() {
      return "MethodRef{" +
          "className='" + className + '\'' +
          ", methodName='" + methodName + '\'' +
          '}';
    }
  }
}
