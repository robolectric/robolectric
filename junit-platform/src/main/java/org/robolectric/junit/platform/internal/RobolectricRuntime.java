package org.robolectric.junit.platform.internal;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import org.robolectric.android.AndroidSdkShadowMatcher;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import org.robolectric.annotation.LooperMode;
import org.robolectric.annotation.ResourcesMode;
import org.robolectric.annotation.SQLiteMode;
import org.robolectric.config.AndroidConfigurer;
import org.robolectric.interceptors.AndroidInterceptors;
import org.robolectric.internal.AndroidSandbox;
import org.robolectric.internal.DefaultManifestFactory;
import org.robolectric.internal.ManifestFactory;
import org.robolectric.internal.ManifestIdentifier;
import org.robolectric.internal.SandboxManager;
import org.robolectric.internal.bytecode.ClassHandler;
import org.robolectric.internal.bytecode.ClassHandlerBuilder;
import org.robolectric.internal.bytecode.InstrumentationConfiguration;
import org.robolectric.internal.bytecode.Interceptors;
import org.robolectric.internal.bytecode.SandboxConfig;
import org.robolectric.internal.bytecode.ShadowInfo;
import org.robolectric.internal.bytecode.ShadowMap;
import org.robolectric.internal.bytecode.ShadowProviders;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.pluginapi.MethodHandleDecorator;
import org.robolectric.pluginapi.Sdk;
import org.robolectric.pluginapi.SdkPicker;
import org.robolectric.pluginapi.config.ConfigurationStrategy;
import org.robolectric.pluginapi.config.ConfigurationStrategy.Configuration;
import org.robolectric.util.inject.Injector;

/**
 * The parts of {@code RobolectricTestRunner} that do not depend on JUnit 4: resolving a test's
 * configuration, choosing and configuring its sandbox.
 *
 * <p>Shared by the Jupiter integration and the JUnit 4 compatibility mode of the {@code
 * robolectric} engine.
 */
@SuppressWarnings("deprecation") // SQLiteMode is deprecated but still supported.
public final class RobolectricRuntime {
  /**
   * Packages that must be loaded by the application class loader, not by sandboxes: the engine and
   * extension (their state is shared between the engine and the sandboxed tests), and the exception
   * types JUnit uses to classify results.
   */
  private static final List<String> PACKAGES_TO_NOT_ACQUIRE =
      Arrays.asList(
          "org.robolectric.junit.platform.",
          "org.robolectric.junit.jupiter.",
          "org.opentest4j.",
          "org.apiguardian.");

  private static final int MAX_DATA_DIR_NAME_LENGTH = 120;
  private static volatile RobolectricRuntime instance;

  static {
    // The same JVM-wide setup as RobolectricTestRunner, which the JUnit Jupiter path does not load.
    // Starts the Poller SunPKCS11-Darwin thread early, outside of any sandbox.
    new SecureRandom();
    // Fixes AWT-backed graphics shadows when using X11 forwarding.
    System.setProperty("java.awt.headless", "true");
    // Fixes a performance regression caused by RSA modulus validation in Bouncy Castle 1.71.
    System.setProperty("org.bouncycastle.rsa.max_mr_tests", "0");
  }

  private final ConfigurationStrategy configurationStrategy;
  private final SdkPicker sdkPicker;
  private final SandboxManager sandboxManager;
  private final AndroidConfigurer androidConfigurer;
  private final ShadowProviders shadowProviders;
  private final ClassHandlerBuilder classHandlerBuilder;
  private final List<MethodHandleDecorator> decorators;
  private final Interceptors interceptors;
  private final Properties buildSystemApiProperties;
  private final Map<ManifestIdentifier, AndroidManifest> manifests = new HashMap<>();
  private final Map<String, Configuration> configurations = new ConcurrentHashMap<>();

  private RobolectricRuntime() {
    Injector injector =
        new Injector.Builder().bind(Properties.class, System.getProperties()).build();
    configurationStrategy = injector.getInstance(ConfigurationStrategy.class);
    sdkPicker = injector.getInstance(SdkPicker.class);
    sandboxManager = injector.getInstance(SandboxManager.class);
    androidConfigurer = injector.getInstance(AndroidConfigurer.class);
    shadowProviders = injector.getInstance(ShadowProviders.class);
    classHandlerBuilder = injector.getInstance(ClassHandlerBuilder.class);
    decorators = Arrays.asList(injector.getInstance(MethodHandleDecorator[].class));
    interceptors = new Interceptors(AndroidInterceptors.all());
    buildSystemApiProperties = loadBuildSystemApiProperties();
  }

