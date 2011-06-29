package com.xtremelabs.robolectric.shadows;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.app.Activity;
import android.content.Context;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.util.AttributeSet;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.tester.android.util.TestAttributeSet;

@RunWith(WithTestDefaultsRunner.class)
public class PreferenceGroupTest {

	private TestPreferenceGroup group;
	private ShadowPreferenceGroup shadow;
	private Context context;
	private TestAttributeSet attrs;
	private Preference pref1, pref2;
	
    @Before
    public void setUp() throws Exception {
    	context = new Activity();
    	attrs = new TestAttributeSet( new HashMap<String, String>() );

    	group = new TestPreferenceGroup(context, attrs);
    	shadow = Robolectric.shadowOf(group);

		pref1 = new Preference(context);
		pref1.setKey("pref1");

		pref2 = new Preference(context);		
		pref2.setKey("pref2");
    }
    
	@Test
	public void shouldInheritFromPreference() {
		assertThat(shadow, instanceOf(ShadowPreference.class));
	}	
	
	@Test
	public void shouldAddPreferences() {		
		assertThat( group.getPreferenceCount(), equalTo(0));
		
		// First add succeeds
		assertThat( group.addPreference(pref1), equalTo(true));
		assertThat( group.getPreferenceCount(), equalTo(1));
		
		// Dupe add fails silently 
		assertThat( group.addPreference(pref1), equalTo(true));
		assertThat( group.getPreferenceCount(), equalTo(1));
	
		// Second add succeeds
		assertThat( group.addPreference(pref2), equalTo(true));
		assertThat( group.getPreferenceCount(), equalTo(2));
	}

	@Test
	public void shouldAddItemFromInflater() {		
		assertThat( group.getPreferenceCount(), equalTo(0));
		
		// First add succeeds
		group.addItemFromInflater(pref1);
		assertThat( group.getPreferenceCount(), equalTo(1));
		
		// Dupe add fails silently 
		group.addItemFromInflater(pref1);
		assertThat( group.getPreferenceCount(), equalTo(1));
	
		// Second add succeeds
		group.addItemFromInflater(pref2);
		assertThat( group.getPreferenceCount(), equalTo(2));
	}
	
	@Test
	public void shouldGetPreference() {
		group.addPreference(pref1);
		group.addPreference(pref2);
		
		assertThat( group.getPreference(0), sameInstance(pref1));
		assertThat( group.getPreference(1), sameInstance(pref2));
	}
	
	@Test
	public void shouldGetPreferenceCount() {
		assertThat( group.getPreferenceCount(), equalTo(0));
		group.addPreference(pref1);
		assertThat( group.getPreferenceCount(), equalTo(1));
		group.addPreference(pref2);
		assertThat( group.getPreferenceCount(), equalTo(2));
	}
	
	@Test
	public void shouldRemovePreference() {
		group.addPreference(pref1);
		group.addPreference(pref2);
		assertThat( group.getPreferenceCount(), equalTo(2));

		// First remove succeeds
		assertThat( group.removePreference(pref1), equalTo(true));
		assertThat( group.getPreferenceCount(), equalTo(1));
		
		// Dupe remove fails
		assertThat( group.removePreference(pref1), equalTo(false));
		assertThat( group.getPreferenceCount(), equalTo(1));
		
		// Second remove succeeds
		assertThat( group.removePreference(pref2), equalTo(true));
		assertThat( group.getPreferenceCount(), equalTo(0));
	}
	
	@Test
	public void shouldRemoveAll() {
		group.addPreference(pref1);
		group.addPreference(pref2);
		assertThat( group.getPreferenceCount(), equalTo(2));

		group.removeAll();
		assertThat( group.getPreferenceCount(), equalTo(0));		
	}
	
	@Test
	public void shouldFindPreference() {
		group.addPreference(pref1);
		group.addPreference(pref2);

		assertThat( group.findPreference(pref1.getKey()), sameInstance(pref1));
		assertThat( group.findPreference(pref2.getKey()), sameInstance(pref2));
	}

	@Test
	public void shouldFindPreferenceRecursively() {
		TestPreferenceGroup group2 = new TestPreferenceGroup(context, attrs);
		group2.addPreference(pref2);
		
		group.addPreference(pref1);
		group.addPreference(group2);

		assertThat( group.findPreference(pref2.getKey()), sameInstance(pref2));		
	}
	
	@Test 
	public void shouldSetEnabledRecursively() {
		boolean[] values = { false, true };
		
		TestPreferenceGroup group2 = new TestPreferenceGroup(context, attrs);
		group2.addPreference(pref2);
		
		group.addPreference(pref1);
		group.addPreference(group2);

		for( boolean enabled : values ) {
			group.setEnabled(enabled);
			
			assertThat(group.isEnabled(), equalTo(enabled));
			assertThat(group2.isEnabled(), equalTo(enabled));
			assertThat(pref1.isEnabled(), equalTo(enabled));
			assertThat(pref2.isEnabled(), equalTo(enabled));
		}		
	}

	private static class TestPreferenceGroup extends PreferenceGroup {
		public TestPreferenceGroup(Context context, AttributeSet attrs) {
			super(context, attrs);
		}		
	}
}
