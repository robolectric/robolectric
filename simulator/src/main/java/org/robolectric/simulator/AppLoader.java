package org.robolectric.simulator;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.pm.ActivityInfo;
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
    Application application = RuntimeEnvironment.getApplication();

    // Create an intent that will find the main launcher activity
    Intent intent = new Intent(Intent.ACTION_MAIN, null);
    intent.addCategory(Intent.CATEGORY_LAUNCHER);

    // Query the PackageManager for activities matching the intent
    List<ResolveInfo> resolveInfoList =
        application.getPackageManager().queryIntentActivities(intent, 0);

    Preconditions.checkArgument(
        !resolveInfoList.isEmpty(), "Could not find a launcher Activity in provided manifest");

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
