package com.xtremelabs.robolectric.shadows;


import android.content.res.Configuration;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class ConfigurationTest {

    private Configuration configuration;

    @Before
    public void setUp() throws Exception {
        configuration = new Configuration();
    }

    @Test
    public void testSetToDefaults() throws Exception {
        configuration.setToDefaults();
        assertThat(configuration.screenLayout, equalTo(Configuration.SCREENLAYOUT_LONG_NO | Configuration.SCREENLAYOUT_SIZE_NORMAL));
    }

}