  /** Returns the JVM-wide instance; sandboxes are shared by every test run in the JVM. */
  public static RobolectricRuntime get() {
    RobolectricRuntime result = instance;
    if (result == null) {
      synchronized (RobolectricRuntime.class) {
        result = instance;
        if (result == null) {
          instance = result = new RobolectricRuntime();
        }
      }
    }
    return result;
  }

  /** Returns the configuration of {@code method} when run as part of {@code testClass}. */
  public Configuration getConfiguration(Class<?> testClass, Method method) {
    String key = testClass.getName() + "#" + method;
    return configurations.computeIfAbsent(
        key, unused -> configurationStrategy.getConfig(testClass, method));
  }

  public AndroidManifest getAppManifest(Configuration configuration) {
    Config config = configuration.get(Config.class);
    ManifestFactory manifestFactory = new DefaultManifestFactory(buildSystemApiProperties);
    ManifestIdentifier identifier = manifestFactory.identify(config);
    synchronized (manifests) {
      return manifests.computeIfAbsent(identifier, RobolectricRuntime::createAndroidManifest);
    }
  }

  public List<Sdk> selectSdks(Configuration configuration, AndroidManifest manifest) {
    return sdkPicker.selectSdks(configuration, manifest);
  }

  /**
   * Returns the sandbox class loader configuration of a test, like {@code
   * RobolectricTestRunner#createClassLoaderConfig}.
   */
  public InstrumentationConfiguration createClassLoaderConfig(
      Configuration configuration, Class<?> testClass, Method method) {
    InstrumentationConfiguration.Builder builder = InstrumentationConfiguration.newBuilder();
    String customPackages = System.getProperty("org.robolectric.packagesToNotAcquire", "");
    for (String pkg : customPackages.split(",", -1)) {
      if (!pkg.isEmpty()) {
        builder.doNotAcquirePackage(pkg);
      }
    }
    String customClassesRegex =
        System.getProperty("org.robolectric.classesToNotInstrumentRegex", "");
    if (!customClassesRegex.isEmpty()) {
      builder.setDoNotInstrumentClassRegex(customClassesRegex);
    }
    for (Class<?> shadowClass : getExtraShadows(configuration)) {
      ShadowInfo shadowInfo = ShadowMap.obtainShadowInfo(shadowClass);
      builder.addInstrumentedClass(shadowInfo.shadowedClassName);
    }
    for (SandboxConfig sandboxConfig :
        Arrays.asList(
            testClass.getAnnotation(SandboxConfig.class),
            method.getAnnotation(SandboxConfig.class))) {
      if (sandboxConfig != null) {
        for (String pkgName : sandboxConfig.instrumentedPackages()) {
          builder.addInstrumentedPackage(pkgName);
        }
      }
    }
    androidConfigurer.configure(builder, interceptors);
    androidConfigurer.withConfig(builder, configuration.get(Config.class));
    return withEnginePackagesNotAcquired(builder.build());
  }

  /** Adds the packages that must not be loaded by sandboxes to {@code config}. */
  public static InstrumentationConfiguration withEnginePackagesNotAcquired(
      InstrumentationConfiguration config) {
    InstrumentationConfiguration.Builder builder = new InstrumentationConfiguration.Builder(config);
    for (String pkg : PACKAGES_TO_NOT_ACQUIRE) {
      builder.doNotAcquirePackage(pkg);
    }
    return builder.build();
  }

  public static Class<?>[] getExtraShadows(Configuration configuration) {
    return configuration.get(Config.class).shadows();
  }

  public SandboxSpec createSpec(
      Configuration configuration, Sdk sdk, InstrumentationConfiguration classLoaderConfig) {
    return new SandboxSpec(
        classLoaderConfig,
        sdk,
        configuration.get(ResourcesMode.Mode.class),
        configuration.get(LooperMode.Mode.class),
        configuration.get(SQLiteMode.Mode.class),
        configuration.get(GraphicsMode.Mode.class));
  }

