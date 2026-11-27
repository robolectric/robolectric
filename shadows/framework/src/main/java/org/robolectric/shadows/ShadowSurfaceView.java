package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.N_MR1;
import static android.os.Build.VERSION_CODES.O;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.media.ImageReader;
import android.opengl.GLSurfaceView;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewTreeObserver;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;
import javax.annotation.Nullable;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.Resetter;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

/**
 * A {@link SurfaceView} whose holder can draw: {@link SurfaceHolder#lockCanvas()} draws into a
 * bitmap the size of the surface, and a test runs the holder's callbacks with {@link
 * FakeSurfaceHolder#emitSurfaceCreated()}, {@link FakeSurfaceHolder#emitSurfaceChanged} and {@link
 * FakeSurfaceHolder#emitSurfaceDestroyed()}.
 *
 * <p>With {@link #setFollowsViewLifecycle(boolean)} the surface follows the view instead, as on a
 * device: the callbacks run on the main thread as the view is attached, laid out, shown, hidden and
 * detached, and {@link SurfaceHolder#getSurface()} is a {@link Surface} while the surface exists.
 * With native graphics that surface is backed by an {@link ImageReader} of the surface's size, so
 * what is drawn into it can be read back with {@link android.view.PixelCopy}. A {@link
 * android.opengl.GLSurfaceView} never follows the view, as Robolectric cannot run its GL thread.
 */
@Implements(SurfaceView.class)
@SuppressWarnings({"UnusedDeclaration"})
public class ShadowSurfaceView extends ShadowView {
  @SuppressWarnings("NonFinalStaticField")
  private static volatile boolean followsViewLifecycle;

  private final FakeSurfaceHolder fakeSurfaceHolder = new FakeSurfaceHolder(() -> realView);
  // Whether this view's surface follows it: set while attached with setFollowsViewLifecycle on.
  private boolean attached;
  private final ViewTreeObserver.OnPreDrawListener preDrawListener =
      () -> {
        updateSurfaceState();
        return true;
      };

  /**
   * Sets whether a {@link SurfaceView}'s surface follows the view as on a device: created, changed
   * and destroyed with the view's attachment, size and visibility, with a {@link Surface} while it
   * exists. Off by default, when the holder's callbacks run only when a test emits them; reset
   * between tests. {@link android.opengl.GLSurfaceView} is not affected.
   *
   * <p>Like any window content, a surface only follows a view in a visible window: with legacy
   * graphics, set {@code robolectric.areWindowsMarkedVisible}. {@link
   * SurfaceView#getSurfaceControl()} is not provided.
   */
  public static void setFollowsViewLifecycle(boolean follows) {
    followsViewLifecycle = follows;
  }

  @Resetter
  public static void reset() {
    followsViewLifecycle = false;
  }

  private boolean followsView() {
    return followsViewLifecycle && !(realView instanceof GLSurfaceView);
  }

  /**
   * Does nothing, unless the surface follows the view ({@link #setFollowsViewLifecycle}): then it
   * starts following the view's size and visibility.
   */
  @Implementation
  protected void onAttachedToWindow() {
    if (!followsView()) {
      return;
    }
    attached = true;
    realView.getViewTreeObserver().addOnPreDrawListener(preDrawListener);
    updateSurfaceState();
  }

  /** Destroys the surface, if it exists, before the framework detaches the view. */
  @Implementation
  protected void onDetachedFromWindow() {
    if (attached) {
      attached = false;
      realView.getViewTreeObserver().removeOnPreDrawListener(preDrawListener);
      updateSurfaceState();
    }
    reflector(SurfaceViewReflector.class, realView).onDetachedFromWindow();
  }

  /**
   * When the surface follows the view, creates, resizes or destroys it to match the view, as the
   * framework's does; otherwise runs the framework's.
   */
  @Implementation(minSdk = O)
  protected void updateSurface() {
    if (attached) {
      updateSurfaceState();
    } else {
      reflector(SurfaceViewReflector.class, realView).updateSurface();
    }
  }

  /** As {@link #updateSurface()}. */
  @Implementation(maxSdk = N_MR1)
  protected void updateWindow(boolean force, boolean redrawNeeded) {
    if (attached) {
      updateSurfaceState();
    } else {
      reflector(SurfaceViewReflector.class, realView).updateWindow(force, redrawNeeded);
    }
  }

  private void updateSurfaceState() {
    boolean visible =
        attached
            && realView.getWindowVisibility() == View.VISIBLE
            && realView.getVisibility() == View.VISIBLE;
    fakeSurfaceHolder.update(visible);
  }

  @Implementation
  protected SurfaceHolder getHolder() {
    return fakeSurfaceHolder;
  }

  public FakeSurfaceHolder getFakeSurfaceHolder() {
    return fakeSurfaceHolder;
  }

  @ForType(SurfaceView.class)
  interface SurfaceViewReflector {
    @Direct
    void onDetachedFromWindow();

