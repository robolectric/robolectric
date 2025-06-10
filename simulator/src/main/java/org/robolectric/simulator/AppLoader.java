package org.robolectric.simulator;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import com.google.common.base.Preconditions;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.ConscryptMode;
import org.robolectric.annotation.GraphicsMode;
import org.robolectric.annotation.LooperMode;
import org.robolectric.annotation.SQLiteMode;
import org.robolectric.annotation.experimental.LazyApplication;
import org.robolectric.internal.AndroidSandbox;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.pluginapi.config.Configurer;
import org.robolectric.util.inject.Injector;

/** Loads an apk into the simulator */
public class AppLoader implements Runnable {

  private final AndroidSandbox sandbox;

  private final Path apkPath;

  public AppLoader(AndroidSandbox sandbox, Path apkPath) {
    this.sandbox = sandbox;
    this.apkPath = apkPath;
  }

  @SuppressWarnings("unchecked")
  private Config getBestConfig() {
    Injector injector =
        new Injector.Builder().bind(Properties.class, System.getProperties()).build();
    Configurer<?>[] configurers = injector.getInstance(Configurer[].class);
    Configurer<Config> bestConfigConfigurer = null;
    for (Configurer<?> configurer : configurers) {
      if (configurer.getConfigClass().equals(Config.class)) {
        // The array is sorted based on priority and Supersedes annotations, so the first
        // configurer in the list is the best one.
        bestConfigConfigurer = (Configurer<Config>) configurer;
        break;
      }
    }
    return bestConfigConfigurer.defaultConfig();
  }

  @Override
  public void run() {
    Thread.currentThread().setContextClassLoader(sandbox.getRobolectricClassLoader());

    AndroidManifest manifest = new AndroidManifest(null, null, null, null, "", this.apkPath);

    FixedConfiguration configuration =
        FixedConfiguration.newBuilder()
            .put(ConscryptMode.Mode.class, ConscryptMode.Mode.OFF)
            .put(LooperMode.Mode.class, LooperMode.Mode.PAUSED)
            .put(LazyApplication.LazyLoad.class, LazyApplication.LazyLoad.OFF)
            .put(GraphicsMode.Mode.class, GraphicsMode.Mode.NATIVE)
            .put(SQLiteMode.Mode.class, SQLiteMode.Mode.NATIVE)
            .put(Config.class, getBestConfig())
            .build();

    this.sandbox.getTestEnvironment().setUpApplicationState("simulator", configuration, manifest);

    Application application = RuntimeEnvironment.getApplication();

    // Create an intent that will find the main launcher activity
    Intent intent = new Intent(Intent.ACTION_MAIN, null);
    intent.addCategory(Intent.CATEGORY_LAUNCHER);

    // Query the PackageManager for activities matching the intent
    List<ResolveInfo> resolveInfoList =
        application.getPackageManager().queryIntentActivities(intent, 0);

    Preconditions.checkArgument(!resolveInfoList.isEmpty());

    ResolveInfo resolveInfo = resolveInfoList.get(0);
    ActivityInfo activityInfo = resolveInfo.activityInfo;

    Objects.requireNonNull(activityInfo);
    Objects.requireNonNull(activityInfo.name);
    // Start the main Activity
    try {
      Class<? extends Activity> activityClass =
          Class.forName(activityInfo.name).asSubclass(Activity.class);
      new Simulator(activityClass).start();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }
}
