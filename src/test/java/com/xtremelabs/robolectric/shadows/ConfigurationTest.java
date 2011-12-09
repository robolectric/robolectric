package com.xtremelabs.robolectric.shadows;


import java.util.Locale;

import android.content.res.Configuration;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class ConfigurationTest {

    private Configuration configuration;
    private ShadowConfiguration shConfiguration;

    @Before
    public void setUp() throws Exception {
        configuration = new Configuration();
        shConfiguration = Robolectric.shadowOf( configuration );
    }

    @Test
    public void testSetToDefaults() throws Exception {
        configuration.setToDefaults();
        assertThat(configuration.screenLayout, equalTo(Configuration.SCREENLAYOUT_LONG_NO | Configuration.SCREENLAYOUT_SIZE_NORMAL));
    }
    
    @Test
    public void testSetLocale() {
    	shConfiguration.setLocale( Locale.US );
    	assertThat( configuration.locale, equalTo( Locale.US ) );

    	shConfiguration.setLocale( Locale.FRANCE);
    	assertThat( configuration.locale, equalTo( Locale.FRANCE ) );
}

}
