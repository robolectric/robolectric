package com.xtremelabs.robolectric.shadows;

import android.app.Activity;
import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;

import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.tester.android.util.TestAttributeSet;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.HashMap;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class PreferenceTest {

	private TestPreference preference;
	private ShadowPreference shadow;

	private Context context;
	private TestAttributeSet attrs;
	
	@Before
	public void setup() {
		context = new Activity();
		attrs = new TestAttributeSet( new HashMap<String, String>() );
		preference = new TestPreference(context, attrs);
		shadow = Robolectric.shadowOf( preference );
	}

	@Test
	public void shouldConstruct() {
		int defStyle = 7;
		
		preference = new TestPreference(context, attrs, defStyle);
		shadow = Robolectric.shadowOf(preference);		
		assertThat(shadow.getContext(), sameInstance(context));
		assertThat(shadow.getAttrs(), sameInstance((AttributeSet)attrs));
		assertThat(shadow.getDefStyle(), equalTo(defStyle));	
		
		preference = new TestPreference(context, attrs);
		shadow = Robolectric.shadowOf(preference);		
		assertThat(shadow.getContext(), sameInstance( context ));
		assertThat(shadow.getAttrs(), sameInstance((AttributeSet)attrs));
		assertThat(shadow.getDefStyle(), equalTo(0));		

		preference = new TestPreference(context);
		shadow = Robolectric.shadowOf(preference);		
		assertThat(shadow.getContext(), sameInstance( context ));
		assertThat(shadow.getAttrs(), nullValue());
		assertThat(shadow.getDefStyle(), equalTo(0));		
}
	
	@Test
	public void shouldInitializeFromAttributes() {
		String key = "key_value";
		HashMap<String, String> hash = new HashMap<String, String>();
		hash.put( "android:key", key );
		attrs = new TestAttributeSet( hash );
		
		preference = new TestPreference(context, attrs);
		assertThat( preference.getKey(), equalTo(key) );
	}
	
	@Test
	public void shouldHaveAKey() {
		String key = "key_value";
		
		assertThat(preference.getKey(), nullValue());
		preference.setKey(key);
		assertThat(preference.getKey(), equalTo(key));
	}

	@Test
	public void shouldHaveATitle() {
		CharSequence title = "Test Preference";
		
		assertThat(preference.getTitle(), nullValue());
		preference.setTitle(title);
		assertThat(preference.getTitle(), equalTo(title));
	}
	
	@Test
	public void shouldSetTitleByResId() {
		CharSequence expected = "Hello";
		
		assertThat(preference.getTitle(), not(equalTo(expected)));	
		preference.setTitle(R.string.hello);
		assertThat(preference.getTitle(), equalTo(expected));	
	}

	@Test
	public void shouldHaveASummary() {
		CharSequence summary = "This is only a test.";
		
		assertThat(preference.getSummary(), nullValue());
		preference.setSummary(summary);
		assertThat(preference.getSummary(), equalTo(summary));
	}
	
	@Test
	public void shouldSetSummaryByResId() {
		CharSequence expected = "Hello";
		
		assertThat(preference.getSummary(), not(equalTo(expected)));	
		preference.setSummary(R.string.hello);
		assertThat(preference.getSummary(), equalTo(expected));	
	}

	
	@Test
	public void shouldEnable() {
		assertThat(preference.isEnabled(), equalTo(true));
		
		preference.setEnabled(false);
		assertThat(preference.isEnabled(), equalTo(false));
		
		preference.setEnabled(true);
		assertThat(preference.isEnabled(), equalTo(true));
	}
		
	@Test
	public void testShouldPersist() {
		boolean[] values = { true, false };
		
		for( boolean shouldPersist : values ) { 
			shadow.setShouldPersist(shouldPersist);
			assertThat(preference.shouldPersist(), equalTo(shouldPersist));
		}
	}
	
	@Test
	public void testPersistedInt() {
		int defaultValue = 727;
		int[] values = { 0, 1, 2, 2011 };
		
		for(int persistedInt : values) {			
			shadow.persistInt(persistedInt);
			
			shadow.setShouldPersist(false);
			assertThat(preference.getPersistedInt(defaultValue), equalTo(defaultValue));
			
			shadow.setShouldPersist(true);
			assertThat(preference.getPersistedInt(defaultValue), equalTo(persistedInt));			
		}
	}
	
	@Test
	public void testCallChangeListener() {
		Integer[] values = { 0, 1, 2, 2011 };
		
		for(Integer newValue : values) {			
			assertThat(preference.callChangeListener(newValue), equalTo(true));
			assertThat(shadow.getCallChangeListenerValue(), sameInstance((Object)newValue));
		}
	}
	
	////////////////////////////////////////////////////////////
	// TestPreference
	////////////////////////////////////////////////////////////

	private static class TestPreference extends Preference {
		public TestPreference(Context context) {
			super(context);
		}
		
		public TestPreference(Context context, AttributeSet attrs) {
			super(context, attrs);
		}
		
		public TestPreference(Context context, AttributeSet attrs, int defStyle) {
			super(context, attrs, defStyle);
		}
		   
		public boolean shouldPersist() {
			return super.shouldPersist();
		}
		
		public int getPersistedInt(int defaultReturnValue) {
			return super.getPersistedInt(defaultReturnValue);
		}	
		
		public boolean persistInt(int value) {
			return super.persistInt(value);
		}
		
		public boolean callChangeListener(Object newValue) {
			return super.callChangeListener(newValue);
		}
	}
}
