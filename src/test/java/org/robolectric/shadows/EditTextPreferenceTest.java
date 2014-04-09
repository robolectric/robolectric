package org.robolectric.shadows;

import android.content.Context;
import android.preference.EditTextPreference;
import android.widget.EditText;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static junit.framework.Assert.*;
import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class EditTextPreferenceTest {

  private static final String SOME_TEXT = "some text";
  private EditTextPreference preference;

  private Context context;

  @Before
  public void setup() {
    context = Robolectric.application;
    preference = new EditTextPreference(context);
  }

  @Test
  public void testConstructor() {
    preference = new EditTextPreference(context);
    assertNotNull(preference.getEditText());
  }

  @Test
  public void setTextInEditTextShouldStoreText() {
    final EditText editText = preference.getEditText();
    editText.setText(SOME_TEXT);

    assertThat(editText.getText().toString()).isEqualTo(SOME_TEXT);
  }

  @Test
  public void setTextShouldStoreText() {
    preference.setText("some other text");
    assertThat(preference.getText()).isEqualTo("some other text");
  }

  @Test
  public void setTextShouldStoreNull() {
    preference.setText(null);
    assertNull(preference.getText());
  }
}
