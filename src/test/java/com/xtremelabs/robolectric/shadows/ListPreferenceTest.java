package com.xtremelabs.robolectric.shadows;

import android.app.Activity;
import android.preference.ListPreference;
import com.xtremelabs.robolectric.R;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class ListPreferenceTest {

	private ListPreference listPreference;
	private ShadowListPreference shadow;

	@Before
	public void setUp() throws Exception {
		listPreference = new ListPreference(new Activity());
		shadow = Robolectric.shadowOf(listPreference);
    }
    
	@Test
	public void shouldInheritFromDialogPreference() {
		assertThat(shadow, instanceOf(ShadowDialogPreference.class));
	}	
	
	@Test
	public void shouldHaveEntries() {
		CharSequence[] entries = { "this", "is", "only", "a", "test" };
		
		assertThat(listPreference.getEntries(), nullValue());
		listPreference.setEntries(entries);
		assertThat(listPreference.getEntries(), sameInstance(entries));		
	}
	
	@Test
	public void shouldSetEntriesByResourceId() {
		assertThat(listPreference.getEntries(), nullValue());
		listPreference.setEntries(R.array.greetings);
		assertThat(listPreference.getEntries(), notNullValue());			
	}
	
	@Test
	public void shouldHaveEntryValues() {
		CharSequence[] entryValues = { "this", "is", "only", "a", "test" };
		
		assertThat(listPreference.getEntryValues(), nullValue());
		listPreference.setEntryValues(entryValues);
		assertThat(listPreference.getEntryValues(), sameInstance(entryValues));		
	}
	
	@Test
	public void shouldSetEntryValuesByResourceId() {
		assertThat(listPreference.getEntryValues(), nullValue());
		listPreference.setEntryValues(R.array.greetings);
		assertThat(listPreference.getEntryValues(), notNullValue());			
	}
	
	@Test
	public void shouldSetValue() {
		assertThat(listPreference.getValue(), nullValue());
		listPreference.setValue("testing");
		assertThat(listPreference.getValue(), equalTo("testing"));
	}
}
