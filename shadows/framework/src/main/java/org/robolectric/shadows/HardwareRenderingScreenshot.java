package org.robolectric.shadows;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.HardwareRenderer;
import android.graphics.PixelFormat;
import android.graphics.RenderNode;
import android.media.Image;
import android.media.Image.Plane;
import android.media.ImageReader;
import android.os.Build.VERSION;
import android.os.Build.VERSION_CODES;
import android.util.DisplayMetrics;
import android.view.Surface;
import android.view.View;
import com.android.internal.R;
import java.nio.IntBuffer;
import org.robolectric.annotation.GraphicsMode;
import org.robolectric.util.ReflectionHelpers;

/**
 * Helper class to provide hardware rendering-based screenshot to {@link ShadowPixelCopy} and {@link
 * ShadowUiAutomation}.
 */
public final class HardwareRenderingScreenshot {

  static final String USE_HARDWARE_RENDERER_NATIVE_ENV = "robolectric.screenshot.hwrdr.native";

  private HardwareRenderingScreenshot() {}

  /**
   * Indicates whether {@link #takeScreenshot(View, Bitmap)} can run, by validating the API level,
   * the presence of the {@link #USE_HARDWARE_RENDERER_NATIVE_ENV} property, and the {@link
   * GraphicsMode}.
   */
  static boolean canTakeScreenshot() {
    return VERSION.SDK_INT >= VERSION_CODES.S
        && Boolean.getBoolean(HardwareRenderingScreenshot.USE_HARDWARE_RENDERER_NATIVE_ENV)
        && ShadowView.useRealGraphics();
  }

  /**
   * Generates a bitmap given the current view using HardwareRenderer with native graphics calls.
   * Requires API 31+ (S).
   *
   * <p>This code mirrors the behavior of LayoutLib's RenderSessionImpl.renderAndBuildResult(); see
   * https://googleplex-android.googlesource.com/platform/frameworks/layoutlib/+/refs/heads/master-layoutlib-native/bridge/src/com/android/layoutlib/bridge/impl/RenderSessionImpl.java#573
   */
  static void takeScreenshot(View view, Bitmap destBitmap) {
    int width = view.getWidth();
    int height = view.getHeight();

    try (ImageReader imageReader =
        ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 1)) {
      // Note on pixel format:
      // - Android Bitmap requires ARGB_8888.
      // - ImageReader is configured as RGBA_8888.
      // - However the native libs/hwui/pipeline/skia/SkiaHostPipeline.cpp always treats
      //   the buffer as BGRA_8888, thus matching what the Android Bitmap object requires.

      HardwareRenderer renderer = new HardwareRenderer();
      Surface surface = imageReader.getSurface();
      renderer.setSurface(surface);
      Image nativeImage = imageReader.acquireNextImage();

      setupRendererShadowProperties(renderer, view);

      RenderNode node = getRenderNode(view);
      renderer.setContentRoot(node);

      renderer.createRenderRequest().syncAndDraw();

      int[] renderPixels = new int[width * height];

      Plane[] planes = nativeImage.getPlanes();
      IntBuffer srcBuff = planes[0].getBuffer().asIntBuffer();
      srcBuff.get(renderPixels);

      destBitmap.setPixels(
          renderPixels,
          /* offset= */ 0,
          /* stride= */ width,
          /* x= */ 0,
          /* y= */ 0,
          width,
          height);
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
