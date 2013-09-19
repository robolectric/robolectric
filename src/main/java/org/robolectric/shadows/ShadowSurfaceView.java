package org.robolectric.shadows;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

import java.util.HashSet;
import java.util.Set;

@Implements(value = SurfaceView.class)
@SuppressWarnings({"UnusedDeclaration"})
public class ShadowSurfaceView extends ShadowView {

  private FakeSurfaceHolder fakeSurfaceHolder = new FakeSurfaceHolder();

  @Implementation
  public SurfaceHolder getHolder() {
    return fakeSurfaceHolder;
  }

  public FakeSurfaceHolder getFakeSurfaceHolder() {
    return fakeSurfaceHolder;
  }

  /**
   * Fake version of {@link SurfaceHolder} that gives access to the stored callbacks,
   * so they can be called by tests.
   */
  public static class FakeSurfaceHolder implements SurfaceHolder {

    final private Set<Callback> callbacks = new HashSet<Callback>();

    @Override public void addCallback(Callback callback) {
      callbacks.add(callback);
    }

    public Set<Callback> getCallbacks() {
      return callbacks;
    }

    @Override public void removeCallback(Callback callback) {
      callbacks.remove(callback);
    }

    @Override public boolean isCreating() {
      return false;
    }

    @Override public void setType(int i) {
    }

    @Override public void setFixedSize(int i, int i1) {
    }

    @Override public void setSizeFromLayout() {
    }

    @Override public void setFormat(int i) {
    }

    @Override public void setKeepScreenOn(boolean b) {
    }

    @Override public Canvas lockCanvas() {
      return null;
    }

    @Override public Canvas lockCanvas(Rect rect) {
      return null;
    }

    @Override public void unlockCanvasAndPost(Canvas canvas) {
    }

    @Override public Rect getSurfaceFrame() {
      return null;
    }

    @Override public Surface getSurface() {
      return null;
    }
  }
}
