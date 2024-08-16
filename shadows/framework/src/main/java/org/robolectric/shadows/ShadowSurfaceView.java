package org.robolectric.shadows;

import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import java.util.HashSet;
import java.util.Set;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(SurfaceView.class)
@SuppressWarnings({"UnusedDeclaration"})
public class ShadowSurfaceView extends ShadowView {
  private final FakeSurfaceHolder fakeSurfaceHolder = new FakeSurfaceHolder();

  @Implementation
  protected void onAttachedToWindow() {}

  @Implementation
  protected SurfaceHolder getHolder() {
    return fakeSurfaceHolder;
  }

  public FakeSurfaceHolder getFakeSurfaceHolder() {
    return fakeSurfaceHolder;
  }

  /** Robolectric implementation of {@link android.view.SurfaceHolder}. */
  public static class FakeSurfaceHolder implements SurfaceHolder {
    private final Set<Callback> callbacks = new HashSet<>();
    // The default format is RGB_565.
    private int requestedFormat = PixelFormat.RGB_565;

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

    @Override
    public boolean isCreating() {
      return false;
    }

    @Override
    public void setType(int i) {}

    @Override
    public void setFixedSize(int i, int i1) {}

    @Override
    public void setSizeFromLayout() {}

    @Override
    public void setFormat(int format) {
      if (format == PixelFormat.OPAQUE) {
        requestedFormat = PixelFormat.RGB_565;
      } else {
        requestedFormat = format;
      }
    }

    @Override
    public void setKeepScreenOn(boolean b) {}

    @Override
    public Canvas lockCanvas() {
      return null;
    }

    @Override
    public Canvas lockCanvas(Rect rect) {
      return null;
    }

    @Override
    public void unlockCanvasAndPost(Canvas canvas) {}

    @Override
    public Rect getSurfaceFrame() {
      return null;
    }

    @Override
    public Surface getSurface() {
      return null;
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
