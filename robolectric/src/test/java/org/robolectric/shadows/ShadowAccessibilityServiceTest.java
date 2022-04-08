package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.LOLLIPOP;
import static android.os.Build.VERSION_CODES.N;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityService.GestureResultCallback;
import android.accessibilityservice.GestureDescription;
import android.accessibilityservice.GestureDescription.StrokeDescription;
import android.graphics.Path;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityWindowInfo;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.Arrays;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
public class ShadowAccessibilityServiceTest {
  private MyService service;
  private ShadowAccessibilityService shadow;

  @Before
  public void setUp() {
    service = Robolectric.setupService(MyService.class);
    shadow = shadowOf(service);
  }

  /** After performing a global action, it should be recorded. */
  @Test
  public void shouldRecordPerformedAction() {
    service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
    assertThat(shadow.getGlobalActionsPerformed().size()).isEqualTo(1);
    assertThat(shadow.getGlobalActionsPerformed().get(0)).isEqualTo(1);
  }

  /**
   * The AccessibilityService shadow should return an empty list if no window data is provided.
   */
  @Test
  @Config(minSdk = LOLLIPOP)
  public void shouldReturnEmptyListIfNoWindowDataProvided() {
    assertThat(service.getWindows()).isEmpty();
  }

  /**
   * The AccessibilityService shadow should return an empty list if null window data is provided.
   */
  @Test
  @Config(minSdk = LOLLIPOP)
  public void shouldReturnEmptyListIfNullWindowDataProvided() {
    shadow.setWindows(null);
    assertThat(service.getWindows()).isEmpty();
  }

  @Test
  @Config(minSdk = N)
  public void getGesturesDispatched_returnsNothingInitially() {
    assertThat(shadow.getGesturesDispatched()).isEmpty();
  }

  @Test
  @Config(minSdk = N)
  public void getGesturesDispatched_returnsFirstGestureDescription() {
    GestureDescription gestureDescription = createTestGesture();
    GestureResultCallback gestureResultCallback = createEmptyGestureResultCallback();

    service.dispatchGesture(gestureDescription, gestureResultCallback, /*handler=*/ null);

    assertThat(shadow.getGesturesDispatched().get(0).description())
        .isSameInstanceAs(gestureDescription);
  }

  @Test
  @Config(minSdk = N)
  public void getGesturesDispatched_returnsFirstGestureResultCallback() {
    GestureDescription gestureDescription = createTestGesture();
    GestureResultCallback gestureResultCallback = createEmptyGestureResultCallback();

    service.dispatchGesture(gestureDescription, gestureResultCallback, /*handler=*/ null);

    assertThat(shadow.getGesturesDispatched().get(0).callback())
        .isSameInstanceAs(gestureResultCallback);
  }

  @Test
  @Config(minSdk = N)
  public void setCanDispatchGestures_false_causesDispatchGestureToReturnFalse() {
    GestureDescription gestureDescription = createTestGesture();
    GestureResultCallback gestureResultCallback = createEmptyGestureResultCallback();

    shadow.setCanDispatchGestures(false);

    assertThat(
            service.dispatchGesture(gestureDescription, gestureResultCallback, /*handler=*/ null))
        .isFalse();
  }

  @Test
  @Config(minSdk = N)
  public void setCanDispatchGestures_false_stopsRecordingDispatchedGestures() {
    GestureDescription gestureDescription = createTestGesture();
    GestureResultCallback gestureResultCallback = createEmptyGestureResultCallback();

    shadow.setCanDispatchGestures(false);
    service.dispatchGesture(gestureDescription, gestureResultCallback, /*handler=*/ null);

    assertThat(shadow.getGesturesDispatched()).isEmpty();
  }

  @Test
  @Config(minSdk = N)
  public void setCanDispatchGestures_true_causesDispatchGestureToReturnTrue() {
    GestureDescription gestureDescription = createTestGesture();
    GestureResultCallback gestureResultCallback = createEmptyGestureResultCallback();
    shadow.setCanDispatchGestures(false);

    shadow.setCanDispatchGestures(true);

    assertThat(
            service.dispatchGesture(gestureDescription, gestureResultCallback, /*handler=*/ null))
        .isTrue();
  }

  /**
   * The AccessibilityService shadow should return consistent window data.
   */
  @Test
  @Config(minSdk = LOLLIPOP)
  public void shouldReturnPopulatedWindowData() {
    AccessibilityWindowInfo w1 = AccessibilityWindowInfo.obtain();
    w1.setId(1);
    AccessibilityWindowInfo w2 = AccessibilityWindowInfo.obtain();
    w2.setId(2);
    AccessibilityWindowInfo w3 = AccessibilityWindowInfo.obtain();
    w3.setId(3);
    shadow.setWindows(Arrays.asList(w1, w2, w3));
    assertThat(service.getWindows()).hasSize(3);
    assertThat(service.getWindows()).containsExactly(w1, w2, w3).inOrder();
  }

  private static GestureDescription createTestGesture() {
    Path path = new Path();
    path.moveTo(/*x=*/ 100, /*y=*/ 200);
    path.lineTo(/*x=*/ 100, /*y=*/ 800);
    return new GestureDescription.Builder()
        .addStroke(new StrokeDescription(path, /*startTime=*/ 0, /*duration=*/ 100))
        .build();
  }

  private static GestureResultCallback createEmptyGestureResultCallback() {
    return new GestureResultCallback() {
      @Override
      public void onCompleted(GestureDescription description) {}

      @Override
      public void onCancelled(GestureDescription description) {}
    };
  }

  public static class MyService extends AccessibilityService {
    @Override
    public void onDestroy() {
      super.onDestroy();
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent arg0) {
      //Do nothing
    }

    @Override
    public void onInterrupt() {
      //Do nothing
    }
  }
}

