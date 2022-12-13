package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.Q;
import static android.os.Build.VERSION_CODES.R;
import static android.os.Build.VERSION_CODES.S;
import static android.os.Build.VERSION_CODES.S_V2;

import android.graphics.RenderNode;
import android.graphics.RenderNode.PositionUpdateListener;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;
import org.robolectric.nativeruntime.DefaultNativeRuntimeLoader;
import org.robolectric.nativeruntime.RenderNodeNatives;
import org.robolectric.shadows.ShadowNativeRenderNode.Picker;

/** Shadow for {@link RenderNode} that is backed by native code */
@Implements(value = RenderNode.class, minSdk = Q, shadowPicker = Picker.class)
public class ShadowNativeRenderNode {
  @Implementation
  protected static long nCreate(String name) {
    DefaultNativeRuntimeLoader.injectAndLoad();
    return RenderNodeNatives.nCreate(name);
  }

  @Implementation
  protected static long nGetNativeFinalizer() {
    return RenderNodeNatives.nGetNativeFinalizer();
  }

  @Implementation
  protected static void nOutput(long renderNode) {
    RenderNodeNatives.nOutput(renderNode);
  }

  @Implementation(minSdk = R)
  protected static int nGetUsageSize(long renderNode) {
    return RenderNodeNatives.nGetUsageSize(renderNode);
  }

  @Implementation(minSdk = R)
  protected static int nGetAllocatedSize(long renderNode) {
    return RenderNodeNatives.nGetAllocatedSize(renderNode);
  }

  @Implementation(maxSdk = S_V2)
  protected static void nRequestPositionUpdates(long renderNode, PositionUpdateListener callback) {
    RenderNodeNatives.nRequestPositionUpdates(renderNode, callback);
  }

  @Implementation
  protected static void nAddAnimator(long renderNode, long animatorPtr) {
    RenderNodeNatives.nAddAnimator(renderNode, animatorPtr);
  }

  @Implementation
  protected static void nEndAllAnimators(long renderNode) {
    RenderNodeNatives.nEndAllAnimators(renderNode);
  }

  @Implementation(minSdk = S)
  protected static void nDiscardDisplayList(long renderNode) {
    RenderNodeNatives.nDiscardDisplayList(renderNode);
  }

  @Implementation
  protected static boolean nIsValid(long renderNode) {
    return RenderNodeNatives.nIsValid(renderNode);
  }

  @Implementation
  protected static void nGetTransformMatrix(long renderNode, long nativeMatrix) {
    RenderNodeNatives.nGetTransformMatrix(renderNode, nativeMatrix);
  }

  @Implementation
  protected static void nGetInverseTransformMatrix(long renderNode, long nativeMatrix) {
    RenderNodeNatives.nGetInverseTransformMatrix(renderNode, nativeMatrix);
  }

  @Implementation
  protected static boolean nHasIdentityMatrix(long renderNode) {
    return RenderNodeNatives.nHasIdentityMatrix(renderNode);
  }

  @Implementation
  protected static boolean nOffsetTopAndBottom(long renderNode, int offset) {
    return RenderNodeNatives.nOffsetTopAndBottom(renderNode, offset);
  }

  @Implementation
  protected static boolean nOffsetLeftAndRight(long renderNode, int offset) {
    return RenderNodeNatives.nOffsetLeftAndRight(renderNode, offset);
  }

  @Implementation
  protected static boolean nSetLeftTopRightBottom(
      long renderNode, int left, int top, int right, int bottom) {
    return RenderNodeNatives.nSetLeftTopRightBottom(renderNode, left, top, right, bottom);
  }

  @Implementation
  protected static boolean nSetLeft(long renderNode, int left) {
    return RenderNodeNatives.nSetLeft(renderNode, left);
  }

  @Implementation
  protected static boolean nSetTop(long renderNode, int top) {
    return RenderNodeNatives.nSetTop(renderNode, top);
  }

  @Implementation
  protected static boolean nSetRight(long renderNode, int right) {
    return RenderNodeNatives.nSetRight(renderNode, right);
  }

