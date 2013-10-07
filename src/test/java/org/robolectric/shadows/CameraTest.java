package org.robolectric.shadows;


import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.view.Surface;
import android.view.SurfaceHolder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.TestRunners;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Fail.fail;

@RunWith(TestRunners.WithDefaults.class)
public class CameraTest {

  private Camera camera;
  private ShadowCamera shadowCamera;

  @Before
  public void setUp() throws Exception {
    camera = Camera.open();
    shadowCamera = Robolectric.shadowOf(camera);
  }

  @After
  public void tearDown() throws Exception {
    ShadowCamera.clearCameraInfo();
  }

  @Test
  public void testOpen() throws Exception {
    assertThat(camera).isNotNull();
    assertThat(ShadowCamera.getLastOpenedCameraId()).isEqualTo(0);
  }

  @Test
  public void testOpenWithId() throws Exception {
    camera = Camera.open(12);
    assertThat(camera).isNotNull();
    assertThat(ShadowCamera.getLastOpenedCameraId()).isEqualTo(12);
  }

  @Test
  public void testUnlock() throws Exception {
    assertThat(shadowCamera.isLocked()).isTrue();
    camera.unlock();
    assertThat(shadowCamera.isLocked()).isFalse();
  }

  @Test
  public void testReconnect() throws Exception {
    camera.unlock();
    assertThat(shadowCamera.isLocked()).isFalse();
    camera.reconnect();
    assertThat(shadowCamera.isLocked()).isTrue();
  }

  @Test
  public void testGetParameters() throws Exception {
    Camera.Parameters parameters = camera.getParameters();
    assertThat(parameters).isNotNull();
    assertThat(parameters.getSupportedPreviewFormats()).isNotNull();
    assertThat(parameters.getSupportedPreviewFormats().size()).isNotEqualTo(0);
  }

  @Test
  public void testSetParameters() throws Exception {
    Camera.Parameters parameters = camera.getParameters();
    assertThat(parameters.getPreviewFormat()).isEqualTo(ImageFormat.NV21);
    parameters.setPreviewFormat(ImageFormat.JPEG);
    camera.setParameters(parameters);
    assertThat(camera.getParameters().getPreviewFormat()).isEqualTo(ImageFormat.JPEG);
  }

  @Test
  public void testSetPreviewDisplay() throws Exception {
    SurfaceHolder previewSurfaceHolder = new TestSurfaceHolder();
    camera.setPreviewDisplay(previewSurfaceHolder);
    assertThat(shadowCamera.getPreviewDisplay()).isSameAs(previewSurfaceHolder);
  }

  @Test
  public void testStartPreview() throws Exception {
    assertThat(shadowCamera.isPreviewing()).isFalse();
    camera.startPreview();
    assertThat(shadowCamera.isPreviewing()).isTrue();
  }

  @Test
  public void testStopPreview() throws Exception {
    camera.startPreview();
    assertThat(shadowCamera.isPreviewing()).isTrue();
    camera.stopPreview();
    assertThat(shadowCamera.isPreviewing()).isFalse();
  }

  @Test
  public void testRelease() throws Exception {
    assertThat(shadowCamera.isReleased()).isFalse();
    camera.release();
    assertThat(shadowCamera.isReleased()).isTrue();
  }

  @Test
  public void testSetPreviewCallbacks() throws Exception {
    TestPreviewCallback callback = new TestPreviewCallback();
    assertThat(callback.camera).isNull();
    assertThat(callback.data).isNull();

    camera.setPreviewCallback(callback);
    shadowCamera.invokePreviewCallback("foobar".getBytes());

    assertThat(callback.camera).isSameAs(camera);
    assertThat(callback.data).isEqualTo("foobar".getBytes());
  }

  @Test
  public void testSetOneShotPreviewCallbacks() throws Exception {
    TestPreviewCallback callback = new TestPreviewCallback();
    assertThat(callback.camera).isNull();
    assertThat(callback.data).isNull();

    camera.setOneShotPreviewCallback(callback);
    shadowCamera.invokePreviewCallback("foobar".getBytes());

    assertThat(callback.camera).isSameAs(camera);
    assertThat(callback.data).isEqualTo("foobar".getBytes());
  }

  @Test
  public void testPreviewCallbacksWithBuffers() throws Exception {
    TestPreviewCallback callback = new TestPreviewCallback();
    assertThat(callback.camera).isNull();
    assertThat(callback.data).isNull();

    camera.setPreviewCallbackWithBuffer(callback);
    shadowCamera.invokePreviewCallback("foobar".getBytes());

    assertThat(callback.camera).isSameAs(camera);
    assertThat(callback.data).isEqualTo("foobar".getBytes());
  }

  @Test
  public void testClearPreviewCallback() throws Exception {
    TestPreviewCallback callback = new TestPreviewCallback();
    assertThat(callback.camera).isNull();
    assertThat(callback.data).isNull();

    camera.setPreviewCallback(callback);
    camera.setPreviewCallback(null);

    shadowCamera.invokePreviewCallback("foobar".getBytes());
    assertThat(callback.camera).isNull();
    assertThat(callback.data).isNull();

    camera.setOneShotPreviewCallback(callback);
    camera.setOneShotPreviewCallback(null);

    shadowCamera.invokePreviewCallback("foobar".getBytes());
    assertThat(callback.camera).isNull();
    assertThat(callback.data).isNull();

    camera.setPreviewCallbackWithBuffer(callback);
    camera.setPreviewCallbackWithBuffer(null);

    shadowCamera.invokePreviewCallback("foobar".getBytes());
    assertThat(callback.camera).isNull();
    assertThat(callback.data).isNull();
  }

