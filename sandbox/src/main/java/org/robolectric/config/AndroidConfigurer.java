package org.robolectric.config;

import java.nio.charset.StandardCharsets;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implements;
import org.robolectric.internal.bytecode.InstrumentationConfiguration;
import org.robolectric.internal.bytecode.Interceptors;
import org.robolectric.internal.bytecode.MethodRef;
import org.robolectric.internal.bytecode.ShadowProviders;

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
        .doNotAcquireClass("org.robolectric.RobolectricTestRunner")
        .doNotAcquireClass("org.robolectric.shadow.api.ShadowPicker");

    builder
        .doNotAcquirePackage(
            "com.almworks.sqlite4java") // Fix #958: SQLite native library must be loaded once.
        .doNotAcquirePackage("io.mockk.proxy.")
        .doNotAcquirePackage("kotlin.")
        .doNotAcquirePackage("org.bouncycastle.")
        .doNotAcquirePackage("org.conscrypt.")
        .doNotAcquirePackage("org.hamcrest")
        .doNotAcquirePackage("org.jacoco.")
        .doNotAcquirePackage("org.objectweb.asm")
        .doNotAcquirePackage("org.robolectric.manifest.")
        .doNotAcquirePackage("org.robolectric.res.")
        .doNotAcquirePackage("org.robolectric.RobolectricTestRunner$")
        .doNotAcquirePackage("org.w3c.")
        .doNotAcquirePackage("org.xml.")
        .doNotAcquirePackage("org.specs2") // Required for Maven SureFire / RoboSpecs.
        .doNotAcquirePackage("scala."); // Required for Maven SureFire / RoboSpecs.

    builder
        .addClassNameTranslation(
            "java.net.ExtendedResponseCache", "org.robolectric.fakes.RoboExtendedResponseCache")
        .addClassNameTranslation(
            "java.net.ResponseSource", "org.robolectric.fakes.RoboResponseSource")
        // Needed for android.net.Uri in older SDK versions
        .addClassNameTranslation("java.nio.charset.Charsets", StandardCharsets.class.getName())
        .addClassNameTranslation("java.lang.UnsafeByteSequence", Object.class.getName())
        .addClassNameTranslation("java.util.jar.StrictJarFile", Object.class.getName())
        .addClassNameTranslation("sun.misc.Cleaner", "java.lang.ref.Cleaner$Cleanable")
        .addClassNameTranslation(
            "android.app.sdksandbox.sandboxactivity.SdkSandboxActivityAuthority",
            "org.robolectric.fakes.RoboSdkSandboxActivityAuthority");

    // Don't acquire legacy support packages.
    builder
        .doNotInstrumentPackage("android.support.constraint.")
        .doNotInstrumentPackage("android.support.v7.view.")
        .doNotInstrumentPackage("android.arch")
        .doNotInstrumentPackage("android.support.test")
        .doNotInstrumentPackage("android.R"); // android.R* are pure data classes.

    // Instrumenting this Exceptions causes "java.lang.NegativeArraySizeException: -2" and
    // leads to java.lang.NoClassDefFoundError.
    builder.doNotInstrumentClass("android.app.RecoverableSecurityException");

    builder
        .addInstrumentedPackage("dalvik.")
        .addInstrumentedPackage("libcore.")
        .addInstrumentedPackage("android.")
        .addInstrumentedPackage("com.android.internal.")
        .addInstrumentedPackage("org.apache.http.") // For httpclient shadows.
        .addInstrumentedPackage("org.ccil.cowan.tagsoup.") // For the System.arraycopy interceptor.
        .addInstrumentedPackage("org.kxml2."); // For the System.arraycopy interceptor.

    // Mockito's MockMethodDispatcher must only exist in the Bootstrap class loader.
    builder.doNotAcquireClass(
        "org.mockito.internal.creation.bytebuddy.inject.MockMethodDispatcher");

    for (String packagePrefix : shadowProviders.getInstrumentedPackages()) {
      builder.addInstrumentedPackage(packagePrefix);
    }
  }
}
