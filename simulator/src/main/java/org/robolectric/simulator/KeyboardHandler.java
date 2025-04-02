package org.robolectric.simulator;

import android.os.Handler;
import android.os.Looper;
import com.google.common.collect.ImmutableMap;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import org.robolectric.shadows.ShadowUiAutomation;

/** A {@link KeyListener} that forwards KeyEvents to Robolectric {@link ShadowUiAutomation}. */
public final class KeyboardHandler implements KeyListener {

  private final Handler handler = new Handler(Looper.getMainLooper());

  // Map of AWT KeyEvent constants to Android KeyEvent constants
  private static final ImmutableMap<Integer, Integer> KEY_MAP;

  // Whether the shift key is being pressed.
  private boolean shiftKeyPressed;

  static {
    KEY_MAP =
        ImmutableMap.<Integer, Integer>builder()
            .put(KeyEvent.VK_A, android.view.KeyEvent.KEYCODE_A)
            .put(KeyEvent.VK_B, android.view.KeyEvent.KEYCODE_B)
            .put(KeyEvent.VK_C, android.view.KeyEvent.KEYCODE_C)
            .put(KeyEvent.VK_D, android.view.KeyEvent.KEYCODE_D)
            .put(KeyEvent.VK_E, android.view.KeyEvent.KEYCODE_E)
            .put(KeyEvent.VK_F, android.view.KeyEvent.KEYCODE_F)
            .put(KeyEvent.VK_G, android.view.KeyEvent.KEYCODE_G)
            .put(KeyEvent.VK_H, android.view.KeyEvent.KEYCODE_H)
            .put(KeyEvent.VK_I, android.view.KeyEvent.KEYCODE_I)
            .put(KeyEvent.VK_J, android.view.KeyEvent.KEYCODE_J)
            .put(KeyEvent.VK_K, android.view.KeyEvent.KEYCODE_K)
            .put(KeyEvent.VK_L, android.view.KeyEvent.KEYCODE_L)
            .put(KeyEvent.VK_M, android.view.KeyEvent.KEYCODE_M)
            .put(KeyEvent.VK_N, android.view.KeyEvent.KEYCODE_N)
            .put(KeyEvent.VK_O, android.view.KeyEvent.KEYCODE_O)
            .put(KeyEvent.VK_P, android.view.KeyEvent.KEYCODE_P)
            .put(KeyEvent.VK_Q, android.view.KeyEvent.KEYCODE_Q)
            .put(KeyEvent.VK_R, android.view.KeyEvent.KEYCODE_R)
            .put(KeyEvent.VK_S, android.view.KeyEvent.KEYCODE_S)
            .put(KeyEvent.VK_T, android.view.KeyEvent.KEYCODE_T)
            .put(KeyEvent.VK_U, android.view.KeyEvent.KEYCODE_U)
            .put(KeyEvent.VK_V, android.view.KeyEvent.KEYCODE_V)
            .put(KeyEvent.VK_W, android.view.KeyEvent.KEYCODE_W)
            .put(KeyEvent.VK_X, android.view.KeyEvent.KEYCODE_X)
            .put(KeyEvent.VK_Y, android.view.KeyEvent.KEYCODE_Y)
            .put(KeyEvent.VK_Z, android.view.KeyEvent.KEYCODE_Z)
            .put(KeyEvent.VK_0, android.view.KeyEvent.KEYCODE_0)
            .put(KeyEvent.VK_1, android.view.KeyEvent.KEYCODE_1)
            .put(KeyEvent.VK_2, android.view.KeyEvent.KEYCODE_2)
            .put(KeyEvent.VK_3, android.view.KeyEvent.KEYCODE_3)
            .put(KeyEvent.VK_4, android.view.KeyEvent.KEYCODE_4)
            .put(KeyEvent.VK_5, android.view.KeyEvent.KEYCODE_5)
            .put(KeyEvent.VK_6, android.view.KeyEvent.KEYCODE_6)
            .put(KeyEvent.VK_7, android.view.KeyEvent.KEYCODE_7)
            .put(KeyEvent.VK_8, android.view.KeyEvent.KEYCODE_8)
            .put(KeyEvent.VK_9, android.view.KeyEvent.KEYCODE_9)
            .put(KeyEvent.VK_ENTER, android.view.KeyEvent.KEYCODE_ENTER)
            .put(KeyEvent.VK_TAB, android.view.KeyEvent.KEYCODE_TAB)
            .put(KeyEvent.VK_SPACE, android.view.KeyEvent.KEYCODE_SPACE)
            .put(KeyEvent.VK_LEFT, android.view.KeyEvent.KEYCODE_DPAD_LEFT)
            .put(KeyEvent.VK_RIGHT, android.view.KeyEvent.KEYCODE_DPAD_RIGHT)
            .put(KeyEvent.VK_UP, android.view.KeyEvent.KEYCODE_DPAD_UP)
            .put(KeyEvent.VK_DOWN, android.view.KeyEvent.KEYCODE_DPAD_DOWN)
            .put(KeyEvent.VK_HOME, android.view.KeyEvent.KEYCODE_HOME)
            .put(KeyEvent.VK_END, android.view.KeyEvent.KEYCODE_ENDCALL)
            .put(KeyEvent.VK_INSERT, android.view.KeyEvent.KEYCODE_INSERT)
            .put(KeyEvent.VK_BACK_SPACE, android.view.KeyEvent.KEYCODE_DEL)
            .put(KeyEvent.VK_DELETE, android.view.KeyEvent.KEYCODE_FORWARD_DEL)
            .put(KeyEvent.VK_PAGE_UP, android.view.KeyEvent.KEYCODE_PAGE_UP)
            .put(KeyEvent.VK_PAGE_DOWN, android.view.KeyEvent.KEYCODE_PAGE_DOWN)
            .put(KeyEvent.VK_F1, android.view.KeyEvent.KEYCODE_F1)
            .put(KeyEvent.VK_F2, android.view.KeyEvent.KEYCODE_F2)
            .put(KeyEvent.VK_F3, android.view.KeyEvent.KEYCODE_F3)
            .put(KeyEvent.VK_F4, android.view.KeyEvent.KEYCODE_F4)
            .put(KeyEvent.VK_F5, android.view.KeyEvent.KEYCODE_F5)
            .put(KeyEvent.VK_F6, android.view.KeyEvent.KEYCODE_F6)
            .put(KeyEvent.VK_F7, android.view.KeyEvent.KEYCODE_F7)
            .put(KeyEvent.VK_F8, android.view.KeyEvent.KEYCODE_F8)
            .put(KeyEvent.VK_F9, android.view.KeyEvent.KEYCODE_F9)
            .put(KeyEvent.VK_F10, android.view.KeyEvent.KEYCODE_F10)
            .put(KeyEvent.VK_F11, android.view.KeyEvent.KEYCODE_F11)
            .put(KeyEvent.VK_F12, android.view.KeyEvent.KEYCODE_F12)
            .put(KeyEvent.VK_ESCAPE, android.view.KeyEvent.KEYCODE_ESCAPE)
            .put(KeyEvent.VK_BACK_QUOTE, android.view.KeyEvent.KEYCODE_GRAVE)
            .put(KeyEvent.VK_MINUS, android.view.KeyEvent.KEYCODE_MINUS)
            .put(KeyEvent.VK_EQUALS, android.view.KeyEvent.KEYCODE_EQUALS)
            .put(KeyEvent.VK_OPEN_BRACKET, android.view.KeyEvent.KEYCODE_LEFT_BRACKET)
            .put(KeyEvent.VK_CLOSE_BRACKET, android.view.KeyEvent.KEYCODE_RIGHT_BRACKET)
            .put(KeyEvent.VK_BACK_SLASH, android.view.KeyEvent.KEYCODE_BACKSLASH)
            .put(KeyEvent.VK_SEMICOLON, android.view.KeyEvent.KEYCODE_SEMICOLON)
            .put(KeyEvent.VK_QUOTE, android.view.KeyEvent.KEYCODE_APOSTROPHE)
            .put(KeyEvent.VK_COMMA, android.view.KeyEvent.KEYCODE_COMMA)
            .put(KeyEvent.VK_PERIOD, android.view.KeyEvent.KEYCODE_PERIOD)
            .put(KeyEvent.VK_SLASH, android.view.KeyEvent.KEYCODE_SLASH)
            .put(KeyEvent.VK_CAPS_LOCK, android.view.KeyEvent.KEYCODE_CAPS_LOCK)
            .put(KeyEvent.VK_CONTROL, android.view.KeyEvent.KEYCODE_CTRL_LEFT)
            .put(KeyEvent.VK_ALT, android.view.KeyEvent.KEYCODE_ALT_LEFT)
            .put(KeyEvent.VK_META, android.view.KeyEvent.KEYCODE_META_LEFT)
            .buildOrThrow();
  }

