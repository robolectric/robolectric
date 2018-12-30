package org.robolectric.internal;

import com.google.auto.service.AutoService;
import com.google.common.collect.Lists;
import java.lang.reflect.Method;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import javax.annotation.Priority;
import javax.inject.Inject;
import org.robolectric.ApkLoader;
import org.robolectric.android.internal.AndroidBridge.BridgeFactory;
import org.robolectric.android.internal.AndroidBridge.TheFactory;
import org.robolectric.annotation.Config;
import org.robolectric.internal.bytecode.Sandbox;
import org.robolectric.manifest.AndroidManifest;

@AutoService(AndroidSandbox.class)
@Priority(Integer.MIN_VALUE)
@SuppressWarnings("NewApi")
public class AndroidSandbox extends Sandbox {

  private final SandboxConfig sandboxConfig;

  private final ExecutorService executorService;
  private final Bridge bridge;
  private final List<ShadowProvider> shadowProviders;

  @Inject
  public AndroidSandbox(SandboxConfig sandboxConfig, boolean useLegacyResources,
      ClassLoader robolectricClassLoader,
      ApkLoader apkLoader) {
    super(robolectricClassLoader);

    this.sandboxConfig = sandboxConfig;

    executorService = Executors.newSingleThreadExecutor(r -> {
      Thread thread = new Thread(r,
          "main thread for AndroidSandbox(sdk=" + sandboxConfig + "; " +
              "resources=" + (useLegacyResources ? "legacy" : "binary") + ")");
      thread.setContextClassLoader(robolectricClassLoader);
      return thread;
    });

    BridgeFactory bridgeFactory = getBridgeFactory();
    bridge = bridgeFactory.build(sandboxConfig, apkLoader);

    this.shadowProviders =
        Lists.newArrayList(ServiceLoader.load(ShadowProvider.class, robolectricClassLoader));
  }

  protected BridgeFactory getBridgeFactory() {
    try {
      return bootstrappedClass(TheFactory.class)
          .asSubclass(BridgeFactory.class)
          .getConstructor()
          .newInstance();
    } catch (ReflectiveOperationException e) {
      throw new RuntimeException(e);
    }
  }

  public SdkConfig getSdkConfig() {
    return sandboxConfig.sdkConfig;
  }

  protected Bridge getBridge() {
    return bridge;
  }

  public void executeSynchronously(Runnable runnable) {
    Future<?> future = executorService.submit(runnable);
    try {
      future.get();
    } catch (InterruptedException | ExecutionException e) {
      throw new RuntimeException(e);
    }
  }

  public <V> V executeSynchronously(Callable<V> callable) throws Exception {
    Future<V> future = executorService.submit(callable);
    return future.get();
  }

  public void initialize(MethodConfig methodConfig) {
    executeSynchronously(() ->
        bridge.setUpApplicationState(
            methodConfig.getMethod(),
            methodConfig.getConfig(),
            methodConfig.getAppManifest(),
            this)
    );
  }

  public void tearDown() {
    executeSynchronously(bridge::tearDownApplication);
  }

  public void reset() {
    executeSynchronously(() -> {
      for (ShadowProvider shadowProvider : shadowProviders) {
        shadowProvider.reset();
      }
    });
  }

  public static class SandboxConfig {

    public final SdkConfig sdkConfig;
    public final boolean isLegacyResourcesMode;
    public final ClassLoader classLoader;

    public SandboxConfig(SdkConfig sdkConfig, boolean isLegacyResourcesMode,
        ClassLoader classLoader) {
      this.sdkConfig = sdkConfig;
      this.isLegacyResourcesMode = isLegacyResourcesMode;
      this.classLoader = classLoader;
    }
  }

  public interface MethodConfig {

    Method getMethod();

    Config getConfig();

    AndroidManifest getAppManifest();
  }

}