  @Implementation
  protected static boolean nSetBottom(long renderNode, int bottom) {
    return RenderNodeNatives.nSetBottom(renderNode, bottom);
  }

  @Implementation
  protected static int nGetLeft(long renderNode) {
    return RenderNodeNatives.nGetLeft(renderNode);
  }

  @Implementation
  protected static int nGetTop(long renderNode) {
    return RenderNodeNatives.nGetTop(renderNode);
  }

  @Implementation
  protected static int nGetRight(long renderNode) {
    return RenderNodeNatives.nGetRight(renderNode);
  }

  @Implementation
  protected static int nGetBottom(long renderNode) {
    return RenderNodeNatives.nGetBottom(renderNode);
  }

  @Implementation
  protected static boolean nSetCameraDistance(long renderNode, float distance) {
    return RenderNodeNatives.nSetCameraDistance(renderNode, distance);
  }

  @Implementation
  protected static boolean nSetPivotY(long renderNode, float pivotY) {
    return RenderNodeNatives.nSetPivotY(renderNode, pivotY);
  }

  @Implementation
  protected static boolean nSetPivotX(long renderNode, float pivotX) {
    return RenderNodeNatives.nSetPivotX(renderNode, pivotX);
  }

  @Implementation
  protected static boolean nResetPivot(long renderNode) {
    return RenderNodeNatives.nResetPivot(renderNode);
  }

  @Implementation
  protected static boolean nSetLayerType(long renderNode, int layerType) {
    return RenderNodeNatives.nSetLayerType(renderNode, layerType);
  }

  @Implementation
  protected static int nGetLayerType(long renderNode) {
    return RenderNodeNatives.nGetLayerType(renderNode);
  }

  @Implementation
  protected static boolean nSetLayerPaint(long renderNode, long paint) {
    return RenderNodeNatives.nSetLayerPaint(renderNode, paint);
  }

  @Implementation
  protected static boolean nSetClipToBounds(long renderNode, boolean clipToBounds) {
    return RenderNodeNatives.nSetClipToBounds(renderNode, clipToBounds);
  }

  @Implementation
  protected static boolean nGetClipToBounds(long renderNode) {
    return RenderNodeNatives.nGetClipToBounds(renderNode);
  }

  @Implementation
  protected static boolean nSetClipBounds(
      long renderNode, int left, int top, int right, int bottom) {
    return RenderNodeNatives.nSetClipBounds(renderNode, left, top, right, bottom);
  }

  @Implementation
  protected static boolean nSetClipBoundsEmpty(long renderNode) {
    return RenderNodeNatives.nSetClipBoundsEmpty(renderNode);
  }

  @Implementation
  protected static boolean nSetProjectBackwards(long renderNode, boolean shouldProject) {
    return RenderNodeNatives.nSetProjectBackwards(renderNode, shouldProject);
  }

  @Implementation
  protected static boolean nSetProjectionReceiver(long renderNode, boolean shouldReceive) {
    return RenderNodeNatives.nSetProjectionReceiver(renderNode, shouldReceive);
  }

  @Implementation
  protected static boolean nSetOutlineRoundRect(
      long renderNode, int left, int top, int right, int bottom, float radius, float alpha) {
    return RenderNodeNatives.nSetOutlineRoundRect(
        renderNode, left, top, right, bottom, radius, alpha);
  }

  @Implementation(minSdk = R)
  protected static boolean nSetOutlinePath(long renderNode, long nativePath, float alpha) {
    return RenderNodeNatives.nSetOutlinePath(renderNode, nativePath, alpha);
  }

  @Implementation
  protected static boolean nSetOutlineEmpty(long renderNode) {
    return RenderNodeNatives.nSetOutlineEmpty(renderNode);
  }

  @Implementation
  protected static boolean nSetOutlineNone(long renderNode) {
    return RenderNodeNatives.nSetOutlineNone(renderNode);
  }

  @Implementation(minSdk = S)
  protected static boolean nClearStretch(long renderNode) {
    return RenderNodeNatives.nClearStretch(renderNode);
  }

