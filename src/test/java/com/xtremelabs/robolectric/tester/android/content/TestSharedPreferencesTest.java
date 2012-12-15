package com.xtremelabs.robolectric.tester.android.content;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.SharedPreferences;

import com.xtremelabs.robolectric.WithTestDefaultsRunner;

/**
 * TestSharedPreferencesTest
 */
@RunWith(WithTestDefaultsRunner.class)
public class TestSharedPreferencesTest {
	protected final static String FILENAME = "filename";
	private HashMap<String, Map<String, Object>> content;
    private SharedPreferences.Editor editor;
    private TestSharedPreferences sharedPreferences;
    
    private static final Set<String> stringSet;
    
    static {
    	stringSet = new HashSet<String>();
    	stringSet.add( "string1" );
    	stringSet.add( "string2" );
    	stringSet.add( "string3" );
    }
    
    @Before
    public void setUp() {
        content = new HashMap<String, Map<String, Object>>();

        sharedPreferences = new TestSharedPreferences(content, FILENAME, 3);
        editor = sharedPreferences.edit();
        editor.putBoolean("boolean", true);
        editor.putFloat("float", 1.1f);
        editor.putInt("int", 2);
        editor.putLong("long", 3l);
        editor.putString("string", "foobar");
        editor.putStringSet( "stringSet", stringSet );
    }

    @Test
    public void commit_shouldStoreValues() throws Exception {
        editor.commit();

        TestSharedPreferences anotherSharedPreferences = new TestSharedPreferences(content, FILENAME, 3);
        assertTrue(anotherSharedPreferences.getBoolean("boolean", false));
        assertThat(anotherSharedPreferences.getFloat("float", 666f), equalTo(1.1f));
        assertThat(anotherSharedPreferences.getInt("int", 666), equalTo(2));
        assertThat(anotherSharedPreferences.getLong("long", 666l), equalTo(3l));
        assertThat(anotherSharedPreferences.getString("string", "wacka wa"), equalTo("foobar"));
        assertThat(anotherSharedPreferences.getStringSet( "stringSet", null ), equalTo(stringSet ));
    }

    @Test
    public void getAll_shouldReturnAllValues() throws Exception {
        editor.commit();
        Map<String, ?> all = sharedPreferences.getAll();
        assertThat(all.size(), equalTo(6));
        assertThat((Integer) all.get("int"), equalTo(2));
    }

    @Test
    public void commit_shouldRemoveValuesThenSetValues() throws Exception {
    	content.put(FILENAME, new HashMap<String, Object>());
    	content.get(FILENAME).put("deleteMe", "foo");

    	editor.remove("deleteMe");
    	
    	editor.putString("dontDeleteMe", "baz");
        editor.remove("dontDeleteMe");

        editor.commit();

        TestSharedPreferences anotherSharedPreferences = new TestSharedPreferences(content, FILENAME, 3);
        assertTrue(anotherSharedPreferences.getBoolean("boolean", false));
        assertThat(anotherSharedPreferences.getFloat("float", 666f), equalTo(1.1f));
        assertThat(anotherSharedPreferences.getInt("int", 666), equalTo(2));
        assertThat(anotherSharedPreferences.getLong("long", 666l), equalTo(3l));
        assertThat(anotherSharedPreferences.getString("string", "wacka wa"), equalTo("foobar"));

        assertThat(anotherSharedPreferences.getString("deleteMe", "awol"), equalTo("awol"));
        assertThat(anotherSharedPreferences.getString("dontDeleteMe", "oops"), equalTo("baz"));
    }

    @Test
    public void commit_shouldClearThenSetValues() throws Exception {
    	content.put(FILENAME, new HashMap<String, Object>());
    	content.get(FILENAME).put("deleteMe", "foo");

    	editor.clear();
    	editor.putString("dontDeleteMe", "baz");

        editor.commit();

        TestSharedPreferences anotherSharedPreferences = new TestSharedPreferences(content, FILENAME, 3);
        assertTrue(anotherSharedPreferences.getBoolean("boolean", false));
        assertThat(anotherSharedPreferences.getFloat("float", 666f), equalTo(1.1f));
        assertThat(anotherSharedPreferences.getInt("int", 666), equalTo(2));
        assertThat(anotherSharedPreferences.getLong("long", 666l), equalTo(3l));
        assertThat(anotherSharedPreferences.getString("string", "wacka wa"), equalTo("foobar"));

        assertThat(anotherSharedPreferences.getString("deleteMe", "awol"), equalTo("awol"));
        assertThat(anotherSharedPreferences.getString("dontDeleteMe", "oops"), equalTo("baz"));
    }
    
    @Test
    public void apply_shouldStoreValues() throws Exception {
        editor.apply();

        TestSharedPreferences anotherSharedPreferences = new TestSharedPreferences(content, FILENAME, 3);
        assertThat(anotherSharedPreferences.getString("string", "wacka wa"), equalTo("foobar"));
    }

    @Test
    public void shouldReturnDefaultValues() throws Exception {
        TestSharedPreferences anotherSharedPreferences = new TestSharedPreferences(content, "bazBang", 3);

        assertFalse(anotherSharedPreferences.getBoolean("boolean", false));
        assertThat(anotherSharedPreferences.getFloat("float", 666f), equalTo(666f));
        assertThat(anotherSharedPreferences.getInt("int", 666), equalTo(666));
        assertThat(anotherSharedPreferences.getLong("long", 666l), equalTo(666l));
        assertThat(anotherSharedPreferences.getString("string", "wacka wa"), equalTo("wacka wa"));
    }

    @Test
    public void shouldStoreRegisteredListeners() {
        TestSharedPreferences anotherSharedPreferences = new TestSharedPreferences(content, "bazBang", 3);
        anotherSharedPreferences.registerOnSharedPreferenceChangeListener(testListener);
        assertTrue(anotherSharedPreferences.hasListener(testListener));
    }

    @Test
    public void shouldRemoveRegisteredListenersOnUnresgister() {
        TestSharedPreferences anotherSharedPreferences = new TestSharedPreferences(content, "bazBang", 3);
        anotherSharedPreferences.registerOnSharedPreferenceChangeListener(testListener);

        anotherSharedPreferences.unregisterOnSharedPreferenceChangeListener(testListener);
        assertFalse(anotherSharedPreferences.hasListener(testListener));
    }

    private SharedPreferences.OnSharedPreferenceChangeListener testListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        }
    };
}
