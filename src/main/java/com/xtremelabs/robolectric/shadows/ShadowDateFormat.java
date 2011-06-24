package com.xtremelabs.robolectric.shadows;

import android.content.Context;
import android.text.format.DateFormat;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(DateFormat.class)
public class ShadowDateFormat {

	@Implementation
	public final static java.text.DateFormat getDateFormat(Context context) {
		return java.text.DateFormat.getDateInstance();
	}
}
