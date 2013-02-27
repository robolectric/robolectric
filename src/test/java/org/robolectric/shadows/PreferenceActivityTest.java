package org.robolectric.shadows;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertNotNull;
import static org.fest.assertions.api.Assertions.assertThat;

import org.robolectric.TestRunners;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.app.Activity;
import android.preference.PreferenceActivity;
import org.robolectric.R;
import org.robolectric.Robolectric;

@RunWith(TestRunners.WithDefaults.class)
public class PreferenceActivityTest {

    private TestPreferenceActivity activity;
    private ShadowPreferenceActivity shadow;

    @Before
    public void setUp() throws Exception {
        activity = new TestPreferenceActivity();
        shadow = Robolectric.shadowOf(activity);
    }

    @Test
    public void shouldInitializeListViewInOnCreate() {
        shadow.callOnCreate(null);
        assertThat(activity.getListView()).isNotNull();
    }

    @Test
    public void shouldInheritFromListActivity() {
        assertThat(shadow).isInstanceOf(ShadowListActivity.class);
    }

    @Test
    public void shouldNotInitializePreferenceScreen() {
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
