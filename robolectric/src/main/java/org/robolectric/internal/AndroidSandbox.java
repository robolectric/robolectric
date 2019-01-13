package org.robolectric.internal;

import com.google.common.collect.Lists;
import java.lang.reflect.Method;
import java.util.List;
import java.util.ServiceLoader;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.robolectric.ApkLoader;
import org.robolectric.android.internal.AndroidBridge.BridgeFactory;
import org.robolectric.android.internal.AndroidBridge.TheFactory;
import org.robolectric.annotation.Config;
import org.robolectric.internal.bytecode.InstrumentationConfiguration;
import org.robolectric.internal.bytecode.Sandbox;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.sandbox.UrlResourceProvider;

/**
 * A Robolectric {@link Sandbox} configured for use as a simulation of a running Android device.
 */
@SuppressWarnings("NewApi")
public class AndroidSandbox extends Sandbox {

  private final SdkConfig sdkConfig;

  private final ExecutorService executorService;
  private final Bridge bridge;
  private final List<ShadowProvider> shadowProviders;

  protected AndroidSandbox(InstrumentationConfiguration instrumentationConfiguration,
      UrlResourceProvider resourceProvider, SdkConfig sdkConfig, boolean useLegacyResources,
      ApkLoader apkLoader) {
    super(instrumentationConfiguration, resourceProvider);

    this.sdkConfig = sdkConfig;

    executorService = Executors.newSingleThreadExecutor(r -> {
      Thread thread = new Thread(r,
          "main thread for AndroidSandbox(sdk=" + sdkConfig + "; " +
              "resources=" + (useLegacyResources ? "legacy" : "binary") + ")");
      thread.setContextClassLoader(getClassLoader());
      return thread;
    });

    BridgeFactory bridgeFactory = getBridgeFactory();
    bridge = bridgeFactory.build(sdkConfig, useLegacyResources, apkLoader);

    this.shadowProviders =
        Lists.newArrayList(ServiceLoader.load(ShadowProvider.class, getClassLoader()));
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
    return sdkConfig;
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

  public interface MethodConfig {

    Method getMethod();

    Config getConfig();

    AndroidManifest getAppManifest();
  }

}
