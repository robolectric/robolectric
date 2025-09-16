package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;
import static android.os.Build.VERSION_CODES.S_V2;
import static android.os.Build.VERSION_CODES.TIRAMISU;
import static android.os.Build.VERSION_CODES.UPSIDE_DOWN_CAKE;

import android.graphics.RenderNode;
import android.graphics.RenderNode.PositionUpdateListener;
import java.lang.ref.WeakReference;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.nativeruntime.RenderNodeNatives;
import org.robolectric.shadows.ShadowNativeRenderNode.Picker;

/** Shadow for {@link RenderNode} that is backed by native code */
@Implements(
    value = RenderNode.class,
    minSdk = Q,
    shadowPicker = Picker.class,
    callNativeMethodsByDefault = true)
public class ShadowNativeRenderNode {
  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static long nCreate(String name) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return RenderNodeNatives.nCreate(name);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static long nGetNativeFinalizer() {
    return RenderNodeNatives.nGetNativeFinalizer();
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static void nOutput(long renderNode) {
    RenderNodeNatives.nOutput(renderNode);
  }

  @Implementation(minSdk = R, maxSdk = UPSIDE_DOWN_CAKE)
  protected static int nGetUsageSize(long renderNode) {
    return RenderNodeNatives.nGetUsageSize(renderNode);
  }

  @Implementation(minSdk = R, maxSdk = UPSIDE_DOWN_CAKE)
  protected static int nGetAllocatedSize(long renderNode) {
    return RenderNodeNatives.nGetAllocatedSize(renderNode);
  }

  @Implementation(maxSdk = S_V2)
  protected static void nRequestPositionUpdates(long renderNode, PositionUpdateListener callback) {
    RenderNodeNatives.nRequestPositionUpdates(renderNode, callback);
  }

  @Implementation(minSdk = TIRAMISU, maxSdk = UPSIDE_DOWN_CAKE)
  protected static void nRequestPositionUpdates(
      long renderNode, WeakReference<PositionUpdateListener> callback) {
    nRequestPositionUpdates(renderNode, callback.get());
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static void nAddAnimator(long renderNode, long animatorPtr) {
    RenderNodeNatives.nAddAnimator(renderNode, animatorPtr);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static void nEndAllAnimators(long renderNode) {
    RenderNodeNatives.nEndAllAnimators(renderNode);
  }

  @Implementation(minSdk = TIRAMISU, maxSdk = UPSIDE_DOWN_CAKE)
  protected static void nForceEndAnimators(long renderNode) {
    RenderNodeNatives.nForceEndAnimators(renderNode);
  }

  @Implementation(minSdk = S, maxSdk = UPSIDE_DOWN_CAKE)
  protected static void nDiscardDisplayList(long renderNode) {
    RenderNodeNatives.nDiscardDisplayList(renderNode);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static boolean nIsValid(long renderNode) {
    return RenderNodeNatives.nIsValid(renderNode);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static void nGetTransformMatrix(long renderNode, long nativeMatrix) {
    RenderNodeNatives.nGetTransformMatrix(renderNode, nativeMatrix);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static void nGetInverseTransformMatrix(long renderNode, long nativeMatrix) {
    RenderNodeNatives.nGetInverseTransformMatrix(renderNode, nativeMatrix);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static boolean nHasIdentityMatrix(long renderNode) {
    return RenderNodeNatives.nHasIdentityMatrix(renderNode);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static boolean nOffsetTopAndBottom(long renderNode, int offset) {
    return RenderNodeNatives.nOffsetTopAndBottom(renderNode, offset);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static boolean nOffsetLeftAndRight(long renderNode, int offset) {
    return RenderNodeNatives.nOffsetLeftAndRight(renderNode, offset);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static boolean nSetLeftTopRightBottom(
      long renderNode, int left, int top, int right, int bottom) {
    return RenderNodeNatives.nSetLeftTopRightBottom(renderNode, left, top, right, bottom);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static boolean nSetLeft(long renderNode, int left) {
    return RenderNodeNatives.nSetLeft(renderNode, left);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static boolean nSetTop(long renderNode, int top) {
    return RenderNodeNatives.nSetTop(renderNode, top);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static boolean nSetRight(long renderNode, int right) {
    return RenderNodeNatives.nSetRight(renderNode, right);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static boolean nSetBottom(long renderNode, int bottom) {
    return RenderNodeNatives.nSetBottom(renderNode, bottom);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static int nGetLeft(long renderNode) {
    return RenderNodeNatives.nGetLeft(renderNode);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static int nGetTop(long renderNode) {
    return RenderNodeNatives.nGetTop(renderNode);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static int nGetRight(long renderNode) {
    return RenderNodeNatives.nGetRight(renderNode);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static int nGetBottom(long renderNode) {
    return RenderNodeNatives.nGetBottom(renderNode);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static boolean nSetCameraDistance(long renderNode, float distance) {
    return RenderNodeNatives.nSetCameraDistance(renderNode, distance);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static boolean nSetPivotY(long renderNode, float pivotY) {
    return RenderNodeNatives.nSetPivotY(renderNode, pivotY);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static boolean nSetPivotX(long renderNode, float pivotX) {
    return RenderNodeNatives.nSetPivotX(renderNode, pivotX);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static boolean nResetPivot(long renderNode) {
    return RenderNodeNatives.nResetPivot(renderNode);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static boolean nSetLayerType(long renderNode, int layerType) {
    return RenderNodeNatives.nSetLayerType(renderNode, layerType);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static int nGetLayerType(long renderNode) {
    return RenderNodeNatives.nGetLayerType(renderNode);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static boolean nSetLayerPaint(long renderNode, long paint) {
    return RenderNodeNatives.nSetLayerPaint(renderNode, paint);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static boolean nSetClipToBounds(long renderNode, boolean clipToBounds) {
    return RenderNodeNatives.nSetClipToBounds(renderNode, clipToBounds);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static boolean nGetClipToBounds(long renderNode) {
    return RenderNodeNatives.nGetClipToBounds(renderNode);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static boolean nSetClipBounds(
      long renderNode, int left, int top, int right, int bottom) {
    return RenderNodeNatives.nSetClipBounds(renderNode, left, top, right, bottom);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static boolean nSetClipBoundsEmpty(long renderNode) {
    return RenderNodeNatives.nSetClipBoundsEmpty(renderNode);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static boolean nSetProjectBackwards(long renderNode, boolean shouldProject) {
    return RenderNodeNatives.nSetProjectBackwards(renderNode, shouldProject);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static boolean nSetProjectionReceiver(long renderNode, boolean shouldReceive) {
    return RenderNodeNatives.nSetProjectionReceiver(renderNode, shouldReceive);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static boolean nSetOutlineRoundRect(
      long renderNode, int left, int top, int right, int bottom, float radius, float alpha) {
    return RenderNodeNatives.nSetOutlineRoundRect(
        renderNode, left, top, right, bottom, radius, alpha);
  }

  @Implementation(minSdk = R, maxSdk = UPSIDE_DOWN_CAKE)
  protected static boolean nSetOutlinePath(long renderNode, long nativePath, float alpha) {
    return RenderNodeNatives.nSetOutlinePath(renderNode, nativePath, alpha);
  }

  @Implementation(maxSdk = Q)
  protected static boolean nSetOutlineConvexPath(long renderNode, long nativePath, float alpha) {
    return nSetOutlinePath(renderNode, nativePath, alpha);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static boolean nSetOutlineEmpty(long renderNode) {
    return RenderNodeNatives.nSetOutlineEmpty(renderNode);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static boolean nSetOutlineNone(long renderNode) {
    return RenderNodeNatives.nSetOutlineNone(renderNode);
  }

  @Implementation(minSdk = S, maxSdk = UPSIDE_DOWN_CAKE)
  protected static boolean nClearStretch(long renderNode) {
    return RenderNodeNatives.nClearStretch(renderNode);
  }

  @Implementation(minSdk = S, maxSdk = UPSIDE_DOWN_CAKE)
  protected static boolean nStretch(
      long renderNode, float vecX, float vecY, float maxStretchX, float maxStretchY) {
    return RenderNodeNatives.nStretch(renderNode, vecX, vecY, maxStretchX, maxStretchY);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static boolean nHasShadow(long renderNode) {
    return RenderNodeNatives.nHasShadow(renderNode);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static boolean nSetSpotShadowColor(long renderNode, int color) {
    return RenderNodeNatives.nSetSpotShadowColor(renderNode, color);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static boolean nSetAmbientShadowColor(long renderNode, int color) {
    return RenderNodeNatives.nSetAmbientShadowColor(renderNode, color);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static int nGetSpotShadowColor(long renderNode) {
    return RenderNodeNatives.nGetSpotShadowColor(renderNode);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static int nGetAmbientShadowColor(long renderNode) {
    return RenderNodeNatives.nGetAmbientShadowColor(renderNode);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static boolean nSetClipToOutline(long renderNode, boolean clipToOutline) {
    return RenderNodeNatives.nSetClipToOutline(renderNode, clipToOutline);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static boolean nSetRevealClip(
      long renderNode, boolean shouldClip, float x, float y, float radius) {
    return RenderNodeNatives.nSetRevealClip(renderNode, shouldClip, x, y, radius);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static boolean nSetAlpha(long renderNode, float alpha) {
    return RenderNodeNatives.nSetAlpha(renderNode, alpha);
  }

  @Implementation(minSdk = S, maxSdk = UPSIDE_DOWN_CAKE)
  protected static boolean nSetRenderEffect(long renderNode, long renderEffect) {
    return RenderNodeNatives.nSetRenderEffect(renderNode, renderEffect);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static boolean nSetHasOverlappingRendering(
      long renderNode, boolean hasOverlappingRendering) {
    return RenderNodeNatives.nSetHasOverlappingRendering(renderNode, hasOverlappingRendering);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static void nSetUsageHint(long renderNode, int usageHint) {
    RenderNodeNatives.nSetUsageHint(renderNode, usageHint);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static boolean nSetElevation(long renderNode, float lift) {
    return RenderNodeNatives.nSetElevation(renderNode, lift);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static boolean nSetTranslationX(long renderNode, float translationX) {
    return RenderNodeNatives.nSetTranslationX(renderNode, translationX);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static boolean nSetTranslationY(long renderNode, float translationY) {
    return RenderNodeNatives.nSetTranslationY(renderNode, translationY);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static boolean nSetTranslationZ(long renderNode, float translationZ) {
    return RenderNodeNatives.nSetTranslationZ(renderNode, translationZ);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static boolean nSetRotation(long renderNode, float rotation) {
    return RenderNodeNatives.nSetRotation(renderNode, rotation);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static boolean nSetRotationX(long renderNode, float rotationX) {
    return RenderNodeNatives.nSetRotationX(renderNode, rotationX);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static boolean nSetRotationY(long renderNode, float rotationY) {
    return RenderNodeNatives.nSetRotationY(renderNode, rotationY);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static boolean nSetScaleX(long renderNode, float scaleX) {
    return RenderNodeNatives.nSetScaleX(renderNode, scaleX);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static boolean nSetScaleY(long renderNode, float scaleY) {
    return RenderNodeNatives.nSetScaleY(renderNode, scaleY);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static boolean nSetStaticMatrix(long renderNode, long nativeMatrix) {
    return RenderNodeNatives.nSetStaticMatrix(renderNode, nativeMatrix);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static boolean nSetAnimationMatrix(long renderNode, long animationMatrix) {
    return RenderNodeNatives.nSetAnimationMatrix(renderNode, animationMatrix);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static boolean nHasOverlappingRendering(long renderNode) {
    return RenderNodeNatives.nHasOverlappingRendering(renderNode);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static boolean nGetAnimationMatrix(long renderNode, long animationMatrix) {
    return RenderNodeNatives.nGetAnimationMatrix(renderNode, animationMatrix);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static boolean nGetClipToOutline(long renderNode) {
    return RenderNodeNatives.nGetClipToOutline(renderNode);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static float nGetAlpha(long renderNode) {
    return RenderNodeNatives.nGetAlpha(renderNode);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static float nGetCameraDistance(long renderNode) {
    return RenderNodeNatives.nGetCameraDistance(renderNode);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static float nGetScaleX(long renderNode) {
    return RenderNodeNatives.nGetScaleX(renderNode);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static float nGetScaleY(long renderNode) {
    return RenderNodeNatives.nGetScaleY(renderNode);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static float nGetElevation(long renderNode) {
    return RenderNodeNatives.nGetElevation(renderNode);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static float nGetTranslationX(long renderNode) {
    return RenderNodeNatives.nGetTranslationX(renderNode);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static float nGetTranslationY(long renderNode) {
    return RenderNodeNatives.nGetTranslationY(renderNode);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static float nGetTranslationZ(long renderNode) {
    return RenderNodeNatives.nGetTranslationZ(renderNode);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static float nGetRotation(long renderNode) {
    return RenderNodeNatives.nGetRotation(renderNode);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static float nGetRotationX(long renderNode) {
    return RenderNodeNatives.nGetRotationX(renderNode);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static float nGetRotationY(long renderNode) {
    return RenderNodeNatives.nGetRotationY(renderNode);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static boolean nIsPivotExplicitlySet(long renderNode) {
    return RenderNodeNatives.nIsPivotExplicitlySet(renderNode);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static float nGetPivotX(long renderNode) {
    return RenderNodeNatives.nGetPivotX(renderNode);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static float nGetPivotY(long renderNode) {
    return RenderNodeNatives.nGetPivotY(renderNode);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static int nGetWidth(long renderNode) {
    return RenderNodeNatives.nGetWidth(renderNode);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static int nGetHeight(long renderNode) {
    return RenderNodeNatives.nGetHeight(renderNode);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static boolean nSetAllowForceDark(long renderNode, boolean allowForceDark) {
    return RenderNodeNatives.nSetAllowForceDark(renderNode, allowForceDark);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static boolean nGetAllowForceDark(long renderNode) {
    return RenderNodeNatives.nGetAllowForceDark(renderNode);
  }

  @Implementation(maxSdk = UPSIDE_DOWN_CAKE)
  protected static long nGetUniqueId(long renderNode) {
    return RenderNodeNatives.nGetUniqueId(renderNode);
  }

  @Implementation(minSdk = Q, maxSdk = R)
  protected static void nSetDisplayList(long renderNode, long newData) {
    // No-op
    // In Q and R, ending recording was a two-part operation, one part is calling
    // RecordingCanvas.finishRecording (which returned a long displayList),
    // and then calling RenderNode.nSetDisplayList with that result. However, in S, these
    // were combined into one, and all that is needed is to call RecordingCanvas.finishRecording.
  }

  @Implementation(minSdk = UPSIDE_DOWN_CAKE, maxSdk = UPSIDE_DOWN_CAKE)
  protected static void nSetIsTextureView(long renderNode) {
    // no-op
  }

  /** Shadow picker for {@link RenderNode}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(ShadowRenderNodeQ.class, ShadowNativeRenderNode.class);
    }
  }
}
