package android.server.wm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assume.assumeTrue;

import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.view.Display;
import androidx.test.core.app.ActivityScenario;
import androidx.test.filters.SdkSuppress;
import org.junit.Test;
import org.robolectric.annotation.Config;
import org.robolectric.testapp.TestActivity;

/** Tests activity window changes through Android APIs. */
@Config(minSdk = Build.VERSION_CODES.P, qualifiers = "w411dp-h891dp-port")
@SdkSuppress(minSdkVersion = Build.VERSION_CODES.P)
public class ActivityWindowTests extends MultiDisplayTestBase {
  @Test
  public void requestedOrientation_recreatesAndShowsActivity() {
    PackageManager packageManager = mContext.getPackageManager();
    assumeTrue(
        "robolectric".equalsIgnoreCase(Build.FINGERPRINT)
            || (packageManager.hasSystemFeature(PackageManager.FEATURE_SCREEN_LANDSCAPE)
                && packageManager.hasSystemFeature(PackageManager.FEATURE_SCREEN_PORTRAIT)));

    ActivityScenario<TestActivity> scenario =
        launchActivityOnDisplay(TestActivity.class, Display.DEFAULT_DISPLAY);
    waitAndAssertResumedActivityOnDisplay(
        scenario, Display.DEFAULT_DISPLAY, "Activity must be resumed");
    TestActivity initialActivity = getActivity(scenario);
    assumeTrue(initialActivity.getResources().getConfiguration().smallestScreenWidthDp < 600);
    int initialOrientation = initialActivity.getResources().getConfiguration().orientation;
    int requestedOrientation =
        initialOrientation == Configuration.ORIENTATION_PORTRAIT
            ? ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
            : ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
    int targetOrientation =
        initialOrientation == Configuration.ORIENTATION_PORTRAIT
            ? Configuration.ORIENTATION_LANDSCAPE
            : Configuration.ORIENTATION_PORTRAIT;

    scenario.onActivity(activity -> activity.setRequestedOrientation(requestedOrientation));

    waitForOrFail(
        "Activity must be recreated and shown in the requested orientation",
        () ->
            getActivity(scenario) != initialActivity
                && getActivity(scenario).getResources().getConfiguration().orientation
                    == targetOrientation
                && getActivity(scenario).getWindow().getDecorView().isShown());
    assertNotSame(initialActivity, getActivity(scenario));
    assertEquals(requestedOrientation, getActivity(scenario).getRequestedOrientation());
  }
}