  @Implementation(minSdk = S)
  protected static boolean nStretch(
      long renderNode, float vecX, float vecY, float maxStretchX, float maxStretchY) {
    return RenderNodeNatives.nStretch(renderNode, vecX, vecY, maxStretchX, maxStretchY);
  }

  @Implementation
  protected static boolean nHasShadow(long renderNode) {
    return RenderNodeNatives.nHasShadow(renderNode);
  }

  @Implementation
  protected static boolean nSetSpotShadowColor(long renderNode, int color) {
    return RenderNodeNatives.nSetSpotShadowColor(renderNode, color);
  }

  @Implementation
  protected static boolean nSetAmbientShadowColor(long renderNode, int color) {
    return RenderNodeNatives.nSetAmbientShadowColor(renderNode, color);
  }

  @Implementation
  protected static int nGetSpotShadowColor(long renderNode) {
    return RenderNodeNatives.nGetSpotShadowColor(renderNode);
  }

  @Implementation
  protected static int nGetAmbientShadowColor(long renderNode) {
    return RenderNodeNatives.nGetAmbientShadowColor(renderNode);
  }

  @Implementation
  protected static boolean nSetClipToOutline(long renderNode, boolean clipToOutline) {
    return RenderNodeNatives.nSetClipToOutline(renderNode, clipToOutline);
  }

  @Implementation
  protected static boolean nSetRevealClip(
      long renderNode, boolean shouldClip, float x, float y, float radius) {
    return RenderNodeNatives.nSetRevealClip(renderNode, shouldClip, x, y, radius);
  }

  @Implementation
  protected static boolean nSetAlpha(long renderNode, float alpha) {
    return RenderNodeNatives.nSetAlpha(renderNode, alpha);
  }

  @Implementation(minSdk = S)
  protected static boolean nSetRenderEffect(long renderNode, long renderEffect) {
    return RenderNodeNatives.nSetRenderEffect(renderNode, renderEffect);
  }

  @Implementation
  protected static boolean nSetHasOverlappingRendering(
      long renderNode, boolean hasOverlappingRendering) {
    return RenderNodeNatives.nSetHasOverlappingRendering(renderNode, hasOverlappingRendering);
  }

  @Implementation
  protected static void nSetUsageHint(long renderNode, int usageHint) {
    RenderNodeNatives.nSetUsageHint(renderNode, usageHint);
  }

  @Implementation
  protected static boolean nSetElevation(long renderNode, float lift) {
    return RenderNodeNatives.nSetElevation(renderNode, lift);
  }

  @Implementation
  protected static boolean nSetTranslationX(long renderNode, float translationX) {
    return RenderNodeNatives.nSetTranslationX(renderNode, translationX);
  }

  @Implementation
  protected static boolean nSetTranslationY(long renderNode, float translationY) {
    return RenderNodeNatives.nSetTranslationY(renderNode, translationY);
  }

  @Implementation
  protected static boolean nSetTranslationZ(long renderNode, float translationZ) {
    return RenderNodeNatives.nSetTranslationZ(renderNode, translationZ);
  }

  @Implementation
  protected static boolean nSetRotation(long renderNode, float rotation) {
    return RenderNodeNatives.nSetRotation(renderNode, rotation);
  }

  @Implementation
  protected static boolean nSetRotationX(long renderNode, float rotationX) {
    return RenderNodeNatives.nSetRotationX(renderNode, rotationX);
  }

  @Implementation
  protected static boolean nSetRotationY(long renderNode, float rotationY) {
    return RenderNodeNatives.nSetRotationY(renderNode, rotationY);
  }

  @Implementation
  protected static boolean nSetScaleX(long renderNode, float scaleX) {
    return RenderNodeNatives.nSetScaleX(renderNode, scaleX);
  }

  @Implementation
  protected static boolean nSetScaleY(long renderNode, float scaleY) {
    return RenderNodeNatives.nSetScaleY(renderNode, scaleY);
  }

