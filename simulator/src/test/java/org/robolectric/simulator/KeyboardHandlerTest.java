package org.robolectric.simulator;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Shadows.shadowOf;

import android.app.Activity;
import android.os.Looper;
import android.widget.EditText;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.awt.Component;
import java.awt.event.KeyEvent;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;

@RunWith(AndroidJUnit4.class)
public class KeyboardHandlerTest {
  private KeyboardHandler keyboardHandler;
  private EditText editText;

  @Before
  public void setUp() {
    Activity activity = Robolectric.setupActivity(Activity.class);
    editText = new EditText(activity);
    activity.setContentView(editText);
    editText.requestFocus();
    keyboardHandler = new KeyboardHandler();
  }

  @Test
  public void keyPressed_injectsEvent() {
    KeyEvent awtEvent =
        new KeyEvent(
            new DummyComponent(),
            KeyEvent.KEY_PRESSED,
            System.currentTimeMillis(),
            0,
            KeyEvent.VK_A,
            'a');

    keyboardHandler.keyPressed(awtEvent);
    shadowOf(Looper.getMainLooper()).idle();

    KeyEvent awtEventUp =
        new KeyEvent(
            new DummyComponent(),
            KeyEvent.KEY_RELEASED,
            System.currentTimeMillis(),
            0,
            KeyEvent.VK_A,
            'a');
    keyboardHandler.keyReleased(awtEventUp);
    shadowOf(Looper.getMainLooper()).idle();

    assertThat(editText.getText().toString()).isEqualTo("a");
  }

  @Test
  public void shiftKeyPressed_injectsShiftEvent() {
    KeyEvent shiftEvent =
        new KeyEvent(
            new DummyComponent(),
            KeyEvent.KEY_PRESSED,
            System.currentTimeMillis(),
            0,
            KeyEvent.VK_SHIFT,
            KeyEvent.CHAR_UNDEFINED);
    keyboardHandler.keyPressed(shiftEvent);
    shadowOf(Looper.getMainLooper()).idle();

    KeyEvent aEvent =
        new KeyEvent(
            new DummyComponent(),
            KeyEvent.KEY_PRESSED,
            System.currentTimeMillis(),
            0,
            KeyEvent.VK_A,
            'A');
    keyboardHandler.keyPressed(aEvent);
    shadowOf(Looper.getMainLooper()).idle();

    KeyEvent aEventUp =
        new KeyEvent(
            new DummyComponent(),
            KeyEvent.KEY_RELEASED,
            System.currentTimeMillis(),
            0,
            KeyEvent.VK_A,
            'A');
    keyboardHandler.keyReleased(aEventUp);
    shadowOf(Looper.getMainLooper()).idle();

    assertThat(editText.getText().toString()).isEqualTo("A");
  }

  private static class DummyComponent extends Component {}
}
