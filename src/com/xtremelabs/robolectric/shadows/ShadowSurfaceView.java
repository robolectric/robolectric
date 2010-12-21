package com.xtremelabs.robolectric.shadows;

import android.graphics.Canvas;
import android.graphics.Rect;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import com.xtremelabs.robolectric.internal.Implementation;
import com.xtremelabs.robolectric.internal.Implements;

@Implements(SurfaceView.class)
@SuppressWarnings({"UnusedDeclaration"})
public class ShadowSurfaceView extends ShadowView {
    @Implementation
    public SurfaceHolder getHolder() {
        return new SurfaceHolder() {
            @Override public void addCallback(Callback callback) {
            }

            @Override public void removeCallback(Callback callback) {
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
        };
    }
}
