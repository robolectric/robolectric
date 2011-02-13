package com.xtremelabs.robolectric.shadows;

import android.content.pm.ApplicationInfo;

import com.xtremelabs.robolectric.internal.Implements;

/**
 * Shadows the {@code android.content.pm.ApplicationInfo} class. 
 */
@Implements(ApplicationInfo.class)
public class ShadowApplicationInfo extends ShadowPackageItemInfo {

	public String flags;
	
	public void __constructor__() {
	}
	
}
