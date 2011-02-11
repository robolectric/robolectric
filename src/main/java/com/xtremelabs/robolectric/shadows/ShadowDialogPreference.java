package com.xtremelabs.robolectric.shadows;

import android.preference.DialogPreference;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(DialogPreference.class)
public class ShadowDialogPreference extends ShadowPreference {
	
	private static final String androidns="http://schemas.android.com/apk/res/android";

    @Implementation
    public CharSequence getDialogMessage() {
    	return (CharSequence) attrs.getAttributeValue(androidns,"dialogMessage");
    }
}
