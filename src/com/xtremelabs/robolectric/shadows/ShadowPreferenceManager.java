package com.xtremelabs.robolectric.shadows;

import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;
import com.xtremelabs.robolectric.view.TestSharedPreferences;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

@Implements(PreferenceManager.class)
public class ShadowPreferenceManager {
	
	@Implementation
	public static SharedPreferences getDefaultSharedPreferences(Context context) {
		return new TestSharedPreferences("__default__", Context.MODE_PRIVATE); 
	}

}
