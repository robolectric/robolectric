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

import android.annotation.ColorLong;

/**
 * Native methods for BaseRecordingCanvas JNI registration.
 *
 * <p>Native method signatures are derived from
 * https://cs.android.com/android/platform/superproject/+/android-12.0.0_r1:frameworks/base/graphics/java/android/graphics/BaseRecordingCanvas.java
 */
public final class BaseRecordingCanvasNatives {
  public static native void nDrawBitmap(
      long nativeCanvas,
      long bitmapHandle,
      float left,
      float top,
      long nativePaintOrZero,
      int canvasDensity,
      int screenDensity,
      int bitmapDensity);

  public static native void nDrawBitmap(
      long nativeCanvas,
      long bitmapHandle,
      float srcLeft,
      float srcTop,
      float srcRight,
      float srcBottom,
      float dstLeft,
      float dstTop,
      float dstRight,
      float dstBottom,
      long nativePaintOrZero,
      int screenDensity,
      int bitmapDensity);

  public static native void nDrawBitmap(
      long nativeCanvas,
      int[] colors,
      int offset,
      int stride,
      float x,
      float y,
      int width,
      int height,
      boolean hasAlpha,
      long nativePaintOrZero);

  public static native void nDrawColor(long nativeCanvas, int color, int mode);

  public static native void nDrawColor(
      long nativeCanvas, long nativeColorSpace, @ColorLong long color, int mode);

  public static native void nDrawPaint(long nativeCanvas, long nativePaint);

  public static native void nDrawPoint(long canvasHandle, float x, float y, long paintHandle);

  public static native void nDrawPoints(
      long canvasHandle, float[] pts, int offset, int count, long paintHandle);

  public static native void nDrawLine(
      long nativeCanvas, float startX, float startY, float stopX, float stopY, long nativePaint);

  public static native void nDrawLines(
      long canvasHandle, float[] pts, int offset, int count, long paintHandle);

  public static native void nDrawRect(
      long nativeCanvas, float left, float top, float right, float bottom, long nativePaint);

  public static native void nDrawOval(
      long nativeCanvas, float left, float top, float right, float bottom, long nativePaint);

  public static native void nDrawCircle(
      long nativeCanvas, float cx, float cy, float radius, long nativePaint);

  public static native void nDrawArc(
      long nativeCanvas,
      float left,
      float top,
      float right,
      float bottom,
      float startAngle,
      float sweep,
      boolean useCenter,
      long nativePaint);

  public static native void nDrawRoundRect(
      long nativeCanvas,
      float left,
      float top,
      float right,
      float bottom,
      float rx,
      float ry,
      long nativePaint);

  public static native void nDrawDoubleRoundRect(
      long nativeCanvas,
      float outerLeft,
      float outerTop,
      float outerRight,
      float outerBottom,
      float outerRx,
      float outerRy,
      float innerLeft,
      float innerTop,
      float innerRight,
      float innerBottom,
      float innerRx,
      float innerRy,
      long nativePaint);

  public static native void nDrawDoubleRoundRect(
      long nativeCanvas,
      float outerLeft,
      float outerTop,
      float outerRight,
      float outerBottom,
      float[] outerRadii,
      float innerLeft,
      float innerTop,
      float innerRight,
      float innerBottom,
      float[] innerRadii,
      long nativePaint);

  public static native void nDrawPath(long nativeCanvas, long nativePath, long nativePaint);

  public static native void nDrawRegion(long nativeCanvas, long nativeRegion, long nativePaint);

  public static native void nDrawNinePatch(
      long nativeCanvas,
      long nativeBitmap,
      long ninePatch,
      float dstLeft,
      float dstTop,
      float dstRight,
      float dstBottom,
      long nativePaintOrZero,
      int screenDensity,
      int bitmapDensity);

  public static native void nDrawBitmapMatrix(
      long nativeCanvas, long bitmapHandle, long nativeMatrix, long nativePaint);

