package org.robolectric.internal.bytecode;

import android.R;
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
import org.robolectric.util.Transcript;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;
import java.util.Set;

import static java.util.Arrays.asList;

/**
 * Configuration rules for {@link org.robolectric.internal.bytecode.InstrumentingClassLoader}.
 */
public class InstrumentingClassLoaderConfig {
  private static final List<String> INSTRUMENTED_PACKAGES = new ArrayList<>();
  private static final List<String> CLASSES_TO_ALWAYS_DELEGATE = stringify(
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
      ParallelUniverseInterface.class
  );

  static {
    INSTRUMENTED_PACKAGES.addAll(Arrays.asList("dalvik.", "libcore.", "android.", "com.android.internal.", "com.google.android.gms."));
    for (ShadowProvider provider : ServiceLoader.load(ShadowProvider.class)) {
      Collections.addAll(INSTRUMENTED_PACKAGES, provider.getProvidedPackageNames());
    }
  }

  /**
   * Determine if {@link org.robolectric.internal.bytecode.InstrumentingClassLoader} should instrument a given class.
   *
   * @param   classInfo The class to check.
   * @return  True if the class should be instrumented.
   */
  public boolean shouldInstrument(ClassInfo classInfo) {
    return !(classInfo.isInterface() || classInfo.isAnnotation() || classInfo.hasAnnotation(DoNotInstrument.class)) && (isFromAndroidSdk(classInfo) || classInfo.hasAnnotation(Instrument.class));
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
            || CLASSES_TO_ALWAYS_DELEGATE.contains(name)
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
    return Collections.unmodifiableSet(new HashSet<>(asList(
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
    )));
  }

  /**
   * Map from a requested class to an alternate stand-in, or not.
   *
   * @return Mapping of class name translations.
   */
  public Map<String, String> classNameTranslations() {
    Map<String, String> map = new HashMap<>();
    map.put("java.net.ExtendedResponseCache", RoboExtendedResponseCache.class.getName());
    map.put("java.net.ResponseSource", RoboResponseSource.class.getName());
    map.put("java.nio.charset.Charsets", RoboCharsets.class.getName());
    return map;
  }

  public boolean containsStubs(ClassInfo classInfo) {
    return classInfo.getName().startsWith("com.google.android.maps.");
  }

  private static List<String> stringify(Class... classes) {
    ArrayList<String> strings = new ArrayList<>();
    for (Class aClass : classes) {
      strings.add(aClass.getName());
    }
    return strings;
  }

  private boolean isFromAndroidSdk(ClassInfo classInfo) {
    final String className = classInfo.getName();
    for (String instrumentedPackage : INSTRUMENTED_PACKAGES) {
      if (className.startsWith(instrumentedPackage)) {
        return true;
      }
    }
    return false;
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