  @Test
  public void testDisplayOrientation() {
    camera.setDisplayOrientation(180);
    assertThat(shadowCamera.getDisplayOrientation()).isEqualTo(180);
  }

  @Test
  public void testSetDisplayOrientationUpdatesCameraInfos() {
    addBackCamera();
    addFrontCamera();

    camera = Camera.open(1);
    camera.setDisplayOrientation(180);

    Camera.CameraInfo cameraQuery = new Camera.CameraInfo();
    Camera.getCameraInfo(ShadowCamera.getLastOpenedCameraId(), cameraQuery);
    assertThat(cameraQuery.orientation).isEqualTo(180);
  }

  @Test
  public void testAutoFocus() {
    assertThat(shadowCamera.hasRequestedAutoFocus()).isEqualTo(false);
    TestAutoFocusCallback callback = new TestAutoFocusCallback();

    camera.autoFocus(callback);

    assertThat(shadowCamera.hasRequestedAutoFocus()).isEqualTo(true);
    shadowCamera.invokeAutoFocusCallback(true, camera);
    assertThat(callback.success).isEqualTo(true);
    assertThat(callback.camera).isEqualTo(camera);
  }

  @Test
  public void testInvokeAutoFocusCallbackMissing() {
    try {
      shadowCamera.invokeAutoFocusCallback(true, camera);
      fail("expected an IllegalStateException");
    } catch (IllegalStateException e) {
      // expected
    }
  }

  @Test
  public void testCameraInfoNoCameras() throws Exception {
    assertThat(Camera.getNumberOfCameras()).isEqualTo(0);
  }

  @Test
  public void testCameraInfoBackOnly() throws Exception {
    Camera.CameraInfo cameraQuery = new Camera.CameraInfo();

    addBackCamera();
    Camera.getCameraInfo(0, cameraQuery);

    assertThat(Camera.getNumberOfCameras()).isEqualTo(1);
    assertThat(cameraQuery.facing).isEqualTo(Camera.CameraInfo.CAMERA_FACING_BACK);
    assertThat(cameraQuery.orientation).isEqualTo(0);
  }

  @Test
  public void testCameraInfoBackAndFront() throws Exception {
    Camera.CameraInfo cameraQuery = new Camera.CameraInfo();
    addBackCamera();
    addFrontCamera();

    assertThat(Camera.getNumberOfCameras()).isEqualTo(2);
    Camera.getCameraInfo(0, cameraQuery);
    assertThat(cameraQuery.facing).isEqualTo(Camera.CameraInfo.CAMERA_FACING_BACK);
    assertThat(cameraQuery.orientation).isEqualTo(0);
    Camera.getCameraInfo(1, cameraQuery);
    assertThat(cameraQuery.facing).isEqualTo(Camera.CameraInfo.CAMERA_FACING_FRONT);
    assertThat(cameraQuery.orientation).isEqualTo(90);
  }

  private void addBackCamera() {
    Camera.CameraInfo frontCamera = new Camera.CameraInfo();
    frontCamera.facing = Camera.CameraInfo.CAMERA_FACING_BACK;
    frontCamera.orientation = 0;
    ShadowCamera.addCameraInfo(0, frontCamera);
  }

  private void addFrontCamera() {
    Camera.CameraInfo backCamera = new Camera.CameraInfo();
    backCamera.facing = Camera.CameraInfo.CAMERA_FACING_FRONT;
    backCamera.orientation = 90;
    ShadowCamera.addCameraInfo(1, backCamera);
  }

  private static class TestPreviewCallback implements Camera.PreviewCallback {
    public Camera camera = null;
    public byte[] data = null;

    @Override
    public void onPreviewFrame(byte[] data, Camera camera) {
      this.data = data;
      this.camera = camera;
    }
  }

  private static class TestAutoFocusCallback implements Camera.AutoFocusCallback {
    public boolean success;
    public Camera camera;

    public void onAutoFocus(boolean success, Camera camera) {
      this.success = success;
      this.camera = camera;
    }
  }

  private static class TestSurfaceHolder implements SurfaceHolder {

    @Override
    public void addCallback(Callback callback) {
    }

    @Override
    public Surface getSurface() {
      return null;
    }

    @Override
    public Rect getSurfaceFrame() {
      return null;
    }

    @Override
    public boolean isCreating() {
      return false;
    }

    @Override
    public Canvas lockCanvas() {
      return null;
    }

    @Override
    public Canvas lockCanvas(Rect dirty) {
      return null;
    }

    @Override
    public void removeCallback(Callback callback) {
    }

    @Override
    public void setFixedSize(int width, int height) {
    }

    @Override
    public void setFormat(int format) {
    }

    @Override
    public void setKeepScreenOn(boolean screenOn) {
    }

    @Override
    public void setSizeFromLayout() {
    }

    @Override
    public void setType(int type) {
    }

    @Override
    public void unlockCanvasAndPost(Canvas canvas) {
    }
  }
}
