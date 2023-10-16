package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static org.robolectric.shadow.api.Shadow.newInstanceOf;

import android.hardware.Camera;
import android.os.Build;
import android.view.SurfaceHolder;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.ReflectionHelpers.ClassParameter;

@Implements(Camera.class)
public class ShadowCamera {
  // These are completely arbitrary and likely outdated default parameters that have been added long
  // ago.
  private static final ImmutableMap<String, String> DEFAULT_PARAMS =
      ImmutableMap.<String, String>builder()
          .put("picture-size", "1280x960")
          .put("preview-size", "640x480")
          .put("preview-fps-range", "10,30")
          .put("preview-frame-rate", "30")
          .put("preview-format", "yuv420sp")
          .put("picture-format-values", "yuv420sp,jpeg")
          .put("preview-format-values", "yuv420sp,jpeg")
          .put("picture-size-values", "320x240,640x480,800x600")
          .put("preview-size-values", "320x240,640x480")
          .put("preview-fps-range-values", "(15000,15000),(10000,30000)")
          .put("preview-frame-rate-values", "10,15,30")
          .put("exposure-compensation", "0")
          .put("exposure-compensation-step", "0.5")
          .put("min-exposure-compensation", "-6")
          .put("max-exposure-compensation", "6")
          .put("focus-mode-values", Camera.Parameters.FOCUS_MODE_AUTO)
          .put("focus-mode", Camera.Parameters.FOCUS_MODE_AUTO)
          .put(
              "flash-mode-values",
              Camera.Parameters.FLASH_MODE_AUTO
                  + ","
                  + Camera.Parameters.FLASH_MODE_ON
                  + ","
                  + Camera.Parameters.FLASH_MODE_OFF)
          .put("flash-mode", Camera.Parameters.FLASH_MODE_AUTO)
          .put("max-num-focus-areas", "1")
          .put("max-num-metering-areas", "1")
          .build();

  private static int lastOpenedCameraId;

  private int id;
  private boolean locked = true;
  private boolean previewing;
  private boolean released;
  private Camera.Parameters parameters;
  private Camera.PreviewCallback previewCallback;
  private List<byte[]> callbackBuffers = new ArrayList<>();
  private SurfaceHolder surfaceHolder;
  private int displayOrientation;
  private Camera.AutoFocusCallback autoFocusCallback;
  private boolean autoFocusing;
  private boolean shutterSoundEnabled = true;

  private static final Map<Integer, Camera.CameraInfo> cameras = new HashMap<>();
  private static final Map<Integer, Camera.Parameters> cameraParameters = new HashMap<>();

  @RealObject private Camera realCamera;

  @Implementation
  protected static Camera open() {
    return open(0);
  }

  @Implementation
  protected static Camera open(int cameraId) {
    lastOpenedCameraId = cameraId;
    Camera camera = newInstanceOf(Camera.class);
    ShadowCamera shadowCamera = Shadow.extract(camera);
    shadowCamera.id = cameraId;
    if (cameraParameters.containsKey(cameraId)) {
      shadowCamera.parameters = cameraParameters.get(cameraId);
    } else {
      cameraParameters.put(cameraId, camera.getParameters());
    }
    return camera;
  }

  public static int getLastOpenedCameraId() {
    return lastOpenedCameraId;
  }

  @Implementation
  protected void unlock() {
    locked = false;
  }

  @Implementation
  protected void reconnect() {
    locked = true;
  }

  @Implementation
  protected Camera.Parameters getParameters() {
    if (parameters == null) {
      parameters =
          ReflectionHelpers.callConstructor(
              Camera.Parameters.class, ClassParameter.from(Camera.class, realCamera));
      Joiner.MapJoiner mapJoiner = Joiner.on(";").withKeyValueSeparator("=");
      parameters.unflatten(mapJoiner.join(DEFAULT_PARAMS));
    }
    return parameters;
  }

  @Implementation
  protected void setParameters(Camera.Parameters params) {
    parameters = params;
  }

  @Implementation
  protected void setPreviewDisplay(SurfaceHolder holder) {
    surfaceHolder = holder;
  }

  @Implementation
  protected void startPreview() {
    previewing = true;
  }

  @Implementation
  protected void stopPreview() {
    previewing = false;
  }

  @Implementation
  protected void release() {
    released = true;
  }

  @Implementation
  protected void setPreviewCallback(Camera.PreviewCallback cb) {
    previewCallback = cb;
  }

  @Implementation
  protected void setOneShotPreviewCallback(Camera.PreviewCallback cb) {
    previewCallback = cb;
  }

