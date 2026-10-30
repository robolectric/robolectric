package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.UiAutomation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Build.VERSION_CODES;
import android.os.SystemClock;
import android.provider.Settings;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import org.robolectric.annotation.LooperMode;
import org.robolectric.shadows.testing.TestActivity;

/** Test for {@link ShadowUiAutomation}. */
@RunWith(AndroidJUnit4.class)
public class ShadowUiAutomationTest {

  @Test
  @Config(minSdk = VERSION_CODES.P)
  public void grantRuntimePermission_grantsPermission() {
    UiAutomation uiAutomation = InstrumentationRegistry.getInstrumentation().getUiAutomation();
    Context context = ApplicationProvider.getApplicationContext();
    String packageName = context.getPackageName();
    assertThat(context.checkSelfPermission(android.Manifest.permission.READ_MEDIA_IMAGES))
        .isEqualTo(PackageManager.PERMISSION_DENIED);
    assertThat(context.checkSelfPermission(android.Manifest.permission.READ_MEDIA_VIDEO))
        .isEqualTo(PackageManager.PERMISSION_DENIED);

    uiAutomation.grantRuntimePermission(packageName, android.Manifest.permission.READ_MEDIA_IMAGES);
    uiAutomation.grantRuntimePermission(packageName, android.Manifest.permission.READ_MEDIA_VIDEO);

    assertThat(context.checkSelfPermission(android.Manifest.permission.READ_MEDIA_IMAGES))
        .isEqualTo(PackageManager.PERMISSION_GRANTED);
    assertThat(context.checkSelfPermission(android.Manifest.permission.READ_MEDIA_VIDEO))
        .isEqualTo(PackageManager.PERMISSION_GRANTED);
  }

  @Test
  public void setAnimationScale_zero() throws Exception {
    ShadowUiAutomation.setAnimationScaleCompat(0);

    ContentResolver cr = ApplicationProvider.getApplicationContext().getContentResolver();
    assertThat(Settings.Global.getFloat(cr, Settings.Global.ANIMATOR_DURATION_SCALE)).isEqualTo(0);
    assertThat(Settings.Global.getFloat(cr, Settings.Global.TRANSITION_ANIMATION_SCALE))
        .isEqualTo(0);
    assertThat(Settings.Global.getFloat(cr, Settings.Global.WINDOW_ANIMATION_SCALE)).isEqualTo(0);
  }

  @Test
  public void setAnimationScale_one() throws Exception {
    ShadowUiAutomation.setAnimationScaleCompat(1);

    ContentResolver cr = ApplicationProvider.getApplicationContext().getContentResolver();
    assertThat(Settings.Global.getFloat(cr, Settings.Global.ANIMATOR_DURATION_SCALE)).isEqualTo(1);
    assertThat(Settings.Global.getFloat(cr, Settings.Global.TRANSITION_ANIMATION_SCALE))
        .isEqualTo(1);
    assertThat(Settings.Global.getFloat(cr, Settings.Global.WINDOW_ANIMATION_SCALE)).isEqualTo(1);
  }

  @Test
  public void setRotation_freeze90_rotatesToLandscape() {
    UiAutomation uiAutomation = InstrumentationRegistry.getInstrumentation().getUiAutomation();

    uiAutomation.setRotation(UiAutomation.ROTATION_FREEZE_90);

    assertThat(ShadowDisplay.getDefaultDisplay().getRotation()).isEqualTo(Surface.ROTATION_90);
    assertThat(Resources.getSystem().getConfiguration().orientation)
        .isEqualTo(Configuration.ORIENTATION_LANDSCAPE);
  }

  @Test
  public void setRotation_freeze180_rotatesToPortrait() {
    UiAutomation uiAutomation = InstrumentationRegistry.getInstrumentation().getUiAutomation();

    uiAutomation.setRotation(UiAutomation.ROTATION_FREEZE_180);

    assertThat(ShadowDisplay.getDefaultDisplay().getRotation()).isEqualTo(Surface.ROTATION_180);
    assertThat(Resources.getSystem().getConfiguration().orientation)
        .isEqualTo(Configuration.ORIENTATION_PORTRAIT);
  }

