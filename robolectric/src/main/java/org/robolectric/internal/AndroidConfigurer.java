package org.robolectric.internal;

import java.util.ServiceLoader;
import org.robolectric.ApkLoader;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.TestLifecycle;
import org.robolectric.android.fakes.RoboCharsets;
import org.robolectric.android.fakes.RoboExtendedResponseCache;
import org.robolectric.android.fakes.RoboResponseSource;
import org.robolectric.android.internal.AndroidBridge.BridgeFactory;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implements;
import org.robolectric.internal.bytecode.InstrumentationConfiguration;
import org.robolectric.internal.bytecode.Interceptors;
import org.robolectric.internal.bytecode.MethodRef;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.ResourcePath;
import org.robolectric.res.ResourceTable;
import org.robolectric.res.builder.XmlBlock;
import org.robolectric.shadow.api.ShadowPicker;

public class AndroidConfigurer {
  public static void withConfig(InstrumentationConfiguration.Builder builder, Config config) {
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
        builder.addInstrumentedClass(className);
      }
    }
    for (String packageName : config.instrumentedPackages()) {
      builder.addInstrumentedPackage(packageName);
    }
  }

  public static void configure(InstrumentationConfiguration.Builder builder, Interceptors interceptors) {
    for (MethodRef methodRef : interceptors.getAllMethodRefs()) {
      builder.addInterceptedMethod(methodRef);
    }

    builder
        .doNotAcquireClass(ApkLoader.class)
        .doNotAcquireClass(AndroidManifest.class)
        .doNotAcquireClass(BridgeFactory.class)
        .doNotAcquireClass(RobolectricTestRunner.class)
        .doNotAcquireClass(RobolectricTestRunner.HelperTestRunner.class)
        .doNotAcquireClass(ResourcePath.class)
        .doNotAcquireClass(ResourceTable.class)
        .doNotAcquireClass(TestLifecycle.class)
        .doNotAcquireClass(ShadowPicker.class)
        .doNotAcquireClass(XmlBlock.class);

    builder
        .doNotAcquirePackage("javax.")
        .doNotAcquirePackage("org.junit")
        .doNotAcquirePackage("org.hamcrest")
        .doNotAcquirePackage("org.robolectric.annotation.")
        .doNotAcquirePackage("org.robolectric.internal.")
        .doNotAcquirePackage("org.robolectric.manifest.")
        .doNotAcquirePackage("org.robolectric.res.")
        .doNotAcquirePackage("org.robolectric.util.")
        .doNotAcquirePackage("org.robolectric.RobolectricTestRunner$")
        .doNotAcquirePackage("sun.")
        .doNotAcquirePackage("com.sun.")
        .doNotAcquirePackage("org.w3c.")
        .doNotAcquirePackage("org.xml.")
        .doNotAcquirePackage("org.specs2")  // allows for android projects with mixed scala\java tests to be
        .doNotAcquirePackage("scala.")      //  run with Maven Surefire (see the RoboSpecs project on github)
        .doNotAcquirePackage("kotlin.")
         // Fix #958: SQLite native library must be loaded once.
        .doNotAcquirePackage("com.almworks.sqlite4java")
        .doNotAcquirePackage("org.jacoco.");

    builder
        .addClassNameTranslation(
            "java.net.ExtendedResponseCache", RoboExtendedResponseCache.class.getName())
        .addClassNameTranslation("java.net.ResponseSource", RoboResponseSource.class.getName())
        .addClassNameTranslation("java.nio.charset.Charsets", RoboCharsets.class.getName())
        .addClassNameTranslation("java.lang.UnsafeByteSequence", Object.class.getName())
        .addClassNameTranslation("java.util.jar.StrictJarFile", Object.class.getName());

    // Instrumenting these classes causes a weird failure.
    builder.doNotInstrumentClass("android.R")
        .doNotInstrumentClass("android.R$styleable");

    builder.addInstrumentedPackage("dalvik.")
        .addInstrumentedPackage("libcore.")
        .addInstrumentedPackage("android.")
        .addInstrumentedPackage("androidx.")
        .addInstrumentedPackage("com.android.internal.")
        .addInstrumentedPackage("org.apache.http.")
        .addInstrumentedPackage("org.ccil.cowan.tagsoup")
        .addInstrumentedPackage("org.kxml2.");

    // Room's migration package uses GSON and reflection to create Java classes from JSON files.
    // This results in an error where two __robo_data__ fields get added to the same object.
    builder.doNotInstrumentPackage("androidx.room.migration");
    builder.doNotInstrumentPackage("androidx.test");
    builder.doNotInstrumentPackage("android.arch.persistence.room.migration");
    builder.doNotInstrumentPackage("android.support.test");

    for (ShadowProvider provider : ServiceLoader.load(ShadowProvider.class)) {
      for (String packagePrefix : provider.getProvidedPackageNames()) {
        builder.addInstrumentedPackage(packagePrefix);
      }
    }
  }
}
