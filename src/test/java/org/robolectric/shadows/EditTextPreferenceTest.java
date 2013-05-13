package org.robolectric.shadows;

import android.app.Activity;
import android.content.Context;
import android.preference.EditTextPreference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import static junit.framework.Assert.assertNotNull;
import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(TestRunners.WithDefaults.class)
public class EditTextPreferenceTest {

  private static final String SOME_TEXT = "some text";
  private EditTextPreference preference;

  private Context context;

  @Before
  public void setup() {
    context = new Activity();
    preference = new EditTextPreference(context);
  }

  @Test
  public void testConstructor() {
    preference = new EditTextPreference(context);
    assertNotNull(preference.getEditText());
  }

  @Test
  public void testSetText() {
    preference.setText(SOME_TEXT);
    assertThat((String) preference.getEditText().getText().toString()).isEqualTo(SOME_TEXT);
  }

}
