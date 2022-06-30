package android.view;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThrows;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.internal.DoNotInstrument;

/**
 * Test {@link android.view.KeyCharacterMap}.
 *
 * <p>Inspired from Android cts/tests/tests/view/src/android/view/cts/KeyCharacterMap.java
 */
@DoNotInstrument
@RunWith(AndroidJUnit4.class)
public final class KeyCharacterMapTest {

  private KeyCharacterMap keyCharacterMap;

  @Before
  public void setup() {
    keyCharacterMap = KeyCharacterMap.load(KeyCharacterMap.VIRTUAL_KEYBOARD);
  }

  @Test
  public void testLoad() {
    keyCharacterMap = null;
    keyCharacterMap = KeyCharacterMap.load(KeyCharacterMap.BUILT_IN_KEYBOARD);
    assertNotNull(keyCharacterMap);
  }

  @Test
  public void testGetMatchNull() {
    assertThrows(
        IllegalArgumentException.class, () -> keyCharacterMap.getMatch(KeyEvent.KEYCODE_0, null));
  }

  @Test
  public void testGetMatchMetaStateNull() {
    assertThrows(
        IllegalArgumentException.class,
        () -> keyCharacterMap.getMatch(KeyEvent.KEYCODE_0, null, 1));
  }

  @Test
  public void testGetKeyboardType() {
    assertThat(keyCharacterMap.getKeyboardType()).isEqualTo(KeyCharacterMap.FULL);
  }

  @Test
  public void testGetEventsNull() {
    assertThrows(IllegalArgumentException.class, () -> keyCharacterMap.getEvents(null));
  }

  @Test
  public void testGetEventsLowerCase() {
    KeyEvent[] events = keyCharacterMap.getEvents("test".toCharArray());

    assertThat(events[0].getAction()).isEqualTo(KeyEvent.ACTION_DOWN);
    assertThat(events[0].getKeyCode()).isEqualTo(KeyEvent.KEYCODE_T);
    assertThat(events[1].getAction()).isEqualTo(KeyEvent.ACTION_UP);
    assertThat(events[1].getKeyCode()).isEqualTo(KeyEvent.KEYCODE_T);

    assertThat(events[2].getAction()).isEqualTo(KeyEvent.ACTION_DOWN);
    assertThat(events[2].getKeyCode()).isEqualTo(KeyEvent.KEYCODE_E);
    assertThat(events[3].getAction()).isEqualTo(KeyEvent.ACTION_UP);
    assertThat(events[3].getKeyCode()).isEqualTo(KeyEvent.KEYCODE_E);

    assertThat(events[4].getAction()).isEqualTo(KeyEvent.ACTION_DOWN);
    assertThat(events[4].getKeyCode()).isEqualTo(KeyEvent.KEYCODE_S);
    assertThat(events[5].getAction()).isEqualTo(KeyEvent.ACTION_UP);
    assertThat(events[5].getKeyCode()).isEqualTo(KeyEvent.KEYCODE_S);

    assertThat(events[6].getAction()).isEqualTo(KeyEvent.ACTION_DOWN);
    assertThat(events[6].getKeyCode()).isEqualTo(KeyEvent.KEYCODE_T);
    assertThat(events[7].getAction()).isEqualTo(KeyEvent.ACTION_UP);
    assertThat(events[7].getKeyCode()).isEqualTo(KeyEvent.KEYCODE_T);
  }

  @Test
  public void testGetEventsCapital() {
    // Just assert that we got something back, there are many ways to return correct KeyEvents for
    // this sequence.
    assertThat(keyCharacterMap.getEvents("Test".toCharArray())).isNotEmpty();
  }

  @Test
  public void testUnknownCharacters() {
    assertThat(keyCharacterMap.get(KeyEvent.KEYCODE_UNKNOWN, 0)).isEqualTo(0);
    assertThat(keyCharacterMap.get(KeyEvent.KEYCODE_BACK, 0)).isEqualTo(0);
  }

  @Test
  public void testGetNumber() {
    assertThat(keyCharacterMap.getNumber(KeyEvent.KEYCODE_1)).isEqualTo('1');
  }

  @Test
  public void testGetDisplayLabel() {
    assertThat(keyCharacterMap.getDisplayLabel(KeyEvent.KEYCODE_W)).isEqualTo('W');
  }

  @Test
  public void testIsPrintingKey() {
    assertThat(keyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_W)).isTrue();
    assertThat(keyCharacterMap.isPrintingKey(KeyEvent.KEYCODE_ALT_LEFT)).isFalse();
  }
}
