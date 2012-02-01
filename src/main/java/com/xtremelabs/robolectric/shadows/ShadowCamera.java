package com.xtremelabs.robolectric.shadows;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.hardware.Camera;
import android.view.SurfaceHolder;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;
import com.xtremelabs.robolectric.internal.RealObject;

/**
 * Shadows the Android {@code Camera} class.
 */
@Implements(Camera.class)
public class ShadowCamera {

    private boolean locked;
    private boolean previewing;
    private boolean released;
    private Camera.Parameters parameters;
    private Camera.PreviewCallback previewCallback;
    private SurfaceHolder surfaceHolder;
    
    private static Map<Integer, Camera.CameraInfo> cameras = new HashMap<Integer,Camera.CameraInfo>();

    @RealObject
    private Camera realCamera;

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

    @Implementation
    public void setPreviewCallback(Camera.PreviewCallback cb) {
        previewCallback = cb;
    }

    @Implementation
    public void setOneShotPreviewCallback(Camera.PreviewCallback cb) {
        previewCallback = cb;
    }

    @Implementation
    public void setPreviewCallbackWithBuffer(Camera.PreviewCallback cb) {
        previewCallback = cb;
    }
    
    @Implementation
    public static void getCameraInfo(int cameraId, Camera.CameraInfo cameraInfo ) {
    	Camera.CameraInfo foundCam = cameras.get( cameraId );
    	cameraInfo.facing = foundCam.facing;
    	cameraInfo.orientation = foundCam.orientation;
    }
    
    @Implementation
    public static int getNumberOfCameras() {
    	return cameras.size();
    }

    /**
     * Allows test cases to invoke the preview callback, to simulate a frame of camera data.
     *
     * @param data byte buffer of simulated camera data
     */
    public void invokePreviewCallback(byte[] data) {
        if (previewCallback != null) {
            previewCallback.onPreviewFrame(data, realCamera);
        }
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
    
    /**
     * Add a mock {@code Camera.CameraInfo} object to simulate
     * the existence of one or more cameras.  By default, no
     * cameras are defined.
     * 
     * @param id
     * @param camInfo
     */
    public static void addCameraInfo(int id, Camera.CameraInfo camInfo) {
    	cameras.put(id, camInfo); 
    }
    
    public static void clearCameraInfo() {
    	cameras.clear();
    }

}
