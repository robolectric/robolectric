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

/**
 * Native methods for RecordingCanvas JNI registration.
 *
 * <p>Native method signatures are derived from
 * https://cs.android.com/android/platform/superproject/+/android-12.0.0_r1:frameworks/base/graphics/java/android/graphics/RecordingCanvas.java
 */
public final class RecordingCanvasNatives {

  public static native long nCreateDisplayListCanvas(long node, int width, int height);

  public static native void nResetDisplayListCanvas(long canvas, long node, int width, int height);

  public static native int nGetMaximumTextureWidth();

  public static native int nGetMaximumTextureHeight();

  public static native void nEnableZ(long renderer, boolean enableZ);

  public static native void nFinishRecording(long renderer, long renderNode);

  public static native void nDrawRenderNode(long renderer, long renderNode);

  public static native void nDrawTextureLayer(long renderer, long layer);

  public static native void nDrawCircle(
      long renderer, long propCx, long propCy, long propRadius, long propPaint);

  public static native void nDrawRipple(
      long renderer,
      long propCx,
      long propCy,
      long propRadius,
      long propPaint,
      long propProgress,
      long turbulencePhase,
      int color,
      long runtimeEffect);

  public static native void nDrawRoundRect(
      long renderer,
      long propLeft,
      long propTop,
      long propRight,
      long propBottom,
      long propRx,
      long propRy,
      long propPaint);

  public static native void nDrawWebViewFunctor(long canvas, int functor);

  private RecordingCanvasNatives() {}
}