  @Implementation
  protected static boolean nSetStaticMatrix(long renderNode, long nativeMatrix) {
    return RenderNodeNatives.nSetStaticMatrix(renderNode, nativeMatrix);
  }

  @Implementation
  protected static boolean nSetAnimationMatrix(long renderNode, long animationMatrix) {
    return RenderNodeNatives.nSetAnimationMatrix(renderNode, animationMatrix);
  }

  @Implementation
  protected static boolean nHasOverlappingRendering(long renderNode) {
    return RenderNodeNatives.nHasOverlappingRendering(renderNode);
  }

  @Implementation
  protected static boolean nGetAnimationMatrix(long renderNode, long animationMatrix) {
    return RenderNodeNatives.nGetAnimationMatrix(renderNode, animationMatrix);
  }

  @Implementation
  protected static boolean nGetClipToOutline(long renderNode) {
    return RenderNodeNatives.nGetClipToOutline(renderNode);
  }

  @Implementation
  protected static float nGetAlpha(long renderNode) {
    return RenderNodeNatives.nGetAlpha(renderNode);
  }

  @Implementation
  protected static float nGetCameraDistance(long renderNode) {
    return RenderNodeNatives.nGetCameraDistance(renderNode);
  }

  @Implementation
  protected static float nGetScaleX(long renderNode) {
    return RenderNodeNatives.nGetScaleX(renderNode);
  }

  @Implementation
  protected static float nGetScaleY(long renderNode) {
    return RenderNodeNatives.nGetScaleY(renderNode);
  }

  @Implementation
  protected static float nGetElevation(long renderNode) {
    return RenderNodeNatives.nGetElevation(renderNode);
  }

  @Implementation
  protected static float nGetTranslationX(long renderNode) {
    return RenderNodeNatives.nGetTranslationX(renderNode);
  }

  @Implementation
  protected static float nGetTranslationY(long renderNode) {
    return RenderNodeNatives.nGetTranslationY(renderNode);
  }

  @Implementation
  protected static float nGetTranslationZ(long renderNode) {
    return RenderNodeNatives.nGetTranslationZ(renderNode);
  }

  @Implementation
  protected static float nGetRotation(long renderNode) {
    return RenderNodeNatives.nGetRotation(renderNode);
  }

  @Implementation
  protected static float nGetRotationX(long renderNode) {
    return RenderNodeNatives.nGetRotationX(renderNode);
  }

  @Implementation
  protected static float nGetRotationY(long renderNode) {
    return RenderNodeNatives.nGetRotationY(renderNode);
  }

  @Implementation
  protected static boolean nIsPivotExplicitlySet(long renderNode) {
    return RenderNodeNatives.nIsPivotExplicitlySet(renderNode);
  }

  @Implementation
  protected static float nGetPivotX(long renderNode) {
    return RenderNodeNatives.nGetPivotX(renderNode);
  }

  @Implementation
  protected static float nGetPivotY(long renderNode) {
    return RenderNodeNatives.nGetPivotY(renderNode);
  }

  @Implementation
  protected static int nGetWidth(long renderNode) {
    return RenderNodeNatives.nGetWidth(renderNode);
  }

  @Implementation
  protected static int nGetHeight(long renderNode) {
    return RenderNodeNatives.nGetHeight(renderNode);
  }

  @Implementation
  protected static boolean nSetAllowForceDark(long renderNode, boolean allowForceDark) {
    return RenderNodeNatives.nSetAllowForceDark(renderNode, allowForceDark);
  }

  @Implementation
  protected static boolean nGetAllowForceDark(long renderNode) {
    return RenderNodeNatives.nGetAllowForceDark(renderNode);
  }

  @Implementation
  protected static long nGetUniqueId(long renderNode) {
    return RenderNodeNatives.nGetUniqueId(renderNode);
  }

  /** Shadow picker for {@link RenderNode}. */
  public static final class Picker extends GraphicsShadowPicker<Object> {
    public Picker() {
      super(ShadowRenderNodeQ.class, ShadowNativeRenderNode.class);
    }
  }
}