    @Direct
    void updateSurface();

    @Direct
    void updateWindow(boolean force, boolean redrawNeeded);
  }

  /**
   * Robolectric implementation of {@link android.view.SurfaceHolder}.
   *
   * <p>When the surface follows the view ({@link ShadowSurfaceView#setFollowsViewLifecycle}), the
   * callbacks run as the view changes. A test can also run them itself with {@link
   * #emitSurfaceCreated()}, {@link #emitSurfaceChanged} and {@link #emitSurfaceDestroyed()}. {@link
   * #lockCanvas()} draws into a bitmap the size of the surface, kept from frame to frame as a
   * software surface keeps its last buffer; {@link #getLastPostedFrame()} returns what was last
   * posted.
   */
  public static class FakeSurfaceHolder implements SurfaceHolder {
    private final Set<Callback> callbacks = new HashSet<>();
    private final Supplier<View> view;
    // The default format is RGB_565.
    private int requestedFormat = PixelFormat.RGB_565;
    private int fixedWidth;
    private int fixedHeight;
    private int surfaceWidth;
    private int surfaceHeight;
    private boolean creating;
    @Nullable private Surface surface;
    @Nullable private ImageReader imageReader;
    // The size and format the callbacks last heard, while the surface exists.
    private int reportedWidth;
    private int reportedHeight;
    private int reportedFormat;
    @Nullable private Bitmap buffer;
    @Nullable private Bitmap lastPostedFrame;

    public FakeSurfaceHolder() {
      this(() -> null);
    }

    FakeSurfaceHolder(Supplier<View> view) {
      this.view = view;
    }

    @Override
    public void addCallback(Callback callback) {
      callbacks.add(callback);
    }

    public Set<Callback> getCallbacks() {
      return callbacks;
    }

    @Override
    public void removeCallback(Callback callback) {
      callbacks.remove(callback);
    }

    /** Calls each callback's {@link Callback#surfaceCreated}. */
    public void emitSurfaceCreated() {
      new ArrayList<>(callbacks).forEach(callback -> callback.surfaceCreated(this));
    }

    /**
     * Calls each callback's {@link Callback#surfaceChanged}. The surface takes the given size,
     * unless {@link #setFixedSize} set one.
     */
    public void emitSurfaceChanged(int format, int width, int height) {
      surfaceWidth = width;
      surfaceHeight = height;
      new ArrayList<>(callbacks)
          .forEach(callback -> callback.surfaceChanged(this, format, width, height));
    }

    /** Calls each callback's {@link Callback#surfaceDestroyed}. */
    public void emitSurfaceDestroyed() {
      new ArrayList<>(callbacks).forEach(callback -> callback.surfaceDestroyed(this));
    }

    /**
     * Returns a copy of the frame last posted with {@link #unlockCanvasAndPost}, or null if none
     * was.
     */
    @Nullable
    public Bitmap getLastPostedFrame() {
      return lastPostedFrame == null
          ? null
          : lastPostedFrame.copy(lastPostedFrame.getConfig(), false);
    }

    /**
     * Brings the surface in line with the view: created when the view is visible and has a size,
     * changed when its size or format changes, destroyed when the view is hidden or detached.
     */
    void update(boolean visible) {
      Rect frame = layoutFrame();
      if (visible && !frame.isEmpty()) {
        int width = frame.width();
        int height = frame.height();
        if (surface == null) {
          surface = newSurface(width, height);
          surfaceWidth = width;
          surfaceHeight = height;
          creating = true;
          try {
            emitSurfaceCreated();
            reportChanged(width, height);
          } finally {
            creating = false;
          }
        } else if (width != reportedWidth
            || height != reportedHeight
            || requestedFormat != reportedFormat) {
          resizeSurface(width, height);
          surfaceWidth = width;
          surfaceHeight = height;
          reportChanged(width, height);
        }
      } else if (surface != null) {
        emitSurfaceDestroyed();
        releaseSurface();
      }
    }

    private void reportChanged(int width, int height) {
      reportedWidth = width;
      reportedHeight = height;
      reportedFormat = requestedFormat;
      new ArrayList<>(callbacks)
          .forEach(callback -> callback.surfaceChanged(this, requestedFormat, width, height));
    }

    /** The size a laid-out view gives its surface: the fixed size, or else the view's own. */
    private Rect layoutFrame() {
      if (fixedWidth > 0 && fixedHeight > 0) {
        return new Rect(0, 0, fixedWidth, fixedHeight);
      }
      View surfaceView = view.get();
      return surfaceView == null
          ? new Rect()
          : new Rect(0, 0, surfaceView.getWidth(), surfaceView.getHeight());
    }

    private static boolean backedByImageReader() {
      return RuntimeEnvironment.getApiLevel() >= O && ShadowView.useRealGraphics();
    }

