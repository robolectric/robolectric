package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP_MR1;
import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;
import static org.robolectric.Robolectric.buildActivity;
import static org.robolectric.shadows.ShadowLooper.idleMainLooper;
import static org.robolectric.shadows.SystemUi.STANDARD_STATUS_BAR;
import static org.robolectric.shadows.SystemUi.THREE_BUTTON_NAVIGATION;
import static org.robolectric.shadows.SystemUi.systemUiForDefaultDisplay;

import android.app.Activity;
import android.content.ClipData;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowInsets;
import android.view.WindowManager;
import android.window.BackEvent;
import android.window.OnBackAnimationCallback;
import android.window.OnBackInvokedDispatcher;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import java.util.ArrayList;
import java.util.List;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.GraphicsMode;
import org.robolectric.annotation.GraphicsMode.Mode;
import org.robolectric.shadows.SystemUi.NavigationBar;
import org.robolectric.shadows.SystemUi.StatusBar;

@RunWith(AndroidJUnit4.class)
@GraphicsMode(Mode.LEGACY)
public class ShadowWindowManagerGlobalTest {

  @Before
  public void setup() {
    System.setProperty("robolectric.areWindowsMarkedVisible", "true");
  }

  @Test
  public void getWindowSession_shouldReturnSession() {
    assertThat(ShadowWindowManagerGlobal.getWindowSession()).isNotNull();
  }

  @Test
  public void getLastDragClipData() {
    MotionEvent downEvent = MotionEvent.obtain(0L, 0L, MotionEvent.ACTION_DOWN, 12f, 34f, 0);
    Robolectric.buildActivity(DragActivity.class)
        .setup()
        .get()
        .findViewById(android.R.id.content)
        .dispatchTouchEvent(downEvent);

    assertThat(ShadowWindowManagerGlobal.getLastDragClipData()).isNotNull();
  }

  @Test
  public void windowIsVisible() {
    View decorView =
        Robolectric.buildActivity(DragActivity.class).setup().get().getWindow().getDecorView();

    assertThat(decorView.getWindowVisibility()).isEqualTo(View.VISIBLE);
  }

  @SuppressWarnings("MemberName") // In lieu of parameterization.
  private void startPredictiveBackGesture_callsBackCallbackMethods(@BackEvent.SwipeEdge int edge) {
    ShadowApplication.setEnableOnBackInvokedCallback(true);
    float touchSlop =
        ViewConfiguration.get(ApplicationProvider.getApplicationContext()).getScaledTouchSlop();
    try (ActivityController<ActivityWithBackCallback> controller =
        Robolectric.buildActivity(ActivityWithBackCallback.class)) {
      Activity activity = controller.setup().get();
      TestBackAnimationCallback backInvokedCallback = new TestBackAnimationCallback();
      activity
          .getOnBackInvokedDispatcher()
          .registerOnBackInvokedCallback(
              OnBackInvokedDispatcher.PRIORITY_DEFAULT, backInvokedCallback);

      float moveByX = (edge == BackEvent.EDGE_LEFT ? 1 : -1) * touchSlop * 2;
      try (ShadowWindowManagerGlobal.PredictiveBackGesture backGesture =
          ShadowWindowManagerGlobal.startPredictiveBackGesture(edge)) {
        backGesture.moveBy(moveByX, 0f);
      }

      assertThat(backInvokedCallback.onBackStarted).isNotNull();
      assertThat(backInvokedCallback.onBackProgressed).isNotEmpty();
      assertThat(Iterables.getLast(backInvokedCallback.onBackProgressed).getTouchX())
          .isEqualTo(backInvokedCallback.onBackStarted.getTouchX() + moveByX);
      assertThat(Iterables.getLast(backInvokedCallback.onBackProgressed).getProgress())
          .isGreaterThan(0);
      assertThat(backInvokedCallback.onBackInvokedCalled).isTrue();
    }
  }

  @Test
  @Config(minSdk = VERSION_CODES.UPSIDE_DOWN_CAKE)
  public void startPredictiveBackGesture_leftEdge_callsBackCallbackMethods() {
    startPredictiveBackGesture_callsBackCallbackMethods(BackEvent.EDGE_LEFT);
  }

