package org.robolectric.android;

import android.os.Build;
import com.google.common.annotations.VisibleForTesting;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.annotation.Nonnull;
import org.junit.AssumptionViolatedException;
import org.junit.runners.model.FrameworkMethod;
import org.robolectric.annotation.LooperMode;
import org.robolectric.internal.AndroidConfigurer;
import org.robolectric.internal.AndroidSandbox;
import org.robolectric.internal.ManifestIdentifier;
import org.robolectric.internal.ResourcesMode;
import org.robolectric.internal.SandboxManager;
import org.robolectric.internal.bytecode.ClassHandler;
import org.robolectric.internal.bytecode.ClassHandlerBuilder;
import org.robolectric.internal.bytecode.InstrumentationConfiguration;
import org.robolectric.internal.bytecode.Interceptors;
import org.robolectric.internal.bytecode.Sandbox;
import org.robolectric.internal.bytecode.ShadowMap;
import org.robolectric.internal.bytecode.ShadowProviders;
import org.robolectric.internal.bytecode.ShadowWrangler;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.pluginapi.Sdk;
import org.robolectric.pluginapi.config.Configuration;

public class RobolectricManager {
  private final ShadowProviders shadowProviders;
  private final ClassHandlerBuilder classHandlerBuilder;
  private final SandboxManager sandboxManager;
  private final AndroidConfigurer androidConfigurer;
  private final SandboxConfigurer[] sandboxConfigurers;

  private final Map<ManifestIdentifier, AndroidManifest> appManifestsCache = new HashMap<>();

  public RobolectricManager(
      ShadowProviders shadowProviders, ClassHandlerBuilder classHandlerBuilder, SandboxManager sandboxManager,
      AndroidConfigurer androidConfigurer, SandboxConfigurer[] sandboxConfigurers) {
    this.shadowProviders = shadowProviders;
    this.classHandlerBuilder = classHandlerBuilder;
    this.sandboxManager = sandboxManager;
    this.androidConfigurer = androidConfigurer;
    this.sandboxConfigurers = sandboxConfigurers;
  }

  public AndroidSandbox getSandbox(Configuration configuration) {
    ResourcesMode resourcesMode = configuration.get(ResourcesMode.class);
    Sdk sdk = configuration.get(Sdk.class);

    if (resourcesMode == ResourcesMode.LEGACY && sdk.getApiLevel() > Build.VERSION_CODES.P) {
      throw new AssumptionViolatedException("Robolectric doesn't support legacy mode after P");
    }
    sdk.verifySupportedSdk();

    LooperMode.Mode looperMode = configuration.get(LooperMode.Mode.class);

    InstrumentationConfiguration classLoaderConfig = createClassLoaderConfig(configuration);

    return sandboxManager.getAndroidSandbox(classLoaderConfig, sdk, resourcesMode, looperMode);
  }

  /**
   * Create an {@link InstrumentationConfiguration} suitable for the provided
   * {@link FrameworkMethod}.
   *
   * Adds configuration for Android using {@link AndroidConfigurer}.
   *
   * Custom TestRunner subclasses may wish to override this method to provide additional
   * configuration.
   *
   * @param configuration
   * @return an {@link InstrumentationConfiguration}
   */
  @Nonnull
  protected InstrumentationConfiguration createClassLoaderConfig(Configuration configuration) {
    InstrumentationConfiguration.Builder builder =
        InstrumentationConfiguration.newBuilder()
            .doNotAcquirePackage("java.")
            .doNotAcquirePackage("jdk.internal.")
            .doNotAcquirePackage("sun.")
            .doNotAcquirePackage("org.robolectric.annotation.")
            .doNotAcquirePackage("org.robolectric.internal.")
            .doNotAcquirePackage("org.robolectric.pluginapi.")
            .doNotAcquirePackage("org.robolectric.util.");

    String customPackages = System.getProperty("org.robolectric.packagesToNotAcquire", "");
    for (String pkg : customPackages.split(",")) {
      if (!pkg.isEmpty()) {
        builder.doNotAcquirePackage(pkg);
      }
    }

    String customClassesRegex =
        System.getProperty("org.robolectric.classesToNotInstrumentRegex", "");
    if (!customClassesRegex.isEmpty()) {
      builder.setDoNotInstrumentClassRegex(customClassesRegex);
    }

    for (SandboxConfigurer sandboxConfigurer : sandboxConfigurers) {
      sandboxConfigurer.configure(builder);
    }

    SandboxConfigurer sandboxConfigurerFromConfiguration = configuration.get(SandboxConfigurer.class);
    if (sandboxConfigurerFromConfiguration != null) {
      sandboxConfigurerFromConfiguration.configure(builder);
    }

    androidConfigurer.configure(builder, getInterceptors());

    return builder.build();
  }

