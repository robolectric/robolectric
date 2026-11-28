package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;
import static org.robolectric.util.reflector.Reflector.reflector;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.view.TextureView;
import android.view.View;
import org.robolectric.annotation.ClassName;
import org.robolectric.annotation.Filter;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.annotation.RealObject;
import org.robolectric.annotation.Resetter;
import org.robolectric.util.Logger;
import org.robolectric.util.reflector.Accessor;
import org.robolectric.util.reflector.Direct;
import org.robolectric.util.reflector.ForType;

/**
 * Shadow for {@link TextureView}.
 *
 * <p>On a device a {@code TextureView} is a hardware layer: its first hardware draw makes it a GL
 * texture and a {@link SurfaceTexture}, and calls {@code onSurfaceTextureAvailable}. Robolectric
 * has no texture layer, so by default the view never gets a {@code SurfaceTexture}, its listener
 * never runs, and it draws nothing.
 *
 * <p>With {@link #setProvidesSurfaceTexture(boolean)} the view gets a {@code SurfaceTexture} once
 * it is attached and laid out (or drawn), and calls {@code onSurfaceTextureAvailable}; resizing and
 * detaching then call {@code onSurfaceTextureSizeChanged} and {@code onSurfaceTextureDestroyed} as
 * the framework does. Under native graphics, a {@link android.view.Surface} made from that texture
 * draws frames with {@code lockCanvas} (or {@code lockHardwareCanvas}) and {@code
 * unlockCanvasAndPost}: each post calls the texture's {@code OnFrameAvailableListener} and the
 * view's {@code onSurfaceTextureUpdated}, and the view draws the last frame, under its transform
 * and layer paint, as does {@link TextureView#getBitmap}. GL, Vulkan, media and camera producers,
 * which write the texture natively, are not supported.
 */
@Implements(value = TextureView.class)
public class ShadowTextureView extends ShadowView {

  private static volatile boolean providesSurfaceTexture;

  @RealObject private TextureView realTextureView;

  /**
   * Gives each attached, laid-out {@link TextureView} a {@link SurfaceTexture} that a {@link
   * android.view.Surface} can draw frames into with a {@link Canvas}. Off by default. Code that
   * starts from {@code onSurfaceTextureAvailable} then runs, and code that goes on to use the
   * texture with GL or a media decoder fails, as those are not supported.
   */
  public static void setProvidesSurfaceTexture(boolean provides) {
    providesSurfaceTexture = provides;
  }

  @Resetter
  public static void reset() {
    providesSurfaceTexture = false;
    TextureViewFrames.clear();
  }

  @Implementation(minSdk = P, maxSdk = R)
  protected @ClassName("android.view.TextureLayer") Object getTextureLayer() {
    return noTextureLayer();
  }

  @Implementation(methodName = "getTextureLayer", minSdk = S)
  protected @ClassName("android.graphics.TextureLayer") Object getTextureLayerRPlus() {
    return noTextureLayer();
  }

  private Object noTextureLayer() {
    if (providesSurfaceTexture) {
      // Where the framework makes the texture, at a hardware draw.
      surfaceTexture();
      return null;
    }
    // TextureView uses OpenGL, which is not supported by Robolectric. It will try to use
    // nCreateTextureLayer, but that will always return 0 since OpenGL is not supported.
    // NoOp this code so it doesn't crash trying to use the null TextureLayer from OpenGL.
    Logger.error("ShadowTextureView", "TextureView is not supported in Robolectric.");
    return null;
  }

  @Filter(order = Filter.Order.AFTER)
  protected void onAttachedToWindow() {
    makeSurfaceTextureSoon();
  }

  @Filter(order = Filter.Order.AFTER)
  protected void onSizeChanged(int width, int height, int oldWidth, int oldHeight) {
    makeSurfaceTextureSoon();
  }

  /**
   * A device makes the texture at the view's first draw, which a Robolectric test does not do by
   * default, so make it once layout is done instead.
   */
  private void makeSurfaceTextureSoon() {
    if (providesSurfaceTexture
        && reflector(TextureViewReflector.class, realTextureView).getSurface() == null) {
      realTextureView.post(this::surfaceTexture);
    }
  }

