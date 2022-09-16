package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.KITKAT;
import static android.os.Build.VERSION_CODES.S_V2;
import static android.os.Build.VERSION_CODES.TIRAMISU;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.media.Image;
import android.media.ImageReader;
import android.media.ImageReader.OnImageAvailableListener;
import android.os.Handler;
import android.view.Surface;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.ForType;

/** Shadow for {@link android.media.ImageReader} */
@Implements(ImageReader.class)
public class ShadowImageReader {
  // Using same return codes as ImageReader.
  private static final int ACQUIRE_SUCCESS = 0;
  private static final int ACQUIRE_NO_BUFS = 1;
  private static final int ACQUIRE_MAX_IMAGES = 2;
  private final AtomicLong imageCount = new AtomicLong(0);
  private final AtomicBoolean readerValid = new AtomicBoolean(true);
  private final AtomicLong availableBuffers = new AtomicLong(0);
  private final List<Image> openedImages = new ArrayList<>();
  private Surface surface;
  @RealObject private ImageReader imageReader;
  private Canvas canvas;

  @Implementation(minSdk = KITKAT)
  protected void close() {
    readerValid.set(false);
    openedImages.clear();
  }

  @Implementation(minSdk = KITKAT, maxSdk = S_V2)
  protected int nativeImageSetup(Image image) {
    if (!readerValid.get()) {
      throw new IllegalStateException("ImageReader closed.");
    }
    if (openedImages.size() >= imageReader.getMaxImages()) {
      return ACQUIRE_MAX_IMAGES;
    }
    if (availableBuffers.get() == 0) {
      return ACQUIRE_NO_BUFS;
    }
    availableBuffers.getAndDecrement();
    openedImages.add(image);
    ShadowSurfaceImage shadowSurfaceImage = Shadow.extract(image);
    shadowSurfaceImage.setTimeStamp(imageCount.get());
    return ACQUIRE_SUCCESS;
  }

  @Implementation(minSdk = TIRAMISU)
  protected int nativeImageSetup(Image image, boolean useLegacyImageFormat) {
    return nativeImageSetup(image);
  }

  @Implementation(minSdk = KITKAT)
  protected void nativeReleaseImage(Image i) {
    openedImages.remove(i);
  }

  @Implementation(minSdk = KITKAT)
  protected Surface nativeGetSurface() {
    if (surface == null) {
      surface = new FakeSurface();
    }
    return surface;
  }

  private class FakeSurface extends Surface {
    public FakeSurface() {}

    @Override
    public Canvas lockCanvas(Rect inOutDirty) {
      if (canvas == null) {
        canvas = new Canvas();
      }
      return canvas;
    }

    @Override
    public Canvas lockHardwareCanvas() {
      if (canvas == null) {
        canvas = new Canvas();
      }
      return canvas;
    }

    @Override
    public void unlockCanvasAndPost(Canvas canvas) {
      availableBuffers.getAndIncrement();
      imageCount.getAndIncrement();
      OnImageAvailableListener listener =
          reflector(ImageReaderReflector.class, imageReader).getListener();
      Handler handler = reflector(ImageReaderReflector.class, imageReader).getListenerHandler();
      if (listener == null) {
        return;
      }
      if (handler == null) {
        Objects.requireNonNull(listener).onImageAvailable(imageReader);
        return;
      }

      Objects.requireNonNull(handler)
          .post(() -> Objects.requireNonNull(listener).onImageAvailable(imageReader));
    }
  }

  /** Shadow for {@link android.media.Image} */
  @Implements(className = "android.media.ImageReader$SurfaceImage")
  public static class ShadowSurfaceImage {
    @RealObject Object surfaceImage;

    @Implementation(minSdk = KITKAT)
    protected int getWidth() {
      ImageReader reader = ReflectionHelpers.getField(surfaceImage, "this$0");
      return reader.getWidth();
    }

    @Implementation(minSdk = KITKAT)
    protected int getHeight() {
      ImageReader reader = ReflectionHelpers.getField(surfaceImage, "this$0");
      return reader.getHeight();
    }

    @Implementation(minSdk = KITKAT)
    protected int getFormat() {
      ImageReader reader = ReflectionHelpers.getField(surfaceImage, "this$0");
      return reader.getImageFormat();
    }

    public void setTimeStamp(long timestamp) {
      ReflectionHelpers.setField(surfaceImage, "mTimestamp", timestamp);
    }
  }

  @ForType(ImageReader.class)
  interface ImageReaderReflector {
    @Accessor("mListener")
    OnImageAvailableListener getListener();

    @Accessor("mListenerHandler")
    Handler getListenerHandler();
  }
}
