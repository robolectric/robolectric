/*
 * Copyright (C) 2022 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.robolectric.nativeruntime;

import android.graphics.RenderNode.PositionUpdateListener;

/**
 * Native methods for RenderNode JNI registration.
 *
 * <p>Native method signatures are derived from
 * https://cs.android.com/android/platform/superproject/+/android-12.0.0_r1:frameworks/base/graphics/java/android/graphics/RenderNode.java
 */
public final class RenderNodeNatives {

  public static native long nCreate(String name);

  public static native long nGetNativeFinalizer();

  public static native void nOutput(long renderNode);

  public static native int nGetUsageSize(long renderNode);

  public static native int nGetAllocatedSize(long renderNode);

  public static native void nRequestPositionUpdates(
      long renderNode, PositionUpdateListener callback);

  public static native void nAddAnimator(long renderNode, long animatorPtr);

  public static native void nEndAllAnimators(long renderNode);

  public static native void nDiscardDisplayList(long renderNode);

  public static native boolean nIsValid(long renderNode);

  public static native void nGetTransformMatrix(long renderNode, long nativeMatrix);

  public static native void nGetInverseTransformMatrix(long renderNode, long nativeMatrix);

  public static native boolean nHasIdentityMatrix(long renderNode);

  public static native boolean nOffsetTopAndBottom(long renderNode, int offset);

  public static native boolean nOffsetLeftAndRight(long renderNode, int offset);

  public static native boolean nSetLeftTopRightBottom(
      long renderNode, int left, int top, int right, int bottom);

  public static native boolean nSetLeft(long renderNode, int left);

  public static native boolean nSetTop(long renderNode, int top);

  public static native boolean nSetRight(long renderNode, int right);

  public static native boolean nSetBottom(long renderNode, int bottom);

  public static native int nGetLeft(long renderNode);

  public static native int nGetTop(long renderNode);

  public static native int nGetRight(long renderNode);

  public static native int nGetBottom(long renderNode);

  public static native boolean nSetCameraDistance(long renderNode, float distance);

  public static native boolean nSetPivotY(long renderNode, float pivotY);

  public static native boolean nSetPivotX(long renderNode, float pivotX);

  public static native boolean nResetPivot(long renderNode);

  public static native boolean nSetLayerType(long renderNode, int layerType);

  public static native int nGetLayerType(long renderNode);

  public static native boolean nSetLayerPaint(long renderNode, long paint);

  public static native boolean nSetClipToBounds(long renderNode, boolean clipToBounds);

  public static native boolean nGetClipToBounds(long renderNode);

  public static native boolean nSetClipBounds(
      long renderNode, int left, int top, int right, int bottom);

  public static native boolean nSetClipBoundsEmpty(long renderNode);

  public static native boolean nSetProjectBackwards(long renderNode, boolean shouldProject);

  public static native boolean nSetProjectionReceiver(long renderNode, boolean shouldReceive);

  public static native boolean nSetOutlineRoundRect(
      long renderNode, int left, int top, int right, int bottom, float radius, float alpha);

  public static native boolean nSetOutlinePath(long renderNode, long nativePath, float alpha);

  public static native boolean nSetOutlineEmpty(long renderNode);

  public static native boolean nSetOutlineNone(long renderNode);

  public static native boolean nClearStretch(long renderNode);

  public static native boolean nStretch(
      long renderNode, float vecX, float vecY, float maxStretchX, float maxStretchY);

  public static native boolean nHasShadow(long renderNode);

  public static native boolean nSetSpotShadowColor(long renderNode, int color);

  public static native boolean nSetAmbientShadowColor(long renderNode, int color);

  public static native int nGetSpotShadowColor(long renderNode);

  public static native int nGetAmbientShadowColor(long renderNode);

  public static native boolean nSetClipToOutline(long renderNode, boolean clipToOutline);

  public static native boolean nSetRevealClip(
      long renderNode, boolean shouldClip, float x, float y, float radius);

  public static native boolean nSetAlpha(long renderNode, float alpha);

  public static native boolean nSetRenderEffect(long renderNode, long renderEffect);

  public static native boolean nSetHasOverlappingRendering(
      long renderNode, boolean hasOverlappingRendering);

  public static native void nSetUsageHint(long renderNode, int usageHint);

  public static native boolean nSetElevation(long renderNode, float lift);

  public static native boolean nSetTranslationX(long renderNode, float translationX);

  public static native boolean nSetTranslationY(long renderNode, float translationY);

  public static native boolean nSetTranslationZ(long renderNode, float translationZ);

  public static native boolean nSetRotation(long renderNode, float rotation);

  public static native boolean nSetRotationX(long renderNode, float rotationX);

  public static native boolean nSetRotationY(long renderNode, float rotationY);

  public static native boolean nSetScaleX(long renderNode, float scaleX);

  public static native boolean nSetScaleY(long renderNode, float scaleY);

  public static native boolean nSetStaticMatrix(long renderNode, long nativeMatrix);

  public static native boolean nSetAnimationMatrix(long renderNode, long animationMatrix);

  public static native boolean nHasOverlappingRendering(long renderNode);

  public static native boolean nGetAnimationMatrix(long renderNode, long animationMatrix);

  public static native boolean nGetClipToOutline(long renderNode);

  public static native float nGetAlpha(long renderNode);

  public static native float nGetCameraDistance(long renderNode);

  public static native float nGetScaleX(long renderNode);

  public static native float nGetScaleY(long renderNode);

  public static native float nGetElevation(long renderNode);

  public static native float nGetTranslationX(long renderNode);

  public static native float nGetTranslationY(long renderNode);

  public static native float nGetTranslationZ(long renderNode);

  public static native float nGetRotation(long renderNode);

  public static native float nGetRotationX(long renderNode);

  public static native float nGetRotationY(long renderNode);

  public static native boolean nIsPivotExplicitlySet(long renderNode);

  public static native float nGetPivotX(long renderNode);

  public static native float nGetPivotY(long renderNode);

  public static native int nGetWidth(long renderNode);

  public static native int nGetHeight(long renderNode);

  public static native boolean nSetAllowForceDark(long renderNode, boolean allowForceDark);

  public static native boolean nGetAllowForceDark(long renderNode);

  public static native long nGetUniqueId(long renderNode);

  private RenderNodeNatives() {}
}
