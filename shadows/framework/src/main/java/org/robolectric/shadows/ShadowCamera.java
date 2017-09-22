package org.robolectric.shadows;

import static org.robolectric.shadow.api.Shadow.newInstanceOf;

import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.view.SurfaceHolder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.robolectric.Shadows;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;

@Implements(Camera.class)
public class ShadowCamera {

  private static int lastOpenedCameraId;

  private int id;
  private boolean locked;
  private boolean previewing;
  private boolean released;
  private Camera.Parameters parameters;
  private Camera.PreviewCallback previewCallback;
  private List<byte[]> callbackBuffers = new ArrayList<>();
  private SurfaceHolder surfaceHolder;
  private int displayOrientation;
  private Camera.AutoFocusCallback autoFocusCallback;
  private boolean autoFocusing;

  private static Map<Integer, Camera.CameraInfo> cameras = new HashMap<>();

  @RealObject
  private Camera realCamera;

  @Implementation
  public void __constructor__() {
    locked = true;
    previewing = false;
    released = false;
  }

  @Implementation
  public static Camera open() {
    lastOpenedCameraId = 0;
    Camera camera = newInstanceOf(Camera.class);
    Shadows.shadowOf(camera).id = 0;
    return camera;
  }

  @Implementation
  public static Camera open(int cameraId) {
    lastOpenedCameraId = cameraId;
    Camera camera = newInstanceOf(Camera.class);
    Shadows.shadowOf(camera).id = cameraId;
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
      parameters = newInstanceOf(Camera.Parameters.class);
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

  @Implementation
  public void addCallbackBuffer(byte[] callbackBuffer) {
    callbackBuffers.add(callbackBuffer);
  }

  public List<byte[]> getAddedCallbackBuffers() {
    return Collections.unmodifiableList(callbackBuffers);
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
    autoFocusing = true;
  }

  @Implementation
  public void cancelAutoFocus() {
    autoFocusCallback = null;
    autoFocusing = false;
  }

  public boolean hasRequestedAutoFocus() {
    return autoFocusing;
  }

  public void invokeAutoFocusCallback(boolean success, Camera camera) {
    if (autoFocusCallback == null) {
      throw new IllegalStateException(
          "cannot invoke AutoFocusCallback before autoFocus() has been called "
              + "or after cancelAutoFocus() has been called "
              + "or after the callback has been invoked.");
    }
    autoFocusCallback.onAutoFocus(success, camera);
    autoFocusCallback = null;
    autoFocusing = false;
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
   * @param id The camera id
   * @param camInfo The CameraInfo
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
    private int exposureCompensation = 0;
    private String focusMode;
    private List<String> supportedFocusModes = new ArrayList<>();

    @Implementation
    public Camera.Size getPictureSize() {
      Camera.Size pictureSize = newInstanceOf(Camera.class).new Size(0, 0);
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
      Camera.Size previewSize = newInstanceOf(Camera.class).new Size(0, 0);
      previewSize.width = previewWidth;
      previewSize.height = previewHeight;
      return previewSize;
    }

    @Implementation
    public List<Camera.Size> getSupportedPictureSizes() {
      List<Camera.Size> supportedSizes = new ArrayList<>();
      addSize(supportedSizes, 320, 240);
      addSize(supportedSizes, 640, 480);
      addSize(supportedSizes, 800, 600);
      return supportedSizes;
    }

    @Implementation
    public List<Integer> getSupportedPictureFormats() {
      List<Integer> formats = new ArrayList<>();
      formats.add(ImageFormat.NV21);
      formats.add(ImageFormat.JPEG);
      return formats;
    }

    @Implementation
    public List<Integer> getSupportedPreviewFormats() {
      List<Integer> formats = new ArrayList<>();
      formats.add(ImageFormat.NV21);
      formats.add(ImageFormat.JPEG);
      return formats;
    }

    @Implementation
    public List<int[]> getSupportedPreviewFpsRange() {
      List<int[]> supportedRanges = new ArrayList<>();
      addRange(supportedRanges, 15000, 15000);
      addRange(supportedRanges, 10000, 30000);
      return supportedRanges;
    }

    @Implementation
    public List<Integer> getSupportedPreviewFrameRates() {
      List<Integer> supportedRates = new ArrayList<>();
      supportedRates.add(10);
      supportedRates.add(15);
      supportedRates.add(30);
      return supportedRates;
    }

    @Implementation
    public List<Camera.Size> getSupportedPreviewSizes() {
      List<Camera.Size> supportedSizes = new ArrayList<>();
      addSize(supportedSizes, 320, 240);
      addSize(supportedSizes, 640, 480);
      return supportedSizes;
    }

    public void setSupportedFocusModes(String... focusModes) {
      supportedFocusModes = Arrays.asList(focusModes);
    }

    @Implementation
    public List<String> getSupportedFocusModes() {
      return supportedFocusModes;
    }

    @Implementation
    public String getFocusMode() {
      return focusMode;
    }

    @Implementation
    public void setFocusMode(String focusMode) {
      this.focusMode = focusMode;
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

    @Implementation
    public int getMinExposureCompensation() {
      return -6;
    }

    @Implementation
    public int getMaxExposureCompensation() {
      return 6;
    }

    @Implementation
    public float getExposureCompensationStep() {
      return 0.5f;
    }

    @Implementation
    public int getExposureCompensation() {
      return exposureCompensation;
    }

    @Implementation
    public void setExposureCompensation(int compensation) {
      exposureCompensation = compensation;
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
      Camera.Size newSize = newInstanceOf(Camera.class).new Size(0, 0);
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

  @Implements(Camera.Size.class)
  public static class ShadowSize {
    @RealObject private Camera.Size realCameraSize;

    @Implementation
    public void __constructor__(Camera camera, int width, int height) {
      realCameraSize.width = width;
      realCameraSize.height = height;
    }
  }
}
