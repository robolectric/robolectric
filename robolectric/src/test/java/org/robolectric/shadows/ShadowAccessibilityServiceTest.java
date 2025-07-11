package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.N;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;
import static android.os.Build.VERSION_CODES.TIRAMISU;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityService.GestureResultCallback;
import android.accessibilityservice.AccessibilityService.ScreenshotResult;
import android.accessibilityservice.AccessibilityService.TakeScreenshotCallback;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.accessibilityservice.GestureDescription;
import android.accessibilityservice.GestureDescription.StrokeDescription;
import android.graphics.Path;
import android.view.Display;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import android.view.inputmethod.EditorInfo;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.MoreExecutors;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import javax.annotation.Nonnull;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.testing.TestAccessibilityService;

@RunWith(AndroidJUnit4.class)
public class ShadowAccessibilityServiceTest {
  private TestAccessibilityService service;
  private ShadowAccessibilityService shadow;

  @Before
  public void setUp() {
    service = Robolectric.setupService(TestAccessibilityService.class);
    shadow = shadowOf(service);
  }

  /** After performing a global action, it should be recorded. */
  @Test
  public void shouldRecordPerformedAction() {
    service.performGlobalAction(AccessibilityService.GLOBAL_ACTION_BACK);
    assertThat(shadow.getGlobalActionsPerformed()).hasSize(1);
    assertThat(shadow.getGlobalActionsPerformed().get(0)).isEqualTo(1);
  }

  /** The AccessibilityService shadow should return an empty list if no window data is provided. */
  @Test
  public void shouldReturnEmptyListIfNoWindowDataProvided() {
    assertThat(service.getWindows()).isEmpty();
  }

  /**
   * The AccessibilityService shadow should return an empty list if null window data is provided.
   */
  @Test
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

    service.dispatchGesture(gestureDescription, gestureResultCallback, /* handler= */ null);

