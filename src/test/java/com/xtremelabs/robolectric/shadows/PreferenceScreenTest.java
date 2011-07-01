package com.xtremelabs.robolectric.shadows;

import android.app.Activity;
import android.app.Dialog;
import android.preference.PreferenceScreen;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class PreferenceScreenTest {

	private PreferenceScreen screen;
	private ShadowPreferenceScreen shadow;

    @Before
    public void setUp() throws Exception {
    	screen = Robolectric.newInstanceOf(PreferenceScreen.class);
    	shadow = Robolectric.shadowOf(screen);
    }
    
	@Test
	public void shouldInheritFromPreferenceGroup() {
		assertThat(shadow, instanceOf(ShadowPreferenceGroup.class));
	}
	
	@Test
	public void shouldSetDialog() {
		Dialog dialog = new Dialog(new Activity());
		
		assertThat(screen.getDialog(), nullValue());
		shadow.setDialog(dialog);
		assertThat(screen.getDialog(), sameInstance(dialog));		
	}
}
