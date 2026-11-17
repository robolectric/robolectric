package org.robolectric.internal;

import com.google.common.annotations.VisibleForTesting;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.Map;
import java.util.concurrent.ThreadFactory;
import javax.inject.Inject;
import javax.inject.Named;
import org.robolectric.android.internal.AndroidTestEnvironment;
import org.robolectric.annotation.ResourcesMode;
import org.robolectric.config.ConfigurationRegistry;
import org.robolectric.internal.bytecode.ClassInstrumentor;
import org.robolectric.internal.bytecode.InstrumentationConfiguration;
import org.robolectric.internal.bytecode.Sandbox;
import org.robolectric.internal.bytecode.SandboxClassLoader;
import org.robolectric.internal.bytecode.ShadowProviders;
import org.robolectric.internal.bytecode.UrlResourceProvider;
import org.robolectric.pluginapi.Sdk;
import org.robolectric.pluginapi.config.ConfigurationStrategy.Configuration;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;
import org.robolectric.util.inject.Injector;

/** Sandbox simulating an Android device. */
@SuppressWarnings("NewApi")
public class AndroidSandbox extends Sandbox {
  private final Sdk sdk;
  private final TestEnvironment testEnvironment;

  @Inject
  public AndroidSandbox(
      @Named("runtimeSdk") Sdk runtimeSdk,
      @Named("compileSdk") Sdk compileSdk,
      ResourcesMode.Mode resourcesMode,
      TestEnvironmentSpec testEnvironmentSpec,
      SdkSandboxClassLoader sdkSandboxClassLoader,
      ShadowProviders shadowProviders) {
    super(sdkSandboxClassLoader);

    ClassLoader robolectricClassLoader = getRobolectricClassLoader();

    Injector sandboxScope =
        new Injector.Builder(robolectricClassLoader)
            .bind(TestEnvironment.class, bootstrappedClass(testEnvironmentSpec.getClazz()))
            .bind(new Injector.Key<>(Sdk.class, "runtimeSdk"), runtimeSdk)
            .bind(new Injector.Key<>(Sdk.class, "compileSdk"), compileSdk)
            .bind(ResourcesMode.Mode.class, resourcesMode)
            .bind(ShadowProvider[].class, shadowProviders.inClassLoader(robolectricClassLoader))
            .build();

    sdk = runtimeSdk;
    this.testEnvironment = runOnMainThread(() -> sandboxScope.getInstance(TestEnvironment.class));
  }

  @Override
  protected ThreadFactory mainThreadFactory() {
    return r -> {
      String name = "SDK " + sdk.getApiLevel();
      return new Thread(new ThreadGroup(name), r, name + " Main Thread");
    };
  }

  public Sdk getSdk() {
    return sdk;
  }

  public TestEnvironment getTestEnvironment() {
    return testEnvironment;
  }

  @Override
  public String toString() {
    return "AndroidSandbox[SDK " + sdk + "]";
  }

  /**
   * Makes {@code configuration} available to code running in this sandbox through {@link
   * ConfigurationRegistry}.
   *
   * <p>{@link TestEnvironment#setUpApplicationState} populates the registry for each test, but code
   * can run in the sandbox before that, e.g. {@code @BeforeClass} methods and class rules. Loading
   * a class whose shadow is chosen by a {@code ShadowPicker} that reads the registry (such as the
   * graphics shadows, which depend on {@link org.robolectric.annotation.GraphicsMode}) would
   * otherwise fail, and leave the class unusable for the lifetime of the sandbox.
   */
  public void updateConfigurationRegistry(Configuration configuration) {
    Class<?> registryClass = bootstrappedClass(ConfigurationRegistry.class);
    Object registry =
        ReflectionHelpers.callConstructor(
            registryClass, ClassParameter.from(Map.class, configuration.map()));
    ReflectionHelpers.setStaticField(registryClass, "instance", registry);
  }

  /**
   * Provides a mechanism for tests to inject a different AndroidTestEnvironment. For test use only.
   */
  @VisibleForTesting
  public static class TestEnvironmentSpec {

    private final Class<? extends AndroidTestEnvironment> clazz;

    @Inject
    public TestEnvironmentSpec() {
      clazz = AndroidTestEnvironment.class;
    }

    public TestEnvironmentSpec(Class<? extends AndroidTestEnvironment> clazz) {
      this.clazz = clazz;
    }

    public Class<? extends AndroidTestEnvironment> getClazz() {
      return clazz;
    }
  }

  /** Adapter from Sdk to ResourceLoader. */
  public static class SdkSandboxClassLoader extends SandboxClassLoader {

    public SdkSandboxClassLoader(
        InstrumentationConfiguration config,
        @Named("runtimeSdk") Sdk runtimeSdk,
        ClassInstrumentor classInstrumentor) {
      super(config, new UrlResourceProvider(toUrl(runtimeSdk.getJarPath())), classInstrumentor);
    }

    private static URL toUrl(Path path) {
      try {
        return path.toUri().toURL();
      } catch (MalformedURLException e) {
        throw new RuntimeException(e);
      }
    }
  }
}
