package com.xtremelabs.robolectric.shadows;

import android.content.Context;
import android.text.format.DateFormat;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

import java.util.Locale;

@Implements(DateFormat.class)
public class ShadowDateFormat {

	@Implementation
	public final static java.text.DateFormat getDateFormat(Context context) {
        return new java.text.SimpleDateFormat("MMM-DD-yyyy", Locale.ROOT);
	}

	@Implementation
	public final static java.text.DateFormat getTimeFormat(Context context) {
        return new java.text.SimpleDateFormat("HH:mm:ss", Locale.ROOT);
	}
}
