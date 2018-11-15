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
import static org.robolectric.shadows.ShadowVirtualRefBasePtr.get;
import static org.robolectric.shadows.ShadowVirtualRefBasePtr.put;

import android.graphics.drawable.VectorDrawable;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.robolectric.annotation.Implementation;
import org.robolectric.annotation.Implements;

@Implements(value = VectorDrawable.class, minSdk = N)
public class ShadowVectorDrawable extends ShadowDrawable {
  //  private static native long nCreateTree(long rootGroupPtr);
//  private static native long nCreateTreeFromCopy(long treeToCopy, long rootGroupPtr);
//  private static native void nSetRendererViewportSize(long rendererPtr, float viewportWidth,
//                                                      float viewportHeight);
//  private static native boolean nSetRootAlpha(long rendererPtr, float alpha);
//  private static native float nGetRootAlpha(long rendererPtr);
//  private static native void nSetAllowCaching(long rendererPtr, boolean allowCaching);
//
//  private static native int nDraw(long rendererPtr, long canvasWrapperPtr,
//                                  long colorFilterPtr, Rect bounds, boolean needsMirroring, boolean canReuseCache);

  private static final int STROKE_WIDTH_INDEX = 0;
  private static final int STROKE_COLOR_INDEX = 1;
  private static final int STROKE_ALPHA_INDEX = 2;
  private static final int FILL_COLOR_INDEX = 3;
  private static final int FILL_ALPHA_INDEX = 4;
  private static final int TRIM_PATH_START_INDEX = 5;
  private static final int TRIM_PATH_END_INDEX = 6;
  private static final int TRIM_PATH_OFFSET_INDEX = 7;
  private static final int STROKE_LINE_CAP_INDEX = 8;
  private static final int STROKE_LINE_JOIN_INDEX = 9;
  private static final int STROKE_MITER_LIMIT_INDEX = 10;
  private static final int FILL_TYPE_INDEX = 11;
  private static final int TOTAL_PROPERTY_COUNT = 12;

  private static class Path implements Cloneable {
    float strokeWidth;
    int strokeColor;
    float strokeAlpha;
    int fillColor;
    float fillAlpha;
    float trimPathStart;
    float trimPathEnd;
    float trimPathOffset;
    int strokeLineCap;
    int strokeLineJoin;
    float strokeMiterLimit;
    int fillType;

    @Override
    protected Object clone() {
      try {
        return super.clone();
      } catch (CloneNotSupportedException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private static Path getPath(long pathPtr) {
    return get(pathPtr, Path.class);
  }

  @Implementation
  protected static long nCreateFullPath() {
    return put(new Path());
  }

  @Implementation
  protected static long nCreateFullPath(long nativeFullPathPtr) {
    return put(getPath(nativeFullPathPtr).clone());
  }

  @Implementation
  protected static boolean nGetFullPathProperties(long pathPtr, byte[] properties, int length) {
    if (length != TOTAL_PROPERTY_COUNT * 4) return false;

    Path path = getPath(pathPtr);
    ByteBuffer propertiesBB = ByteBuffer.wrap(properties);
    propertiesBB.order(ByteOrder.nativeOrder());
    propertiesBB.putFloat(STROKE_WIDTH_INDEX * 4, path.strokeWidth);
    propertiesBB.putInt(STROKE_COLOR_INDEX * 4, path.strokeColor);
    propertiesBB.putFloat(STROKE_ALPHA_INDEX * 4, path.strokeAlpha);
    propertiesBB.putInt(FILL_COLOR_INDEX * 4, path.fillColor);
    propertiesBB.putFloat(FILL_ALPHA_INDEX * 4, path.fillAlpha);
    propertiesBB.putFloat(TRIM_PATH_START_INDEX * 4, path.trimPathStart);
    propertiesBB.putFloat(TRIM_PATH_END_INDEX * 4, path.trimPathEnd);
    propertiesBB.putFloat(TRIM_PATH_OFFSET_INDEX * 4, path.trimPathOffset);
    propertiesBB.putInt(STROKE_LINE_CAP_INDEX * 4, path.strokeLineCap);
    propertiesBB.putInt(STROKE_LINE_JOIN_INDEX * 4, path.strokeLineJoin);
    propertiesBB.putFloat(STROKE_MITER_LIMIT_INDEX * 4, path.strokeMiterLimit);
    propertiesBB.putInt(FILL_TYPE_INDEX * 4, path.fillType);

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
      int fillType) {
    Path path = getPath(pathPtr);
    path.strokeWidth = strokeWidth;
    path.strokeColor = strokeColor;
    path.strokeAlpha = strokeAlpha;
    path.fillColor = fillColor;
    path.fillAlpha = fillAlpha;
    path.trimPathStart = trimPathStart;
    path.trimPathEnd = trimPathEnd;
    path.trimPathOffset = trimPathOffset;
    path.strokeLineCap = strokeLineCap;
    path.strokeLineJoin = strokeLineJoin;
    path.strokeMiterLimit = strokeMiterLimit;
    path.fillType = fillType;
  }

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


  static class Group implements Cloneable {
    float rotation;
    float pivotX;
    float pivotY;
    float scaleX;
    float scaleY;
    float translateX;
    float translateY;

    @Override
    protected Object clone() {
      try {
        return super.clone();
      } catch (CloneNotSupportedException e) {
        throw new RuntimeException(e);
      }
    }
  }

  private static Group getGroup(long groupPtr) {
    return get(groupPtr, Group.class);
  }

  @Implementation
  protected static long nCreateGroup() {
    return put(new Group());
  }

  @Implementation
  protected static long nCreateGroup(long groupPtr) {
    return put(getGroup(groupPtr).clone());
  }

  //  public static void nSetName(long nodePtr, String name) {
  //  }

  @Implementation
  protected static boolean nGetGroupProperties(long groupPtr, float[] properties, int length) {
    if (length != 7) return false;
    Group group = getGroup(groupPtr);
    properties[0] = group.rotation;
    properties[1] = group.pivotX;
    properties[2] = group.pivotY;
    properties[3] = group.scaleX;
    properties[4] = group.scaleY;
    properties[5] = group.translateX;
    properties[6] = group.translateY;
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
      float translateY) {
    Group group = getGroup(groupPtr);
    group.rotation = rotate;
    group.pivotX = pivotX;
    group.pivotY = pivotY;
    group.scaleX = scaleX;
    group.scaleY = scaleY;
    group.translateX = translateX;
    group.translateY = translateY;
  }

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