package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.content.Context;
import android.hardware.input.InputManager;
import android.hardware.input.InputManager.InputDeviceListener;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.VerifiedMotionEvent;
import android.view.View;
import android.widget.Button;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;

/** Unit tests for {@link ShadowInputManager}. */
@RunWith(AndroidJUnit4.class)
public class ShadowInputManagerTest {
  /** {@code InputManager.INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH}, hidden. */
  private static final int INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH = 2;

  private InputManager inputManager;

  @Before
  public void setUp() {
    Context context = ApplicationProvider.getApplicationContext();
    inputManager = (InputManager) context.getSystemService(Context.INPUT_SERVICE);
  }

  @Config(minSdk = R) // verifyMotionEvent was added in SDK 30 (R)
  @Test
  public void verifyMotionEvent() {
    MotionEvent motionEvent =
        MotionEvent.obtain(12345, 23456, MotionEvent.ACTION_UP, 30.0f, 40.0f, 0);
    VerifiedMotionEvent verifiedMotionEvent =
        (VerifiedMotionEvent) inputManager.verifyInputEvent(motionEvent);

    assertThat(verifiedMotionEvent.getRawX()).isEqualTo(30.0f);
    assertThat(verifiedMotionEvent.getRawY()).isEqualTo(40.0f);
    assertThat(verifiedMotionEvent.getEventTimeNanos()).isEqualTo(23456000000L);
    assertThat(verifiedMotionEvent.getDownTimeNanos()).isEqualTo(12345000000L);
  }

  @Test
  public void injectInputEvent_tap_clicksView() {
    try (ActivityController<Activity> controller =
        Robolectric.buildActivity(Activity.class).setup()) {
      AtomicInteger clicks = new AtomicInteger();
      Button button = new Button(controller.get());
      button.setOnClickListener(v -> clicks.incrementAndGet());
      controller.get().setContentView(button);
      ShadowLooper.idleMainLooper();
      int[] location = new int[2];
      button.getLocationOnScreen(location);
      float x = location[0] + button.getWidth() / 2f;
      float y = location[1] + button.getHeight() / 2f;
      long downTime = SystemClock.uptimeMillis();
      MotionEvent down = MotionEvent.obtain(downTime, downTime, MotionEvent.ACTION_DOWN, x, y, 0);
      down.setSource(InputDevice.SOURCE_TOUCHSCREEN);
      MotionEvent up = MotionEvent.obtain(downTime, downTime + 10, MotionEvent.ACTION_UP, x, y, 0);
      up.setSource(InputDevice.SOURCE_TOUCHSCREEN);

      assertThat(inputManager.injectInputEvent(down, INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH))
          .isTrue();
      assertThat(inputManager.injectInputEvent(up, INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH))
          .isTrue();
      down.recycle();
      up.recycle();
      ShadowLooper.idleMainLooper();

      assertThat(clicks.get()).isEqualTo(1);
    }
  }

  @Test
  public void injectInputEvent_key_reachesFocusedView() {
    try (ActivityController<Activity> controller =
        Robolectric.buildActivity(Activity.class).setup()) {
      List<Integer> keys = new ArrayList<>();
      View view = new View(controller.get());
      view.setFocusable(true);
      view.setFocusableInTouchMode(true);
      view.setOnKeyListener((v, keyCode, event) -> {
        if (event.getAction() == KeyEvent.ACTION_DOWN) {
          keys.add(keyCode);
        }
        return true;
      });
      controller.get().setContentView(view);
      assertThat(view.requestFocus()).isTrue();
      ShadowLooper.idleMainLooper();

      inputManager.injectInputEvent(
          new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_A),
          INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH);
      inputManager.injectInputEvent(
          new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_A),
          INJECT_INPUT_EVENT_MODE_WAIT_FOR_FINISH);

      assertThat(keys).containsExactly(KeyEvent.KEYCODE_A);
    }
  }

  static class InputDeviceListenerNoOp implements InputDeviceListener {
    @Override
    public void onInputDeviceAdded(int deviceId) {}

    @Override
    public void onInputDeviceRemoved(int deviceId) {}

    @Override
    public void onInputDeviceChanged(int deviceId) {}
  }

  @Test
  public void testRegisterInputDeviceListener_doesNotCrash() {
    InputDeviceListenerNoOp listener = new InputDeviceListenerNoOp();
    inputManager.registerInputDeviceListener(listener, new Handler(Looper.getMainLooper()));
    inputManager.unregisterInputDeviceListener(listener);
  }

  @Test
  public void getInputDeviceIds_doesNotCrash() {
    assertThat(inputManager.getInputDeviceIds()).isEmpty();
  }

  @Test
  public void addVirtualInputDevice() {
    // A virtual device has a negative id.
    InputDevice inputDevice = createInputDevice(-10);
    assertThat(inputDevice.isVirtual()).isTrue();
    shadowOf(inputManager).addInputDevice(inputDevice);
    assertThat(inputManager.getInputDevice(-10)).isNotNull();
    assertThat(inputManager.getInputDeviceIds()).asList().containsExactly(-10);
  }

  @Test
  public void addInputDevice_withKeyCodes() {
    // A virtual device has a negative id.
    InputDevice inputDevice = createInputDevice(-10);
    assertThat(inputDevice.isVirtual()).isTrue();
    shadowOf(inputManager).addInputDevice(inputDevice);
    shadowOf(inputManager).addInputDeviceKeys(inputDevice.getId(), new int[] {100, 101});
    assertThat(inputDevice.hasKeys(100, 101, 102)).isEqualTo(new boolean[] {true, true, false});
  }

  @Test
  public void removeInputDevice() {
    InputDevice inputDevice = createInputDevice(1);
    shadowOf(inputManager).addInputDevice(inputDevice);
    assertThat(inputManager.getInputDevice(1)).isNotNull();

    shadowOf(inputManager).removeInputDevice(1);
    assertThat(inputManager.getInputDevice(1)).isNull();
  }

  private static InputDevice createInputDevice(int inputDeviceId) {
    if (RuntimeEnvironment.getApiLevel() >= UPSIDE_DOWN_CAKE) {
      // Use the Robolectric InputDeviceBuilder for U+.
      return InputDeviceBuilder.newBuilder().setId(inputDeviceId).build();
    } else {
      InputDevice inputDevice = Shadow.newInstanceOf(InputDevice.class);
      ReflectionHelpers.setField(inputDevice, "mId", inputDeviceId);
      return inputDevice;
    }
  }
}
