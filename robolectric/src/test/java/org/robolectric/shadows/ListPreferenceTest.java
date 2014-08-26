package org.robolectric.shadows;

import android.app.Activity;
import android.preference.ListPreference;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.R;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.robolectric.Robolectric.buildActivity;

@RunWith(TestRunners.WithDefaults.class)
public class ListPreferenceTest {

  private ListPreference listPreference;
  private ShadowListPreference shadow;

  @Before
  public void setUp() throws Exception {
    listPreference = new ListPreference(buildActivity(Activity.class).create().get());
    shadow = Robolectric.shadowOf(listPreference);
  }

  @Test
  public void shouldInheritFromDialogPreference() {
    assertThat(shadow).isInstanceOf(ShadowDialogPreference.class);
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
