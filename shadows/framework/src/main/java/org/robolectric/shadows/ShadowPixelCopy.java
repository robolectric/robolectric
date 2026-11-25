package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;
import static com.google.common.base.Preconditions.checkState;
import static java.util.Objects.requireNonNull;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorSpace;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.Image;
import android.media.ImageReader;
import android.os.Handler;
import android.os.Looper;
import android.view.PixelCopy;
import android.view.PixelCopy.OnPixelCopyFinishedListener;
import android.view.PixelCopy.Request;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewRootImpl;
import android.view.Window;
import android.view.WindowManagerGlobal;
import java.util.concurrent.Executor;
import java.util.function.Consumer;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import org.robolectric.annotation.ClassName;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowWindowManagerGlobal.WindowManagerGlobalReflector;
import org.robolectric.util.PerfStatsCollector;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Constructor;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;

/**
 * Shadow for PixelCopy that uses View.draw to create screenshots. The real PixelCopy performs a
 * full hardware capture of the screen at the given location, which is impossible in Robolectric.
 *
 * <p>If listenerThread is backed by a paused looper, make sure to call ShadowLooper.idle() to
 * ensure the screenshot finishes.
 */
@Implements(value = PixelCopy.class, minSdk = O)
public class ShadowPixelCopy {

  @Implementation
  protected static void request(
      SurfaceView source,
      @Nonnull Bitmap dest,
      @Nonnull OnPixelCopyFinishedListener listener,
      @Nonnull Handler listenerThread) {
    if (ShadowView.areRealDrawTraversalsEnabled() && hasRealSurface(source)) {
      // when realViewDrawTraversals are on, the view will be backed by a valid Surface.
      // so just use that
      reflector(PixelCopyReflector.class).request(source, dest, listener, listenerThread);
    } else {
      takeScreenshot(source, dest, null);
      alertFinished(listener, listenerThread, PixelCopy.SUCCESS);
    }
  }

  @Implementation
  protected static void request(
      @Nonnull SurfaceView source,
      @Nullable Rect srcRect,
      @Nonnull Bitmap dest,
      @Nonnull OnPixelCopyFinishedListener listener,
      @Nonnull Handler listenerThread) {
    if (ShadowView.areRealDrawTraversalsEnabled() && hasRealSurface(source)) {
      // when realViewDrawTraversals are on, the view will be backed by a valid Surface.
      // so just use that
      reflector(PixelCopyReflector.class).request(source, srcRect, dest, listener, listenerThread);
    } else {
      if (srcRect != null && srcRect.isEmpty()) {
        throw new IllegalArgumentException("sourceRect is empty");
      }
      takeScreenshot(source, dest, srcRect);
      alertFinished(listener, listenerThread, PixelCopy.SUCCESS);
    }
  }

  @Implementation
  protected static void request(
      @Nonnull Window source,
      @Nonnull Bitmap dest,
      @Nonnull OnPixelCopyFinishedListener listener,
      @Nonnull Handler listenerThread) {
    request(source, null, dest, listener, listenerThread);
  }

  @Implementation
  protected static void request(
      @Nonnull Window source,
      @Nullable Rect srcRect,
      @Nonnull Bitmap dest,
      @Nonnull OnPixelCopyFinishedListener listener,
      @Nonnull Handler listenerThread) {
    if (ShadowView.areRealDrawTraversalsEnabled()) {
      // when realViewDrawTraversals are on, the view will be backed by a valid Surface.
      // so just use that
      reflector(PixelCopyReflector.class).request(source, srcRect, dest, listener, listenerThread);
    } else {
      if (srcRect != null && srcRect.isEmpty()) {
        throw new IllegalArgumentException("sourceRect is empty");
      }
      takeScreenshot(source.getDecorView(), dest, srcRect);
      alertFinished(listener, listenerThread, PixelCopy.SUCCESS);
    }
  }

  @Implementation
  protected static void request(
      @Nonnull Surface source,
      @Nullable Rect srcRect,
      @Nonnull Bitmap dest,
      @Nonnull OnPixelCopyFinishedListener listener,
      @Nonnull Handler listenerThread) {
    if (srcRect != null && srcRect.isEmpty()) {
      throw new IllegalArgumentException("sourceRect is empty");
    }
    if (ShadowView.areRealDrawTraversalsEnabled()) {
      captureImageFromSurface(source, dest, srcRect);
    } else {
      View view = findViewForSurface(requireNonNull(source));

      Rect adjustedSrcRect = null;
      if (srcRect != null) {
        adjustedSrcRect = new Rect(srcRect);
        int[] locationInSurface = ShadowView.getLocationInSurfaceCompat(view);
        // offset the srcRect by the decor view's location in the surface
        adjustedSrcRect.offset(-locationInSurface[0], -locationInSurface[1]);
      }
      takeScreenshot(view, dest, adjustedSrcRect);
    }
    alertFinished(listener, listenerThread, PixelCopy.SUCCESS);
  }

