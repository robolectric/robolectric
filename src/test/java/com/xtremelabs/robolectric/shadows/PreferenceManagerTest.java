package com.xtremelabs.robolectric.shadows;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.tester.android.content.TestSharedPreferences;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

@RunWith(WithTestDefaultsRunner.class)
public class PreferenceManagerTest {
    @Test
    public void shouldProvideDefaultSharedPreferences() throws Exception {
        Map<String, Map<String, Object>> content = Robolectric.getShadowApplication().getSharedPreferenceMap();

        TestSharedPreferences testPrefs = new TestSharedPreferences(content, "__default__", Context.MODE_PRIVATE);
        Editor editor = testPrefs.edit();
        editor.putInt("foobar", 13);
        editor.commit();

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Robolectric.application);

        assertNotNull(prefs);
        assertEquals(13, prefs.getInt("foobar", 0));
    }

}
