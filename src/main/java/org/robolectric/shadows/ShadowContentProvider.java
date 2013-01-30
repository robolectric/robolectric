package com.xtremelabs.robolectric.shadows;

import android.content.ContentProvider;
import android.content.Context;

import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(ContentProvider.class)
public class ShadowContentProvider {

	@Implementation
	public final Context getContext() {
		return Robolectric.application;
	}

}
