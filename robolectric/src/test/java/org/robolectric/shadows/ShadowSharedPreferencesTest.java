package org.robolectric.shadows;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyZeroInteractions;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ShadowSharedPreferencesTest {
  private final static String FILENAME = "filename";
  private SharedPreferences.Editor editor;
  private SharedPreferences sharedPreferences;

  private final Set<String> stringSet = new HashSet<>();

  private Context context;

  @Before
  public void setUp() {
    context = RuntimeEnvironment.application;

    sharedPreferences = context.getSharedPreferences(FILENAME, 3);
    // Ensure no shared preferences have leaked from previous tests.
    assertThat(sharedPreferences.getAll()).hasSize(0);

    editor = sharedPreferences.edit();
    editor.putBoolean("boolean", true);
    editor.putFloat("float", 1.1f);
    editor.putInt("int", 2);
    editor.putLong("long", 3L);
    editor.putString("string", "foobar");

    stringSet.add( "string1" );
    stringSet.add( "string2" );
    stringSet.add( "string3" );
    editor.putStringSet("stringSet", stringSet);
  }

  @Test
  public void commit_shouldStoreValues() throws Exception {
    editor.commit();

    SharedPreferences anotherSharedPreferences = context.getSharedPreferences(FILENAME, 3);
    assertTrue(anotherSharedPreferences.getBoolean("boolean", false));
    assertThat(anotherSharedPreferences.getFloat("float", 666f)).isEqualTo(1.1f);
    assertThat(anotherSharedPreferences.getInt("int", 666)).isEqualTo(2);
    assertThat(anotherSharedPreferences.getLong("long", 666L)).isEqualTo(3L);
    assertThat(anotherSharedPreferences.getString("string", "wacka wa")).isEqualTo("foobar");
    assertThat(anotherSharedPreferences.getStringSet("stringSet", null)).isEqualTo(stringSet);
  }

  @Test
  public void commit_shouldClearEditsThatNeedRemoveAndEditsThatNeedCommit() throws Exception {
    editor.commit();
    editor.remove("string").commit();

    assertThat(sharedPreferences.getString("string", "no value for key")).isEqualTo("no value for key");

    SharedPreferences anotherSharedPreferences = context.getSharedPreferences(FILENAME, 3);
    anotherSharedPreferences.edit().putString("string", "value for key").commit();

    editor.commit();
    assertThat(sharedPreferences.getString("string", "no value for key")).isEqualTo("value for key");
  }

  @Test
  public void getAll_shouldReturnAllValues() throws Exception {
    editor.commit();
    assertThat(sharedPreferences.getAll()).hasSize(6);
    assertThat(sharedPreferences.getAll().get("int")).isEqualTo(2);
  }

  @Test
  public void commit_shouldRemoveValuesThenSetValues() throws Exception {
    editor.putString("deleteMe", "foo").commit();

    editor.remove("deleteMe");

    editor.putString("dontDeleteMe", "baz");
    editor.remove("dontDeleteMe");

    editor.commit();

    SharedPreferences anotherSharedPreferences = context.getSharedPreferences(FILENAME, 3);
    assertThat(anotherSharedPreferences.getBoolean("boolean", false)).isTrue();
    assertThat(anotherSharedPreferences.getFloat("float", 666f)).isEqualTo(1.1f);
    assertThat(anotherSharedPreferences.getInt("int", 666)).isEqualTo(2);
    assertThat(anotherSharedPreferences.getLong("long", 666L)).isEqualTo(3L);
    assertThat(anotherSharedPreferences.getString("string", "wacka wa")).isEqualTo("foobar");

    assertThat(anotherSharedPreferences.getString("deleteMe", "awol")).isEqualTo("awol");
    assertThat(anotherSharedPreferences.getString("dontDeleteMe", "oops")).isEqualTo("oops");
  }

  @Test
  public void commit_shouldClearThenSetValues() throws Exception {
    editor.putString("deleteMe", "foo");

    editor.clear();
    editor.putString("dontDeleteMe", "baz");

    editor.commit();

    SharedPreferences anotherSharedPreferences = context.getSharedPreferences(FILENAME, 3);
    assertTrue(anotherSharedPreferences.getBoolean("boolean", false));
    assertThat(anotherSharedPreferences.getFloat("float", 666f)).isEqualTo(1.1f);
    assertThat(anotherSharedPreferences.getInt("int", 666)).isEqualTo(2);
    assertThat(anotherSharedPreferences.getLong("long", 666L)).isEqualTo(3L);
    assertThat(anotherSharedPreferences.getString("string", "wacka wa")).isEqualTo("foobar");

    // Android always calls clear before put on any open editor, so here "foo" is preserved rather than cleared.
    assertThat(anotherSharedPreferences.getString("deleteMe", "awol")).isEqualTo("foo");
    assertThat(anotherSharedPreferences.getString("dontDeleteMe", "oops")).isEqualTo("baz");
  }

  @Test
  public void putString_shouldRemovePairIfValueIsNull() throws Exception {
    editor.putString("deleteMe", "foo");

    editor.putString("deleteMe", null);
    editor.commit();

    assertThat(sharedPreferences.getString("deleteMe", null)).isNull();
  }

  @Test
  public void putStringSet_shouldRemovePairIfValueIsNull() throws Exception {
    editor.putStringSet("deleteMe", new HashSet<String>());

    editor.putStringSet("deleteMe", null);
    editor.commit();

    assertThat(sharedPreferences.getStringSet("deleteMe", null)).isNull();
  }

  @Test
  public void apply_shouldStoreValues() throws Exception {
    editor.apply();

    SharedPreferences anotherSharedPreferences = context.getSharedPreferences(FILENAME, 3);
    assertThat(anotherSharedPreferences.getString("string", "wacka wa")).isEqualTo("foobar");
  }

  @Test
  public void shouldReturnDefaultValues() throws Exception {
    SharedPreferences anotherSharedPreferences = context.getSharedPreferences("bazBang", 3);

    assertFalse(anotherSharedPreferences.getBoolean("boolean", false));
    assertThat(anotherSharedPreferences.getFloat("float", 666f)).isEqualTo(666f);
    assertThat(anotherSharedPreferences.getInt("int", 666)).isEqualTo(666);
    assertThat(anotherSharedPreferences.getLong("long", 666L)).isEqualTo(666L);
    assertThat(anotherSharedPreferences.getString("string", "wacka wa")).isEqualTo("wacka wa");
  }

  @Test
  public void shouldRemoveRegisteredListenersOnUnresgister() {
    SharedPreferences anotherSharedPreferences = context.getSharedPreferences("bazBang", 3);

    SharedPreferences.OnSharedPreferenceChangeListener mockListener = mock(SharedPreferences.OnSharedPreferenceChangeListener.class);
    anotherSharedPreferences.registerOnSharedPreferenceChangeListener(mockListener);

    anotherSharedPreferences.unregisterOnSharedPreferenceChangeListener(mockListener);

    anotherSharedPreferences.edit().putString("key", "value");
    verifyZeroInteractions(mockListener);
  }

  @Test
  public void shouldTriggerRegisteredListeners() {
    SharedPreferences anotherSharedPreferences = context.getSharedPreferences("bazBang", 3);

    final String testKey = "foo";

    final List<String> transcript = new ArrayList<>();

    SharedPreferences.OnSharedPreferenceChangeListener listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
      @Override
      public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        transcript.add(key + " called");
      }
    };
    anotherSharedPreferences.registerOnSharedPreferenceChangeListener(listener);
    anotherSharedPreferences.edit().putString(testKey, "bar").commit();

    assertThat(transcript).containsExactly(testKey+ " called");
  }
}
