package org.robolectric.shadows;

import static org.robolectric.util.reflector.Reflector.reflector;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PorterDuff;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.os.Handler;
import android.os.Looper;
import android.view.TextureView;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.annotation.Nullable;
import org.robolectric.shadows.ShadowSurfaceTexture.SurfaceTextureReflector;
import org.robolectric.shadows.ShadowTextureView.TextureViewReflector;

/**
 * The frames of the {@link SurfaceTexture}s that {@link ShadowTextureView} gives its views, which a
 * {@link android.view.Surface} made from one draws into ({@link ShadowNativeSurface}). On a device
 * the texture is a GL texture that a hardware layer composites; here it is a bitmap the view draws.
 */
final class TextureViewFrames {

  private static final Map<SurfaceTexture, TextureViewFrames> frames = new WeakHashMap<>();

  // Weak, as the map's key is this texture: a strong reference would keep every entry.
  private final WeakReference<SurfaceTexture> texture;
  private final WeakReference<TextureView> view;
  private final Handler mainHandler = new Handler(Looper.getMainLooper());
  private final AtomicBoolean updatePending = new AtomicBoolean();

  // The buffer a locked canvas draws into, kept from frame to frame as a device's is.
  private Bitmap back;
  @Nullable private Canvas locked;
  // The last frame posted, and the copy of it the view last drew, which only the main thread reads.
  private Bitmap front;
  private Bitmap shown;

  private TextureViewFrames(SurfaceTexture texture, TextureView view) {
    this.texture = new WeakReference<>(texture);
    this.view = new WeakReference<>(view);
  }

  /** Gives {@code texture} frames that {@code view} draws, replacing any view that had it. */
  static TextureViewFrames register(SurfaceTexture texture, TextureView view) {
    synchronized (frames) {
      TextureViewFrames target = frames.get(texture);
      if (target == null || target.view.get() != view) {
        target = new TextureViewFrames(texture, view);
        frames.put(texture, target);
      }
      return target;
    }
  }

  /**
   * The frames of {@code texture} while a view shows it, or null. A texture never given to a view,
   * or one its view has let go of (detached, or replaced with {@code setSurfaceTexture}), shows
   * nothing anywhere, as on a device.
   */
  @Nullable
  static TextureViewFrames of(@Nullable SurfaceTexture texture) {
    if (texture == null) {
      return null;
    }
    TextureViewFrames target;
    synchronized (frames) {
      target = frames.get(texture);
    }
    if (target == null) {
      return null;
    }
    TextureView current = target.view.get();
    if (current == null || current.getSurfaceTexture() != texture) {
      return null;
    }
    return target;
  }

  static void clear() {
    synchronized (frames) {
      frames.clear();
    }
  }

  /**
   * A canvas over the next frame, at the view's size, or null if a canvas is already locked or the
   * view has no size.
   */
  @Nullable
  synchronized Canvas lock(@Nullable Rect dirty) {
    TextureView current = view.get();
    if (current == null || locked != null) {
      return null;
    }
    int width = current.getWidth();
    int height = current.getHeight();
    if (width <= 0 || height <= 0) {
      return null;
    }
    if (back == null || back.getWidth() != width || back.getHeight() != height) {
      back = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
    }
    Canvas canvas = new Canvas(back);
    if (dirty != null) {
      canvas.clipRect(dirty);
    }
    locked = canvas;
    return canvas;
  }

  /** Whether {@code canvas} is the one {@link #lock} handed out and not yet posted. */
  synchronized boolean owns(Canvas canvas) {
    return canvas != null && canvas == locked;
  }

  /**
   * Publishes the locked canvas's frame, then tells the texture's {@code OnFrameAvailableListener}
   * and, on the main thread, the view and its {@code onSurfaceTextureUpdated}.
   */
  void post(Canvas canvas) {
    synchronized (this) {
      if (canvas != locked) {
        return;
      }
      locked = null;
      front = copy(back, front);
    }
    SurfaceTexture current = texture.get();
    if (current != null) {
      // As the native producer does: delivered on the listener's handler.
      reflector(SurfaceTextureReflector.class).postEventFromNative(new WeakReference<>(current));
    }
    if (!updatePending.compareAndSet(false, true)) {
      return;
    }
    mainHandler.post(() -> {
      updatePending.set(false);
      TextureView shownBy = view.get();
      SurfaceTexture surfaceTexture = texture.get();
      if (shownBy == null
          || surfaceTexture == null
          || shownBy.getSurfaceTexture() != surfaceTexture) {
        return;
      }
      shownBy.invalidate();
      TextureView.SurfaceTextureListener listener =
          reflector(TextureViewReflector.class, shownBy).getListener();
      if (listener != null) {
        listener.onSurfaceTextureUpdated(surfaceTexture);
      }
    });
  }

  /** The last frame posted, copied for drawing on the main thread; null before the first. */
  @Nullable
  synchronized Bitmap frame() {
    if (front == null) {
      return null;
    }
    shown = copy(front, shown);
    return shown;
  }

  /**
   * Copies {@code from} into {@code into}, or into a new bitmap if that is null or another size.
   */
  private static Bitmap copy(Bitmap from, @Nullable Bitmap into) {
    if (into == null
        || into.getWidth() != from.getWidth()
        || into.getHeight() != from.getHeight()) {
      into = Bitmap.createBitmap(from.getWidth(), from.getHeight(), Bitmap.Config.ARGB_8888);
    }
    Canvas canvas = new Canvas(into);
    canvas.drawColor(0, PorterDuff.Mode.CLEAR);
    canvas.drawBitmap(from, 0, 0, null);
    return into;
  }
}
