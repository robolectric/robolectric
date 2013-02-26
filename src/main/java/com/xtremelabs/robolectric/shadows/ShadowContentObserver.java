package com.xtremelabs.robolectric.shadows;

import android.database.ContentObserver;
import android.net.Uri;
import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;
import org.robolectric.internal.RealObject;


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