  public KeyboardHandler() {}

  @Override
  public void keyTyped(KeyEvent e) {
    // No-op
  }

  @Override
  public void keyPressed(KeyEvent e) {
    if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
      shiftKeyPressed = true;
      return;
    }
    handler.post(
        () -> {
          if (!KEY_MAP.containsKey(e.getKeyCode())) {
            System.err.println("Unknown mapping for AWT key " + e.getKeyCode());
          } else {
            if (shiftKeyPressed) {
              ShadowUiAutomation.injectInputEvent(
                  newAndroidKeyEvent(
                      android.view.KeyEvent.ACTION_DOWN,
                      android.view.KeyEvent.KEYCODE_SHIFT_LEFT,
                      android.view.KeyEvent.META_SHIFT_ON
                          | android.view.KeyEvent.META_SHIFT_LEFT_ON));
            }

            ShadowUiAutomation.injectInputEvent(
                newAndroidKeyEvent(
                    android.view.KeyEvent.ACTION_DOWN,
                    KEY_MAP.get(e.getKeyCode()),
                    shiftKeyPressed
                        ? android.view.KeyEvent.META_SHIFT_ON
                            | android.view.KeyEvent.META_SHIFT_LEFT_ON
                        : 0));
          }
        });
  }

  @Override
  public void keyReleased(KeyEvent e) {
    if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
      shiftKeyPressed = false;
      return;
    }
    handler.post(
        () -> {
          if (!KEY_MAP.containsKey(e.getKeyCode())) {
            System.err.println("Unknown mapping for AWT key " + e.getKeyCode());
          } else {
            ShadowUiAutomation.injectInputEvent(
                newAndroidKeyEvent(
                    android.view.KeyEvent.ACTION_UP,
                    KEY_MAP.get(e.getKeyCode()),
                    shiftKeyPressed
                        ? android.view.KeyEvent.META_SHIFT_ON
                            | android.view.KeyEvent.META_SHIFT_LEFT_ON
                        : 0));
            if (shiftKeyPressed) {
              ShadowUiAutomation.injectInputEvent(
                  newAndroidKeyEvent(
                      android.view.KeyEvent.ACTION_UP,
                      android.view.KeyEvent.KEYCODE_SHIFT_LEFT,
                      0));
            }
          }
        });
  }

  private static android.view.KeyEvent newAndroidKeyEvent(int action, int code, int metaState) {
    return new android.view.KeyEvent(
        /* downTime= */ 0, /* eventTime= */ 0, action, code, /* repeat= */ 0, metaState);
  }
}
