package org.robolectric.integrationtests.androidx;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.app.Application;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Rect;
import android.os.Bundle;
import android.view.View;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.window.core.layout.WindowSizeClass;
import androidx.window.core.layout.WindowSizeClassSelectors;
import androidx.window.layout.WindowMetricsCalculator;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowLooper;

/**
 * Shows how to test adaptive layouts: the window size an activity sees on phones, foldables,
 * tablets and desktops, and how it reacts when that size changes.
 */
@RunWith(AndroidJUnit4.class)
public class AdaptiveLayoutTest {
  private final Application application = ApplicationProvider.getApplicationContext();

  @Test
  @Config(qualifiers = "w411dp-h891dp")
  public void phone_hasCompactWidth() {
    WindowSizeClass sizeClass = windowSizeClass(Robolectric.setupActivity(AdaptiveActivity.class));

    assertThat(sizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND))
        .isFalse();
  }

  @Test
  @Config(qualifiers = "w673dp-h841dp")
  public void unfoldedFoldable_hasMediumWidth() {
    WindowSizeClass sizeClass = windowSizeClass(Robolectric.setupActivity(AdaptiveActivity.class));

    assertThat(sizeClass.getMinWidthDp()).isEqualTo(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND);
  }

  @Test
  @Config(qualifiers = "w1280dp-h800dp-land")
  public void tablet_hasLargeWidth() {
    WindowSizeClass sizeClass = windowSizeClass(Robolectric.setupActivity(AdaptiveActivity.class));

    assertThat(sizeClass.getMinWidthDp()).isEqualTo(WindowSizeClass.WIDTH_DP_LARGE_LOWER_BOUND);
  }

  @Test
  @Config(qualifiers = "w1920dp-h1080dp-land")
  public void desktop_hasExtraLargeWidth() {
    WindowSizeClass sizeClass = windowSizeClass(Robolectric.setupActivity(AdaptiveActivity.class));

    assertThat(sizeClass.getMinWidthDp())
        .isEqualTo(WindowSizeClass.WIDTH_DP_EXTRA_LARGE_LOWER_BOUND);
  }

  @Test
  @Config(qualifiers = "w1280dp-h800dp-land")
  public void resize_whenActivityHandlesIt_keepsTheActivityAndNotifiesIt() {
    handleSizeChanges(AdaptiveActivity.class);
    ActivityController<AdaptiveActivity> controller =
        Robolectric.buildActivity(AdaptiveActivity.class).setup();
    AdaptiveActivity activity = controller.get();

    RuntimeEnvironment.setQualifiers("w500dp-h800dp-port");
    controller.configurationChange();

    assertThat(controller.get()).isSameInstanceAs(activity);
    assertThat(activity.lastConfiguration.screenWidthDp).isEqualTo(500);
    assertThat(windowSizeClass(activity).getMinWidthDp()).isEqualTo(0);
  }

  @Test
  @Config(qualifiers = "w1280dp-h800dp-land")
  public void resize_whenActivityDoesNotHandleIt_recreatesTheActivity() {
    ActivityController<AdaptiveActivity> controller =
        Robolectric.buildActivity(AdaptiveActivity.class).setup();
    AdaptiveActivity activity = controller.get();

    RuntimeEnvironment.setQualifiers("w500dp-h800dp-port");
    controller.configurationChange();

    assertThat(controller.get()).isNotSameInstanceAs(activity);
    assertThat(windowSizeClass(controller.get()).getMinWidthDp()).isEqualTo(0);
  }

  @Test
  @Config(qualifiers = "w1280dp-h800dp-land-mdpi")
  public void resize_laysOutTheContentAgain() {
    handleSizeChanges(AdaptiveActivity.class);
    ActivityController<AdaptiveActivity> controller =
        Robolectric.buildActivity(AdaptiveActivity.class).setup();
    AdaptiveActivity activity = controller.get();
    assertThat(activity.content.getWidth()).isEqualTo(1280);

    RuntimeEnvironment.setQualifiers("w500dp-h800dp-port-mdpi");
    controller.configurationChange();
    ShadowLooper.idleMainLooper();

    assertThat(activity.content.getWidth()).isEqualTo(500);
  }

  @Test
  @Config(qualifiers = "w1280dp-h800dp-land-mdpi")
  public void windowMetrics_coverTheWholeDisplay() {
    AdaptiveActivity activity = Robolectric.setupActivity(AdaptiveActivity.class);
    WindowMetricsCalculator calculator = WindowMetricsCalculator.getOrCreate();

    assertThat(calculator.computeCurrentWindowMetrics(activity).getBounds())
        .isEqualTo(new Rect(0, 0, 1280, 800));
    assertThat(calculator.computeMaximumWindowMetrics(activity).getBounds())
        .isEqualTo(new Rect(0, 0, 1280, 800));
  }

  @Test
  public void desktopFeatures_canBeDeclared() {
    PackageManager packageManager = application.getPackageManager();
    assertThat(packageManager.hasSystemFeature(PackageManager.FEATURE_FREEFORM_WINDOW_MANAGEMENT))
        .isFalse();

    shadowOf(packageManager)
        .setSystemFeature(PackageManager.FEATURE_FREEFORM_WINDOW_MANAGEMENT, true);
    shadowOf(packageManager).setSystemFeature(PackageManager.FEATURE_PC, true);

    assertThat(packageManager.hasSystemFeature(PackageManager.FEATURE_FREEFORM_WINDOW_MANAGEMENT))
        .isTrue();
    assertThat(packageManager.hasSystemFeature(PackageManager.FEATURE_PC)).isTrue();
  }

  @Test
  public void topResumedActivity_isTheOneTheUserLastInteractedWith() {
    ActivityController<AdaptiveActivity> first =
        Robolectric.buildActivity(AdaptiveActivity.class).setup();
    ActivityController<AdaptiveActivity> second =
        Robolectric.buildActivity(AdaptiveActivity.class).setup();

    first.topActivityResumed(false);

    assertThat(first.get().isTopResumed).isFalse();
    assertThat(second.get().isTopResumed).isTrue();
  }

  private static WindowSizeClass windowSizeClass(Activity activity) {
    Rect bounds =
        WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(activity).getBounds();
    float density = activity.getResources().getDisplayMetrics().density;
    return WindowSizeClassSelectors.computeWindowSizeClass(
        WindowSizeClass.BREAKPOINTS_V2, bounds.width() / density, bounds.height() / density);
  }

  private void handleSizeChanges(Class<? extends Activity> activityClass) {
    ActivityInfo activityInfo = new ActivityInfo();
    activityInfo.name = activityClass.getName();
    activityInfo.packageName = application.getPackageName();
    activityInfo.configChanges =
        ActivityInfo.CONFIG_SCREEN_SIZE
            | ActivityInfo.CONFIG_SMALLEST_SCREEN_SIZE
            | ActivityInfo.CONFIG_SCREEN_LAYOUT
            | ActivityInfo.CONFIG_ORIENTATION;
    shadowOf(application.getPackageManager()).addOrUpdateActivity(activityInfo);
  }

  /** An activity that records what the system tells it about its window. */
  public static class AdaptiveActivity extends Activity {
    View content;
    Configuration lastConfiguration;
    boolean isTopResumed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      content = new View(this);
      setContentView(content);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
      super.onConfigurationChanged(newConfig);
      lastConfiguration = newConfig;
    }

    @Override
    public void onTopResumedActivityChanged(boolean isTopResumedActivity) {
      super.onTopResumedActivityChanged(isTopResumedActivity);
      isTopResumed = isTopResumedActivity;
    }
  }
}