  @Implementation
  protected void setPreviewCallbackWithBuffer(Camera.PreviewCallback cb) {
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
  protected void addCallbackBuffer(byte[] callbackBuffer) {
    callbackBuffers.add(callbackBuffer);
  }

  public List<byte[]> getAddedCallbackBuffers() {
    return Collections.unmodifiableList(callbackBuffers);
  }

  @Implementation
  protected void setDisplayOrientation(int degrees) {
    displayOrientation = degrees;
    if (cameras.containsKey(id)) {
      cameras.get(id).orientation = degrees;
    }
  }

  public int getDisplayOrientation() {
    return displayOrientation;
  }

  @Implementation
  protected void autoFocus(Camera.AutoFocusCallback callback) {
    autoFocusCallback = callback;
    autoFocusing = true;
  }

  @Implementation
  protected void cancelAutoFocus() {
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
  protected static void getCameraInfo(int cameraId, Camera.CameraInfo cameraInfo) {
    Camera.CameraInfo foundCam = cameras.get(cameraId);
    cameraInfo.facing = foundCam.facing;
    cameraInfo.orientation = foundCam.orientation;
    // canDisableShutterSound was added in API 17.
    if (Build.VERSION.SDK_INT >= JELLY_BEAN_MR1) {
      cameraInfo.canDisableShutterSound = foundCam.canDisableShutterSound;
    }
  }

  @Implementation
  protected static int getNumberOfCameras() {
    return cameras.size();
  }

  @Implementation
  protected void takePicture(
      Camera.ShutterCallback shutter, Camera.PictureCallback raw, Camera.PictureCallback jpeg) {
    if (shutter != null) {
      shutter.onShutter();
    }

    if (raw != null) {
      raw.onPictureTaken(new byte[0], realCamera);
    }

    if (jpeg != null) {
      jpeg.onPictureTaken(new byte[0], realCamera);
    }
  }

  @Implementation(minSdk = JELLY_BEAN_MR1)
  protected boolean enableShutterSound(boolean enabled) {
    if (!enabled && cameras.containsKey(id) && !cameras.get(id).canDisableShutterSound) {
      return false;
    }
    shutterSoundEnabled = enabled;
    return true;
  }

  /** Returns {@code true} if the default shutter sound is played when taking a picture. */
  public boolean isShutterSoundEnabled() {
    return shutterSoundEnabled;
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
   * Add a mock {@code Camera.CameraInfo} object to simulate the existence of one or more cameras.
   * By default, no cameras are defined.
   *
   * @param id The camera id
   * @param camInfo The CameraInfo
   */
  public static void addCameraInfo(int id, Camera.CameraInfo camInfo) {
    cameras.put(id, camInfo);
  }

  @Resetter
  public static void clearCameraInfo() {
    cameras.clear();
    cameraParameters.clear();
  }

  /** Shadows the Android {@code Camera.Parameters} class. */
  @Implements(Camera.Parameters.class)
  public static class ShadowParameters {

    @SuppressWarnings("nullness:initialization.field.uninitialized") // Managed by Robolectric
    @RealObject
    private Camera.Parameters realParameters;

    public void initSupportedPreviewSizes() {
      realParameters.remove("preview-size-values");
    }

    public void setSupportedFocusModes(String... focusModes) {
      realParameters.set("focus-mode-values", Joiner.on(",").join(focusModes));
      if (focusModes.length == 0) {
        realParameters.remove("focus-mode");
      }
    }

    public void setSupportedFlashModes(String... flashModes) {
      realParameters.set("flash-mode-values", Joiner.on(",").join(flashModes));
      if (flashModes.length == 0) {
        realParameters.remove("flash-mode");
      }
    }

    /**
     * Allows test cases to set the maximum number of focus areas. See {@link
     * Camera.Parameters#getMaxNumFocusAreas}.
     */
    public void setMaxNumFocusAreas(int maxNumFocusAreas) {
      realParameters.set("max-num-focus-areas", maxNumFocusAreas);
    }

    public void addSupportedPreviewSize(int width, int height) {
      List<String> sizesStrings = new ArrayList<>();
      List<Camera.Size> sizes = realParameters.getSupportedPreviewSizes();
      if (sizes == null) {
        sizes = ImmutableList.of();
      }
      for (Camera.Size size : sizes) {
        sizesStrings.add(size.width + "x" + size.height);
      }
      sizesStrings.add(width + "x" + height);
      realParameters.set("preview-size-values", Joiner.on(",").join(sizesStrings));
    }

    /**
     * Allows test cases to set the maximum number of metering areas. See {@link
     * Camera.Parameters#getMaxNumMeteringAreas}.
     */
    public void setMaxNumMeteringAreas(int maxNumMeteringAreas) {
      realParameters.set("max-num-metering-areas", maxNumMeteringAreas);
    }

    public int getPreviewWidth() {
      return realParameters.getPreviewSize().width;
    }

    public int getPreviewHeight() {
      return realParameters.getPreviewSize().height;
    }

    public int getPictureWidth() {
      return realParameters.getPictureSize().width;
    }

    public int getPictureHeight() {
      return realParameters.getPictureSize().height;
    }

    public int getRotation() {
      return realParameters.getInt("rotation");
    }
  }
}