  public static native void nDrawBitmapMesh(
      long nativeCanvas,
      long bitmapHandle,
      int meshWidth,
      int meshHeight,
      float[] verts,
      int vertOffset,
      int[] colors,
      int colorOffset,
      long nativePaint);

  public static native void nDrawVertices(
      long nativeCanvas,
      int mode,
      int n,
      float[] verts,
      int vertOffset,
      float[] texs,
      int texOffset,
      int[] colors,
      int colorOffset,
      short[] indices,
      int indexOffset,
      int indexCount,
      long nativePaint);

  public static native void nDrawGlyphs(
      long nativeCanvas,
      int[] glyphIds,
      float[] positions,
      int glyphIdStart,
      int positionStart,
      int glyphCount,
      long nativeFont,
      long nativePaint);

  public static native void nDrawText(
      long nativeCanvas,
      char[] text,
      int index,
      int count,
      float x,
      float y,
      int flags,
      long nativePaint);

  public static native void nDrawText(
      long nativeCanvas,
      String text,
      int start,
      int end,
      float x,
      float y,
      int flags,
      long nativePaint);

  // Variant for O..O_MR1 that includes a Typeface pointer.
  public static native void nDrawText(
      long nativeCanvas,
      char[] text,
      int index,
      int count,
      float x,
      float y,
      int flags,
      long nativePaint,
      long nativeTypeface);

  // Variant for O..O_MR1 that includes a Typeface pointer.
  public static native void nDrawText(
      long nativeCanvas,
      String text,
      int start,
      int end,
      float x,
      float y,
      int flags,
      long nativePaint,
      long nativeTypeface);

  public static native void nDrawTextRun(
      long nativeCanvas,
      String text,
      int start,
      int end,
      int contextStart,
      int contextEnd,
      float x,
      float y,
      boolean isRtl,
      long nativePaint);

  public static native void nDrawTextRun(
      long nativeCanvas,
      char[] text,
      int start,
      int count,
      int contextStart,
      int contextCount,
      float x,
      float y,
      boolean isRtl,
      long nativePaint,
      long nativePrecomputedText);

  // Variant for O..O_MR1 that includes a Typeface pointer.
  public static native void nDrawTextRun(
      long nativeCanvas,
      String text,
      int start,
      int end,
      int contextStart,
      int contextEnd,
      float x,
      float y,
      boolean isRtl,
      long nativePaint,
      long nativeTypeface);

  // Variant for O..O_MR1 that includes a Typeface pointer.
  public static native void nDrawTextRunTypeface(
      long nativeCanvas,
      char[] text,
      int start,
      int count,
      int contextStart,
      int contextCount,
      float x,
      float y,
      boolean isRtl,
      long nativePaint,
      long nativeTypeface);

  public static native void nDrawTextOnPath(
      long nativeCanvas,
      char[] text,
      int index,
      int count,
      long nativePath,
      float hOffset,
      float vOffset,
      int bidiFlags,
      long nativePaint);

  public static native void nDrawTextOnPath(
      long nativeCanvas,
      String text,
      long nativePath,
      float hOffset,
      float vOffset,
      int flags,
      long nativePaint);

  // Variant for O..O_MR1 that includes a Typeface pointer.
  public static native void nDrawTextOnPath(
      long nativeCanvas,
      char[] text,
      int index,
      int count,
      long nativePath,
      float hOffset,
      float vOffset,
      int bidiFlags,
      long nativePaint,
      long nativeTypeface);

  // Variant for O..O_MR1 that includes a Typeface pointer.
  public static native void nDrawTextOnPath(
      long nativeCanvas,
      String text,
      long nativePath,
      float hOffset,
      float vOffset,
      int flags,
      long nativePaint,
      long nativeTypeface);

  public static native void nPunchHole(
      long renderer, float left, float top, float right, float bottom, float rx, float ry);

  private BaseRecordingCanvasNatives() {}
}
