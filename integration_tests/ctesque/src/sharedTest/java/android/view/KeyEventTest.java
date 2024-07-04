package android.view;

import static org.junit.Assert.assertEquals;
import static org.junit.Assume.assumeTrue;

import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test {@link KeyEventTest}.
 *
 * <p>Inspired from Android cts/tests/tests/view/src/android/view/cts/KeyEventTest.java
 */
@RunWith(AndroidJUnit4.class)
public final class KeyEventTest {

  @Test
  public void testKeyCodeFromString() {
    assertEquals(KeyEvent.KEYCODE_A, KeyEvent.keyCodeFromString("KEYCODE_A"));
    assertEquals(
        KeyEvent.KEYCODE_A, KeyEvent.keyCodeFromString(Integer.toString(KeyEvent.KEYCODE_A)));
    assertEquals(KeyEvent.KEYCODE_UNKNOWN, KeyEvent.keyCodeFromString("keycode_a"));
    assertEquals(KeyEvent.KEYCODE_UNKNOWN, KeyEvent.keyCodeFromString("a"));
    assertEquals(0, KeyEvent.keyCodeFromString("0"));
    assertEquals(1, KeyEvent.keyCodeFromString("1"));
    assertEquals(KeyEvent.KEYCODE_HOME, KeyEvent.keyCodeFromString("3"));
    assertEquals(
        KeyEvent.KEYCODE_POWER,
        KeyEvent.keyCodeFromString(Integer.toString(KeyEvent.KEYCODE_POWER)));
    assertEquals(
        KeyEvent.KEYCODE_MENU, KeyEvent.keyCodeFromString(Integer.toString(KeyEvent.KEYCODE_MENU)));
    assertEquals(KeyEvent.KEYCODE_UNKNOWN, KeyEvent.keyCodeFromString("back"));

    assertEquals(
        KeyEvent.KEYCODE_UNKNOWN, KeyEvent.keyCodeFromString("KEYCODE_NOT_A_REAL_KEYCODE"));
    assertEquals(KeyEvent.KEYCODE_UNKNOWN, KeyEvent.keyCodeFromString("NOT_A_REAL_KEYCODE"));
    assertEquals(KeyEvent.KEYCODE_UNKNOWN, KeyEvent.keyCodeFromString("KEYCODE"));
    assertEquals(KeyEvent.KEYCODE_UNKNOWN, KeyEvent.keyCodeFromString("KEYCODE_"));
    assertEquals(KeyEvent.KEYCODE_UNKNOWN, KeyEvent.keyCodeFromString(""));
    assertEquals(
        KeyEvent.getMaxKeyCode(),
        KeyEvent.keyCodeFromString(Integer.toString(KeyEvent.getMaxKeyCode())));
  }

  /**
   * Verify the "starting in {@link android.os.Build.VERSION_CODES#Q} the prefix "KEYCODE_" is
   * optional." statement in {@link KeyEvent#keyCodeFromString(String)} reference docs.
   */
  @Test
  public void testKeyCodeFromString_prefixOptionalFromQ() {
    assumeTrue(VERSION.SDK_INT >= VERSION_CODES.Q);
    assertEquals(KeyEvent.KEYCODE_A, KeyEvent.keyCodeFromString("A"));

    assertEquals(KeyEvent.KEYCODE_POWER, KeyEvent.keyCodeFromString("POWER"));
  }
}
