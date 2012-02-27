package com.xtremelabs.robolectric.shadows;

import android.app.Activity;
import android.app.Application;
import android.content.ContentResolver;
import android.provider.Settings;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class SettingsTest {
    private Activity activity;
    private ContentResolver contentResolver;

    @Before
    public void setUp() throws Exception {
        activity = new Activity();
        contentResolver = activity.getContentResolver();
    }

    @Test
    public void whileApplicationStaysSame_shouldRememberOldSettings() throws Exception {
        Settings.System.putInt(contentResolver, "property", 1);
        assertThat(Settings.System.getInt(contentResolver, "property", 0), equalTo(1));

        activity = new Activity();
        contentResolver = activity.getContentResolver();
        assertThat(Settings.System.getInt(contentResolver, "property", 0), equalTo(1));
    }

    @Test
    public void whenApplicationChanges_shouldStartWithNewSettings() throws Exception {
        Settings.System.putInt(contentResolver, "property", 1);
        assertThat(Settings.System.getInt(contentResolver, "property", 0), equalTo(1));

        Robolectric.application = new Application();
        activity = new Activity();
        contentResolver = activity.getContentResolver();
        assertThat(Settings.System.getInt(contentResolver, "property", 0), equalTo(0));
    }

    @Test
    public void testSystemGetInt() throws Exception {
        assertThat(Settings.System.getInt(contentResolver, "property", 0), equalTo(0));
        assertThat(Settings.System.getInt(contentResolver, "property", 2), equalTo(2));

        Settings.System.putInt(contentResolver, "property", 1);
        assertThat(Settings.System.getInt(contentResolver, "property", 0), equalTo(1));
    }

    @Test
    public void testSecureGetInt() throws Exception {
        assertThat(Settings.Secure.getInt(contentResolver, "property", 0), equalTo(0));
        assertThat(Settings.Secure.getInt(contentResolver, "property", 2), equalTo(2));

        Settings.Secure.putInt(contentResolver, "property", 1);
        assertThat(Settings.Secure.getInt(contentResolver, "property", 0), equalTo(1));
    }

    @Test
    public void testSystemGetString() throws Exception {
        assertThat(Settings.System.getString(contentResolver, "property"), nullValue());

        Settings.System.putString(contentResolver, "property", "value");
        assertThat(Settings.System.getString(contentResolver, "property"), equalTo("value"));
    }

    @Test
    public void testSystemGetLong() throws Exception {
        assertThat(Settings.System.getLong(contentResolver, "property", 10L), equalTo(10L));
        Settings.System.putLong(contentResolver, "property", 42L);
        assertThat(Settings.System.getLong(contentResolver, "property"), equalTo(42L));
        assertThat(Settings.System.getLong(contentResolver, "property", 10L), equalTo(42L));
    }

    @Test
    public void testSystemGetFloat() throws Exception {
        assertThat(Settings.System.getFloat(contentResolver, "property", 23.23f), equalTo(23.23f));
        Settings.System.putFloat(contentResolver, "property", 42.42f);
        assertThat(Settings.System.getFloat(contentResolver, "property", 10L), equalTo(42.42f));
    }

    @Test(expected = Settings.SettingNotFoundException.class)
    public void testSystemGetLong_exception() throws Exception {
        Settings.System.getLong(contentResolver, "property");
    }

    @Test(expected = Settings.SettingNotFoundException.class)
    public void testSystemGetInt_exception() throws Exception {
        Settings.System.getInt(contentResolver, "property");
    }

    @Test(expected = Settings.SettingNotFoundException.class)
    public void testSystemGetFloat_exception() throws Exception {
        Settings.System.getFloat(contentResolver, "property");
    }
}
