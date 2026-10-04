package org.robolectric.simulator;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import com.google.common.base.Preconditions;
import java.util.List;
import java.util.Objects;
import org.robolectric.RuntimeEnvironment;

/**
 * Starts the simulator with the launcher activity of the app that is set up. It has to be loaded by
 * the sandbox.
 */
public class AppLoader implements Runnable {

  @Override
  public void run() {
    // Android always has a default handler, and apps' crash reporters wrap it. Install it before
    // the Application is created. A crash on another thread ends the simulator, like a crash on
    // the main thread does.
    SimulatorUncaughtExceptionHandler.installIfNone(() -> System.exit(1));

    Application application = RuntimeEnvironment.getApplication();

    // Create an intent that will find the main launcher activity
    Intent intent = new Intent(Intent.ACTION_MAIN, null);
    intent.addCategory(Intent.CATEGORY_LAUNCHER);

    // Query the PackageManager for activities matching the intent
    List<ResolveInfo> resolveInfoList =
        application.getPackageManager().queryIntentActivities(intent, 0);

    Preconditions.checkArgument(
        !resolveInfoList.isEmpty(), "Could not find a launcher Activity in provided manifest");

    ActivityInfo activityInfo = firstInManifestOrder(application, resolveInfoList);

    Objects.requireNonNull(activityInfo);
    Objects.requireNonNull(activityInfo.name);
    // An activity alias names the alias, not a class; launch the activity it targets.
    String activityClassName =
        activityInfo.targetActivity != null ? activityInfo.targetActivity : activityInfo.name;
    // Start the main Activity
    try {
      Class<? extends Activity> activityClass =
          Class.forName(activityClassName).asSubclass(Activity.class);
      new Simulator(activityClass).start();
    } catch (Exception e) {
      throw new RuntimeException(e);
    }
  }

  /**
   * Returns the launcher that the manifest declares first. Robolectric's PackageManager returns
   * activities sorted by name, but a merged manifest lists the app's own activities before the ones
   * its libraries add, such as LeakCanary's launcher, and Android launches the first one.
   */
  private static ActivityInfo firstInManifestOrder(
      Application application, List<ResolveInfo> launchers) {
    try {
      PackageInfo packageInfo =
          application
              .getPackageManager()
              .getPackageInfo(application.getPackageName(), PackageManager.GET_ACTIVITIES);
      if (packageInfo.activities != null) {
        for (ActivityInfo declared : packageInfo.activities) {
          for (ResolveInfo launcher : launchers) {
            if (launcher.activityInfo.name.equals(declared.name)) {
              return launcher.activityInfo;
            }
          }
        }
      }
    } catch (PackageManager.NameNotFoundException e) {
      // Fall back to the first launcher below.
    }
    return launchers.get(0).activityInfo;
  }
}
