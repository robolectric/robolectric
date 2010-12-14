package com.xtremelabs.robolectric.view;

import android.content.SharedPreferences;
import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.Matchers.equalTo;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public class TestSharedPreferencesTest {

    private SharedPreferences.Editor editor;

    @Before
    public void setUp() {
        TestSharedPreferences testSharedPreferences = new TestSharedPreferences("prefsName", 3);
        editor = testSharedPreferences.edit();
        editor.putBoolean("boolean", true);
        editor.putFloat("float", 1.1f);
        editor.putInt("int", 2);
        editor.putLong("long", 3l);
        editor.putString("string", "foobar");
    }

    @Test
    public void commit_shouldStoreValues() throws Exception {
        editor.commit();

        TestSharedPreferences anotherSharedPreferences = new TestSharedPreferences("prefsName", 3);
        assertTrue(anotherSharedPreferences.getBoolean("boolean", false));
        assertThat(anotherSharedPreferences.getFloat("float", 666f), equalTo(1.1f));
        assertThat(anotherSharedPreferences.getInt("int", 666), equalTo(2));
        assertThat(anotherSharedPreferences.getLong("long", 666l), equalTo(3l));
        assertThat(anotherSharedPreferences.getString("string", "wacka wa"), equalTo("foobar"));
    }

    @Test
    public void apply_shouldStoreValues() throws Exception {
        editor.apply();

        TestSharedPreferences anotherSharedPreferences = new TestSharedPreferences("prefsName", 3);
        assertThat(anotherSharedPreferences.getString("string", "wacka wa"), equalTo("foobar"));
    }

    @Test
    public void shouldReturnDefaultValues() throws Exception {
        TestSharedPreferences anotherSharedPreferences = new TestSharedPreferences("bazBang", 3);

        assertFalse(anotherSharedPreferences.getBoolean("boolean", false));
        assertThat(anotherSharedPreferences.getFloat("float", 666f), equalTo(666f));
        assertThat(anotherSharedPreferences.getInt("int", 666), equalTo(666));
        assertThat(anotherSharedPreferences.getLong("long", 666l), equalTo(666l));
        assertThat(anotherSharedPreferences.getString("string", "wacka wa"), equalTo("wacka wa"));
    }

    @Test
    public void reset_shouldClearCommittedValues() {
        editor.commit();

        TestSharedPreferences anotherSharedPreferences = new TestSharedPreferences("prefsName", 3);
        TestSharedPreferences.reset();

        assertThat(anotherSharedPreferences.getString("string", "wacka wa"), equalTo("wacka wa"));
    }
}