  @Test
  @Config(minSdk = VERSION_CODES.UPSIDE_DOWN_CAKE)
  public void startPredictiveBackGesture_rightEdge_callsBackCallbackMethods() {
    startPredictiveBackGesture_callsBackCallbackMethods(BackEvent.EDGE_RIGHT);
  }

  @Test
  @Config(minSdk = VERSION_CODES.UPSIDE_DOWN_CAKE)
  public void startPredictiveBackGesture_cancel_callbackIsCancelled() {
    ShadowApplication.setEnableOnBackInvokedCallback(true);
    try (ActivityController<ActivityWithBackCallback> controller =
        Robolectric.buildActivity(ActivityWithBackCallback.class)) {
      Activity activity = controller.setup().get();
      TestBackAnimationCallback backInvokedCallback = new TestBackAnimationCallback();
      activity
          .getOnBackInvokedDispatcher()
          .registerOnBackInvokedCallback(
              OnBackInvokedDispatcher.PRIORITY_DEFAULT, backInvokedCallback);

      try (ShadowWindowManagerGlobal.PredictiveBackGesture backGesture =
          ShadowWindowManagerGlobal.startPredictiveBackGesture(BackEvent.EDGE_LEFT)) {
        backGesture.cancel();
      }

      assertThat(backInvokedCallback.onBackStarted).isNotNull();
      assertThat(backInvokedCallback.onBackCancelledCalled).isTrue();
    }
  }

  @Test
  @Config(minSdk = VERSION_CODES.UPSIDE_DOWN_CAKE)
  public void startPredictiveBackGesture_withExclusion_isNotCalled() {
    ShadowApplication.setEnableOnBackInvokedCallback(true);
    Display display = ShadowDisplay.getDefaultDisplay();
    try (ActivityController<ActivityWithBackCallback> controller =
        Robolectric.buildActivity(ActivityWithBackCallback.class)) {
      Activity activity = controller.setup().get();
      TestBackAnimationCallback backInvokedCallback = new TestBackAnimationCallback();
      activity
          .getOnBackInvokedDispatcher()
          .registerOnBackInvokedCallback(
              OnBackInvokedDispatcher.PRIORITY_DEFAULT, backInvokedCallback);
      // Exclude the entire display.
      activity
          .findViewById(android.R.id.content)
          .setSystemGestureExclusionRects(
              ImmutableList.of(new Rect(0, 0, display.getWidth(), display.getHeight())));

      ShadowWindowManagerGlobal.PredictiveBackGesture backGesture =
          ShadowWindowManagerGlobal.startPredictiveBackGesture(BackEvent.EDGE_LEFT);

      assertThat(backGesture).isNull();
      assertThat(backInvokedCallback.onBackStarted).isNull();
    }
  }

  @Test
  @Config(minSdk = VERSION_CODES.UPSIDE_DOWN_CAKE)
  public void startPredictiveBackGesture_cancelledTouchEventsDispatchedToWindow() {
    ShadowApplication.setEnableOnBackInvokedCallback(true);
    try (ActivityController<ActivityWithBackCallback> controller =
        Robolectric.buildActivity(ActivityWithBackCallback.class)) {
      Activity activity = controller.setup().get();
      List<MotionEvent> touchEvents = new ArrayList<>();
      activity
          .getOnBackInvokedDispatcher()
          .registerOnBackInvokedCallback(
              OnBackInvokedDispatcher.PRIORITY_DEFAULT, new TestBackAnimationCallback());
      activity
          .findViewById(android.R.id.content)
          .setOnTouchListener(
              new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                  touchEvents.add(event);
                  return true;
                }
              });

      ShadowWindowManagerGlobal.startPredictiveBackGesture(BackEvent.EDGE_LEFT).close();

