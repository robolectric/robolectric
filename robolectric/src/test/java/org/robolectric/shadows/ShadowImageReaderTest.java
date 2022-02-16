package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT;
import static com.google.common.truth.Truth.assertThat;
import static org.junit.Assert.assertThrows;

import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.HandlerThread;
import android.view.Surface;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.annotation.Config;

/** Tests for {@link ShadowImageReader}. */
@RunWith(AndroidJUnit4.class)
@Config(minSdk = KITKAT)
public class ShadowImageReaderTest {
  private static final int WIDTH = 640;
  private static final int HEIGHT = 480;
  private static final int IMAGE_FORMAT = ImageFormat.YUV_420_888;
  private static final int MAX_IMAGES = 4;
  private final ImageReader imageReader =
      ImageReader.newInstance(WIDTH, HEIGHT, IMAGE_FORMAT, MAX_IMAGES);

  @Test
  public void newInstance_returnsImageReader() {
    assertThat(imageReader).isNotNull();
  }

  @Test
  public void getWidth_returnsWidth() {
    assertThat(imageReader.getWidth()).isEqualTo(WIDTH);
  }

  @Test
  public void getHeight_returnsHeight() {
    assertThat(imageReader.getHeight()).isEqualTo(HEIGHT);
  }

  @Test
  public void getFormat_returnsFormat() {
    assertThat(imageReader.getImageFormat()).isEqualTo(IMAGE_FORMAT);
  }

  @Test
  public void getMaxImages_returnsMaxImages() {
    assertThat(imageReader.getMaxImages()).isEqualTo(MAX_IMAGES);
  }

  @Test
  public void getSurface_returnsValidSurface() {
    assertThat(imageReader.getSurface()).isNotNull();
    assertThat(imageReader.getSurface().isValid()).isTrue();
  }

  @Test
  public void setOnImageAvailableListener_isInvokedWhenSurfaceIsUpdated() throws Exception {
    AtomicBoolean listenerCalled = new AtomicBoolean(false);
    CountDownLatch latch = new CountDownLatch(1);
    HandlerThread cbHandlerThread = new HandlerThread("CallBackHandlerThread");
    cbHandlerThread.start();
    Handler handler = new Handler(cbHandlerThread.getLooper());

    imageReader.setOnImageAvailableListener(
        (reader) -> {
          listenerCalled.set(true);
          latch.countDown();
        },
        handler);
    postUpdateOnSurface();
    latch.await();

    assertThat(listenerCalled.get()).isTrue();
  }

  private void postUpdateOnSurface() {
    Surface surface = imageReader.getSurface();
    surface.unlockCanvasAndPost(surface.lockCanvas(new Rect()));
  }

  @Test
  public void acquireNextImage_returnsNullImageWithoutSurfaceUpdate() {
    assertThat(imageReader.acquireNextImage()).isNull();
  }

  @Test
  public void acquireLatestImage_returnsNullImageWithoutSurfaceUpdate() {
    assertThat(imageReader.acquireLatestImage()).isNull();
  }

  @Test
  public void acquireNextImage_returnsValidImageWithSurfaceUpdate() {
    postUpdateOnSurface();
    Image image = imageReader.acquireNextImage();

    assertThat(image).isNotNull();
    assertThat(image.getWidth()).isEqualTo(WIDTH);
    assertThat(image.getHeight()).isEqualTo(HEIGHT);
    assertThat(image.getFormat()).isEqualTo(IMAGE_FORMAT);
    assertThat(image.getTimestamp()).isGreaterThan(0);
  }

  @Test
  public void acquireNextImage_throwsWhenImageReaderIsClosed() {
    imageReader.close();

    assertThrows(IllegalStateException.class, imageReader::acquireNextImage);
  }

  @Test
  public void acquireLatestImage_throwsWhenImageReaderIsClosed() {
    imageReader.close();

    assertThrows(IllegalStateException.class, imageReader::acquireLatestImage);
  }

  @Test
  public void acquireNextImage_closingImage() {
    for (int i = 0; i < MAX_IMAGES; i++) {
      postUpdateOnSurface();
      imageReader.acquireNextImage().close();
    }

    postUpdateOnSurface();

    assertThat(imageReader.acquireNextImage()).isNotNull();
  }

  @Test
  public void acquireNextImage_throwsWhenAllImagesAreUsed() {
    for (int i = 0; i < MAX_IMAGES; i++) {
      postUpdateOnSurface();
      imageReader.acquireNextImage();
    }

    assertThrows(IllegalStateException.class, imageReader::acquireNextImage);
  }

  @Test
  public void acquireLatestImage_returnsLatestImage() {
    for (int i = 0; i < MAX_IMAGES; i++) {
      postUpdateOnSurface();
    }

    assertThat(imageReader.acquireLatestImage().getTimestamp()).isEqualTo(MAX_IMAGES);
  }
}
