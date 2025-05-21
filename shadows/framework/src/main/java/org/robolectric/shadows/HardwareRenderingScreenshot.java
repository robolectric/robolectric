package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.P;
import static android.os.Build.VERSION_CODES.Q;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.HardwareRenderer;
import android.graphics.PixelFormat;
import android.graphics.RenderNode;
import android.media.Image;
import android.media.Image.Plane;
import android.media.ImageReader;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.view.View;
import android.view.ViewRootImpl;
import com.android.internal.R;
import java.util.Objects;
import java.util.WeakHashMap;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.GraphicsMode;
import org.robolectric.shadow.api.Shadow;
import org.robolectric.util.ReflectionHelpers;

/**
 * Helper class to provide hardware rendering-based screenshot to {@link ShadowPixelCopy} and {@link
 * ShadowUiAutomation}.
 */
public final class HardwareRenderingScreenshot {

  // It is important to reuse HardwareRenderer objects, and ensure that after a HardwareRenderer is
  // collected, no associated views in the same View hierarchy will be rendered as well.
  private static final WeakHashMap<ViewRootImpl, HardwareRenderer> hardwareRenderers =
      new WeakHashMap<>();

  static final String PIXEL_COPY_RENDER_MODE = "robolectric.pixelCopyRenderMode";

  static final String USE_EMBEDDED_VIEW_ROOT = "robolectric.useEmbeddedViewRoot";

  private HardwareRenderingScreenshot() {}

  /**
   * Indicates whether {@link #takeScreenshot(View, Bitmap)} can run, by validating the API level,
   * the value of the {@link #PIXEL_COPY_RENDER_MODE} property, and the {@link GraphicsMode}.
   */
  static boolean canTakeScreenshot(View view) {
    return RuntimeEnvironment.getApiLevel() >= P
        && "hardware".equalsIgnoreCase(System.getProperty(PIXEL_COPY_RENDER_MODE, "hardware"))
        && ShadowView.useRealGraphics()
        && view.canHaveDisplayList();
  }

  /**
   * Generates a bitmap given the current view using hardware accelerated canvases with native
   * graphics calls. Requires API 28+ (S).
   *
   * <p>This code mirrors the behavior of LayoutLib's RenderSessionImpl.renderAndBuildResult(); see
   * https://googleplex-android.googlesource.com/platform/frameworks/layoutlib/+/refs/heads/master-layoutlib-native/bridge/src/com/android/layoutlib/bridge/impl/RenderSessionImpl.java#573
   */
  static void takeScreenshot(View view, Bitmap destBitmap) {
    int width = view.getWidth();
    int height = view.getHeight();

    try (ImageReader imageReader =
        ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 1)) {
      ViewRootImpl viewRootImpl = view.getViewRootImpl();
      Objects.requireNonNull(viewRootImpl, "View not attached");
      Surface surface = imageReader.getSurface();

      if (RuntimeEnvironment.getApiLevel() >= Q) {
        // HardwareRenderer is only available on API 29+ (Q).
        HardwareRenderer renderer =
            hardwareRenderers.computeIfAbsent(
                viewRootImpl,
                k -> {
                  if (Boolean.parseBoolean(System.getProperty(USE_EMBEDDED_VIEW_ROOT, "false"))) {
                    ShadowViewRootImpl shadowViewRootImpl = Shadow.extract(viewRootImpl);
                    // Required to avoid a VerifyError when this lambda class is loaded on SDK <
                    // 29, where ThreadedRenderer is not a subclass of HardwareRenderer.
                    Object threadedRenderer = shadowViewRootImpl.getThreadedRenderer();
                    return (HardwareRenderer) threadedRenderer;
                  } else {
                    return new HardwareRenderer();
                  }
                });

        renderer.setSurface(surface);
        setupRendererShadowProperties(renderer, view);
        RenderNode node = getRenderNode(view);
        renderer.setContentRoot(node);
        renderer.createRenderRequest().syncAndDraw();
      } else {
        // Note this API does not set any light source properties, so it will not render
        // drop shadows.
        Canvas canvas = surface.lockHardwareCanvas();
        view.draw(canvas);
        surface.unlockCanvasAndPost(canvas);
      }
      Image nativeImage = imageReader.acquireNextImage();
      Plane[] planes = nativeImage.getPlanes();
      destBitmap.copyPixelsFromBuffer(planes[0].getBuffer());
      surface.release();
    }
  }

  private static RenderNode getRenderNode(View view) {
    return ReflectionHelpers.callInstanceMethod(view, "updateDisplayListIfDirty");
  }

  private static void setupRendererShadowProperties(HardwareRenderer renderer, View view) {
    Context context = view.getContext();
    Resources resources = context.getResources();
    DisplayMetrics displayMetrics = resources.getDisplayMetrics();

    // Get the LightSourceGeometry and LightSourceAlpha from resources.
    // The default values are the ones recommended by the getLightSourceGeometry() and
    // getLightSourceAlpha() documentation.
    // This matches LayoutLib's RenderSessionImpl#renderAndBuildResult() implementation.

    TypedArray a = context.obtainStyledAttributes(null, R.styleable.Lighting, 0, 0);
    float lightX = displayMetrics.widthPixels / 2f;
    float lightY = a.getDimension(R.styleable.Lighting_lightY, 0f);
    float lightZ = a.getDimension(R.styleable.Lighting_lightZ, 600f * displayMetrics.density);
    float lightRadius =
        a.getDimension(R.styleable.Lighting_lightRadius, 800f * displayMetrics.density);
    float ambientShadowAlpha = a.getFloat(R.styleable.Lighting_ambientShadowAlpha, 0.039f);
    float spotShadowAlpha = a.getFloat(R.styleable.Lighting_spotShadowAlpha, 0.19f);
    a.recycle();

    renderer.setLightSourceGeometry(lightX, lightY, lightZ, lightRadius);
    renderer.setLightSourceAlpha(ambientShadowAlpha, spotShadowAlpha);
  }
}
