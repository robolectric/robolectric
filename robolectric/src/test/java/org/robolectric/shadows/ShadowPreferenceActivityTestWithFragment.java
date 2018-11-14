package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.Robolectric;

/**
 * Current Android examples show adding a PreferenceFragment as part of the hosting Activity
 * lifecycle. This resulted in a null pointer exception when trying to access a Context while
 * inflating the Preference objects defined in xml. This class tests that path.
 */
@RunWith(AndroidJUnit4.class)
public class ShadowPreferenceActivityTestWithFragment {
  private TestPreferenceActivity activity = Robolectric.setupActivity(TestPreferenceActivity.class);
  private TestPreferenceFragment fragment;
  private static final String FRAGMENT_TAG = "fragmentPreferenceTag";

  @Before
  public void before() {
    this.fragment = (TestPreferenceFragment) this.activity.getFragmentManager().findFragmentByTag(FRAGMENT_TAG);
  }

  @Test
  public void fragmentIsNotNull() {
    assertThat(this.fragment).isNotNull();
  }

  @Test
  public void preferenceAddedWithCorrectDetails() {
    Preference preference = fragment.findPreference("edit_text");
    assertThat(preference).isNotNull();
    assertThat(preference.getTitle()).isEqualTo("EditText Test");
    assertThat(preference.getSummary()).isEqualTo("");
  }

  private static class TestPreferenceActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      FragmentManager fragmentManager = this.getFragmentManager();
      TestPreferenceFragment fragment = new TestPreferenceFragment();
      fragmentManager.beginTransaction().replace(android.R.id.content, fragment, FRAGMENT_TAG).commit();
    }
  }

  public static class TestPreferenceFragment extends PreferenceFragment {
    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      this.addPreferencesFromResource(R.xml.preferences);
    }
  }
}
