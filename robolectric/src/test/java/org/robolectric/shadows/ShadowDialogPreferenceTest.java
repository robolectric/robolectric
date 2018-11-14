package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.os.Bundle;
import android.preference.DialogPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.Robolectric;

@RunWith(AndroidJUnit4.class)
public class ShadowDialogPreferenceTest {

  @Test
  public void inflate_shouldCreateDialogPreference() {
    final PreferenceScreen screen = inflatePreferenceActivity();
    final DialogPreference preference = (DialogPreference) screen.findPreference("dialog");

    assertThat(preference.getTitle()).isEqualTo("Dialog Preference");
    assertThat(preference.getSummary()).isEqualTo("This is the dialog summary");
    assertThat(preference.getDialogMessage()).isEqualTo("This is the dialog message");
    assertThat(preference.getPositiveButtonText()).isEqualTo("YES");
    assertThat(preference.getNegativeButtonText()).isEqualTo("NO");
  }

  private PreferenceScreen inflatePreferenceActivity() {
    PreferenceActivity activity = Robolectric.setupActivity(TestPreferenceActivity.class);
    return activity.getPreferenceScreen();
  }

  @SuppressWarnings("FragmentInjection")
  private static class TestPreferenceActivity extends PreferenceActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      addPreferencesFromResource(R.xml.dialog_preferences);
    }
  }
}
