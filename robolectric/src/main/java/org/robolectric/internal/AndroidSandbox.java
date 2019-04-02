package org.robolectric.internal;

import com.google.common.annotations.VisibleForTesting;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.concurrent.ThreadFactory;
import javax.inject.Inject;
import javax.inject.Named;
import org.robolectric.ApkLoader;
import org.robolectric.android.internal.AndroidEnvironment;
import org.robolectric.internal.bytecode.ClassHandler;
import org.robolectric.internal.bytecode.ClassInstrumentor;
import org.robolectric.internal.bytecode.InstrumentationConfiguration;
import org.robolectric.internal.bytecode.Interceptors;
import org.robolectric.internal.bytecode.Sandbox;
import org.robolectric.internal.bytecode.SandboxClassLoader;
import org.robolectric.internal.bytecode.ShadowProviders;
import org.robolectric.internal.bytecode.UrlResourceProvider;
import org.robolectric.pluginapi.Sdk;
import org.robolectric.util.inject.Injector;

/** Sandbox simulating an Android device. */
@SuppressWarnings("NewApi")
public class AndroidSandbox extends Sandbox {
  private final Sdk sdk;
  private final Environment environment;

  private String nativeLibraryName;

  @Inject
  public AndroidSandbox(
      @Named("runtimeSdk") Sdk runtimeSdk,
      @Named("compileSdk") Sdk compileSdk,
      ResourcesMode resourcesMode,
      ApkLoader apkLoader,
      EnvironmentSpec environmentSpec,
      SdkSandboxClassLoader sdkSandboxClassLoader,
      ShadowProviders shadowProviders) {
    super(sdkSandboxClassLoader);

    ClassLoader robolectricClassLoader = getRobolectricClassLoader();

    Injector sandboxScope =
        new Injector.Builder(robolectricClassLoader)
            .bind(ApkLoader.class, apkLoader) // shared singleton
            .bind(Environment.class, bootstrappedClass(environmentSpec.getEnvironmentClass()))
            .bind(new Injector.Key<>(Sdk.class, "runtimeSdk"), runtimeSdk)
            .bind(new Injector.Key<>(Sdk.class, "compileSdk"), compileSdk)
            .bind(ResourcesMode.class, resourcesMode)
            .bind(ShadowProvider[].class, shadowProviders.inClassLoader(robolectricClassLoader))
            .build();

    sdk = runtimeSdk;

    this.environment = runOnMainThread(() -> sandboxScope.getInstance(Environment.class));
  }

  @Override
  public void configure(ClassHandler classHandler, Interceptors interceptors) {
    super.configure(classHandler, interceptors);
    loadNativeLibrary();
  }

  //TODO(rickychow): This needs to be generated based on sdk version and platform
  public void overrideNativeLibraryName(String nativeLibraryName) {
    this.nativeLibraryName = nativeLibraryName;
  }

  private void loadNativeLibrary() {
    if (nativeLibraryName == null) {
      return;
    }

    //JNI_OnLoad uses the classloader on the method which called System.LoadLibrary
    //We need the sandbox class loader to be used on JNI_OnLoad for registration purposes
    try {
      bootstrappedClass(NativeLibraryLoader.class)
          .getConstructor(new Class<?>[] {String.class}).newInstance(nativeLibraryName);
    } catch (Throwable e) {
      throw new RuntimeException("Error loading native library", e);
    }
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

  public Environment getEnvironment() {
    return environment;
  }

  @Override
  public String toString() {
    return "AndroidSandbox[SDK " + sdk + "]";
  }

  /** Provides a mechanism for tests to inject a different AndroidEnvironment. For test use only. */
  @VisibleForTesting
  public static class EnvironmentSpec {

    private final Class<? extends AndroidEnvironment> environmentClass;

    @Inject
    public EnvironmentSpec() {
      environmentClass = AndroidEnvironment.class;
    }

    public EnvironmentSpec(Class<? extends AndroidEnvironment> environmentClass) {
      this.environmentClass = environmentClass;
    }

    public Class<? extends AndroidEnvironment> getEnvironmentClass() {
      return environmentClass;
    }
  }

  /** Adapter from Sdk to ResourceLoader. */
  public static class SdkSandboxClassLoader extends SandboxClassLoader {

    public SdkSandboxClassLoader(InstrumentationConfiguration config,
        @Named("runtimeSdk") Sdk runtimeSdk, ClassInstrumentor classInstrumentor) {
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
