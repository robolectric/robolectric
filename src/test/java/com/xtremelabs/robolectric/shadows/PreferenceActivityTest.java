package com.xtremelabs.robolectric.shadows;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.junit.Assert.assertThat;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.app.Activity;
import android.preference.PreferenceActivity;
import android.widget.ListView;

import com.xtremelabs.robolectric.R;
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
    public void shouldGetListView() {
    	shadow.setListView( new ListView( new Activity() ) );
    	assertThat( activity.getListView(), notNullValue() );    	
    }
    
	@Test
	public void shouldInheritFromListActivity() {
		assertThat(shadow, instanceOf(ShadowListActivity.class));
	}
	
	@Test
	public void shouldNotInitializePreferenceScreen() {
		assertThat(activity.getPreferenceScreen(), nullValue());
	}
    
	@Test
	public void shouldRecordPreferencesResourceId() {
		assertThat(shadow.getPreferencesResId(), equalTo(-1));
		activity.addPreferencesFromResource(R.xml.preferences);
		assertThat(shadow.getPreferencesResId(), equalTo(R.xml.preferences));		
	}
	
	@Test
	public void shouldLoadPreferenceScreen() {
		activity.addPreferencesFromResource(R.xml.preferences);
		assertThat( activity.getPreferenceScreen().getPreferenceCount(), equalTo(6));
	}
	
	private static class TestPreferenceActivity extends PreferenceActivity {		
	}
}
