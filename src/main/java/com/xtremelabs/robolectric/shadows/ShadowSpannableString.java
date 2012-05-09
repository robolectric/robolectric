package com.xtremelabs.robolectric.shadows;

import android.text.SpannableString;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(SpannableString.class)
public class ShadowSpannableString {
	
	private CharSequence text = "";
	
	public void __constructor__(CharSequence source) {
		text = source;
	}
	
	@Implementation
	public String toString() {
		return text.toString();
	}
	
}
