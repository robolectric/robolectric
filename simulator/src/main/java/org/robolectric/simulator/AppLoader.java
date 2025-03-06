package org.robolectric.simulator;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.ResolveInfo;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.ConscryptMode;
import org.robolectric.annotation.GraphicsMode;
import org.robolectric.annotation.LooperMode;
import org.robolectric.annotation.SQLiteMode;
import org.robolectric.annotation.experimental.LazyApplication;
import org.robolectric.internal.AndroidSandbox;
import org.robolectric.manifest.AndroidManifest;
import org.robolectric.pluginapi.config.ConfigurationStrategy;

/** Loads an apk into the simulator */
public class AppLoader implements Runnable {

  private final AndroidSandbox sandbox;

  private final Path apkPath;

  public AppLoader(AndroidSandbox sandbox, Path apkPath) {
    this.sandbox = sandbox;
    this.apkPath = apkPath;
  }

  @Override
  public void run() {
    Thread.currentThread().setContextClassLoader(sandbox.getRobolectricClassLoader());

    AndroidManifest manifest = new AndroidManifest(null, null, null, null, "", this.apkPath);

    this.sandbox
        .getTestEnvironment()
        .setUpApplicationState("simulator", new FixedConfiguration(), manifest);

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

    Preconditions.checkNotNull(activityInfo);
    Preconditions.checkNotNull(activityInfo.name);
    // Start the main Activity
    try {
      Class<? extends Activity> activityClass =
          Class.forName(activityInfo.name).asSubclass(Activity.class);
      System.err.println("Launching " + activityClass.getName());
      Robolectric.setupActivity(activityClass);
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
    new Simulator().start();
  }

  /**
   * A {@link ConfigurationStrategy.Configuration} that provides a fixed set of values for the
   * configuration.
   */
  public static class FixedConfiguration implements ConfigurationStrategy.Configuration {

    private static final ImmutableMap<Class<?>, Object> MODES =
        ImmutableMap.of(
            Config.class, new Config.Builder().build(),
            ConscryptMode.Mode.class, ConscryptMode.Mode.OFF,
            LooperMode.Mode.class, LooperMode.Mode.PAUSED,
            LazyApplication.LazyLoad.class, LazyApplication.LazyLoad.OFF,
            GraphicsMode.Mode.class, GraphicsMode.Mode.NATIVE,
            SQLiteMode.Mode.class, SQLiteMode.Mode.NATIVE);

    @Override
    public <T> T get(Class<T> aClass) {
      return aClass.cast(MODES.get(aClass));
    }

    @Override
    public Collection<Class<?>> keySet() {
      return MODES.keySet();
    }

    @Override
    public Map<Class<?>, Object> map() {
      return MODES;
    }
  }
}
