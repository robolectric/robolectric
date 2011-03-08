package com.xtremelabs.robolectric.shadows;


import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import android.preference.DialogPreference;

@Implements(DialogPreference.class)
public class ShadowDialogPreference extends ShadowPreference {
	
	private static final String ANDROID_NS = "http://schemas.android.com/apk/res/android";

    @Implementation
    public CharSequence getDialogMessage() {
    	return attrs.getAttributeValue(ANDROID_NS,"dialogMessage");
    }
}
