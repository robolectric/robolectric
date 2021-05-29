package org.robolectric.config;

import java.nio.charset.StandardCharsets;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implements;
import org.robolectric.internal.bytecode.InstrumentationConfiguration;
import org.robolectric.internal.bytecode.Interceptors;
import org.robolectric.internal.bytecode.MethodRef;
import org.robolectric.internal.bytecode.ShadowProviders;
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
        .doNotAcquireClass("org.robolectric.TestLifecycle")
        .doNotAcquireClass("org.robolectric.manifest.AndroidManifest")
        .doNotAcquireClass("org.robolectric.RobolectricTestRunner")
        .doNotAcquireClass("org.robolectric.RobolectricTestRunner.HelperTestRunner")
        .doNotAcquireClass("org.robolectric.shadow.api.ShadowPicker")
        .doNotAcquireClass("org.robolectric.res.ResourcePath")
        .doNotAcquireClass("org.robolectric.res.ResourceTable")
        .doNotAcquireClass("org.robolectric.ApkLoader")
        .doNotAcquireClass("org.robolectric.res.builder.XmlBlock");

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
        .doNotAcquirePackage("io.mockk.proxy.")
        .doNotAcquirePackage("org.bouncycastle.")
        .doNotAcquirePackage("org.conscrypt.")
        // Fix #958: SQLite native library must be loaded once.
        .doNotAcquirePackage("com.almworks.sqlite4java")
        .doNotAcquirePackage("org.jacoco.");

    builder
        .addClassNameTranslation(
            "java.net.ExtendedResponseCache", "org.robolectric.fakes.RoboExtendedResponseCache")
        .addClassNameTranslation(
            "java.net.ResponseSource", "org.robolectric.fakes.RoboResponseSource")
        // Needed for android.net.Uri in older SDK versions
        .addClassNameTranslation("java.nio.charset.Charsets", StandardCharsets.class.getName())
        .addClassNameTranslation("java.lang.UnsafeByteSequence", Object.class.getName())
        .addClassNameTranslation("java.util.jar.StrictJarFile", Object.class.getName());

    if (Util.getJavaVersion() >= 9) {
      builder.addClassNameTranslation("sun.misc.Cleaner", "java.lang.ref.Cleaner$Cleanable");
    }

    // Instrumenting these classes causes a weird failure.
    builder.doNotInstrumentClass("android.R")
        .doNotInstrumentClass("android.R$styleable");

    builder
        .addInstrumentedPackage("dalvik.")
        .addInstrumentedPackage("libcore.")
        .addInstrumentedPackage("android.")
        .addInstrumentedPackage("com.android.internal.")
        .addInstrumentedPackage("org.apache.http.")
        .addInstrumentedPackage("org.ccil.cowan.tagsoup")
        .addInstrumentedPackage("org.kxml2.");

    builder.doNotInstrumentPackage("android.arch");
    builder.doNotInstrumentPackage("android.support.test");

    // Mockito's MockMethodDispatcher must only exist in the Bootstrap class loader.
    builder.doNotAcquireClass(
        "org.mockito.internal.creation.bytebuddy.inject.MockMethodDispatcher");

    for (String packagePrefix : shadowProviders.getInstrumentedPackages()) {
      builder.addInstrumentedPackage(packagePrefix);
    }
  }
}