  @Test
  public void setRotation_freezeCurrent_doesNothing() {
    UiAutomation uiAutomation = InstrumentationRegistry.getInstrumentation().getUiAutomation();

    uiAutomation.setRotation(UiAutomation.ROTATION_FREEZE_CURRENT);

    assertThat(ShadowDisplay.getDefaultDisplay().getRotation()).isEqualTo(Surface.ROTATION_0);
    assertThat(Resources.getSystem().getConfiguration().orientation)
        .isEqualTo(Configuration.ORIENTATION_PORTRAIT);
  }

  @LooperMode(LooperMode.Mode.INSTRUMENTATION_TEST)
  @Test
  public void setAnimationScale_zero_instrumentationTestLooperMode() throws Exception {
    setAnimationScale_zero();
  }

  @LooperMode(LooperMode.Mode.INSTRUMENTATION_TEST)
  @Test
  public void setRotation_freeze90_rotatesToLandscape_instrumentationTestLooperMode() {
    setRotation_freeze90_rotatesToLandscape();
  }

  @Test
  @Config(minSdk = VERSION_CODES.UPSIDE_DOWN_CAKE)
  @GraphicsMode(GraphicsMode.Mode.NATIVE)
  public void getRootInActiveWindow_noActiveWindow_returnsNull() {
    UiAutomation uiAutomation = InstrumentationRegistry.getInstrumentation().getUiAutomation();

    assertThat(uiAutomation.getRootInActiveWindow()).isNull();
  }