      assertThat(touchEvents).isNotEmpty();
      assertThat(touchEvents.get(0).getAction()).isEqualTo(MotionEvent.ACTION_DOWN);
      assertThat(Iterables.getLast(touchEvents).getAction()).isEqualTo(MotionEvent.ACTION_CANCEL);
    }
  }

  @Test
  @Config(minSdk = VERSION_CODES.UPSIDE_DOWN_CAKE)
  public void startPredictiveBackGesture_invalidPosition_throwsIllegalArgumentException() {
    ShadowApplication.setEnableOnBackInvokedCallback(true);
    assertThrows(
        IllegalArgumentException.class,
        () -> ShadowWindowManagerGlobal.startPredictiveBackGesture(BackEvent.EDGE_LEFT, -1f));
  }

  @Test
  @Config(minSdk = VERSION_CODES.UPSIDE_DOWN_CAKE)
  public void startPredictiveBackGesture_alreadyOngoing_throwsIllegalStateException() {
    ShadowApplication.setEnableOnBackInvokedCallback(true);
    try (ActivityController<ActivityWithBackCallback> controller =
        Robolectric.buildActivity(ActivityWithBackCallback.class)) {
      Activity activity = controller.setup().get();
      activity
          .getOnBackInvokedDispatcher()
          .registerOnBackInvokedCallback(
              OnBackInvokedDispatcher.PRIORITY_DEFAULT, new TestBackAnimationCallback());

      ShadowWindowManagerGlobal.startPredictiveBackGesture(BackEvent.EDGE_LEFT);

      assertThrows(
          IllegalStateException.class,
          () -> ShadowWindowManagerGlobal.startPredictiveBackGesture(BackEvent.EDGE_LEFT));
    }
  }

  static final class DragActivity extends Activity {
    @Override
    protected void onCreate(@Nullable Bundle bundle) {
      super.onCreate(bundle);
      View contentView = new View(this);
      contentView.setOnTouchListener(
          (view, motionEvent) -> {
            if (motionEvent.getActionMasked() == MotionEvent.ACTION_DOWN) {
              ClipData clipData = ClipData.newPlainText("label", "text");
              DragShadowBuilder dragShadowBuilder = new DragShadowBuilder(view);
              if (RuntimeEnvironment.getApiLevel() >= VERSION_CODES.N) {
                view.startDragAndDrop(clipData, dragShadowBuilder, null, 0);
              } else {
                view.startDrag(clipData, dragShadowBuilder, null, 0);
              }
            }
            return true;
          });
      setContentView(contentView);
    }
  }

  public static final class ActivityWithBackCallback extends Activity {}

  private static final class TestBackAnimationCallback implements OnBackAnimationCallback {
    @Nullable public BackEvent onBackStarted;
    public List<BackEvent> onBackProgressed = new ArrayList<>();
    public boolean onBackInvokedCalled = false;
    public boolean onBackCancelledCalled = false;

    @Override
    public void onBackStarted(@NonNull BackEvent backEvent) {
      onBackStarted = backEvent;
    }

    @Override
    public void onBackProgressed(@NonNull BackEvent backEvent) {
      onBackProgressed.add(backEvent);
    }

    @Override
    public void onBackInvoked() {
      onBackInvokedCalled = true;
    }

    @Override
    public void onBackCancelled() {
      onBackCancelledCalled = true;
    }
  }

  @Test
  public void windowInsets() {
    systemUiForDefaultDisplay().setBehavior(STANDARD_STATUS_BAR, THREE_BUTTON_NAVIGATION);
    ActivityController<WindowInsetsActivity> controller = buildActivity(WindowInsetsActivity.class);

    controller.setup();
    idleMainLooper();

    StatusBar statusBar = systemUiForDefaultDisplay().getStatusBar();
    NavigationBar navBar = systemUiForDefaultDisplay().getNavigationBar();
    assertThat(controller.get().systemInsets)
        .isEqualTo(new Rect(0, statusBar.getSize(), 0, navBar.getSize()));
    assertThat(controller.get().windowInsets).isNotNull();
  }

  @Config(minSdk = R)
  @Test
  public void windowInsetsController_hideStatusBar() {
    systemUiForDefaultDisplay().setBehavior(STANDARD_STATUS_BAR, THREE_BUTTON_NAVIGATION);
    ActivityController<WindowInsetsActivity> controller = buildActivity(WindowInsetsActivity.class);
    controller.setup();
    idleMainLooper();

    controller.get().getWindow().getInsetsController().hide(WindowInsets.Type.statusBars());
    idleMainLooper();

    assertThat(controller.get().windowInsets.getInsets(WindowInsets.Type.statusBars()).top)
        .isEqualTo(0);
    assertThat(controller.get().windowInsets.isVisible(WindowInsets.Type.statusBars())).isFalse();
    assertThat(controller.get().windowInsets.isVisible(WindowInsets.Type.navigationBars()))
        .isTrue();
  }

  @Config(minSdk = R)
  @Test
  public void windowInsetsController_hideSystemBars() {
    systemUiForDefaultDisplay().setBehavior(STANDARD_STATUS_BAR, THREE_BUTTON_NAVIGATION);
    ActivityController<WindowInsetsActivity> controller = buildActivity(WindowInsetsActivity.class);
    controller.setup();
    idleMainLooper();

    controller.get().getWindow().getInsetsController().hide(WindowInsets.Type.systemBars());
    idleMainLooper();

    assertThat(controller.get().windowInsets.isVisible(WindowInsets.Type.statusBars())).isFalse();
    assertThat(controller.get().windowInsets.isVisible(WindowInsets.Type.navigationBars()))
        .isFalse();
  }

  @Config(minSdk = R)
  @Test
  public void windowInsetsController_toggleStatusBar() {
    ActivityController<WindowInsetsActivity> controller = buildActivity(WindowInsetsActivity.class);
    controller.setup();
    idleMainLooper();

    controller.get().getWindow().getInsetsController().hide(WindowInsets.Type.statusBars());
    idleMainLooper();
    controller.get().getWindow().getInsetsController().show(WindowInsets.Type.statusBars());
    idleMainLooper();

    assertThat(controller.get().windowInsets.isVisible(WindowInsets.Type.statusBars()))
        .isEqualTo(true);
  }

  @Config(minSdk = R)
  @Test
  public void windowInsetsController_twoWindows_toggleStatusBar() {
    ActivityController<WindowInsetsActivity> controller = buildActivity(WindowInsetsActivity.class);
    controller.setup();
    idleMainLooper();
    ActivityController<WindowInsetsActivity> controller2 =
        buildActivity(WindowInsetsActivity.class);
    controller2.setup();
    idleMainLooper();

    controller2.get().getWindow().getInsetsController().hide(WindowInsets.Type.statusBars());
    idleMainLooper();

    assertThat(controller2.get().windowInsets.isVisible(WindowInsets.Type.statusBars()))
        .isEqualTo(false);
  }

  public static final class WindowInsetsActivity extends Activity {
    Rect systemInsets;
    WindowInsets windowInsets;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
      setEdgeToEdge(getWindow());
      super.onCreate(savedInstanceState);
      setContentView(
          new View(this) {
            @Override
            public WindowInsets onApplyWindowInsets(WindowInsets insets) {
              windowInsets = insets;
              return super.onApplyWindowInsets(insets);
            }

            @Override
            protected boolean fitSystemWindows(Rect insets) {
              systemInsets = new Rect(insets);
              return super.fitSystemWindows(insets);
            }
          });
    }
  }

  // This sets similar properties to the androidx edgeToEdge API.
  private static void setEdgeToEdge(Window window) {
    if (RuntimeEnvironment.getApiLevel() <= Q) {
      window
          .getDecorView()
          .setSystemUiVisibility(
              window.getDecorView().getSystemUiVisibility()
                  | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                  | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                  | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
    } else if (RuntimeEnvironment.getApiLevel() <= UPSIDE_DOWN_CAKE) {
      window
          .getDecorView()
          .setSystemUiVisibility(
              window.getDecorView().getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
      window.setDecorFitsSystemWindows(false);
    } else {
      window.setDecorFitsSystemWindows(false);
    }
    if (RuntimeEnvironment.getApiLevel() <= LOLLIPOP_MR1) {
      window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
      window.addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
    } else {
      window.setStatusBarColor(Color.TRANSPARENT);
      window.setNavigationBarColor(Color.TRANSPARENT);
    }
    if (RuntimeEnvironment.getApiLevel() > R) {
      window.getAttributes().layoutInDisplayCutoutMode =
          WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS;
    } else if (RuntimeEnvironment.getApiLevel() > P) {
      window.getAttributes().layoutInDisplayCutoutMode =
          WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
    }
  }
}
