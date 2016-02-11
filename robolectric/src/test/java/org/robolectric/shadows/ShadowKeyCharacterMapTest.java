package org.robolectric.shadows;

import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.widget.EditText;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.TestRunners;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(TestRunners.MultiApiWithDefaults.class)
public class ShadowKeyCharacterMapTest {
  private final KeyCharacterMap keyMap = ShadowKeyCharacterMap.load(0);

  @Test
  public void dispatchKeyEvent_shouldSetText() throws Exception {
    EditText editText = new EditText(RuntimeEnvironment.application);
    editText.requestFocus();

    for (KeyEvent evt : keyMap.getEvents("string".toCharArray())) {
      editText.dispatchKeyEvent(evt);
    }

    Thread.sleep(500);
    ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

    assertThat(editText.getText().toString()).isEqualTo("string");
  }
}
