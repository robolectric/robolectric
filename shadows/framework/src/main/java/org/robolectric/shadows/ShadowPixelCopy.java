package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.O;
import static com.google.common.base.Preconditions.checkNotNull;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.annotation.NonNull;
import android.annotation.Nullable;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.view.PixelCopy;
import android.view.PixelCopy.OnPixelCopyFinishedListener;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewRootImpl;
import android.view.Window;
import android.view.WindowManagerGlobal;
import java.util.function.Consumer;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.shadows.ShadowWindowManagerGlobal.WindowManagerGlobalReflector;
import org.robolectric.util.PerfStatsCollector;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Constructor;
import org.robolectric.util.reflector.ForType;
import org.robolectric.util.reflector.Static;
import org.robolectric.versioning.AndroidVersions.U;

/**
 * Shadow for PixelCopy that uses View.draw to create screenshots. The real PixelCopy performs a
 * full hardware capture of the screen at the given location, which is impossible in Robolectric.
 *
 * <p>If listenerThread is backed by a paused looper, make sure to call ShadowLooper.idle() to
 * ensure the screenshot finishes.
 */
@Implements(value = PixelCopy.class, minSdk = O, looseSignatures = true)
public class ShadowPixelCopy {

  @Implementation
  protected static void request(
      SurfaceView source,
      @NonNull Bitmap dest,
      @NonNull OnPixelCopyFinishedListener listener,
      @NonNull Handler listenerThread) {
    takeScreenshot(source, dest, null);
    alertFinished(listener, listenerThread, PixelCopy.SUCCESS);
  }

  @Implementation
  protected static void request(
      @NonNull SurfaceView source,
      @Nullable Rect srcRect,
      @NonNull Bitmap dest,
      @NonNull OnPixelCopyFinishedListener listener,
      @NonNull Handler listenerThread) {
    if (srcRect != null && srcRect.isEmpty()) {
      throw new IllegalArgumentException("sourceRect is empty");
    }
    takeScreenshot(source, dest, srcRect);
    alertFinished(listener, listenerThread, PixelCopy.SUCCESS);
  }

  @Implementation
  protected static void request(
      @NonNull Window source,
      @NonNull Bitmap dest,
      @NonNull OnPixelCopyFinishedListener listener,
      @NonNull Handler listenerThread) {
    request(source, null, dest, listener, listenerThread);
  }

  @Implementation
  protected static void request(
      @NonNull Window source,
      @Nullable Rect srcRect,
      @NonNull Bitmap dest,
      @NonNull OnPixelCopyFinishedListener listener,
      @NonNull Handler listenerThread) {
    if (srcRect != null && srcRect.isEmpty()) {
      throw new IllegalArgumentException("sourceRect is empty");
    }
    takeScreenshot(source.getDecorView(), dest, srcRect);
    alertFinished(listener, listenerThread, PixelCopy.SUCCESS);
  }

  @Implementation
  protected static void request(
      @NonNull Surface source,
      @Nullable Rect srcRect,
      @NonNull Bitmap dest,
      @NonNull OnPixelCopyFinishedListener listener,
      @NonNull Handler listenerThread) {
    if (srcRect != null && srcRect.isEmpty()) {
      throw new IllegalArgumentException("sourceRect is empty");
    }

    View view = findViewForSurface(checkNotNull(source));
    Rect adjustedSrcRect = null;
    if (srcRect != null) {
      adjustedSrcRect = new Rect(srcRect);
      int[] locationInSurface = ShadowView.getLocationInSurfaceCompat(view);
      // offset the srcRect by the decor view's location in the surface
      adjustedSrcRect.offset(-locationInSurface[0], -locationInSurface[1]);
    }
    takeScreenshot(view, dest, adjustedSrcRect);
    alertFinished(listener, listenerThread, PixelCopy.SUCCESS);
  }

  @Implementation(minSdk = U.SDK_INT)
  protected static void request(
      /* PixelCopy.Request */ Object requestObject, /* Executor */
      Object callbackExecutor, /* Consumer<Result> */
      Object listener) {
    PixelCopy.Request request = (PixelCopy.Request) requestObject;
    RequestReflector requestReflector = reflector(RequestReflector.class, request);
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

  private static void takeScreenshot(View view, Bitmap screenshot, @Nullable Rect srcRect) {
    validateBitmap(screenshot);

    Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);

    if (HardwareRenderingScreenshot.canTakeScreenshot(view)) {
      PerfStatsCollector.getInstance()
          .measure(
              "ShadowPixelCopy-Hardware",
              () -> HardwareRenderingScreenshot.takeScreenshot(view, bitmap));
    } else {
      PerfStatsCollector.getInstance()
          .measure(
              "ShadowPixelCopy-Software",
              () -> {
                Canvas screenshotCanvas = new Canvas(bitmap);
                view.draw(screenshotCanvas);
              });
    }

    Rect dst = new Rect(0, 0, screenshot.getWidth(), screenshot.getHeight());

    Canvas resizingCanvas = new Canvas(screenshot);
    Paint paint = new Paint();
    resizingCanvas.drawBitmap(bitmap, srcRect, dst, paint);
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

  @Implements(value = PixelCopy.Request.Builder.class, minSdk = U.SDK_INT, isInAndroidSdk = false)
  public static class ShadowPixelCopyRequestBuilder {

    // TODO(brettchabot): remove once robolectric has proper support for initializing a Surface
    // for now, this copies Android implementation and just omits the valid surface check
    @Implementation
    protected static PixelCopy.Request.Builder ofWindow(View source) {
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

  @ForType(PixelCopy.class)
  private interface PixelCopyReflector {
    @Static
    Rect adjustSourceRectForInsets(Rect insets, Rect srcRect);
  }

  @ForType(PixelCopy.Request.Builder.class)
  private interface BuilderReflector {
    @Constructor
    PixelCopy.Request.Builder newBuilder(PixelCopy.Request request);
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
