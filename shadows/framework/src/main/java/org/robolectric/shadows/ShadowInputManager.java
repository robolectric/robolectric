package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.R;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

import android.hardware.input.InputManager;
import android.view.InputDevice;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.VerifiedKeyEvent;
import android.view.VerifiedMotionEvent;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.ReflectionHelpers;

/** Shadow for {@link InputManager} */
@Implements(value = InputManager.class, looseSignatures = true)
public class ShadowInputManager {

  @Implementation
  protected boolean injectInputEvent(InputEvent event, int mode) {
    // ignore
    return true;
  }

  @Implementation(minSdk = KITKAT)
  protected boolean[] deviceHasKeys(int id, int[] keyCodes) {
    return new boolean[keyCodes.length];
  }

  /** Used in {@link InputDevice#getDeviceIds()} */
  @Implementation
  protected int[] getInputDeviceIds() {
    return new int[0];
  }

  /**
   * Provides a local java implementation, since the real implementation is in system server +
   * native code.
   */
  @Implementation(minSdk = R)
  protected Object verifyInputEvent(Object inputEvent) {
    if (inputEvent instanceof MotionEvent) {
      MotionEvent motionEvent = (MotionEvent) inputEvent;
      return new VerifiedMotionEvent(
          motionEvent.getDeviceId(),
          MILLISECONDS.toNanos(motionEvent.getEventTime()),
          motionEvent.getSource(),
          motionEvent.getDisplayId(),
          motionEvent.getRawX(),
          motionEvent.getRawY(),
          motionEvent.getActionMasked(),
          MILLISECONDS.toNanos(motionEvent.getDownTime()),
          motionEvent.getFlags(),
          motionEvent.getMetaState(),
          motionEvent.getButtonState());
    } else if (inputEvent instanceof KeyEvent) {
      KeyEvent keyEvent = (KeyEvent) inputEvent;
      return new VerifiedKeyEvent(
          keyEvent.getDeviceId(),
          MILLISECONDS.toNanos(keyEvent.getEventTime()),
          keyEvent.getSource(),
          keyEvent.getDisplayId(),
          keyEvent.getAction(),
          MILLISECONDS.toNanos(keyEvent.getDownTime()),
          keyEvent.getFlags(),
          keyEvent.getKeyCode(),
          keyEvent.getScanCode(),
          keyEvent.getMetaState(),
          keyEvent.getRepeatCount());
    } else {
      throw new IllegalArgumentException("unknown input event: " + inputEvent.getClass().getName());
    }
  }

  @Resetter
  public static void reset() {
    ReflectionHelpers.setStaticField(InputManager.class, "sInstance", null);
  }
}
