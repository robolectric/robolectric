package org.robolectric.shadows;

import static org.fest.assertions.api.Assertions.assertThat;

import android.app.Activity;
import android.app.FragmentManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

/**
 * Current Android examples show adding a PreferenceFragment as part of the
 * hosting Activity lifecycle. This resulted in a null pointer exception when
 * trying to access a Context while inflating the Preference objects defined in
 * xml. This class tests that path.
 */
@RunWith(TestRunners.WithDefaults.class)
public class PreferenceActivityTestWithFragment {

  private TestPreferenceActivity activity;
  private TestPreferenceFragment fragment;
  private static final String FRAGMENT_TAG = "fragmentPreferenceTag";

  @Before
  public void before() {
    this.activity = Robolectric.buildActivity(TestPreferenceActivity.class)
        .create()
        .start()
        .resume()
        .visible()
        .get();
    this.fragment = (TestPreferenceFragment)
        this.activity.getFragmentManager().findFragmentByTag(FRAGMENT_TAG);
  }

  @Test
  public void fragmentIsNotNull() {
    // Make sure the activity can instantiate the fragment without getting
    // errors.
    assertThat(this.fragment).isNotNull();
  }

  @Test
  public void preferenceAddedWithCorrectDetails() {
    // Make sure we can find one of the preferences
    Preference preference = this.fragment.findPreference("edit_text");
    assertThat(preference).isNotNull();
    assertThat(preference.getTitle()).isEqualTo("EditText Test");
    assertThat(preference.getSummary()).isEqualTo("");
  }

  private static class TestPreferenceActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      // Add the fragment as part of the activity's life cycle.
      FragmentManager fragmentManager = this.getFragmentManager();
      TestPreferenceFragment fragment = new TestPreferenceFragment();
      fragmentManager.beginTransaction().replace(
          android.R.id.content,
          fragment,
          FRAGMENT_TAG)
      .commit();
    }

  }

  private static class TestPreferenceFragment extends PreferenceFragment {

    @Override
    public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      this.addPreferencesFromResource(R.xml.preferences);
    }
  }

}
