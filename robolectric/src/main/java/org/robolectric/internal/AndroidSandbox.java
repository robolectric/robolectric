package org.robolectric.internal;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ImmutableSet;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ThreadFactory;
import javax.inject.Inject;
import javax.inject.Named;
import org.robolectric.ApkLoader;
import org.robolectric.android.internal.AndroidTestEnvironment;
import org.robolectric.annotation.SQLiteMode;
import org.robolectric.internal.bytecode.ClassInstrumentor;
import org.robolectric.internal.bytecode.InstrumentationConfiguration;
import org.robolectric.internal.bytecode.Sandbox;
import org.robolectric.internal.bytecode.SandboxClassLoader;
import org.robolectric.internal.bytecode.ShadowProviders;
import org.robolectric.internal.bytecode.UrlResourceProvider;
import org.robolectric.pluginapi.Sdk;
import org.robolectric.shadows.SQLiteShadowPicker;
import org.robolectric.util.inject.Injector;

/** Sandbox simulating an Android device. */
@SuppressWarnings("NewApi")
public class AndroidSandbox extends Sandbox {
  private final Sdk sdk;
  private final TestEnvironment testEnvironment;
  private final Set<String> modeInvalidatedClasses = new HashSet<>();
  private SQLiteMode.Mode activeSQLiteMode;

  @Inject
  public AndroidSandbox(
      @Named("runtimeSdk") Sdk runtimeSdk,
      @Named("compileSdk") Sdk compileSdk,
      ResourcesMode resourcesMode,
      ApkLoader apkLoader,
      TestEnvironmentSpec testEnvironmentSpec,
      SdkSandboxClassLoader sdkSandboxClassLoader,
      ShadowProviders shadowProviders,
      SQLiteMode.Mode sqLiteMode) {
    super(sdkSandboxClassLoader);

    ClassLoader robolectricClassLoader = getRobolectricClassLoader();

    Injector sandboxScope =
        new Injector.Builder(robolectricClassLoader)
            .bind(ApkLoader.class, apkLoader) // shared singleton
            .bind(TestEnvironment.class, bootstrappedClass(testEnvironmentSpec.getClazz()))
            .bind(new Injector.Key<>(Sdk.class, "runtimeSdk"), runtimeSdk)
            .bind(new Injector.Key<>(Sdk.class, "compileSdk"), compileSdk)
            .bind(ResourcesMode.class, resourcesMode)
            .bind(ShadowProvider[].class, shadowProviders.inClassLoader(robolectricClassLoader))
            .build();

    sdk = runtimeSdk;
    activeSQLiteMode = sqLiteMode;
    this.testEnvironment = runOnMainThread(() -> sandboxScope.getInstance(TestEnvironment.class));
  }

  @Override
  protected ThreadFactory mainThreadFactory() {
    return r -> {
      String name = "SDK " + sdk.getApiLevel();
      return new Thread(new ThreadGroup(name), r, name + " Main Thread");
    };
  }

  @Override
  protected Set<String> getModeInvalidatedClasses() {
    return ImmutableSet.copyOf(modeInvalidatedClasses);
  }

  @Override
  protected void clearModeInvalidatedClasses() {
    modeInvalidatedClasses.clear();
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

  public void updateModes(SQLiteMode.Mode sqliteMode) {
    if (activeSQLiteMode != sqliteMode) {
      this.activeSQLiteMode = sqliteMode;
      modeInvalidatedClasses.addAll(SQLiteShadowPicker.getAffectedClasses());
    }
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
