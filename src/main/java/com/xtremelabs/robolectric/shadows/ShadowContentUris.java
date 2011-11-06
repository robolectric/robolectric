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

	@Implementation
	public static long parseId(Uri contentUri) {
		if (!contentUri.isHierarchical()) {
			throw new UnsupportedOperationException();
		}
		String path = contentUri.getLastPathSegment();
		if (path == null) return -1;
		return Long.parseLong(path);
	}

}
