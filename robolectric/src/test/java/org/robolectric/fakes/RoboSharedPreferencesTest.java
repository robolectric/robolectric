package org.robolectric.fakes;

import android.content.SharedPreferences;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.TestRunners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(TestRunners.SelfTest.class)
public class RoboSharedPreferencesTest {
  protected final static String FILENAME = "filename";
  private HashMap<String, Map<String, Object>> content;
  private SharedPreferences.Editor editor;
  private RoboSharedPreferences sharedPreferences;

  private static final Set<String> stringSet;

  static {
    stringSet = new HashSet<>();
    stringSet.add( "string1" );
    stringSet.add( "string2" );
    stringSet.add( "string3" );
  }

  @Before
  public void setUp() {
    content = new HashMap<>();

    sharedPreferences = new RoboSharedPreferences(content, FILENAME, 3);
    editor = sharedPreferences.edit();
    editor.putBoolean("boolean", true);
    editor.putFloat("float", 1.1f);
    editor.putInt("int", 2);
    editor.putLong("long", 3l);
    editor.putString("string", "foobar");
    editor.putStringSet("stringSet", stringSet);
  }

  @Test
  public void commit_shouldStoreValues() throws Exception {
    editor.commit();

    RoboSharedPreferences anotherSharedPreferences = new RoboSharedPreferences(content, FILENAME, 3);
    assertTrue(anotherSharedPreferences.getBoolean("boolean", false));
    assertThat(anotherSharedPreferences.getFloat("float", 666f)).isEqualTo(1.1f);
    assertThat(anotherSharedPreferences.getInt("int", 666)).isEqualTo(2);
    assertThat(anotherSharedPreferences.getLong("long", 666l)).isEqualTo(3l);
    assertThat(anotherSharedPreferences.getString("string", "wacka wa")).isEqualTo("foobar");
    assertThat(anotherSharedPreferences.getStringSet("stringSet", null)).isEqualTo(stringSet);
  }

  @Test
  public void commit_shouldClearEditsThatNeedRemoveAndEditsThatNeedCommit() throws Exception {
    editor.commit();
    editor.remove("string").commit();

    assertThat(sharedPreferences.getString("string", "no value for key")).isEqualTo("no value for key");

    RoboSharedPreferences anotherSharedPreferences = new RoboSharedPreferences(content, FILENAME, 3);
    anotherSharedPreferences.edit().putString("string", "value for key").commit();

    editor.commit();
    assertThat(sharedPreferences.getString("string", "no value for key")).isEqualTo("value for key");
  }

  @Test
  public void getAll_shouldReturnAllValues() throws Exception {
    editor.commit();
    Map<String, ?> all = sharedPreferences.getAll();
    assertThat(all.size()).isEqualTo(6);
    assertThat((Integer) all.get("int")).isEqualTo(2);
  }

  @Test
  public void commit_shouldRemoveValuesThenSetValues() throws Exception {
    content.put(FILENAME, new HashMap<String, Object>());
    content.get(FILENAME).put("deleteMe", "foo");

    editor.remove("deleteMe");

    editor.putString("dontDeleteMe", "baz");
    editor.remove("dontDeleteMe");

    editor.commit();

    RoboSharedPreferences anotherSharedPreferences = new RoboSharedPreferences(content, FILENAME, 3);
    assertTrue(anotherSharedPreferences.getBoolean("boolean", false));
    assertThat(anotherSharedPreferences.getFloat("float", 666f)).isEqualTo(1.1f);
    assertThat(anotherSharedPreferences.getInt("int", 666)).isEqualTo(2);
    assertThat(anotherSharedPreferences.getLong("long", 666l)).isEqualTo(3l);
    assertThat(anotherSharedPreferences.getString("string", "wacka wa")).isEqualTo("foobar");

    assertThat(anotherSharedPreferences.getString("deleteMe", "awol")).isEqualTo("awol");
    assertThat(anotherSharedPreferences.getString("dontDeleteMe", "oops")).isEqualTo("baz");
  }

