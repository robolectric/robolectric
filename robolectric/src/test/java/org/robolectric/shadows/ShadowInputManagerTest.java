package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.content.Context;
import android.hardware.input.InputManager;
import android.hardware.input.InputManager.InputDeviceListener;
import android.os.Handler;
import android.os.Looper;
import android.view.InputDevice;
import android.view.MotionEvent;
import android.view.VerifiedMotionEvent;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;

/** Unit tests for {@link ShadowInputManager}. */
@RunWith(AndroidJUnit4.class)
public class ShadowInputManagerTest {
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
