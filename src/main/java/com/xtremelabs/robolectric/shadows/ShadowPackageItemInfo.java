package com.xtremelabs.robolectric.shadows;

import android.content.pm.PackageItemInfo;

import com.xtremelabs.robolectric.internal.Implements;

/**
 * Shadows the {@code android.content.pm.PackageItemInfo} class.
 */
@Implements(PackageItemInfo.class)
public class ShadowPackageItemInfo {

	public String packageName;
	
	public void __constructor__() {
		packageName = "";
	}
	
}