  @Test
  public void commit_shouldClearThenSetValues() throws Exception {
    content.put(FILENAME, new HashMap<String, Object>());
    content.get(FILENAME).put("deleteMe", "foo");

    editor.clear();
    editor.putString("dontDeleteMe", "baz");

    editor.commit();

    RoboSharedPreferences anotherSharedPreferences = new RoboSharedPreferences(content, FILENAME, 3);
    assertTrue(anotherSharedPreferences.getBoolean("boolean", false));
    assertThat(anotherSharedPreferences.getFloat("float", 666f)).isEqualTo(1.1f);
    assertThat(anotherSharedPreferences.getInt("int", 666)).isEqualTo(2);
    assertThat(anotherSharedPreferences.getLong("long", 666l)).isEqualTo(3l);
    assertThat(anotherSharedPreferences.getString("string", "wacka wa")).isEqualTo("foobar");

    assertThat(anotherSharedPreferences.getString("deleteMe", "awol")).isEqualTo("awol");
    assertThat(anotherSharedPreferences.getString("dontDeleteMe", "oops")).isEqualTo("baz");
  }

  @Test
  public void putString_shouldRemovePairIfValueIsNull() throws Exception {
    content.put(FILENAME, new HashMap<String, Object>());
    content.get(FILENAME).put("deleteMe", "foo");

    editor.putString("deleteMe", null);
    editor.commit();

    assertThat(sharedPreferences.getString("deleteMe", null)).isNull();
  }

  @Test
  public void putStringSet_shouldRemovePairIfValueIsNull() throws Exception {
    content.put(FILENAME, new HashMap<String, Object>());
    content.get(FILENAME).put("deleteMe", stringSet);

    editor.putStringSet("deleteMe", null);
    editor.commit();

    assertThat(sharedPreferences.getStringSet("deleteMe", null)).isNull();
  }

  @Test
  public void apply_shouldStoreValues() throws Exception {
    editor.apply();

    RoboSharedPreferences anotherSharedPreferences = new RoboSharedPreferences(content, FILENAME, 3);
    assertThat(anotherSharedPreferences.getString("string", "wacka wa")).isEqualTo("foobar");
  }

  @Test
  public void shouldReturnDefaultValues() throws Exception {
    RoboSharedPreferences anotherSharedPreferences = new RoboSharedPreferences(content, "bazBang", 3);

    assertFalse(anotherSharedPreferences.getBoolean("boolean", false));
    assertThat(anotherSharedPreferences.getFloat("float", 666f)).isEqualTo(666f);
    assertThat(anotherSharedPreferences.getInt("int", 666)).isEqualTo(666);
    assertThat(anotherSharedPreferences.getLong("long", 666l)).isEqualTo(666l);
    assertThat(anotherSharedPreferences.getString("string", "wacka wa")).isEqualTo("wacka wa");
  }

  @Test
  public void shouldStoreRegisteredListeners() {
    RoboSharedPreferences anotherSharedPreferences = new RoboSharedPreferences(content, "bazBang", 3);
    anotherSharedPreferences.registerOnSharedPreferenceChangeListener(testListener);
    assertTrue(anotherSharedPreferences.hasListener(testListener));
  }

  @Test
  public void shouldRemoveRegisteredListenersOnUnresgister() {
    RoboSharedPreferences anotherSharedPreferences = new RoboSharedPreferences(content, "bazBang", 3);
    anotherSharedPreferences.registerOnSharedPreferenceChangeListener(testListener);

    anotherSharedPreferences.unregisterOnSharedPreferenceChangeListener(testListener);
    assertFalse(anotherSharedPreferences.hasListener(testListener));
  }

  @Test
  public void shouldTriggerRegisteredListeners() {
    RoboSharedPreferences anotherSharedPreferences = new RoboSharedPreferences(content, "bazBang", 3);

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

  private SharedPreferences.OnSharedPreferenceChangeListener testListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
    }
  };
}
