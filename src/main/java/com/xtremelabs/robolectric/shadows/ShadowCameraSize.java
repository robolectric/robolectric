package com.xtremelabs.robolectric.shadows;

import android.hardware.Camera;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

/**
 * Shadow for the Android {@code Camera.Size} value object.
 */
@Implements(Camera.Size.class)
public class ShadowCameraSize {
    @RealObject private Camera.Size realCameraSize;

    public void __constructor__(Camera camera, int width, int height) {
        realCameraSize.width = width;
        realCameraSize.height = height;
    }
}
