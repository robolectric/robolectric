package com.xtremelabs.robolectric.shadows;


import android.graphics.Canvas;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.view.Surface;
import android.view.SurfaceHolder;
import com.xtremelabs.robolectric.Robolectric;
import com.xtremelabs.robolectric.WithTestDefaultsRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;

@RunWith(WithTestDefaultsRunner.class)
public class CameraTest {

    private Camera camera;
    private ShadowCamera shadowCamera;

    @Before
    public void setUp() throws Exception {
        camera = Camera.open();
        shadowCamera = Robolectric.shadowOf(camera);
    }

    @Test
    public void testOpen() throws Exception {
        assertThat(camera, notNullValue());
    }

    @Test
    public void testUnlock() throws Exception {
        assertThat(shadowCamera.isLocked(), equalTo(true));
        camera.unlock();
        assertThat(shadowCamera.isLocked(), equalTo(false));
    }

    @Test
    public void testReconnect() throws Exception {
        camera.unlock();
        assertThat(shadowCamera.isLocked(), equalTo(false));
        camera.reconnect();
        assertThat(shadowCamera.isLocked(), equalTo(true));
    }

    @Test
    public void testGetParameters() throws Exception {
        Camera.Parameters parameters = camera.getParameters();
        assertThat(parameters, notNullValue());
        assertThat(parameters.getSupportedPreviewFormats(), notNullValue());
        assertThat(parameters.getSupportedPreviewFormats().size(), not(equalTo(0)));
    }

    @Test
    public void testSetParameters() throws Exception {
        Camera.Parameters parameters = camera.getParameters();
        assertThat(parameters.getPreviewFormat(), equalTo(ImageFormat.NV21));
        parameters.setPreviewFormat(ImageFormat.JPEG);
        camera.setParameters(parameters);
        assertThat(camera.getParameters().getPreviewFormat(), equalTo(ImageFormat.JPEG));
    }

    @Test
    public void testSetPreviewDisplay() throws Exception {
        SurfaceHolder previewSurfaceHolder = new TestSurfaceHolder();
        camera.setPreviewDisplay(previewSurfaceHolder);
        assertThat(shadowCamera.getPreviewDisplay(), sameInstance(previewSurfaceHolder));
    }

    @Test
    public void testStartPreview() throws Exception {
        assertThat(shadowCamera.isPreviewing(), equalTo(false));
        camera.startPreview();
        assertThat(shadowCamera.isPreviewing(), equalTo(true));
    }

    @Test
    public void testStopPreview() throws Exception {
        camera.startPreview();
        assertThat(shadowCamera.isPreviewing(), equalTo(true));
        camera.stopPreview();
        assertThat(shadowCamera.isPreviewing(), equalTo(false));
    }

    @Test
    public void testRelease() throws Exception {
        assertThat(shadowCamera.isReleased(), equalTo(false));
        camera.release();
        assertThat(shadowCamera.isReleased(), equalTo(true));
    }

    private class TestSurfaceHolder implements SurfaceHolder {

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