  @Implementation(minSdk = UPSIDE_DOWN_CAKE)
  protected static void request(
      @ClassName("android.view.PixelCopy$Request") Object requestObject,
      Executor callbackExecutor,
      Consumer</*android.view.PixelCopy$Result*/ ?> listener) {
    PixelCopy.Request request = (PixelCopy.Request) requestObject;
    RequestReflector requestReflector = reflector(RequestReflector.class, request);
    //noinspection Convert2Lambda
    OnPixelCopyFinishedListener legacyListener =
        new OnPixelCopyFinishedListener() {
          @Override
          public void onPixelCopyFinished(int copyResult) {
            ((Consumer<PixelCopy.Result>) listener)
                .accept(
                    reflector(ResultReflector.class)
                        .newResult(copyResult, request.getDestinationBitmap()));
          }
        };
    Rect adjustedSrcRect =
        reflector(PixelCopyReflector.class)
            .adjustSourceRectForInsets(requestReflector.getSourceInsets(), request.getSourceRect());
    PixelCopy.request(
        requestReflector.getSource(),
        adjustedSrcRect,
        request.getDestinationBitmap(),
        legacyListener,
        new Handler(Looper.getMainLooper()));
  }

  /**
   * Returns true if the given SurfaceView's holder provides a Surface whose content is produced by
   * real draw traversals.
   */
  private static boolean hasRealSurface(SurfaceView source) {
    Surface surface = source.getHolder().getSurface();
    if (surface == null) {
      return false;
    }
    Object shadowSurface = Shadow.extract(surface);
    return shadowSurface instanceof ShadowNativeSurface
        && ((ShadowNativeSurface) shadowSurface).getContainerImageReader() != null;
  }

  private static View findViewForSurface(Surface source) {
    for (View windowView :
        reflector(WindowManagerGlobalReflector.class, WindowManagerGlobal.getInstance())
            .getWindowViews()) {
      ShadowViewRootImpl shadowViewRoot = Shadow.extract(windowView.getViewRootImpl());
      if (source.equals(shadowViewRoot.getSurface())) {
        return windowView;
      }
    }

    throw new IllegalArgumentException(
        "Could not find view for surface. Is it attached to a window?");
  }

  /**
   * Method to obtain a Bitmap when using real View draw traversals. This method just takes the
   * image already produced by the draw traversal as opposed to doing a re-draw.
   */
  static void captureImageFromSurface(Surface surface, Bitmap destBitmap, @Nullable Rect srcRect) {
    validateBitmap(destBitmap);
    checkState(Looper.getMainLooper().isCurrentThread());

    ShadowNativeSurface shadowSurface = Shadow.extract(surface);
    ImageReader imageReader = shadowSurface.getContainerImageReader();

    if (doDimensionsMatch(destBitmap, imageReader.getWidth(), imageReader.getHeight(), srcRect)) {
      // fast path, just copy to destination screenshot, no need to resize
      copyFromImageReader(imageReader, destBitmap);
    } else {
      // need to resize bitmap
      Bitmap intermediateBitmap =
          Bitmap.createBitmap(
              imageReader.getWidth(), imageReader.getHeight(), Bitmap.Config.ARGB_8888);

      try {
        copyFromImageReader(imageReader, intermediateBitmap);
        drawScreenshot(intermediateBitmap, srcRect, destBitmap);
      } finally {
        intermediateBitmap.recycle();
      }
    }
  }

  /** Determine if the source dimensions exactly match the given bitmap and cropping srcRect */
  private static boolean doDimensionsMatch(
      Bitmap destBitmap, int sourceWidth, int sourceHeight, @Nullable Rect srcRect) {
    if (destBitmap.getWidth() != sourceWidth) {
      return false;
    }
    if (destBitmap.getHeight() != sourceHeight) {
      return false;
    }
    if (srcRect == null) {
      // abort - srcRect is null, dimensions match!
      return true;
    }
    if (srcRect.left != 0) {
      return false;
    }
    if (srcRect.top != 0) {
      return false;
    }
    if (srcRect.right != sourceWidth) {
      return false;
    }
    if (srcRect.bottom != sourceHeight) {
      return false;
    }
    return true;
  }

  private static void copyFromImageReader(ImageReader imageReader, Bitmap bitmap) {
    try (Image nativeImage = imageReader.acquireNextImage()) {
      ShadowNativeImageReaderSurfaceImage.copyPixelsToBitmap(nativeImage, bitmap);
    }
  }

  private static void takeScreenshot(View view, Bitmap screenshot, @Nullable Rect srcRect) {
    validateBitmap(screenshot);

    boolean useHardwareRendering = HardwareRenderingScreenshot.canTakeScreenshot(view);
    if (useHardwareRendering
        && doDimensionsMatch(screenshot, view.getWidth(), view.getHeight(), srcRect)
        && hasRenderedPixelFormat(screenshot)) {
      // fast path, render straight into the destination screenshot, no need to resize or convert
      PerfStatsCollector.getInstance()
          .measure(
              "ShadowPixelCopy-Hardware",
              () -> HardwareRenderingScreenshot.takeScreenshot(view, screenshot));
      return;
    }

    Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
    try {
      if (useHardwareRendering) {
        PerfStatsCollector.getInstance()
            .measure(
                "ShadowPixelCopy-Hardware",
                () -> HardwareRenderingScreenshot.takeScreenshot(view, bitmap));
        drawScreenshot(bitmap, srcRect, screenshot);
      } else {
        Canvas canvas = new Canvas(bitmap);
        try {
          PerfStatsCollector.getInstance()
              .measure("ShadowPixelCopy-Software", () -> view.draw(canvas));
          drawScreenshot(bitmap, srcRect, screenshot);
        } finally {
          // Detach after the copy. The View may have left saved layers on its Canvas.
          canvas.setBitmap(null);
        }
      }
    } finally {
      bitmap.recycle();
    }
  }

