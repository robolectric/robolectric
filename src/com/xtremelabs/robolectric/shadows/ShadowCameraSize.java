package com.xtremelabs.robolectric.shadows;

import com.xtremelabs.robolectric.util.Implements;
import com.xtremelabs.robolectric.util.RealObject;

import android.hardware.Camera;

/**
 * Shadow for the Android {@code Camera.Size} value object.
 *
 */
@Implements(Camera.Size.class)
public class ShadowCameraSize {
	  @RealObject private Camera.Size realCameraSize;
	  
	  // TODO not being invoked -- why?  Also causes RobolectrictricWiringTest to fail.
/*	  public void __constructor__(int width, int height) {
		  realCameraSize.width = width;
		  realCameraSize.height = height;
	  }*/
	  
}
