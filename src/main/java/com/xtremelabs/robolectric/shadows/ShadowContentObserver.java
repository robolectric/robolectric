package com.xtremelabs.robolectric.shadows;

import android.database.ContentObserver;
import android.net.Uri;

import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.RealObject;

@Implements(ContentObserver.class)
public class ShadowContentObserver {

	@RealObject
	private ContentObserver realObserver;

	@Implementation
	public void dispatchChange( boolean selfChange, Uri uri ) {
		realObserver.onChange(selfChange, uri);
	}
	
	@Implementation
	public void dispatchChange( boolean selfChange ) {
		realObserver.onChange(selfChange);		
	}

}