  /**
   * Determine if the bitmap stores pixels the way {@link HardwareRenderingScreenshot} renders them,
   * so that it can receive a rendered frame without a conversion.
   */
  private static boolean hasRenderedPixelFormat(Bitmap bitmap) {
    return bitmap.getConfig() == Bitmap.Config.ARGB_8888
        && bitmap.isPremultiplied()
        && ColorSpace.get(ColorSpace.Named.SRGB).equals(bitmap.getColorSpace());
  }

  private static void drawScreenshot(Bitmap bitmap, @Nullable Rect srcRect, Bitmap destination) {
    // PixelCopy replaces the destination's content. Erasing it before a blending draw does that for
    // less than a draw with PorterDuff.Mode.SRC, which is slow when it scales the bitmap.
    destination.eraseColor(Color.TRANSPARENT);
    Canvas canvas = new Canvas(destination);
    try {
      Rect dst = new Rect(0, 0, destination.getWidth(), destination.getHeight());
      canvas.drawBitmap(bitmap, srcRect, dst, new Paint());
    } finally {
      // The caller owns the destination and can recycle it as soon as PixelCopy returns.
      canvas.setBitmap(null);
    }
  }

  private static void alertFinished(
      OnPixelCopyFinishedListener listener, Handler listenerThread, int result) {
    if (listenerThread.getLooper() == Looper.getMainLooper()) {
      listener.onPixelCopyFinished(result);
      return;
    }
    listenerThread.post(() -> listener.onPixelCopyFinished(result));
  }

  private static Bitmap validateBitmap(Bitmap bitmap) {
    if (bitmap == null) {
      throw new IllegalArgumentException("Bitmap cannot be null");
    }
    if (bitmap.isRecycled()) {
      throw new IllegalArgumentException("Bitmap is recycled");
    }
    if (!bitmap.isMutable()) {
      throw new IllegalArgumentException("Bitmap is immutable");
    }
    return bitmap;
  }

  @Implements(
      value = PixelCopy.Request.Builder.class,
      minSdk = UPSIDE_DOWN_CAKE,
      isInAndroidSdk = false)
  public static class ShadowPixelCopyRequestBuilder {

    // TODO(brettchabot): remove once robolectric has proper support for initializing a Surface
    // for now, this copies Android implementation and just omits the valid surface check
    @Implementation
    protected static PixelCopy.Request.Builder ofWindow(View source) {
      if (ShadowView.areRealDrawTraversalsEnabled()) {
        return reflector(BuilderReflector.class).ofWindow(source);
      } else {
        if (source == null || !source.isAttachedToWindow()) {
          throw new IllegalArgumentException("View must not be null & must be attached to window");
        }
        final Rect insets = new Rect();
        Surface surface = null;
        final ViewRootImpl root = source.getViewRootImpl();
        if (root != null) {
          surface = root.mSurface;
          insets.set(root.mWindowAttributes.surfaceInsets);
        }
        PixelCopy.Request request = reflector(RequestReflector.class).newRequest(surface, insets);
        return reflector(BuilderReflector.class).newBuilder(request);
      }
    }
  }

  @ForType(PixelCopy.class)
  private interface PixelCopyReflector {
    @Static
    Rect adjustSourceRectForInsets(Rect insets, Rect srcRect);

    @Static
    @Direct
    void request(
        SurfaceView source,
        Bitmap dest,
        OnPixelCopyFinishedListener listener,
        Handler listenerThread);

    @Static
    @Direct
    void request(
        SurfaceView source,
        Rect srcRect,
        Bitmap dest,
        OnPixelCopyFinishedListener listener,
        Handler listenerThread);

    @Static
    @Direct
    void request(
        Window source,
        Rect srcRect,
        Bitmap dest,
        OnPixelCopyFinishedListener listener,
        Handler listenerThread);
  }

  @ForType(PixelCopy.Request.Builder.class)
  private interface BuilderReflector {
    @Constructor
    PixelCopy.Request.Builder newBuilder(PixelCopy.Request request);

    @Static
    @Direct
    Request.Builder ofWindow(View source);
  }

  @ForType(PixelCopy.Request.class)
  private interface RequestReflector {
    @Constructor
    PixelCopy.Request newRequest(Surface surface, Rect insets);

    @Accessor("mSource")
    Surface getSource();

    @Accessor("mSourceInsets")
    Rect getSourceInsets();
  }

  @ForType(PixelCopy.Result.class)
  private interface ResultReflector {
    @Constructor
    PixelCopy.Result newResult(int copyResult, Bitmap bitmap);
  }
}
