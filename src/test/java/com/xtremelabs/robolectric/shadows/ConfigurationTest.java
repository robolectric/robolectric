package com.xtremelabs.robolectric.shadows;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;

import android.content.res.Configuration;

import static org.junit.Assert.assertThat;
import static org.hamcrest.CoreMatchers.equalTo;

@RunWith(WithTestDefaultsRunner.class)
public class ConfigurationTest {

	private Configuration configuration;
	
	@Before
	public void setUp() throws Exception {
        Robolectric.bindDefaultShadowClasses();
		configuration = new Configuration();
	}
	
	@Test
	public void testSetToDefaults() throws Exception {
		configuration.setToDefaults();
		assertThat( configuration.screenLayout, equalTo( Configuration.SCREENLAYOUT_LONG_NO | Configuration.SCREENLAYOUT_SIZE_NORMAL ) );
	}

}
