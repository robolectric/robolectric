package org.robolectric.simulator;

import com.google.common.collect.Iterables;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Properties;
import org.robolectric.android.AndroidSdkShadowMatcher;
import org.robolectric.annotation.ResourcesMode;
import org.robolectric.annotation.SQLiteMode;
import org.robolectric.interceptors.AndroidInterceptors;
import org.robolectric.internal.AndroidSandbox;
import org.robolectric.internal.SandboxManager;
import org.robolectric.internal.bytecode.ClassHandler;
import org.robolectric.internal.bytecode.ClassHandlerBuilder;
import org.robolectric.internal.bytecode.InstrumentationConfiguration;
import org.robolectric.internal.bytecode.Interceptors;
import org.robolectric.internal.bytecode.MethodRef;
import org.robolectric.internal.bytecode.ShadowMap;
import org.robolectric.internal.bytecode.ShadowProviders;
import org.robolectric.pluginapi.Sdk;
import org.robolectric.pluginapi.SdkProvider;
import org.robolectric.simulator.SimulatorClassLoader.JarCollection;
import org.robolectric.util.inject.Injector;

/**
 * A Builder pattern for Robolectric Sandboxes. This also binds a {@link JarCollection} class that
 * encapsulates extra jars that can be used by the Robolectric ClassLoader.
 */
public final class SandboxBuilder {

  private final List<Path> extraJars = new ArrayList<>();

  private SandboxBuilder() {}

  public static SandboxBuilder newBuilder() {
    return new SandboxBuilder();
  }

  @CanIgnoreReturnValue
  public SandboxBuilder addExtraJar(Path jarPath) {
    extraJars.add(jarPath);
    return this;
  }

  @CanIgnoreReturnValue
  public SandboxBuilder addExtraJars(Collection<Path> jarPaths) {
    extraJars.addAll(jarPaths);
    return this;
  }

  public AndroidSandbox build() {
    Injector injector =
        new Injector.Builder()
            .bind(Properties.class, System.getProperties())
            .bind(JarCollection.class, new JarCollection(extraJars))
            .build();

    SdkProvider sdkProvider = injector.getInstance(SdkProvider.class);

    ArrayList<Sdk> sdks = new ArrayList<>(sdkProvider.getSdks());

    Sdk latestSdk = Iterables.getLast(sdks);

    Interceptors interceptors = new Interceptors(AndroidInterceptors.all());

    InstrumentationConfiguration instrumentationConfiguration =
        buildInstrumentationConfiguration(interceptors);

    SandboxManager.SandboxBuilder sandboxBuilder =
        injector.getInstance(SandboxManager.SandboxBuilder.class);

    AndroidSandbox androidSandbox =
        sandboxBuilder.build(
            instrumentationConfiguration,
            latestSdk,
            latestSdk,
            ResourcesMode.Mode.BINARY,
            SQLiteMode.Mode.NATIVE);

    ShadowProviders shadowProviders = injector.getInstance(ShadowProviders.class);
    ClassHandlerBuilder classHandlerBuilder = injector.getInstance(ClassHandlerBuilder.class);

    ShadowMap.Builder smBuilder = shadowProviders.getBaseShadowMap().newBuilder();

    ShadowMap shadowMap = smBuilder.build();
    androidSandbox.replaceShadowMap(shadowMap);

    AndroidSdkShadowMatcher shadowMatcher = new AndroidSdkShadowMatcher(latestSdk.getApiLevel());

    ClassHandler classHandler = classHandlerBuilder.build(shadowMap, shadowMatcher, interceptors);

    androidSandbox.configure(classHandler, interceptors);

    return androidSandbox;
  }

  private InstrumentationConfiguration buildInstrumentationConfiguration(
      Interceptors interceptors) {
    InstrumentationConfiguration.Builder builder = new InstrumentationConfiguration.Builder();

    for (MethodRef methodRef : interceptors.getAllMethodRefs()) {
      builder.addInterceptedMethod(methodRef);
    }

    builder.doNotAcquireClass("org.robolectric.shadow.api.ShadowPicker");

    builder
        .doNotAcquirePackage("org.bouncycastle.")
        .doNotAcquirePackage("org.conscrypt.")
        .doNotAcquirePackage("org.objectweb.asm")
        .doNotAcquirePackage("org.robolectric.manifest.")
        .doNotAcquirePackage("org.robolectric.res.")
        .doNotAcquirePackage("org.w3c.")
        .doNotAcquirePackage("org.xml.");

    builder
        .addClassNameTranslation(
            "java.net.ExtendedResponseCache", "org.robolectric.fakes.RoboExtendedResponseCache")
        .addClassNameTranslation(
            "java.net.ResponseSource", "org.robolectric.fakes.RoboResponseSource")
        // Needed for android.net.Uri in older SDK versions
        .addClassNameTranslation("java.nio.charset.Charsets", StandardCharsets.class.getName())
        .addClassNameTranslation("java.lang.UnsafeByteSequence", Object.class.getName())
        .addClassNameTranslation("java.util.jar.StrictJarFile", Object.class.getName())
        .addClassNameTranslation("sun.misc.Cleaner", "java.lang.ref.Cleaner$Cleanable");

    // Don't instrument legacy support packages.
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

    return builder.build();
  }
}
