package com.xtremelabs.robolectric.shadows;

import android.hardware.Camera;
import android.view.SurfaceHolder;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

/**
 * Shadows the Android {@code Camera} class.
 */
@Implements(Camera.class)
public class ShadowCamera {

    private boolean locked;
    private boolean previewing;
    private boolean released;
    private Camera.Parameters parameters;
    private SurfaceHolder surfaceHolder;

    public void __constructor__() {
        locked = true;
        previewing = false;
        released = false;
    }

    @Implementation
    public static Camera open() {
        return Robolectric.newInstanceOf(Camera.class);
    }

    @Implementation
    public void unlock() {
        locked = false;
    }

    @Implementation
    public void reconnect() {
        locked = true;
    }

    @Implementation
    public Camera.Parameters getParameters() {
        if (null == parameters) {
            parameters = Robolectric.newInstanceOf(Camera.Parameters.class);
        }
        return parameters;
    }

    @Implementation
    public void setParameters(Camera.Parameters params) {
        parameters = params;
    }

    @Implementation
    public void setPreviewDisplay(SurfaceHolder holder) {
        surfaceHolder = holder;
    }

    @Implementation
    public void startPreview() {
        previewing = true;
    }

    @Implementation
    public void stopPreview() {
        previewing = false;
    }

    @Implementation
    public void release() {
        released = true;
    }

    public boolean isLocked() {
        return locked;
    }

    public boolean isPreviewing() {
        return previewing;
    }

    public boolean isReleased() {
        return released;
    }

    public SurfaceHolder getPreviewDisplay() {
        return surfaceHolder;
    }
}
