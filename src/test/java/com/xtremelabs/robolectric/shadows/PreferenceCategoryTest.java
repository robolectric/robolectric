package com.xtremelabs.robolectric.shadows;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.app.Activity;
import android.preference.PreferenceCategory;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;

@RunWith(WithTestDefaultsRunner.class)
public class PreferenceCategoryTest {

	private PreferenceCategory category;
	private ShadowPreferenceCategory shadow;

    @Before
    public void setUp() throws Exception {
    	category = new PreferenceCategory( new Activity() );
    	shadow = Robolectric.shadowOf(category);
    }
    
	@Test
	public void shouldInheritFromPreferenceGroup() {
		assertThat(shadow, instanceOf(ShadowPreferenceGroup.class));
	}	
	
	@Test
	public void shouldEnable() {
		assertThat(category.isEnabled(), equalTo(true));
		
		category.setEnabled(false);
		assertThat(category.isEnabled(), equalTo(false));
		
		category.setEnabled(true);
		assertThat(category.isEnabled(), equalTo(true));
	}  
}
