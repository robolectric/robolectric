package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import android.content.Context;
import android.preference.EditTextPreference;
import android.widget.EditText;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class ShadowEditTextPreferenceTest {

  private static final String SOME_TEXT = "some text";
  private EditTextPreference preference;

  private Context context;

  @Before
  public void setup() {
    context = ApplicationProvider.getApplicationContext();
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
