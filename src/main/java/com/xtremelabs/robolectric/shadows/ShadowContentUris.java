package com.xtremelabs.robolectric.shadows;

import android.content.ContentUris;
import android.net.Uri;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(ContentUris.class)
public class ShadowContentUris {

	@Implementation
	public static Uri withAppendedId(Uri contentUri, long id) {
		return Uri.withAppendedPath(contentUri, String.valueOf(id));
	}

}
