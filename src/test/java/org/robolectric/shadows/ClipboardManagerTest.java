package org.robolectric.shadows;

import android.content.ClipboardManager;
import android.content.Context;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(TestRunners.WithDefaults.class)
public class ClipboardManagerTest {

  private ClipboardManager clipboardManager;

  @Before public void setUp() throws Exception {
    clipboardManager = (ClipboardManager) Robolectric.application.getSystemService(Context.CLIPBOARD_SERVICE);
  }

  @Test
  public void shouldStoreText() throws Exception {
    clipboardManager.setText("BLARG!!!");
    assertThat(clipboardManager.getText().toString()).isEqualTo("BLARG!!!");
  }

  @Test
  public void shouldNotHaveTextIfTextIsNull() throws Exception {
    clipboardManager.setText(null);
    assertFalse(clipboardManager.hasText());
  }

  @Test
  public void shouldNotHaveTextIfTextIsEmpty() throws Exception {
    clipboardManager.setText("");
    assertFalse(clipboardManager.hasText());
  }

  @Test
  public void shouldHaveTextIfEmptyString() throws Exception {
    clipboardManager.setText(" ");
    assertTrue(clipboardManager.hasText());
  }

  @Test
  public void shouldHaveTextIfString() throws Exception {
    clipboardManager.setText("BLARG");
    assertTrue(clipboardManager.hasText());
  }
}
