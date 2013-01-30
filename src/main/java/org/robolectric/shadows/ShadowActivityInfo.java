package com.xtremelabs.robolectric.shadows;

import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

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
