package com.xtremelabs.robolectric.shadows;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.view.TestSharedPreferences;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(WithTestDefaultsRunner.class)
public class PreferenceManagerTest {
    @Test
    public void shouldProvideDefaultSharedPreferences() throws Exception {
        TestSharedPreferences testPrefs = new TestSharedPreferences("__default__", Context.MODE_PRIVATE);
        Editor editor = testPrefs.edit();
        editor.putInt("foobar", 13);
        editor.commit();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(null);

        assertNotNull(prefs);
        assertEquals(13, prefs.getInt("foobar", 0));
    }

}
