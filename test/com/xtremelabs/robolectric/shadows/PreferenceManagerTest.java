package com.xtremelabs.robolectric.shadows;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.xtremelabs.robolectric.DogfoodRobolectricTestRunner;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.view.TestSharedPreferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.preference.PreferenceManager;

@RunWith(DogfoodRobolectricTestRunner.class)
public class PreferenceManagerTest {
	
    @Before 
    public void setUp() throws Exception {
        Robolectric.bindDefaultShadowClasses();
    }
	
	@Test
	public void shouldProvideDefaultSharedPreferences() throws Exception {
		TestSharedPreferences testPrefs = new TestSharedPreferences( "__default__", Context.MODE_PRIVATE );
		Editor editor = testPrefs.edit();
		editor.putInt("foobar", 13);
		editor.commit();
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences( null );
		
		assertNotNull(prefs);
		assertEquals(13, prefs.getInt("foobar", 0));
	}

}
