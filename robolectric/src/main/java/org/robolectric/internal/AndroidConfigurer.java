package org.robolectric.internal;

import org.robolectric.ApkLoader;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.TestLifecycle;
import org.robolectric.android.fakes.RoboCharsets;
import org.robolectric.android.fakes.RoboExtendedResponseCache;
import org.robolectric.android.fakes.RoboResponseSource;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implements;
import org.robolectric.internal.bytecode.InstrumentationConfiguration;
import org.robolectric.internal.bytecode.Interceptors;
import org.robolectric.internal.bytecode.MethodRef;
import org.robolectric.internal.bytecode.ShadowProviders;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.ResourcePath;
import org.robolectric.res.ResourceTable;
import org.robolectric.res.builder.XmlBlock;
import org.robolectric.shadow.api.ShadowPicker;
import org.robolectric.util.Util;

/** Instruments the Android jars */
public class AndroidConfigurer {

  private final ShadowProviders shadowProviders;

  public AndroidConfigurer(ShadowProviders shadowProviders) {
    this.shadowProviders = shadowProviders;
  }

  public void withConfig(InstrumentationConfiguration.Builder builder, Config config) {
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

  public void configure(InstrumentationConfiguration.Builder builder, Interceptors interceptors) {
    for (MethodRef methodRef : interceptors.getAllMethodRefs()) {
      builder.addInterceptedMethod(methodRef);
    }

    builder
        .doNotAcquireClass(TestLifecycle.class)
        .doNotAcquireClass(AndroidManifest.class)
        .doNotAcquireClass(RobolectricTestRunner.class)
        .doNotAcquireClass(RobolectricTestRunner.HelperTestRunner.class)
        .doNotAcquireClass(ShadowPicker.class)
        .doNotAcquireClass(ResourcePath.class)
        .doNotAcquireClass(ResourceTable.class)
        .doNotAcquireClass(ApkLoader.class)
        .doNotAcquireClass(XmlBlock.class);

    builder
        .doNotAcquirePackage("javax.")
        .doNotAcquirePackage("jdk.internal.")
        .doNotAcquirePackage("org.junit")
        .doNotAcquirePackage("org.hamcrest")
        .doNotAcquirePackage("org.robolectric.annotation.")
        .doNotAcquirePackage("org.robolectric.internal.")
        .doNotAcquirePackage("org.robolectric.pluginapi.")
        .doNotAcquirePackage("org.robolectric.manifest.")
        .doNotAcquirePackage("org.robolectric.res.")
        .doNotAcquirePackage("org.robolectric.util.")
        .doNotAcquirePackage("org.robolectric.RobolectricTestRunner$")
        .doNotAcquirePackage("sun.")
        .doNotAcquirePackage("com.sun.")
        .doNotAcquirePackage("org.w3c.")
        .doNotAcquirePackage("org.xml.")
        .doNotAcquirePackage(
            "org.specs2") // allows for android projects with mixed scala\java tests to be
        .doNotAcquirePackage(
            "scala.") //  run with Maven Surefire (see the RoboSpecs project on github)
        .doNotAcquirePackage("kotlin.")
        .doNotAcquirePackage("io.mockk.")
        .doNotAcquirePackage("org.bouncycastle.")
        .doNotAcquirePackage("org.conscrypt.")
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

    if (Util.getJavaVersion() >= 9) {
      builder.addClassNameTranslation("sun.misc.Cleaner", "java.lang.ref.Cleaner$Cleanable");
    }

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

    // exclude arch libraries from instrumentation. These are just android libs and no one
    // should need to shadow them
    builder.doNotInstrumentPackage("androidx.room");
    builder.doNotInstrumentPackage("androidx.arch");
    builder.doNotInstrumentPackage("android.arch");
    builder.doNotInstrumentPackage("androidx.lifecycle");
    builder.doNotInstrumentPackage("androidx.paging");
    builder.doNotInstrumentPackage("androidx.work");
    builder.doNotInstrumentPackage("androidx.datastore");

    // exclude Compose libraries from instrumentation. These are written in Kotlin and
    // fail on any usage due to DefaultConstructorMarker being inaccessible.
    builder.doNotInstrumentPackage("androidx.compose");
    builder.doNotInstrumentPackage("androidx.ui");
    builder.doNotInstrumentPackage("androidx.fragment");

    builder.doNotInstrumentPackage("androidx.test");
    builder.doNotInstrumentPackage("android.support.test");

    // Mockito's MockMethodDispatcher must only exist in the Bootstrap class loader.
    builder.doNotAcquireClass(
        "org.mockito.internal.creation.bytebuddy.inject.MockMethodDispatcher");

    for (String packagePrefix : shadowProviders.getInstrumentedPackages()) {
      builder.addInstrumentedPackage(packagePrefix);
    }
  }
}
