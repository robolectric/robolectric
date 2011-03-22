package com.xtremelabs.robolectric.shadows;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.preference.PreferenceActivity;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;

@RunWith(WithTestDefaultsRunner.class)
public class PreferenceActivityTest {

	private TestPreferenceActivity activity;
	private ShadowPreferenceActivity shadow;
	
    @Before
    public void setUp() throws Exception {
    	activity = new TestPreferenceActivity();
    	shadow = Robolectric.shadowOf(activity);
    }
    
	@Test
	public void shouldInheritFromListActivity() {
		assertThat(shadow, instanceOf(ShadowListActivity.class));
	}
    
	@Test
	public void shouldRecordPreferencesResourceId() {
		int expected = 727;
		
		assertThat(shadow.getPreferencesResId(), equalTo(-1));
		activity.addPreferencesFromResource(expected);
		assertThat(shadow.getPreferencesResId(), equalTo(expected));		
	}
	
	private static class TestPreferenceActivity extends PreferenceActivity {		
	}
}
