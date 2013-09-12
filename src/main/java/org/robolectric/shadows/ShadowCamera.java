package org.robolectric.shadows;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import org.robolectric.Robolectric;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Shadows the Android {@code Camera} class.
 */
@Implements(Camera.class)
public class ShadowCamera {

  private static int lastOpenedCameraId;

  private int id;
  private boolean locked;
  private boolean previewing;
  private boolean released;
  private Camera.Parameters parameters;
  private Camera.PreviewCallback previewCallback;
  private SurfaceHolder surfaceHolder;
  private int displayOrientation;
  private Camera.AutoFocusCallback autoFocusCallback;

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
    lastOpenedCameraId = 0;
    Camera camera = Robolectric.newInstanceOf(Camera.class);
    Robolectric.shadowOf(camera).id = 0;
    return camera;
  }

  @Implementation
  public static Camera open(int cameraId) {
    lastOpenedCameraId = cameraId;
    Camera camera = Robolectric.newInstanceOf(Camera.class);
    Robolectric.shadowOf(camera).id = cameraId;
    return camera;
  }

  public static int getLastOpenedCameraId() {
    return lastOpenedCameraId;
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
  public void setDisplayOrientation(int degrees) {
    displayOrientation = degrees;
    if (cameras.containsKey(id)) {
      cameras.get(id).orientation = degrees;
    }
  }

  public int getDisplayOrientation() {
    return displayOrientation;
  }

  @Implementation
  public void autoFocus(Camera.AutoFocusCallback callback) {
    autoFocusCallback = callback;
  }

  public boolean hasRequestedAutoFocus() {
    return autoFocusCallback != null;
  }

  public void invokeAutoFocusCallback(boolean success, Camera camera) {
    if (autoFocusCallback == null) {
      throw new IllegalStateException(
          "cannot invoke AutoFocusCallback before autoFocus has been called.");
    }
    autoFocusCallback.onAutoFocus(success, camera);
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

  /**
   * Shadows the Android {@code Camera.Parameters} class.
   */
  @Implements(Camera.Parameters.class)
  public static class ShadowParameters {

    private int pictureWidth = 1280;
    private int pictureHeight = 960;
    private int previewWidth = 640;
    private int previewHeight = 480;
    private int previewFormat = ImageFormat.NV21;
    private int previewFpsMin = 10;
    private int previewFpsMax = 30;
    private int previewFps = 30;

    @Implementation
    public Camera.Size getPictureSize() {
      Camera.Size pictureSize = Robolectric.newInstanceOf(Camera.class).new Size(0, 0);
      pictureSize.width = pictureWidth;
      pictureSize.height = pictureHeight;
      return pictureSize;
    }

    @Implementation
    public int getPreviewFormat() {
      return previewFormat;
    }

    @Implementation
    public void getPreviewFpsRange(int[] range) {
      range[0] = previewFpsMin;
      range[1] = previewFpsMax;
    }

    @Implementation
    public int getPreviewFrameRate() {
      return previewFps;
    }

    @Implementation
    public Camera.Size getPreviewSize() {
      Camera.Size previewSize = Robolectric.newInstanceOf(Camera.class).new Size(0, 0);
      previewSize.width = previewWidth;
      previewSize.height = previewHeight;
      return previewSize;
    }

    @Implementation
    public List<Camera.Size> getSupportedPictureSizes() {
      List<Camera.Size> supportedSizes = new ArrayList<Camera.Size>();
      addSize(supportedSizes, 320, 240);
      addSize(supportedSizes, 640, 480);
      addSize(supportedSizes, 800, 600);
      return supportedSizes;
    }

    @Implementation
    public List<Integer> getSupportedPictureFormats() {
      List<Integer> formats = new ArrayList<Integer>();
      formats.add(ImageFormat.NV21);
      formats.add(ImageFormat.JPEG);
      return formats;
    }

    @Implementation
    public List<Integer> getSupportedPreviewFormats() {
      List<Integer> formats = new ArrayList<Integer>();
      formats.add(ImageFormat.NV21);
      formats.add(ImageFormat.JPEG);
      return formats;
    }

    @Implementation
    public List<int[]> getSupportedPreviewFpsRange() {
      List<int[]> supportedRanges = new ArrayList<int[]>();
      addRange(supportedRanges, 15000, 15000);
      addRange(supportedRanges, 10000, 30000);
      return supportedRanges;
    }

    @Implementation
    public List<Integer> getSupportedPreviewFrameRates() {
      List<Integer> supportedRates = new ArrayList<Integer>();
      supportedRates.add(10);
      supportedRates.add(15);
      supportedRates.add(30);
      return supportedRates;
    }

    @Implementation
    public List<Camera.Size> getSupportedPreviewSizes() {
      List<Camera.Size> supportedSizes = new ArrayList<Camera.Size>();
      addSize(supportedSizes, 320, 240);
      addSize(supportedSizes, 640, 480);
      return supportedSizes;
    }

    @Implementation
    public void setPictureSize(int width, int height) {
      pictureWidth = width;
      pictureHeight = height;
    }

    @Implementation
    public void setPreviewFormat(int pixel_format) {
      previewFormat = pixel_format;
    }

    @Implementation
    public void setPreviewFpsRange(int min, int max) {
      previewFpsMin = min;
      previewFpsMax = max;
    }

    @Implementation
    public void setPreviewFrameRate(int fps) {
      previewFps = fps;
    }

    @Implementation
    public void setPreviewSize(int width, int height) {
      previewWidth = width;
      previewHeight = height;
    }

    public int getPreviewWidth() {
      return previewWidth;
    }

    public int getPreviewHeight() {
      return previewHeight;
    }

    public int getPictureWidth() {
      return pictureWidth;
    }

    public int getPictureHeight() {
      return pictureHeight;
    }

    private void addSize(List<Camera.Size> sizes, int width, int height) {
      Camera.Size newSize = Robolectric.newInstanceOf(Camera.class).new Size(0, 0);
      newSize.width = width;
      newSize.height = height;
      sizes.add(newSize);
    }

    private void addRange(List<int[]> ranges, int min, int max) {
      int[] range = new int[2];
      range[0] = min;
      range[1] = max;
      ranges.add(range);
    }

  }

  /**
   * Shadow for the Android {@code Camera.Size} value object.
   */
  @Implements(Camera.Size.class)
  public static class ShadowSize {
    @RealObject private Camera.Size realCameraSize;

    public void __constructor__(Camera camera, int width, int height) {
      realCameraSize.width = width;
      realCameraSize.height = height;
    }
  }
}