  /** Returns the sandbox for {@code spec}, creating it if needed. */
  public AndroidSandbox getSandbox(SandboxSpec spec) {
    spec.sdk.verifySupportedSdk(spec.displayName());
    AndroidSandbox sandbox =
        sandboxManager.getAndroidSandbox(
            spec.classLoaderConfig,
            spec.sdk,
            spec.resourcesMode,
            spec.looperMode,
            spec.sqliteMode,
            spec.graphicsMode);
    SandboxRegistry.register(sandbox);
    return sandbox;
  }

  /**
   * Prepares {@code sandbox} to run a test with {@code configuration}: modes, shadows and the
   * {@code ConfigurationRegistry}. Like {@code RobolectricTestRunner#configureSandbox}, this has to
   * be done for every test, since tests sharing a sandbox can use different shadows.
   */
  public void configureSandbox(AndroidSandbox sandbox, Configuration configuration) {
    SQLiteMode.Mode sqliteMode = configuration.get(SQLiteMode.Mode.class);
    sandbox.updateModes(sqliteMode == null ? SQLiteMode.Mode.LEGACY : sqliteMode);
    sandbox.updateConfigurationRegistry(configuration);
    ShadowMap.Builder builder = shadowProviders.getBaseShadowMap().newBuilder();
    Class<?>[] extraShadows = getExtraShadows(configuration);
    if (extraShadows.length > 0) {
      builder.addShadowClasses(extraShadows);
    }
    ShadowMap shadowMap = builder.build();
    sandbox.replaceShadowMap(shadowMap);
    int apiLevel = sandbox.getSdk().getApiLevel();
    ClassHandler classHandler =
        classHandlerBuilder.build(
            shadowMap, new AndroidSdkShadowMatcher(apiLevel), interceptors, decorators);
    sandbox.configure(classHandler, interceptors);
  }

  /** Returns a file-system-safe directory name for {@code method}'s application data. */
  public static String tempDirName(Method method) {
    String directoryName =
        (method.getDeclaringClass().getSimpleName() + "_" + method.getName())
            .replaceAll("[^a-zA-Z0-9.-]", "_");
    if (directoryName.length() > MAX_DATA_DIR_NAME_LENGTH) {
      directoryName = directoryName.substring(0, MAX_DATA_DIR_NAME_LENGTH);
    }
    return directoryName;
  }

  /** Returns the counterpart of {@code method} in the classes loaded by {@code loader}. */
  public static Method findCounterpart(Method method, ClassLoader loader) {
    try {
      Class<?> declaringClass = Class.forName(method.getDeclaringClass().getName(), false, loader);
      List<Class<?>> parameterTypes = new ArrayList<>();
      for (Class<?> type : method.getParameterTypes()) {
        parameterTypes.add(
            type.isPrimitive() ? type : Class.forName(type.getName(), false, loader));
      }
      return declaringClass.getDeclaredMethod(
          method.getName(), parameterTypes.toArray(new Class<?>[0]));
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException("Cannot find " + method + " in " + loader, e);
    }
  }

  /** Same as {@code RobolectricTestRunner#createAndroidManifest}. */
  private static AndroidManifest createAndroidManifest(ManifestIdentifier identifier) {
    List<AndroidManifest> libraryManifests = new ArrayList<>();
    for (ManifestIdentifier library : identifier.getLibraries()) {
      libraryManifests.add(createAndroidManifest(library));
    }
    return new AndroidManifest(
        identifier.getManifestFile(),
        identifier.getResDir(),
        identifier.getAssetDir(),
        libraryManifests,
        identifier.getPackageName(),
        identifier.getApkFile());
  }

  private static Properties loadBuildSystemApiProperties() {
    Properties properties = new Properties();
    try (InputStream in =
        RobolectricRuntime.class.getResourceAsStream("/com/android/tools/test_config.properties")) {
      if (in != null) {
        properties.load(in);
      }
    } catch (IOException e) {
      // Same as RobolectricTestRunner: fall back to the default manifest configuration.
    }
    return properties;
  }
}
