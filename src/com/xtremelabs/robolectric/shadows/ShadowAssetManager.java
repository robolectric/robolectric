package com.xtremelabs.robolectric.shadows;

import java.io.InputStream;

import android.content.res.AssetManager;
import com.xtremelabs.robolectric.util.Implementation;
import com.xtremelabs.robolectric.util.Implements;

@Implements(AssetManager.class)
public final class ShadowAssetManager {
	
	public ShadowAssetManager() {
	}
	
	public void __constructor__(){
	}

	@Implementation
	public final String[] list(String path) {
		// TODO implement
		return null;
	}

	@Implementation
	public final InputStream open(String fileName, int accessMode) {
		// TODO implement
		return null;
	}

	@Implementation
	public final InputStream open(String fileName) {
		return open(fileName, AssetManager.ACCESS_STREAMING);
	}

	@Implementation
	public String toString() {
		// TODO remove once first tests pass
		return "hello ShadowAssetManager!";
	}

}
