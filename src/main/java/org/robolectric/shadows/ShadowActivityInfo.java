package org.robolectric.shadows;

import org.robolectric.internal.Implementation;
import org.robolectric.internal.Implements;
import org.robolectric.internal.RealObject;

import android.content.pm.ActivityInfo;

@Implements(ActivityInfo.class)
public class ShadowActivityInfo {
	
	@RealObject
	private ActivityInfo realInfo;
	
	@Implementation
	public String toString() {
		return realInfo.name;
	}

}
