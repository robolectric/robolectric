package com.xtremelabs.robolectric.shadows;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertThat;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.app.Activity;
import android.content.Context;
import android.preference.PreferenceGroup;
import android.util.AttributeSet;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.tester.android.util.TestAttributeSet;

@RunWith(WithTestDefaultsRunner.class)
public class PreferenceGroupTest {

	private TestPreferenceGroup group;
	private ShadowPreferenceGroup shadow;
	
    @Before
    public void setUp() throws Exception {
    	Context context = new Activity();
    	TestAttributeSet attrs = new TestAttributeSet( new HashMap<String, String>() );

    	group = new TestPreferenceGroup(context, attrs);
    	shadow = Robolectric.shadowOf(group);
    }
    
	@Test
	public void shouldInheritFromPreference() {
		assertThat(shadow, instanceOf(ShadowPreference.class));
	}	
	
	@Test
	public void shouldEnable() {
		assertThat(shadow.isEnabled(), equalTo(true));
		
		group.setEnabled(false);
		assertThat(shadow.isEnabled(), equalTo(false));
		
		group.setEnabled(true);
		assertThat(shadow.isEnabled(), equalTo(true));
	}
	
	
	private static class TestPreferenceGroup extends PreferenceGroup {		
		public TestPreferenceGroup(Context context, AttributeSet attrs, int defStyle) {
			super(context, attrs, defStyle);
		}
		   
		public TestPreferenceGroup(Context context, AttributeSet attrs) {
			super(context, attrs);
		}		
	}
}
