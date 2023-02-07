package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;

import android.app.Activity;
import android.content.Context;
import android.content.ContextWrapper;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.view.PixelCopy;
import android.view.PixelCopy.OnPixelCopyFinishedListener;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

/**
 * Shadow for PixelCopy that uses View.draw to create screenshots. The real PixelCopy performs a
 * full hardware capture of the screen at the given location, which is impossible in Robolectric.
 *
 * <p>If listenerThread is backed by a paused looper, make sure to call ShadowLooper.idle() to
 * ensure the screenshot finishes.
 */
@Implements(value = PixelCopy.class, minSdk = P)
public class ShadowPixelCopy {
  @Implementation
  protected static void request(
      @NonNull SurfaceView source,
      @Nullable Rect srcRect,
      @NonNull Bitmap dest,
      @NonNull OnPixelCopyFinishedListener listener,
      @NonNull Handler listenerThread) {
    Activity activity = getActivity(source);
    if (srcRect != null && srcRect.isEmpty()) {
      throw new IllegalArgumentException("sourceRect is empty");
    }
    if (activity == null) {
      throw new IllegalArgumentException("SourceView was not attached to an activity");
    }
    takeScreenshot(activity.getWindow(), dest, srcRect);
    alertFinished(listener, listenerThread, PixelCopy.SUCCESS);
  }

  @Implementation
  protected static void request(
      @NonNull Window source,
      @NonNull Bitmap dest,
      @NonNull OnPixelCopyFinishedListener listener,
      @NonNull Handler listenerThread) {
    takeScreenshot(source, dest, null);
    alertFinished(listener, listenerThread, PixelCopy.SUCCESS);
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
    takeScreenshot(source, dest, srcRect);
    alertFinished(listener, listenerThread, PixelCopy.SUCCESS);
  }

  private static void takeScreenshot(Window window, Bitmap screenshot, @Nullable Rect srcRect) {
    validateBitmap(screenshot);

    // Draw the view to a bitmap in the canvas that is the size of the view itself.
    View decorView = window.getDecorView();
    Bitmap bitmap =
        Bitmap.createBitmap(decorView.getWidth(), decorView.getHeight(), Bitmap.Config.ARGB_8888);
    Canvas screenshotCanvas = new Canvas(bitmap);
    decorView.draw(screenshotCanvas);

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

  private static void validateBitmap(Bitmap bitmap) {
    if (bitmap == null) {
      throw new IllegalArgumentException("Bitmap cannot be null");
    }
    if (bitmap.isRecycled()) {
      throw new IllegalArgumentException("Bitmap is recycled");
    }
    if (!bitmap.isMutable()) {
      throw new IllegalArgumentException("Bitmap is immutable");
    }
  }

  private static Activity getActivity(Context context) {
    if (context instanceof Activity) {
      return (Activity) context;
    } else if (context instanceof ContextWrapper) {
      return getActivity(((ContextWrapper) context).getBaseContext());
    } else {
      return null;
    }
  }

  private static Activity getActivity(View view) {
    Activity activity = getActivity(view.getContext());
    if (activity != null) {
      return activity;
    }

    if (view instanceof ViewGroup) {
      ViewGroup viewGroup = (ViewGroup) view;
      if (viewGroup.getChildCount() > 0) {
        // getActivity is known to fail if View is a DecorView such as specified via espresso's
        // isRoot().
        // Make another attempt to find the activity from its first child view
        return getActivity(viewGroup.getChildAt(0).getContext());
      }
    }
    return null;
  }
}
