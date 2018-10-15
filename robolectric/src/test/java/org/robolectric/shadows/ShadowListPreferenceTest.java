package org.robolectric.shadows;

import static com.google.common.truth.Truth.assertThat;
import static org.robolectric.Robolectric.buildActivity;

import android.app.Activity;
import android.preference.ListPreference;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;

@RunWith(AndroidJUnit4.class)
public class ShadowListPreferenceTest {

  private ListPreference listPreference;

  @Before
  public void setUp() throws Exception {
    listPreference = new ListPreference(buildActivity(Activity.class).create().get());
  }

  @Test
  public void shouldHaveEntries() {
    CharSequence[] entries = { "this", "is", "only", "a", "test" };

    assertThat(listPreference.getEntries()).isNull();
    listPreference.setEntries(entries);
    assertThat(listPreference.getEntries()).isSameAs(entries);
  }

  @Test
  public void shouldSetEntriesByResourceId() {
    assertThat(listPreference.getEntries()).isNull();
    listPreference.setEntries(R.array.greetings);
    assertThat(listPreference.getEntries()).isNotNull();
  }

  @Test
  public void shouldHaveEntryValues() {
    CharSequence[] entryValues = { "this", "is", "only", "a", "test" };

    assertThat(listPreference.getEntryValues()).isNull();
    listPreference.setEntryValues(entryValues);
    assertThat(listPreference.getEntryValues()).isSameAs(entryValues);
  }

  @Test
  public void shouldSetEntryValuesByResourceId() {
    assertThat(listPreference.getEntryValues()).isNull();
    listPreference.setEntryValues(R.array.greetings);
    assertThat(listPreference.getEntryValues()).isNotNull();
  }

  @Test
  public void shouldSetValue() {
    assertThat(listPreference.getValue()).isNull();
    listPreference.setValue("testing");
    assertThat(listPreference.getValue()).isEqualTo("testing");
  }
}