  /**
   * The view's frames, after making its texture as {@code getTextureLayer} does if it has none yet
   * and is attached and laid out. A texture the app gave the view ({@code setSurfaceTexture}) is
   * kept, and the listener hears only of one made here. Null if the view has no texture.
   */
  private TextureViewFrames surfaceTexture() {
    TextureViewReflector view = reflector(TextureViewReflector.class, realTextureView);
    SurfaceTexture texture = view.getSurface();
    if (texture != null) {
      // setSurfaceTexture's request for the layer to take the texture; there is no layer.
      view.setUpdateSurface(false);
      return TextureViewFrames.register(texture, realTextureView);
    }
    int width = realTextureView.getWidth();
    int height = realTextureView.getHeight();
    if (!realTextureView.isAttachedToWindow() || width <= 0 || height <= 0) {
      return null;
    }
    texture = new SurfaceTexture(false);
    texture.setDefaultBufferSize(width, height);
    view.setSurface(texture);
    texture.setOnFrameAvailableListener(view.getUpdateListener(), realTextureView.getHandler());
    TextureViewFrames frames = TextureViewFrames.register(texture, realTextureView);
    TextureView.SurfaceTextureListener listener = view.getListener();
    if (listener != null) {
      listener.onSurfaceTextureAvailable(texture, width, height);
    }
    return frames;
  }

  /**
   * No layer while the view provides a texture: the frame is a bitmap the view draws like any other
   * content. Reporting {@link View#LAYER_TYPE_HARDWARE} would send a software draw, such as a
   * screenshot's {@code View.draw}, through a drawing cache that replaces the view's whole area, so
   * a translucent view would erase what is under it.
   */
  @Implementation
  @Override
  protected int getLayerType() {
    if (providesSurfaceTexture) {
      return View.LAYER_TYPE_NONE;
    }
    return reflector(TextureViewReflector.class, realTextureView).getLayerType();
  }

  @Implementation
  protected void draw(Canvas canvas) {
    reflector(TextureViewReflector.class, realTextureView).draw(canvas);
    if (!providesSurfaceTexture) {
      return;
    }
    TextureViewFrames frames = surfaceTexture();
    Bitmap frame = frames == null ? null : frames.frame();
    if (frame == null) {
      return;
    }
    int save = canvas.save();
    canvas.concat(realTextureView.getTransform(null));
    Paint paint = reflector(ViewLayerPaintReflector.class, realTextureView).getLayerPaint();
    canvas.drawBitmap(
        frame,
        null,
        new Rect(0, 0, realTextureView.getWidth(), realTextureView.getHeight()),
        paint);
    canvas.restoreToCount(save);
  }

  /** The last frame, scaled into {@code bitmap}, as the texture layer would copy it. */
  @Implementation
  protected Bitmap getBitmap(Bitmap bitmap) {
    if (!providesSurfaceTexture) {
      return reflector(TextureViewReflector.class, realTextureView).getBitmap(bitmap);
    }
    if (bitmap == null) {
      return null;
    }
    TextureViewFrames frames = TextureViewFrames.of(realTextureView.getSurfaceTexture());
    Bitmap frame = frames == null ? null : frames.frame();
    if (frame != null) {
      Canvas canvas = new Canvas(bitmap);
      Matrix transform = realTextureView.getTransform(null);
      canvas.concat(transform);
      canvas.drawBitmap(frame, null, new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight()), null);
    }
    return bitmap;
  }

  @ForType(TextureView.class)
  interface TextureViewReflector {
    @Accessor("mSurface")
    SurfaceTexture getSurface();

    @Accessor("mSurface")
    void setSurface(SurfaceTexture surface);

    @Accessor("mListener")
    TextureView.SurfaceTextureListener getListener();

    @Accessor("mUpdateSurface")
    void setUpdateSurface(boolean updateSurface);

    @Accessor("mUpdateListener")
    SurfaceTexture.OnFrameAvailableListener getUpdateListener();

    @Direct
    int getLayerType();

    @Direct
    void draw(Canvas canvas);

    @Direct
    Bitmap getBitmap(Bitmap bitmap);
  }

  @ForType(View.class)
  interface ViewLayerPaintReflector {
    @Accessor("mLayerPaint")
    Paint getLayerPaint();
  }
}
