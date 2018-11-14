package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.app.Application;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.widget.EditText;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowKeyCharacterMapTest {
  private final KeyCharacterMap keyMap = ShadowKeyCharacterMap.load(0);

  @Test
  public void dispatchKeyEvent_shouldSetText() throws Exception {
    EditText editText = new EditText((Application) ApplicationProvider.getApplicationContext());
    editText.requestFocus();

    for (KeyEvent evt : keyMap.getEvents("string".toCharArray())) {
      editText.dispatchKeyEvent(evt);
    }

    Thread.sleep(500);
    ShadowLooper.runUiThreadTasksIncludingDelayedTasks();

    assertThat(editText.getText().toString()).isEqualTo("string");
  }
}
