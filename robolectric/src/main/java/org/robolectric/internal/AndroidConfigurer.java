package org.robolectric.internal;

import org.robolectric.RobolectricTestRunner;
import org.robolectric.TestLifecycle;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.internal.DoNotInstrument;
import org.robolectric.annotation.internal.Instrument;
import org.robolectric.internal.bytecode.ClassHandler;
import org.robolectric.internal.bytecode.InstrumentationConfiguration;
import org.robolectric.internal.bytecode.InstrumentingClassLoader;
import org.robolectric.internal.bytecode.Interceptor;
import org.robolectric.internal.bytecode.Interceptors;
import org.robolectric.internal.bytecode.MethodRef;
import org.robolectric.internal.bytecode.ShadowInvalidator;
import org.robolectric.internal.bytecode.ShadowWrangler;
import org.robolectric.internal.dependency.DependencyJar;
import org.robolectric.internal.fakes.RoboCharsets;
import org.robolectric.internal.fakes.RoboExtendedResponseCache;
import org.robolectric.internal.fakes.RoboResponseSource;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.res.ResourcePath;
import org.robolectric.res.ResourceTable;
import org.robolectric.res.builder.XmlBlock;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.TempDirectory;

import java.util.ServiceLoader;

public class AndroidConfigurer {
  public static InstrumentationConfiguration.Builder withConfig(InstrumentationConfiguration.Builder builder, Config config) {
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
    return builder;
  }

  public static InstrumentationConfiguration.Builder configure(InstrumentationConfiguration.Builder builder, Interceptors interceptors) {
    for (MethodRef methodRef : interceptors.getAllMethodRefs()) {
      builder.addInterceptedMethod(methodRef);
    }

    builder
        .doNotAcquireClass(ClassHandler.class)
        .doNotAcquireClass(ClassHandler.Plan.class)
        .doNotAcquireClass(Interceptors.class)
        .doNotAcquireClass(ShadowInvalidator.class)
        .doNotAcquireClass(ShadowedObject.class);

    builder
        .doNotAcquireClass(TestLifecycle.class)
        .doNotAcquireClass(ShadowWrangler.class)
        .doNotAcquireClass(AndroidManifest.class)
        .doNotAcquireClass(InstrumentingClassLoader.class)
        .doNotAcquireClass(SdkEnvironment.class)
        .doNotAcquireClass(SdkConfig.class)
        .doNotAcquireClass(RobolectricTestRunner.class)
        .doNotAcquireClass(RobolectricTestRunner.HelperTestRunner.class)
        .doNotAcquireClass(ResourcePath.class)
        .doNotAcquireClass(ResourceTable.class)
        .doNotAcquireClass(XmlBlock.class)
        .doNotAcquireClass(RealObject.class)
        .doNotAcquireClass(Implements.class)
        .doNotAcquireClass(Implementation.class)
        .doNotAcquireClass(Instrument.class)
        .doNotAcquireClass(DoNotInstrument.class)
        .doNotAcquireClass(Config.class)
        .doNotAcquireClass(IShadow.class)
        .doNotAcquireClass(Interceptor.class)
        .doNotAcquireClass(ReflectionHelpers.class)
        .doNotAcquireClass(ReflectionHelpers.ClassParameter.class)
        .doNotAcquireClass(DirectObjectMarker.class)
        .doNotAcquireClass(DependencyJar.class)
        .doNotAcquireClass(ParallelUniverseInterface.class)
        .doNotAcquireClass(TempDirectory.class);

    builder.doNotAcquirePackage("java.")
      .doNotAcquirePackage("javax.")
      .doNotAcquirePackage("sun.")
      .doNotAcquirePackage("com.sun.")
      .doNotAcquirePackage("org.w3c.")
      .doNotAcquirePackage("org.xml.")
      .doNotAcquirePackage("org.junit")
      .doNotAcquirePackage("org.hamcrest")
      .doNotAcquirePackage("org.specs2")  // allows for android projects with mixed scala\java tests to be
      .doNotAcquirePackage("scala.")      //  run with Maven Surefire (see the RoboSpecs project on github)
      .doNotAcquirePackage("kotlin.")
      .doNotAcquirePackage("com.almworks.sqlite4java"); // Fix #958: SQLite native library must be loaded once.

    builder.addClassNameTranslation("java.net.ExtendedResponseCache", RoboExtendedResponseCache.class.getName())
       .addClassNameTranslation("java.net.ResponseSource", RoboResponseSource.class.getName())
       .addClassNameTranslation("java.nio.charset.Charsets", RoboCharsets.class.getName());

    // Instrumenting these classes causes a weird failure.
    builder.doNotInstrumentClass("android.R")
        .doNotInstrumentClass("android.R$styleable");

    builder.addInstrumentedPackage("dalvik.")
      .addInstrumentedPackage("libcore.")
      .addInstrumentedPackage("android.")
      .addInstrumentedPackage("com.android.internal.")
      .addInstrumentedPackage("org.apache.http.")
      .addInstrumentedPackage("org.kxml2.");


    for (ShadowProvider provider : ServiceLoader.load(ShadowProvider.class)) {
      for (String packagePrefix : provider.getProvidedPackageNames()) {
        builder.addInstrumentedPackage(packagePrefix);
      }
    }
    return builder;
  }
}
