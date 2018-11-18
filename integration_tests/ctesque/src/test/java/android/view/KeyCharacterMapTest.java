package android.view;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertNotNull;

import androidx.test.runner.AndroidJUnit4;
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

  @Test(expected = IllegalArgumentException.class)
  public void testGetMatchNull() {
    keyCharacterMap.getMatch(KeyEvent.KEYCODE_0, null);
  }

  private int getCharacterKeyCode(char oneChar) {
    // Lowercase the character to avoid getting modifiers in the KeyEvent array.
    char[] chars = new char[] {Character.toLowerCase(oneChar)};
    KeyEvent[] events = keyCharacterMap.getEvents(chars);
    return events[0].getKeyCode();
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetMatchMetaStateNull() {
    keyCharacterMap.getMatch(KeyEvent.KEYCODE_0, null, 1);
  }

  @Test
  public void testGetKeyboardType() {
    assertThat(keyCharacterMap.getKeyboardType()).isEqualTo(KeyCharacterMap.FULL);
  }

  @Test(expected = IllegalArgumentException.class)
  public void testGetEventsNull() {
    keyCharacterMap.getEvents(null);
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
}