  @Test
  @Config(minSdk = VERSION_CODES.UPSIDE_DOWN_CAKE)
  @GraphicsMode(GraphicsMode.Mode.NATIVE)
  public void getRootInActiveWindow_activeActivity_queriesHierarchyAndPerformsAction() {
    UiAutomation uiAutomation =
        InstrumentationRegistry.getInstrumentation()
            .getUiAutomation(UiAutomation.FLAG_DONT_SUPPRESS_ACCESSIBILITY_SERVICES);
    try (ActivityController<Activity> controller =
        Robolectric.buildActivity(Activity.class).setup()) {
      Activity activity = controller.get();
      LinearLayout layout = new LinearLayout(activity);
      TextView label = new TextView(activity);
      label.setId(android.R.id.text1);
      label.setText("Hello Accessibility");
      Button button = new Button(activity);
      button.setText("Submit");
      AtomicBoolean clicked = new AtomicBoolean(false);
      button.setOnClickListener(v -> clicked.set(true));
      layout.addView(label);
      layout.addView(button);
      activity.setContentView(layout);
      ShadowLooper.idleMainLooper();

      AccessibilityNodeInfo root = uiAutomation.getRootInActiveWindow();

      assertThat(root).isNotNull();
      assertThat(root.isSealed()).isTrue();
      assertThat(root.getChildCount()).isGreaterThan(0);
      assertThat(root.getChild(0)).isNotNull();

      List<AccessibilityNodeInfo> byId =
          root.findAccessibilityNodeInfosByViewId("android:id/text1");
      assertThat(byId).hasSize(1);
      assertThat(byId.get(0).getText().toString()).isEqualTo("Hello Accessibility");

      List<AccessibilityNodeInfo> buttons = root.findAccessibilityNodeInfosByText("Submit");
      assertThat(buttons).hasSize(1);
      assertThat(buttons.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK)).isTrue();
      ShadowLooper.idleMainLooper();
      assertThat(clicked.get()).isTrue();
    }
  }

  @Test
  @Config(minSdk = VERSION_CODES.UPSIDE_DOWN_CAKE)
  @GraphicsMode(GraphicsMode.Mode.NATIVE)
  public void getRootInActiveWindow_dialogShownHiddenAndDismissed_tracksActiveWindow() {
    UiAutomation uiAutomation = InstrumentationRegistry.getInstrumentation().getUiAutomation();
    try (ActivityController<Activity> controller =
        Robolectric.buildActivity(Activity.class).setup()) {
      Activity activity = controller.get();
      TextView activityText = new TextView(activity);
      activityText.setText("Activity Content");
      activity.setContentView(activityText);
      ShadowLooper.idleMainLooper();

      AlertDialog dialog = new AlertDialog.Builder(activity).setMessage("Dialog Content").show();
      ShadowLooper.idleMainLooper();

      AccessibilityNodeInfo dialogRoot = uiAutomation.getRootInActiveWindow();
      assertThat(dialogRoot).isNotNull();
      assertThat(dialogRoot.findAccessibilityNodeInfosByText("Dialog Content")).hasSize(1);
      assertThat(dialogRoot.findAccessibilityNodeInfosByText("Activity Content")).isEmpty();

      dialog.hide();
      AccessibilityNodeInfo rootAfterHide = uiAutomation.getRootInActiveWindow();
      assertThat(rootAfterHide).isNotNull();
      assertThat(rootAfterHide.findAccessibilityNodeInfosByText("Activity Content")).hasSize(1);
      assertThat(rootAfterHide.findAccessibilityNodeInfosByText("Dialog Content")).isEmpty();

      dialog.show();
      ShadowLooper.idleMainLooper();
      AccessibilityNodeInfo rootAfterReshow = uiAutomation.getRootInActiveWindow();
      assertThat(rootAfterReshow).isNotNull();
      assertThat(rootAfterReshow.findAccessibilityNodeInfosByText("Dialog Content")).hasSize(1);

      dialog.dismiss();
      ShadowLooper.idleMainLooper();
      AccessibilityNodeInfo rootAfterDismiss = uiAutomation.getRootInActiveWindow();
      assertThat(rootAfterDismiss).isNotNull();
      assertThat(rootAfterDismiss.findAccessibilityNodeInfosByText("Activity Content")).hasSize(1);
      assertThat(rootAfterDismiss.findAccessibilityNodeInfosByText("Dialog Content")).isEmpty();
    }
  }

  @Test
  @Config(minSdk = VERSION_CODES.UPSIDE_DOWN_CAKE)
  @GraphicsMode(GraphicsMode.Mode.NATIVE)
  public void getRootInActiveWindow_dialogButtonBoundsInScreen_matchesInjectInputEvent() {
    UiAutomation uiAutomation = InstrumentationRegistry.getInstrumentation().getUiAutomation();
    try (ActivityController<Activity> controller =
        Robolectric.buildActivity(Activity.class).setup()) {
      Activity activity = controller.get();
      AtomicBoolean dialogButtonClicked = new AtomicBoolean(false);
      AlertDialog dialog =
          new AlertDialog.Builder(activity)
              .setMessage("Confirm action?")
              .setPositiveButton("OK", (d, which) -> dialogButtonClicked.set(true))
              .show();
      ShadowLooper.idleMainLooper();

      AccessibilityNodeInfo dialogRoot = uiAutomation.getRootInActiveWindow();
      assertThat(dialogRoot).isNotNull();
      List<AccessibilityNodeInfo> okNodes = dialogRoot.findAccessibilityNodeInfosByText("OK");
      assertThat(okNodes).hasSize(1);

      Rect bounds = new Rect();
      okNodes.get(0).getBoundsInScreen(bounds);
      assertThat(bounds.isEmpty()).isFalse();

      long downTime = SystemClock.uptimeMillis();
      MotionEvent down =
          MotionEvent.obtain(
              downTime,
              downTime,
              MotionEvent.ACTION_DOWN,
              bounds.exactCenterX(),
              bounds.exactCenterY(),
              0);
      down.setSource(InputDevice.SOURCE_TOUCHSCREEN);
      MotionEvent up =
          MotionEvent.obtain(
              downTime,
              downTime + 10,
              MotionEvent.ACTION_UP,
              bounds.exactCenterX(),
              bounds.exactCenterY(),
              0);
      up.setSource(InputDevice.SOURCE_TOUCHSCREEN);

      uiAutomation.injectInputEvent(down, true);
      uiAutomation.injectInputEvent(up, true);
      down.recycle();
      up.recycle();
      ShadowLooper.idleMainLooper();

      assertThat(dialogButtonClicked.get()).isTrue();
      assertThat(dialog.isShowing()).isFalse();
    }
  }

  @Test
  @Config(minSdk = VERSION_CODES.UPSIDE_DOWN_CAKE)
  @GraphicsMode(GraphicsMode.Mode.NATIVE)
  public void getRootInActiveWindow_nonFocusableOverlay_returnsUnderlyingActivityRoot() {
    UiAutomation uiAutomation = InstrumentationRegistry.getInstrumentation().getUiAutomation();
    try (ActivityController<Activity> controller =
        Robolectric.buildActivity(Activity.class).setup()) {
      Activity activity = controller.get();
      TextView activityText = new TextView(activity);
      activityText.setText("Underlying Activity");
      activity.setContentView(activityText);

      TextView overlayView = new TextView(activity);
      overlayView.setText("Non-focusable Overlay");
      WindowManager.LayoutParams overlayParams =
          new WindowManager.LayoutParams(
              100,
              100,
              WindowManager.LayoutParams.TYPE_APPLICATION_PANEL,
              WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE,
              /* _format= */ 0);
      overlayParams.token = activity.getWindow().getDecorView().getWindowToken();
      activity.getWindowManager().addView(overlayView, overlayParams);
      ShadowLooper.idleMainLooper();

      AccessibilityNodeInfo root = uiAutomation.getRootInActiveWindow();

      assertThat(root).isNotNull();
      assertThat(root.findAccessibilityNodeInfosByText("Underlying Activity")).hasSize(1);
      assertThat(root.findAccessibilityNodeInfosByText("Non-focusable Overlay")).isEmpty();

      activity.getWindowManager().removeViewImmediate(overlayView);
    }
  }

  @Test
  @Config(minSdk = VERSION_CODES.UPSIDE_DOWN_CAKE)
  @GraphicsMode(GraphicsMode.Mode.NATIVE)
  public void getRootInActiveWindow_stoppedActivity_returnsNull() {
    UiAutomation uiAutomation = InstrumentationRegistry.getInstrumentation().getUiAutomation();
    try (ActivityController<Activity> controller =
        Robolectric.buildActivity(Activity.class).setup()) {
      assertThat(uiAutomation.getRootInActiveWindow()).isNotNull();

      controller.pause().stop();
      ShadowLooper.idleMainLooper();

      assertThat(uiAutomation.getRootInActiveWindow()).isNull();
    }
  }

  @Test
  @Config(minSdk = VERSION_CODES.VANILLA_ICE_CREAM)
  @GraphicsMode(GraphicsMode.Mode.NATIVE)
  public void getRootInActiveWindow_prefetchingStrategy_returnsActiveRoot() {
    UiAutomation uiAutomation = InstrumentationRegistry.getInstrumentation().getUiAutomation();
    try (ActivityController<Activity> controller =
        Robolectric.buildActivity(Activity.class).setup()) {
      TextView textView = new TextView(controller.get());
      textView.setText("Prefetched Content");
      controller.get().setContentView(textView);
      ShadowLooper.idleMainLooper();

      AccessibilityNodeInfo root =
          uiAutomation.getRootInActiveWindow(
              AccessibilityNodeInfo.FLAG_PREFETCH_DESCENDANTS_HYBRID);

      assertThat(root).isNotNull();
      assertThat(root.findAccessibilityNodeInfosByText("Prefetched Content")).hasSize(1);
    }
  }

  @LooperMode(LooperMode.Mode.INSTRUMENTATION_TEST)
  @Test
  @Config(minSdk = VERSION_CODES.UPSIDE_DOWN_CAKE)
  @GraphicsMode(GraphicsMode.Mode.NATIVE)
  public void getRootInActiveWindow_instrumentationTestLooperMode() {
    UiAutomation uiAutomation = InstrumentationRegistry.getInstrumentation().getUiAutomation();
    AtomicBoolean clicked = new AtomicBoolean(false);
    try (ActivityScenario<TestActivity> scenario = ActivityScenario.launch(TestActivity.class)) {
      scenario.onActivity(
          activity -> {
            Button button = new Button(activity);
            button.setText("Instrumentation Button");
            button.setOnClickListener(v -> clicked.set(true));
            activity.setContentView(button);
          });
      InstrumentationRegistry.getInstrumentation().waitForIdleSync();

      AccessibilityNodeInfo root = uiAutomation.getRootInActiveWindow();

      assertThat(root).isNotNull();
      assertThat(root.getChildCount()).isGreaterThan(0);
      List<AccessibilityNodeInfo> buttons =
          root.findAccessibilityNodeInfosByText("Instrumentation Button");
      assertThat(buttons).hasSize(1);
      assertThat(buttons.get(0).performAction(AccessibilityNodeInfo.ACTION_CLICK)).isTrue();
      InstrumentationRegistry.getInstrumentation().waitForIdleSync();
      assertThat(clicked.get()).isTrue();
    }
  }
}