  public void configure(Sandbox sandbox, Configuration configuration) {
    ShadowMap.Builder builder = shadowProviders.getBaseShadowMap().newBuilder();

    for (SandboxConfigurer sandboxConfigurer : sandboxConfigurers) {
      sandboxConfigurer.configure(builder);
    }

    // Configure shadows *BEFORE* setting the ClassLoader. This is necessary because
    // creating the ShadowMap loads all ShadowProviders via ServiceLoader and this is
    // not available once we install the Robolectric class loader.
    SandboxConfigurer sandboxConfigurerFromConfiguration = configuration.get(SandboxConfigurer.class);
    if (sandboxConfigurerFromConfiguration != null) {
      sandboxConfigurerFromConfiguration.configure(builder);
    }
    ShadowMap shadowMap = builder.build();
    sandbox.replaceShadowMap(shadowMap);

    sandbox.configure(createClassHandler(shadowMap, sandbox), getInterceptors());
  }

  /**
   * Create a {@link ClassHandler} appropriate for the given arguments.
   *
   * Robolectric may chose to cache the returned instance, keyed by <tt>shadowMap</tt> and <tt>sandbox</tt>.
   *
   * Custom TestRunner subclasses may wish to override this method to provide alternate configuration.
   *
   * @param shadowMap the {@link ShadowMap} in effect for this test
   * @param sandbox the {@link Sdk} in effect for this test
   * @return an appropriate {@link ShadowWrangler}.
   * @since 5.0
   */
  @Nonnull
  protected ClassHandler createClassHandler(ShadowMap shadowMap, Sandbox sandbox) {
    int apiLevel = ((AndroidSandbox) sandbox).getSdk().getApiLevel();
    AndroidSdkShadowMatcher shadowMatcher = new AndroidSdkShadowMatcher(apiLevel);
    return classHandlerBuilder.build(shadowMap, shadowMatcher, getInterceptors());
  }

  @Nonnull
  protected Interceptors getInterceptors() {
    return new Interceptors(AndroidInterceptors.all());
  }

  public AndroidManifest cachedCreateAppManifest(ManifestIdentifier identifier) {
    synchronized (appManifestsCache) {
      AndroidManifest appManifest;
      appManifest = appManifestsCache.get(identifier);
      if (appManifest == null) {
        appManifest = createAndroidManifest(identifier);
        appManifestsCache.put(identifier, appManifest);
      }

      return appManifest;
    }
  }

  /**
   * Internal use only.
   *
   * @deprecated Do not use.
   */
  @Deprecated
  @VisibleForTesting
  public static AndroidManifest createAndroidManifest(ManifestIdentifier manifestIdentifier) {
    List<ManifestIdentifier> libraries = manifestIdentifier.getLibraries();

    List<AndroidManifest> libraryManifests = new ArrayList<>();
    for (ManifestIdentifier library : libraries) {
      libraryManifests.add(createAndroidManifest(library));
    }

    return new AndroidManifest(manifestIdentifier.getManifestFile(), manifestIdentifier.getResDir(),
        manifestIdentifier.getAssetDir(), libraryManifests, manifestIdentifier.getPackageName(),
        manifestIdentifier.getApkFile());
  }

}