    private Surface newSurface(int width, int height) {
      if (backedByImageReader()) {
        Surface newSurface = new Surface();
        resizeImageReader(newSurface, width, height);
        return newSurface;
      }
      return new Surface(new SurfaceTexture(0));
    }

    private void resizeSurface(int width, int height) {
      if (backedByImageReader()) {
        resizeImageReader(surface, width, height);
      }
    }

    /** Backs {@code target} with a new ImageReader of the given size, as the framework resizes. */
    private void resizeImageReader(Surface target, int width, int height) {
      ImageReader oldImageReader = imageReader;
      imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2);
      Surface imageReaderSurface = imageReader.getSurface();
      ShadowNativeSurface shadowImageReaderSurface = Shadow.extract(imageReaderSurface);
      shadowImageReaderSurface.setContainerImageReader(imageReader);
      // Moves the ImageReader too, see ShadowNativeSurface.transferFrom.
      target.transferFrom(imageReaderSurface);
      if (oldImageReader != null) {
        oldImageReader.close();
      }
    }

    private void releaseSurface() {
      Surface oldSurface = surface;
      surface = null;
      reportedWidth = 0;
      reportedHeight = 0;
      if (oldSurface != null) {
        oldSurface.release();
      }
      if (imageReader != null) {
        imageReader.close();
        imageReader = null;
      }
    }

    /**
     * Returns true while the surface's {@code surfaceCreated} and first {@code surfaceChanged} run.
     */
    @Override
    public boolean isCreating() {
      return creating;
    }

    @Override
    public void setType(int i) {}

    @Override
    public void setFixedSize(int width, int height) {
      fixedWidth = width;
      fixedHeight = height;
      updateFromView();
    }

    @Override
    public void setSizeFromLayout() {
      fixedWidth = 0;
      fixedHeight = 0;
      updateFromView();
    }

    @Override
    public void setFormat(int format) {
      if (format == PixelFormat.OPAQUE) {
        requestedFormat = PixelFormat.RGB_565;
      } else {
        requestedFormat = format;
      }
      updateFromView();
    }

    /** Applies a change to an existing surface, as the framework does on its next traversal. */
    private void updateFromView() {
      if (surface != null) {
        update(true);
      }
    }

    @Override
    public void setKeepScreenOn(boolean b) {}

    /**
     * Returns a canvas drawing into a bitmap the size of the surface ({@link #getSurfaceFrame()}),
     * or null when the surface has no size.
     */
    @Override
    @Nullable
    public Canvas lockCanvas() {
      Rect frame = getSurfaceFrame();
      if (frame.isEmpty()) {
        return null;
      }
      if (buffer == null
          || buffer.getWidth() != frame.width()
          || buffer.getHeight() != frame.height()) {
        buffer = Bitmap.createBitmap(frame.width(), frame.height(), Bitmap.Config.ARGB_8888);
      }
      return new Canvas(buffer);
    }

    /** As {@link #lockCanvas()}, clipped to {@code dirty}. */
    @Override
    @Nullable
    public Canvas lockCanvas(Rect dirty) {
      Canvas canvas = lockCanvas();
      if (canvas != null && dirty != null) {
        canvas.clipRect(dirty);
      }
      return canvas;
    }

    /**
     * Posts what was drawn; see {@link #getLastPostedFrame()}. When the surface is backed by an
     * ImageReader, the frame is also drawn into it.
     */
    @Override
    public void unlockCanvasAndPost(Canvas canvas) {
      if (buffer == null) {
        return;
      }
      lastPostedFrame = buffer.copy(buffer.getConfig(), false);
      if (surface != null && imageReader != null && surface.isValid()) {
        Canvas surfaceCanvas = surface.lockHardwareCanvas();
        surfaceCanvas.drawBitmap(buffer, 0, 0, null);
        surface.unlockCanvasAndPost(surfaceCanvas);
      }
    }

    /**
     * Returns the surface's size: the fixed size if {@link #setFixedSize} set one, else the size
     * the surface was last given (by the view's layout or {@link #emitSurfaceChanged}), else the
     * view's size.
     */
    @Override
    public Rect getSurfaceFrame() {
      if (fixedWidth > 0 && fixedHeight > 0) {
        return new Rect(0, 0, fixedWidth, fixedHeight);
      }
      if (surfaceWidth > 0 && surfaceHeight > 0) {
        return new Rect(0, 0, surfaceWidth, surfaceHeight);
      }
      return layoutFrame();
    }

    /**
     * Returns the surface while it exists (between {@code surfaceCreated} and destroyed), or null.
     */
    @Override
    @Nullable
    public Surface getSurface() {
      return surface;
    }

    /**
     * Retrieve the requested format by the developers or by Android Frameworks internal logic.
     *
     * @return The requested format, and the default value is {@link PixelFormat#RGB_565}.
     * @see PixelFormat
     */
    public int getRequestedFormat() {
      return requestedFormat;
    }
  }
}
