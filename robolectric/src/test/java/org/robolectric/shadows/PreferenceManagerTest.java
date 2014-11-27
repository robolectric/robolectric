package org.robolectric.shadows;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.TestRunners;
import android.content.TestSharedPreferences;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

@RunWith(TestRunners.WithDefaults.class)
public class PreferenceManagerTest {

  @Test
  public void shouldProvideDefaultSharedPreferences() {
    Map<String, Map<String, Object>> content = ShadowApplication.getInstance().getSharedPreferenceMap();

    TestSharedPreferences testPrefs = new TestSharedPreferences(content, "__default__", Context.MODE_PRIVATE);
    Editor editor = testPrefs.edit();
    editor.putInt("foobar", 13);
    editor.commit();

    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application);

    assertNotNull(prefs);
    assertThat(prefs.getInt("foobar", 0)).isEqualTo(13);
  }

  @Test
  public void shouldReturnTheSameInstanceEachTime() {
    SharedPreferences prefs1 = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application);
    SharedPreferences prefs2 = PreferenceManager.getDefaultSharedPreferences(RuntimeEnvironment.application);

    assertThat(prefs1).isSameAs(prefs2);
  }
}
