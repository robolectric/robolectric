package org.robolectric.shadows;

import static android.os.Looper.getMainLooper;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.app.ActivityOptions;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.os.Build.VERSION_CODES;
import android.os.OutcomeReceiver;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

/** Tests for {@link Activity#requestFullscreenMode} in {@link ShadowActivity}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = VERSION_CODES.UPSIDE_DOWN_CAKE, qualifiers = "w1280dp-h800dp-land-mdpi")
public class ShadowActivityFullscreenRequestTest {
  private static final Rect WINDOW_BOUNDS = new Rect(100, 100, 700, 500);

  @Test
  @Config(minSdk = VERSION_CODES.VANILLA_ICE_CREAM)
  public void enter_inAFreeformWindow_fillsTheDisplay() {
    ActivityController<Activity> controller = buildActivityInWindow().setup();

    Result result = requestFullscreenMode(controller, Activity.FULLSCREEN_MODE_REQUEST_ENTER);

    assertThat(result.approved).isTrue();
    assertThat(controller.get().isInMultiWindowMode()).isFalse();
    assertThat(controller.get().getResources().getConfiguration().screenWidthDp).isEqualTo(1280);
  }

  @Test
  @Config(minSdk = VERSION_CODES.VANILLA_ICE_CREAM)
  public void exit_afterEntering_restoresTheFreeformWindow() {
    ActivityController<Activity> controller = buildActivityInWindow().setup();
    requestFullscreenMode(controller, Activity.FULLSCREEN_MODE_REQUEST_ENTER);

    Result result = requestFullscreenMode(controller, Activity.FULLSCREEN_MODE_REQUEST_EXIT);

    assertThat(result.approved).isTrue();
    assertThat(windowBounds(controller.get())).isEqualTo(WINDOW_BOUNDS);
    assertThat(controller.get().isInMultiWindowMode()).isTrue();
  }

  @Test
  @Config(minSdk = VERSION_CODES.VANILLA_ICE_CREAM)
  public void exit_afterEnteringAndRotating_restoresTheFreeformWindow() {
    ActivityController<Activity> controller = buildActivityInWindow().setup();
    requestFullscreenMode(controller, Activity.FULLSCREEN_MODE_REQUEST_ENTER);
    RuntimeEnvironment.setQualifiers("+port");
    controller.configurationChange();

    Result result = requestFullscreenMode(controller, Activity.FULLSCREEN_MODE_REQUEST_EXIT);

    assertThat(result.approved).isTrue();
    assertThat(windowBounds(controller.get())).isEqualTo(WINDOW_BOUNDS);
  }

  @Test
  public void exit_withoutEntering_fails() {
    declareDesktop();
    ActivityController<Activity> controller = Robolectric.buildActivity(Activity.class).setup();

    Result result = requestFullscreenMode(controller, Activity.FULLSCREEN_MODE_REQUEST_EXIT);

    assertThat(result.error).isInstanceOf(IllegalStateException.class);
    assertThat(result.error).hasMessageThat().contains("not in fullscreen");
  }

  @Test
  @Config(minSdk = VERSION_CODES.VANILLA_ICE_CREAM)
  public void exit_afterTheUserMovedTheWindow_fails() {
    ActivityController<Activity> controller = buildActivityInWindow().setup();
    requestFullscreenMode(controller, Activity.FULLSCREEN_MODE_REQUEST_ENTER);
    shadowOf(controller.get()).setWindowBounds(new Rect(0, 0, 400, 400));
    shadowOf(controller.get()).setWindowBounds(null);

    Result result = requestFullscreenMode(controller, Activity.FULLSCREEN_MODE_REQUEST_EXIT);

    assertThat(result.error).hasMessageThat().contains("not in fullscreen");
    assertThat(controller.get().isInMultiWindowMode()).isFalse();
  }

  @Test
  @Config(minSdk = VERSION_CODES.VANILLA_ICE_CREAM)
  public void enter_whenNotTheTopResumedActivity_fails() {
    ActivityController<Activity> controller = buildActivityInWindow().setup();
    controller.topActivityResumed(false);

    Result result = requestFullscreenMode(controller, Activity.FULLSCREEN_MODE_REQUEST_ENTER);

    assertThat(result.error).hasMessageThat().contains("not the top focused window");
    assertThat(controller.get().isInMultiWindowMode()).isTrue();
  }

  @Test
  @Config(minSdk = VERSION_CODES.BAKLAVA)
  public void enter_whenFillingTheDisplay_fails() {
    ActivityController<Activity> controller = Robolectric.buildActivity(Activity.class).setup();

    Result result = requestFullscreenMode(controller, Activity.FULLSCREEN_MODE_REQUEST_ENTER);

    assertThat(result.error).hasMessageThat().contains("already fully expanded");
  }

  @Test
  @Config(sdk = VERSION_CODES.UPSIDE_DOWN_CAKE)
  public void enter_onU_whenActivitiesDoNotLaunchInFreeformWindows_fails() {
    ActivityController<Activity> controller = buildActivityInWindow().setup();

    Result result = requestFullscreenMode(controller, Activity.FULLSCREEN_MODE_REQUEST_ENTER);

    assertThat(result.error).hasMessageThat().contains("not launched in freeform by default");
    assertThat(controller.get().isInMultiWindowMode()).isTrue();
  }

  @Test
  @Config(sdk = VERSION_CODES.UPSIDE_DOWN_CAKE)
  public void enter_onU_onADesktop_fillsTheDisplay() {
    declareDesktop();
    ActivityController<Activity> controller = buildActivityInWindow().setup();

    Result result = requestFullscreenMode(controller, Activity.FULLSCREEN_MODE_REQUEST_ENTER);

    assertThat(result.approved).isTrue();
    assertThat(controller.get().isInMultiWindowMode()).isFalse();
  }

  private static ActivityController<Activity> buildActivityInWindow() {
    return Robolectric.buildActivity(
        Activity.class,
        null,
        ActivityOptions.makeBasic().setLaunchBounds(WINDOW_BOUNDS).toBundle());
  }

  /** Makes activities launch in freeform windows, as on a desktop. */
  private static void declareDesktop() {
    ShadowPackageManager packageManager =
        shadowOf(ApplicationProvider.getApplicationContext().getPackageManager());
    packageManager.setSystemFeature(PackageManager.FEATURE_FREEFORM_WINDOW_MANAGEMENT, true);
    packageManager.setSystemFeature(PackageManager.FEATURE_PC, true);
  }

  private static Result requestFullscreenMode(
      ActivityController<Activity> controller, int request) {
    Result result = new Result();
    controller.get().requestFullscreenMode(request, result);
    shadowOf(getMainLooper()).idle();
    return result;
  }

  private static Rect windowBounds(Activity activity) {
    return activity.getResources().getConfiguration().windowConfiguration.getBounds();
  }

  private static class Result implements OutcomeReceiver<Void, Throwable> {
    boolean approved;
    Throwable error;

    @Override
    public void onResult(Void result) {
      approved = true;
    }

    @Override
    public void onError(Throwable error) {
      this.error = error;
    }
  }
}
