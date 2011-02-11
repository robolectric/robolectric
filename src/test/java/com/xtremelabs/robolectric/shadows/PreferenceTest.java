package com.xtremelabs.robolectric.shadows;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

import java.util.HashMap;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.app.Activity;
import android.content.Context;
import android.preference.Preference;
import android.util.AttributeSet;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import com.xtremelabs.robolectric.internal.TestAttributeSet;
import com.xtremelabs.robolectric.shadows.DialogPreferenceTest.TestDialogPreference;

@RunWith(WithTestDefaultsRunner.class)
public class PreferenceTest {

	private Preference preference;
	private ShadowPreference shadow;

	private Context context;
	private TestAttributeSet attrs;
	
	@Before
	public void setup() {
		context = new Activity();
		attrs = new TestAttributeSet( new HashMap<String, String>() );
		preference = new TestDialogPreference(context, attrs);
	}

	@Test
	public void testConstructors() {
		int defStyle = 7;
		
		preference = new Preference( context, attrs, defStyle );
		shadow = Robolectric.shadowOf( preference );		
		assertThat( shadow.getContext(), sameInstance( context ) );
		assertThat( shadow.getAttrs(), sameInstance( (AttributeSet)attrs ) );
		assertThat( shadow.getDefStyle(), equalTo( defStyle ) );	
		
		preference = new Preference( context, attrs );
		shadow = Robolectric.shadowOf( preference );		
		assertThat( shadow.getContext(), sameInstance( context ) );
		assertThat( shadow.getAttrs(), sameInstance( (AttributeSet)attrs ) );
		assertThat( shadow.getDefStyle(), equalTo( 0 ) );		
	}	
}
