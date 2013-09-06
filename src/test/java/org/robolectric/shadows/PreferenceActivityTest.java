package org.robolectric.shadows;

import android.preference.PreferenceActivity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

@RunWith(TestRunners.WithDefaults.class)
public class PreferenceActivityTest {

  private TestPreferenceActivity activity;
  private ShadowPreferenceActivity shadow;

  @Before
  public void setUp() throws Exception {
    activity = Robolectric.buildActivity(TestPreferenceActivity.class).create().get();
    shadow = Robolectric.shadowOf(activity);
  }

  @Test
  public void shouldInitializeListViewInOnCreate() {
    assertThat(activity.getListView()).isNotNull();
  }

  @Test
  public void shouldNotInitializePreferenceScreen() {
    TestPreferenceActivity activity = Robolectric.buildActivity(TestPreferenceActivity.class).get();
    assertThat(activity.getPreferenceScreen()).isNull();
  }

  @Test
  public void shouldRecordPreferencesResourceId() {
    assertThat(shadow.getPreferencesResId()).isEqualTo(-1);
    activity.addPreferencesFromResource(R.xml.preferences);
    assertThat(shadow.getPreferencesResId()).isEqualTo(R.xml.preferences);
  }

  @Test
  public void shouldLoadPreferenceScreen() {
    activity.addPreferencesFromResource(R.xml.preferences);
    assertThat(activity.getPreferenceScreen().getPreferenceCount()).isEqualTo(7);
  }

  @Test
  public void shouldFindPreferences() {
    activity.addPreferencesFromResource(R.xml.preferences);
    assertNotNull(activity.findPreference("category"));
    assertNotNull(activity.findPreference("inside_category"));
    assertNotNull(activity.findPreference("screen"));
    assertNotNull(activity.findPreference("inside_screen"));
    assertNotNull(activity.findPreference("checkbox"));
    assertNotNull(activity.findPreference("edit_text"));
    assertNotNull(activity.findPreference("list"));
    assertNotNull(activity.findPreference("preference"));
    assertNotNull(activity.findPreference("ringtone"));
  }

  private static class TestPreferenceActivity extends PreferenceActivity {
  }
}
