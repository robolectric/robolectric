package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.JELLY_BEAN_MR1;
import static com.google.common.truth.Truth.assertThat;
import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.fail;

import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.view.Surface;
import android.view.SurfaceHolder;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;

@RunWith(AndroidJUnit4.class)
public class ShadowCameraTest {

  private Camera camera;
  private ShadowCamera shadowCamera;

  @Before
  public void setUp() throws Exception {
    camera = Camera.open();
    shadowCamera = Shadows.shadowOf(camera);
  }

  @After
  public void tearDown() {
    ShadowCamera.clearCameraInfo();
  }

  @Test
  public void testOpen() {
    assertThat(camera).isNotNull();
    assertThat(ShadowCamera.getLastOpenedCameraId()).isEqualTo(0);
  }

  @Test
  public void testOpenWithId() {
    camera = Camera.open(12);
    assertThat(camera).isNotNull();
    assertThat(ShadowCamera.getLastOpenedCameraId()).isEqualTo(12);
  }

  @Test
  public void testUnlock() {
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
  public void testGetParameters() {
    Camera.Parameters parameters = camera.getParameters();
    assertThat(parameters).isNotNull();
    assertThat(parameters.getSupportedPreviewFormats()).isNotNull();
    assertThat(parameters.getSupportedPreviewFormats().size()).isNotEqualTo(0);
  }

  @Test
  public void testSetParameters() {
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
    assertThat(shadowCamera.getPreviewDisplay()).isSameInstanceAs(previewSurfaceHolder);
  }

  @Test
  public void testStartPreview() {
    assertThat(shadowCamera.isPreviewing()).isFalse();
    camera.startPreview();
    assertThat(shadowCamera.isPreviewing()).isTrue();
  }

  @Test
  public void testStopPreview() {
    camera.startPreview();
    assertThat(shadowCamera.isPreviewing()).isTrue();
    camera.stopPreview();
    assertThat(shadowCamera.isPreviewing()).isFalse();
  }

  @Test
  public void testRelease() {
    assertThat(shadowCamera.isReleased()).isFalse();
    camera.release();
    assertThat(shadowCamera.isReleased()).isTrue();
  }

  @Test
  public void testSetPreviewCallbacks() {
    TestPreviewCallback callback = new TestPreviewCallback();
    assertThat(callback.camera).isNull();
    assertThat(callback.data).isNull();

    camera.setPreviewCallback(callback);
    shadowCamera.invokePreviewCallback("foobar".getBytes(UTF_8));

    assertThat(callback.camera).isSameInstanceAs(camera);
    assertThat(callback.data).isEqualTo("foobar".getBytes(UTF_8));
  }

  @Test
  public void testSetOneShotPreviewCallbacks() {
    TestPreviewCallback callback = new TestPreviewCallback();
    assertThat(callback.camera).isNull();
    assertThat(callback.data).isNull();

    camera.setOneShotPreviewCallback(callback);
    shadowCamera.invokePreviewCallback("foobar".getBytes(UTF_8));

    assertThat(callback.camera).isSameInstanceAs(camera);
    assertThat(callback.data).isEqualTo("foobar".getBytes(UTF_8));
  }

  @Test
  public void testPreviewCallbacksWithBuffers() {
    TestPreviewCallback callback = new TestPreviewCallback();
    assertThat(callback.camera).isNull();
    assertThat(callback.data).isNull();

    camera.setPreviewCallbackWithBuffer(callback);
    shadowCamera.invokePreviewCallback("foobar".getBytes(UTF_8));

    assertThat(callback.camera).isSameInstanceAs(camera);
    assertThat(callback.data).isEqualTo("foobar".getBytes(UTF_8));
  }

  @Test
  public void testClearPreviewCallback() {
    TestPreviewCallback callback = new TestPreviewCallback();
    assertThat(callback.camera).isNull();
    assertThat(callback.data).isNull();

    camera.setPreviewCallback(callback);
    camera.setPreviewCallback(null);

    shadowCamera.invokePreviewCallback("foobar".getBytes(UTF_8));
    assertThat(callback.camera).isNull();
    assertThat(callback.data).isNull();

    camera.setOneShotPreviewCallback(callback);
    camera.setOneShotPreviewCallback(null);

    shadowCamera.invokePreviewCallback("foobar".getBytes(UTF_8));
    assertThat(callback.camera).isNull();
    assertThat(callback.data).isNull();

    camera.setPreviewCallbackWithBuffer(callback);
    camera.setPreviewCallbackWithBuffer(null);

    shadowCamera.invokePreviewCallback("foobar".getBytes(UTF_8));
    assertThat(callback.camera).isNull();
    assertThat(callback.data).isNull();
  }

  @Test
  public void testAddCallbackBuffer() {
    byte[] buf1 = new byte[0];
    byte[] buf2 = new byte[1];
    camera.addCallbackBuffer(buf1);
    assertThat(shadowCamera.getAddedCallbackBuffers()).containsExactly(buf1);
    camera.addCallbackBuffer(buf2);
    assertThat(shadowCamera.getAddedCallbackBuffers()).containsExactly(buf1, buf2);
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
    assertThat(shadowCamera.hasRequestedAutoFocus()).isFalse();
    TestAutoFocusCallback callback = new TestAutoFocusCallback();

    camera.autoFocus(callback);

    assertThat(shadowCamera.hasRequestedAutoFocus()).isTrue();
    shadowCamera.invokeAutoFocusCallback(true, camera);
    assertThat(callback.success).isEqualTo(true);
    assertThat(callback.camera).isEqualTo(camera);

    assertThat(shadowCamera.hasRequestedAutoFocus()).isFalse();
    try {
      shadowCamera.invokeAutoFocusCallback(true, camera);
      fail("expected an IllegalStateException");
    } catch (IllegalStateException e) {
      // expected
    }
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
  public void testCancelAutoFocus() {
    assertThat(shadowCamera.hasRequestedAutoFocus()).isFalse();
    camera.autoFocus(null);
    assertThat(shadowCamera.hasRequestedAutoFocus()).isTrue();
    camera.cancelAutoFocus();
    assertThat(shadowCamera.hasRequestedAutoFocus()).isFalse();
  }

  @Test
  public void testCameraInfoNoCameras() {
    assertThat(Camera.getNumberOfCameras()).isEqualTo(0);
  }

  @Test
  public void testCameraInfoBackOnly() {
    Camera.CameraInfo cameraQuery = new Camera.CameraInfo();

    addBackCamera();
    Camera.getCameraInfo(0, cameraQuery);

    assertThat(Camera.getNumberOfCameras()).isEqualTo(1);
    assertThat(cameraQuery.facing).isEqualTo(Camera.CameraInfo.CAMERA_FACING_BACK);
    assertThat(cameraQuery.orientation).isEqualTo(0);
  }

  @Test
  public void testCameraInfoBackAndFront() {
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

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void testCameraInfoShutterSound() {
    Camera.CameraInfo cameraQueryCannotDisable = new Camera.CameraInfo();
    Camera.CameraInfo cameraInfoCannotDisable = new Camera.CameraInfo();
    cameraInfoCannotDisable.canDisableShutterSound = false;
    ShadowCamera.addCameraInfo(0, cameraInfoCannotDisable);

    Camera.CameraInfo cameraQueryCanDisable = new Camera.CameraInfo();
    Camera.CameraInfo cameraInfoCanDisable = new Camera.CameraInfo();
    cameraInfoCanDisable.canDisableShutterSound = true;
    ShadowCamera.addCameraInfo(1, cameraInfoCanDisable);

    assertThat(Camera.getNumberOfCameras()).isEqualTo(2);
    Camera.getCameraInfo(0, cameraQueryCannotDisable);
    assertThat(cameraQueryCannotDisable.canDisableShutterSound).isFalse();
    Camera.getCameraInfo(1, cameraQueryCanDisable);
    assertThat(cameraQueryCanDisable.canDisableShutterSound).isTrue();
  }

  @Test
  public void testTakePicture() {
    camera.takePicture(null, null, null);

    TestShutterCallback shutterCallback = new TestShutterCallback();
    TestPictureCallback rawCallback = new TestPictureCallback();
    TestPictureCallback jpegCallback = new TestPictureCallback();
    camera.takePicture(shutterCallback, rawCallback, jpegCallback);

    assertThat(shutterCallback.wasCalled).isTrue();
    assertThat(rawCallback.wasCalled).isTrue();
    assertThat(jpegCallback.wasCalled).isTrue();
  }

  @Test
  @Config(minSdk = JELLY_BEAN_MR1)
  public void testShutterEnabled() {
    Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
    cameraInfo.facing = Camera.CameraInfo.CAMERA_FACING_BACK;
    cameraInfo.canDisableShutterSound = false;
    ShadowCamera.addCameraInfo(0, cameraInfo);

    assertThat(Camera.getNumberOfCameras()).isEqualTo(1);
    assertThat(shadowCamera.enableShutterSound(true)).isTrue();
    assertThat(shadowCamera.enableShutterSound(false)).isFalse();

    cameraInfo.canDisableShutterSound = true;
    assertThat(shadowCamera.enableShutterSound(true)).isTrue();
    assertThat(shadowCamera.enableShutterSound(false)).isTrue();
    assertThat(shadowCamera.enableShutterSound(true)).isTrue();
  }

  private void addBackCamera() {
    Camera.CameraInfo backCamera = new Camera.CameraInfo();
    backCamera.facing = Camera.CameraInfo.CAMERA_FACING_BACK;
    backCamera.orientation = 0;
    ShadowCamera.addCameraInfo(0, backCamera);
  }

  private void addFrontCamera() {
    Camera.CameraInfo frontCamera = new Camera.CameraInfo();
    frontCamera.facing = Camera.CameraInfo.CAMERA_FACING_FRONT;
    frontCamera.orientation = 90;
    ShadowCamera.addCameraInfo(1, frontCamera);
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

    @Override
    public void onAutoFocus(boolean success, Camera camera) {
      this.success = success;
      this.camera = camera;
    }
  }

  private static class TestShutterCallback implements Camera.ShutterCallback {
    public boolean wasCalled;

    @Override
    public void onShutter() {
      wasCalled = true;
    }
  }

  private static class TestPictureCallback implements Camera.PictureCallback {
    public boolean wasCalled;

    @Override
    public void onPictureTaken(byte[] data, Camera camera) {
      wasCalled = true;
    }
  }

  private static class TestSurfaceHolder implements SurfaceHolder {

    @Override
    public void addCallback(Callback callback) {}

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
    public void removeCallback(Callback callback) {}

    @Override
    public void setFixedSize(int width, int height) {}

    @Override
    public void setFormat(int format) {}

    @Override
    public void setKeepScreenOn(boolean screenOn) {}

    @Override
    public void setSizeFromLayout() {}

    @Override
    public void setType(int type) {}

    @Override
    public void unlockCanvasAndPost(Canvas canvas) {}
  }
}
