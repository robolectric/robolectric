/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package org.robolectric.shadows;

import static android.os.Build.VERSION_CODES.N;

import android.graphics.drawable.VectorDrawable;
import java.util.Arrays;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(value = VectorDrawable.class, minSdk = N)
public class ShadowVectorDrawable extends ShadowDrawable {
  // Groups and paths have no native state here. The tree pointer is never created, so VGroup and
  // VFullPath only call into native code to merge XML attributes into their properties, and
  // nothing can read those properties back. Registering a native object per node would keep every
  // inflated vector drawable node alive forever, since nothing frees it.
  //  private static native long nCreateTree(long rootGroupPtr);
  //  private static native long nCreateTreeFromCopy(long treeToCopy, long rootGroupPtr);
  //  private static native void nSetRendererViewportSize(long rendererPtr, float viewportWidth,
  //                                                      float viewportHeight);
  //  private static native boolean nSetRootAlpha(long rendererPtr, float alpha);
  //  private static native float nGetRootAlpha(long rendererPtr);
  //  private static native void nSetAllowCaching(long rendererPtr, boolean allowCaching);
  //
  //  private static native int nDraw(long rendererPtr, long canvasWrapperPtr,
  //                                  long colorFilterPtr, Rect bounds, boolean needsMirroring,
  // boolean canReuseCache);

  private static final int TOTAL_PROPERTY_COUNT = 12;

  @Implementation
  protected static long nCreateFullPath() {
    return 0;
  }

  @Implementation
  protected static long nCreateFullPath(long nativeFullPathPtr) {
    return 0;
  }

  @Implementation
  protected static boolean nGetFullPathProperties(long pathPtr, byte[] properties, int length) {
    if (length != TOTAL_PROPERTY_COUNT * 4) return false;
    Arrays.fill(properties, 0, length, (byte) 0);
    return true;
  }

  @Implementation
  protected static void nUpdateFullPathProperties(
      long pathPtr,
      float strokeWidth,
      int strokeColor,
      float strokeAlpha,
      int fillColor,
      float fillAlpha,
      float trimPathStart,
      float trimPathEnd,
      float trimPathOffset,
      float strokeMiterLimit,
      int strokeLineCap,
      int strokeLineJoin,
      int fillType) {}

  //  @Implementation
  //  public static void nUpdateFullPathFillGradient(long pathPtr, long fillGradientPtr) {
  //
  //  }
  //
  //  @Implementation
  //  public static void nUpdateFullPathStrokeGradient(long pathPtr, long strokeGradientPtr) {
  //
  //  }
  //
  //  private static native long nCreateClipPath();
  //  private static native long nCreateClipPath(long clipPathPtr);

  @Implementation
  protected static long nCreateGroup() {
    return 0;
  }

  @Implementation
  protected static long nCreateGroup(long groupPtr) {
    return 0;
  }

  //  public static void nSetName(long nodePtr, String name) {
  //  }

  @Implementation
  protected static boolean nGetGroupProperties(long groupPtr, float[] properties, int length) {
    if (length != 7) return false;
    Arrays.fill(properties, 0, length, 0f);
    return true;
  }

  @Implementation
  protected static void nUpdateGroupProperties(
      long groupPtr,
      float rotate,
      float pivotX,
      float pivotY,
      float scaleX,
      float scaleY,
      float translateX,
      float translateY) {}

  //  private static native void nAddChild(long groupPtr, long nodePtr);
  //  private static native void nSetPathString(long pathPtr, String pathString, int length);
  //
  //  /**
  //   * The setters and getters below for paths and groups are here temporarily, and will be
  //   * removed once the animation in AVD is replaced with RenderNodeAnimator, in which case the
  //   * animation will modify these properties in native. By then no JNI hopping would be necessary
  //   * for VD during animation, and these setters and getters will be obsolete.
  //   */
  //  // Setters and getters during animation.
  //  private static native float nGetRotation(long groupPtr);
  //  private static native void nSetRotation(long groupPtr, float rotation);
  //  private static native float nGetPivotX(long groupPtr);
  //  private static native void nSetPivotX(long groupPtr, float pivotX);
  //  private static native float nGetPivotY(long groupPtr);
  //  private static native void nSetPivotY(long groupPtr, float pivotY);
  //  private static native float nGetScaleX(long groupPtr);
  //  private static native void nSetScaleX(long groupPtr, float scaleX);
  //  private static native float nGetScaleY(long groupPtr);
  //  private static native void nSetScaleY(long groupPtr, float scaleY);
  //  private static native float nGetTranslateX(long groupPtr);
  //  private static native void nSetTranslateX(long groupPtr, float translateX);
  //  private static native float nGetTranslateY(long groupPtr);
  //  private static native void nSetTranslateY(long groupPtr, float translateY);
  //
  //  // Setters and getters for VPath during animation.
  //  private static native void nSetPathData(long pathPtr, long pathDataPtr);
  //  private static native float nGetStrokeWidth(long pathPtr);
  //  private static native void nSetStrokeWidth(long pathPtr, float width);
  //  private static native int nGetStrokeColor(long pathPtr);
  //  private static native void nSetStrokeColor(long pathPtr, int strokeColor);
  //  private static native float nGetStrokeAlpha(long pathPtr);
  //  private static native void nSetStrokeAlpha(long pathPtr, float alpha);
  //  private static native int nGetFillColor(long pathPtr);
  //  private static native void nSetFillColor(long pathPtr, int fillColor);
  //  private static native float nGetFillAlpha(long pathPtr);
  //  private static native void nSetFillAlpha(long pathPtr, float fillAlpha);
  //  private static native float nGetTrimPathStart(long pathPtr);
  //  private static native void nSetTrimPathStart(long pathPtr, float trimPathStart);
  //  private static native float nGetTrimPathEnd(long pathPtr);
  //  private static native void nSetTrimPathEnd(long pathPtr, float trimPathEnd);
  //  private static native float nGetTrimPathOffset(long pathPtr);
  //  private static native void nSetTrimPathOffset(long pathPtr, float trimPathOffset);

}
