package android.server.wm;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;
import static org.junit.Assume.assumeTrue;

import android.app.Activity;
import android.app.PictureInPictureParams;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Build;
import android.util.Rational;
import android.view.Display;
import androidx.test.core.app.ActivityScenario;
import androidx.test.filters.SdkSuppress;
import java.util.concurrent.atomic.AtomicBoolean;
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

  @Test
  public void pictureInPicture_movesActivityIntoSmallerWindow() {
    assumeTrue(
        "robolectric".equalsIgnoreCase(Build.FINGERPRINT)
            || mContext
                .getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE));
    ActivityScenario<PictureInPictureActivity> scenario =
        launchActivityOnDisplay(PictureInPictureActivity.class, Display.DEFAULT_DISPLAY);
    waitAndAssertResumedActivityOnDisplay(
        scenario, Display.DEFAULT_DISPLAY, "Activity must be resumed");
    PictureInPictureActivity activity = getActivity(scenario);
    waitForOrFail(
        "Activity window must be laid out",
        () ->
            activity.getWindow().getDecorView().getWidth() > 0
                && activity.getWindow().getDecorView().getHeight() > 0);
    int initialWidth = activity.getWindow().getDecorView().getWidth();
    int initialHeight = activity.getWindow().getDecorView().getHeight();
    AtomicBoolean accepted = new AtomicBoolean();

    scenario.onActivity(current ->
        accepted.set(
            current.enterPictureInPictureMode(
                new PictureInPictureParams.Builder().setAspectRatio(new Rational(1, 1)).build())));

    assertTrue(accepted.get());
    waitForOrFail(
        "Activity must be notified that it entered picture-in-picture",
        () ->
            activity.enteredPictureInPicture
                && activity.isInPictureInPictureMode()
                && activity.isInMultiWindowMode());
    waitForOrFail(
        "Picture-in-picture window must be smaller",
        () ->
            activity.getWindow().getDecorView().getWidth() < initialWidth
                && activity.getWindow().getDecorView().getHeight() < initialHeight);
  }

  public static class PictureInPictureActivity extends Activity {
    volatile boolean enteredPictureInPicture;

    @Override
    public void onPictureInPictureModeChanged(boolean inPictureInPicture, Configuration config) {
      super.onPictureInPictureModeChanged(inPictureInPicture, config);
      enteredPictureInPicture = inPictureInPicture;
    }
  }
}