    assertThat(shadow.getGesturesDispatched().get(0).description())
        .isSameInstanceAs(gestureDescription);
  }

  @Test
  @Config(minSdk = N)
  public void getGesturesDispatched_returnsFirstGestureResultCallback() {
    GestureDescription gestureDescription = createTestGesture();
    GestureResultCallback gestureResultCallback = createEmptyGestureResultCallback();

    service.dispatchGesture(gestureDescription, gestureResultCallback, /* handler= */ null);

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
            service.dispatchGesture(gestureDescription, gestureResultCallback, /* handler= */ null))
        .isFalse();
  }

  @Test
  @Config(minSdk = N)
  public void setCanDispatchGestures_false_stopsRecordingDispatchedGestures() {
    GestureDescription gestureDescription = createTestGesture();
    GestureResultCallback gestureResultCallback = createEmptyGestureResultCallback();

    shadow.setCanDispatchGestures(false);
    service.dispatchGesture(gestureDescription, gestureResultCallback, /* handler= */ null);

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
            service.dispatchGesture(gestureDescription, gestureResultCallback, /* handler= */ null))
        .isTrue();
  }

  @Test
  @Config(minSdk = TIRAMISU)
  public void startInput_startsInputMethod() {
    assertThat(service.getInputMethod()).isNull();

    // Set flags to get an InputMethod.
    AccessibilityServiceInfo serviceInfo = new AccessibilityServiceInfo();
    serviceInfo.flags |= AccessibilityServiceInfo.FLAG_INPUT_METHOD_EDITOR;
    service.setServiceInfo(serviceInfo);
    assertThat(service.getInputMethod()).isNotNull();
    assertThat(service.getInputMethod().getCurrentInputStarted()).isFalse();
    assertThat(service.getInputMethod().getCurrentInputConnection()).isNull();

    // Start input.
    EditorInfo editorInfo = new EditorInfo();
    editorInfo.hintText = "Watermelon";
    shadow.startInput(editorInfo);
    assertThat(service.getInputMethod()).isNotNull();
    assertThat(service.getInputMethod().getCurrentInputStarted()).isTrue();
    assertThat(service.getInputMethod().getCurrentInputConnection()).isNotNull();
    assertThat(service.getInputMethod().getCurrentInputEditorInfo()).isEqualTo(editorInfo);
  }

  @Test
  @Config(minSdk = R)
  public void takeScreenshot_byDefault_immediatelyReturnsSuccessfully() {
    AtomicReference<ScreenshotResult> screenshotResultAtomicReference = new AtomicReference<>(null);
    TakeScreenshotCallback takeScreenshotCallback =
        new TakeScreenshotCallback() {
          @Override
          public void onSuccess(@Nonnull ScreenshotResult screenshotResult) {
            screenshotResultAtomicReference.set(screenshotResult);
          }

          @Override
          public void onFailure(int i) {}
        };

    service.takeScreenshot(
        /* displayId= */ Display.DEFAULT_DISPLAY,
        MoreExecutors.directExecutor(),
        takeScreenshotCallback);

    assertThat(screenshotResultAtomicReference.get()).isNotNull();
  }

  @Test
  @Config(minSdk = R)
  public void takeScreenshot_afterSettingErrorCode_returnsErrorCode() {
    shadow.setTakeScreenshotErrorCode(
        AccessibilityService.ERROR_TAKE_SCREENSHOT_INTERVAL_TIME_SHORT);
    AtomicReference<Integer> errorCodeAtomicReference = new AtomicReference<>(-1);
    TakeScreenshotCallback takeScreenshotCallback =
        new TakeScreenshotCallback() {
          @Override
          public void onSuccess(@Nonnull ScreenshotResult screenshotResult) {}

          @Override
          public void onFailure(int errorCode) {
            errorCodeAtomicReference.set(errorCode);
          }
        };

    service.takeScreenshot(
        /* displayId= */ Display.DEFAULT_DISPLAY,
        MoreExecutors.directExecutor(),
        takeScreenshotCallback);

    assertThat(errorCodeAtomicReference.get())
        .isEqualTo(AccessibilityService.ERROR_TAKE_SCREENSHOT_INTERVAL_TIME_SHORT);
  }

  @Test
  @Config(minSdk = R)
  public void takeScreenshot_afterUnsettingErrorCode_immediatelyReturnsSuccessfully() {
    AtomicReference<ScreenshotResult> screenshotResultAtomicReference = new AtomicReference<>(null);
    TakeScreenshotCallback takeScreenshotCallback =
        new TakeScreenshotCallback() {
          @Override
          public void onSuccess(@Nonnull ScreenshotResult screenshotResult) {
            screenshotResultAtomicReference.set(screenshotResult);
          }

          @Override
          public void onFailure(int i) {}
        };
    shadow.setTakeScreenshotErrorCode(
        AccessibilityService.ERROR_TAKE_SCREENSHOT_INTERVAL_TIME_SHORT);
    shadow.unsetTakeScreenshotErrorCode();

    service.takeScreenshot(
        /* displayId= */ Display.DEFAULT_DISPLAY,
        MoreExecutors.directExecutor(),
        takeScreenshotCallback);

    assertThat(screenshotResultAtomicReference.get()).isNotNull();
  }

  /** The AccessibilityService shadow should return consistent window data. */
  @Test
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

  @Test
  @Config(minSdk = R)
  public void getWindowsForDefaultDisplay_returnEmptyList() {
    assertThat(service.getWindowsOnAllDisplays().get(Display.DEFAULT_DISPLAY)).isEmpty();
  }

  @Test
  @Config(minSdk = R)
  public void getWindowsForNonDefaultDisplay_returnNullList() {
    assertThat(service.getWindowsOnAllDisplays().get(Display.DEFAULT_DISPLAY + 1)).isNull();
  }

  @Test
  @Config(minSdk = R)
  public void setWindowsOnDisplay_returnPopulatedWindowsOnAllDisplays() {
    AccessibilityWindowInfo w1 = AccessibilityWindowInfo.obtain();
    w1.setId(1);
    AccessibilityWindowInfo w2 = AccessibilityWindowInfo.obtain();
    w2.setId(2);
    AccessibilityWindowInfo w3 = AccessibilityWindowInfo.obtain();
    w3.setId(3);
    AccessibilityWindowInfo w4 = AccessibilityWindowInfo.obtain();
    w4.setId(4);
    AccessibilityWindowInfo w5 = AccessibilityWindowInfo.obtain();
    w5.setId(5);
    AccessibilityWindowInfo w6 = AccessibilityWindowInfo.obtain();
    w6.setId(6);
    AccessibilityWindowInfo w7 = AccessibilityWindowInfo.obtain();
    w7.setId(7);

    shadow.setWindowsOnDisplay(Display.DEFAULT_DISPLAY, Arrays.asList(w1, w2, w3));
    shadow.setWindowsOnDisplay(Display.DEFAULT_DISPLAY + 1, Arrays.asList(w4, w5, w6, w7));

    assertThat(service.getWindows()).hasSize(3);
    assertThat(service.getWindows()).containsExactly(w1, w2, w3).inOrder();
    assertThat(service.getWindowsOnAllDisplays().size()).isEqualTo(2);
    assertThat(service.getWindowsOnAllDisplays().get(Display.DEFAULT_DISPLAY))
        .containsExactly(w1, w2, w3)
        .inOrder();
    assertThat(service.getWindowsOnAllDisplays().get(Display.DEFAULT_DISPLAY + 1))
        .containsExactly(w4, w5, w6, w7)
        .inOrder();
  }

  @Test
  @Config(minSdk = R)
  public void setNullWindowsOnNonDefaultDisplay_nonDefaultDisplayHasWindows_displayIsRemoved() {
    AccessibilityWindowInfo w1 = AccessibilityWindowInfo.obtain();
    w1.setId(1);
    AccessibilityWindowInfo w2 = AccessibilityWindowInfo.obtain();
    w2.setId(2);
    AccessibilityWindowInfo w3 = AccessibilityWindowInfo.obtain();
    w3.setId(3);
    shadow.setWindowsOnDisplay(Display.DEFAULT_DISPLAY + 1, Arrays.asList(w1, w2, w3));

    shadow.setWindowsOnDisplay(Display.DEFAULT_DISPLAY + 1, null);

    assertThat(service.getWindowsOnAllDisplays().get(Display.DEFAULT_DISPLAY + 1)).isNull();
  }

  @Test
  @Config(minSdk = R)
  public void setWindows_nonDefaultDisplayHasWindows_nonDefaultDisplayWindowsNotRemoved() {
    AccessibilityWindowInfo w1 = AccessibilityWindowInfo.obtain();
    w1.setId(1);
    AccessibilityWindowInfo w2 = AccessibilityWindowInfo.obtain();
    w2.setId(2);
    AccessibilityWindowInfo w3 = AccessibilityWindowInfo.obtain();
    w3.setId(3);
    AccessibilityWindowInfo w4 = AccessibilityWindowInfo.obtain();
    w4.setId(4);
    AccessibilityWindowInfo w5 = AccessibilityWindowInfo.obtain();
    w5.setId(5);
    AccessibilityWindowInfo w6 = AccessibilityWindowInfo.obtain();
    w6.setId(6);
    AccessibilityWindowInfo w7 = AccessibilityWindowInfo.obtain();
    w7.setId(7);
    shadow.setWindowsOnDisplay(Display.DEFAULT_DISPLAY + 1, Arrays.asList(w4, w5, w6, w7));

    shadow.setWindows(Arrays.asList(w1, w2, w3));

    assertThat(service.getWindowsOnAllDisplays().get(Display.DEFAULT_DISPLAY + 1))
        .containsExactly(w4, w5, w6, w7)
        .inOrder();
  }

  @Test
  @Config(minSdk = S)
  public void getSystemActions_returnsNull() {
    assertThat(service.getSystemActions()).isNull();
  }

  @Test
  @Config(minSdk = S)
  public void getSystemActions_returnsSetValue() {
    ImmutableList<AccessibilityNodeInfo.AccessibilityAction> actions =
        ImmutableList.of(
            new AccessibilityNodeInfo.AccessibilityAction(
                AccessibilityService.GLOBAL_ACTION_BACK, "Go back"),
            new AccessibilityNodeInfo.AccessibilityAction(
                AccessibilityService.GLOBAL_ACTION_ACCESSIBILITY_ALL_APPS, "All apps"));

    shadow.setSystemActions(actions);

    assertThat(service.getSystemActions()).isEqualTo(actions);
  }

  @Test
  public void setRootInActiveWindow_returnsRootInActiveWindow() {
    AccessibilityNodeInfo node = AccessibilityNodeInfo.obtain();
    node.setText("test");
    shadow.setRootInActiveWindow(node);
    assertThat(service.getRootInActiveWindow()).isEqualTo(node);
  }

  @Test
  public void setRootInActiveWindow_returnsNull() {
    assertThat(service.getRootInActiveWindow()).isNull();
  }

  private static GestureDescription createTestGesture() {
    Path path = new Path();
    path.moveTo(/* x= */ 100, /* y= */ 200);
    path.lineTo(/* x= */ 100, /* y= */ 800);
    return new GestureDescription.Builder()
        .addStroke(new StrokeDescription(path, /* startTime= */ 0, /* duration= */ 100))
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
}
