package com.xtremelabs.robolectric.shadows;

import android.app.Activity;
import android.content.res.Configuration;
import android.content.res.Resources;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class ResourcesTest {

    @Test(expected = Resources.NotFoundException.class)
    public void getStringArray_shouldThrowExceptionIfNotFound() throws Exception {
        new Activity().getResources().getStringArray(-1);
    }

    @Test
    public void testConfiguration() {
        Configuration configuration = new Activity().getResources().getConfiguration();
        assertThat(configuration, notNullValue());
        assertThat(configuration.locale, notNullValue());
    }

    @Test
    public void testNewTheme() {
        assertThat(new Activity().getResources().newTheme(), notNullValue());
    }
}
