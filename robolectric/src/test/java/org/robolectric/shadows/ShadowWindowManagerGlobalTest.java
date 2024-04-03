package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import android.app.Activity;
import android.content.ClipData;
import android.graphics.Rect;
import android.os.Build.VERSION_CODES;
import android.os.Bundle;
import android.os.Looper;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.DragShadowBuilder;
import android.view.View.OnTouchListener;
import android.view.ViewConfiguration;
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

@RunWith(AndroidJUnit4.class)
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
  @Config(minSdk = JELLY_BEAN_MR1)
  public void getWindowSession_withLooper_shouldReturnSession() {
    // method not available in JELLY BEAN, sorry :(
    assertThat(ShadowWindowManagerGlobal.getWindowSession(Looper.getMainLooper())).isNotNull();
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
}
