package org.robolectric.shadows;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;
import org.robolectric.tester.android.content.TestSharedPreferences;

import java.util.Map;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

@RunWith(TestRunners.WithDefaults.class)
public class PreferenceManagerTest {

  @Test
  public void shouldProvideDefaultSharedPreferences() {
    Map<String, Map<String, Object>> content = Robolectric.getShadowApplication().getSharedPreferenceMap();

    TestSharedPreferences testPrefs = new TestSharedPreferences(content, "__default__", Context.MODE_PRIVATE);
    Editor editor = testPrefs.edit();
    editor.putInt("foobar", 13);
    editor.commit();

    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Robolectric.application);

    assertNotNull(prefs);
    assertThat(prefs.getInt("foobar", 0)).isEqualTo(13);
  }

  @Test
  public void shouldReturnTheSameInstanceEachTime() {
    SharedPreferences prefs1 = PreferenceManager.getDefaultSharedPreferences(Robolectric.application);
    SharedPreferences prefs2 = PreferenceManager.getDefaultSharedPreferences(Robolectric.application);

    assertThat(prefs1).isSameAs(prefs2);
  }
}
